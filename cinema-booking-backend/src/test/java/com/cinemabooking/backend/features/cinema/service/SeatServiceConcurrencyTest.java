package com.cinemabooking.backend.features.cinema.service;

import com.cinemabooking.backend.features.cinema.dto.SeatDTO;
import com.cinemabooking.backend.features.cinema.repository.SeatRepository;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("local")
public class SeatServiceConcurrencyTest {

    private static final Logger logger = LoggerFactory.getLogger(SeatServiceConcurrencyTest.class);
    private static final String TEST_SEAT_ID = "test_showtime_seat_C05";

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private Firestore firestore;

    private DocumentReference seatRef;

    @BeforeEach
    public void setUp() throws Exception {
        seatRef = firestore.collection("seats").document(TEST_SEAT_ID);
        // Ensure seat starts as available
        resetSeatToAvailable();
    }

    @AfterEach
    public void tearDown() throws Exception {
        // Cleanup after tests
        seatRef.delete().get();
        logger.info("Cleaned up test seat from Firestore");
    }

    private void resetSeatToAvailable() throws Exception {
        SeatDTO seat = SeatDTO.builder()
                .seatId(TEST_SEAT_ID)
                .showtimeId("test_showtime")
                .seatCode("C05")
                .rowName("C")
                .columnNo(5)
                .seatType("STANDARD")
                .status("available")
                .heldBy(null)
                .heldUntil(0L)
                .bookedBy(null)
                .bookedAt(0L)
                .priceOverride(0.0)
                .build();
        seatRef.set(seat).get();
        logger.info("Initialized test seat to AVAILABLE status");
    }

    @Test
    public void testLockAvailableSeatSuccess() throws Exception {
        logger.info("=== START: testLockAvailableSeatSuccess ===");
        
        // Execute locking
        seatRepository.lockSeats("user_A", Collections.singletonList(TEST_SEAT_ID));

        // Verify state in Firestore
        DocumentSnapshot snapshot = seatRef.get().get();
        assertTrue(snapshot.exists());
        assertEquals("held", snapshot.getString("status"));
        assertEquals("user_A", snapshot.getString("heldBy"));
        
        Long heldUntil = snapshot.getLong("heldUntil");
        assertNotNull(heldUntil);
        assertTrue(heldUntil > System.currentTimeMillis());
        
        logger.info("=== SUCCESS: testLockAvailableSeatSuccess ===");
    }

    @Test
    public void testLockSeatHeldByOtherUserActive() throws Exception {
        logger.info("=== START: testLockSeatHeldByOtherUserActive ===");

        // Setup seat as already held by user_A
        long now = System.currentTimeMillis();
        seatRef.update(
                "status", "held",
                "heldBy", "user_A",
                "heldUntil", now + 120000 // Held for another 2 minutes
        ).get();

        // User B attempts to lock. Should fail.
        ExecutionException exception = assertThrows(ExecutionException.class, () -> {
            seatRepository.lockSeats("user_B", Collections.singletonList(TEST_SEAT_ID));
        });

        assertNotNull(exception.getCause());
        assertTrue(exception.getCause().getMessage().contains("held by another user"));

        // Verify ownership did not change
        DocumentSnapshot snapshot = seatRef.get().get();
        assertEquals("held", snapshot.getString("status"));
        assertEquals("user_A", snapshot.getString("heldBy"));

        logger.info("=== SUCCESS: testLockSeatHeldByOtherUserActive ===");
    }

    @Test
    public void testLockSeatHeldByOtherUserExpired() throws Exception {
        logger.info("=== START: testLockSeatHeldByOtherUserExpired ===");

        // Setup seat as held by user_A but expired 10 seconds ago
        long now = System.currentTimeMillis();
        seatRef.update(
                "status", "held",
                "heldBy", "user_A",
                "heldUntil", now - 10000 // Expired 10s ago
        ).get();

        // User B attempts to lock. Since hold is expired, it should succeed!
        seatRepository.lockSeats("user_B", Collections.singletonList(TEST_SEAT_ID));

        // Verify state in Firestore (User B has successfully taken over hold)
        DocumentSnapshot snapshot = seatRef.get().get();
        assertEquals("held", snapshot.getString("status"));
        assertEquals("user_B", snapshot.getString("heldBy"));
        assertTrue(snapshot.getLong("heldUntil") > now);

        logger.info("=== SUCCESS: testLockSeatHeldByOtherUserExpired ===");
    }

    @Test
    public void testLockSeatAlreadyBooked() throws Exception {
        logger.info("=== START: testLockSeatAlreadyBooked ===");

        // Setup seat as booked by user_A
        seatRef.update(
                "status", "booked",
                "bookedBy", "user_A",
                "bookedAt", System.currentTimeMillis()
        ).get();

        // User B attempts to lock. Should fail.
        ExecutionException exception = assertThrows(ExecutionException.class, () -> {
            seatRepository.lockSeats("user_B", Collections.singletonList(TEST_SEAT_ID));
        });

        assertNotNull(exception.getCause());
        assertTrue(exception.getCause().getMessage().contains("already booked"));

        logger.info("=== SUCCESS: testLockSeatAlreadyBooked ===");
    }

    @Test
    public void testConcurrentLocks() throws Exception {
        logger.info("=== START: testConcurrentLocks ===");

        // Ensure seat starts as available
        resetSeatToAvailable();

        int numThreads = 2;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch latch = new CountDownLatch(1);

        Callable<Boolean> taskUserA = () -> {
            latch.await(); // wait for start signal
            try {
                logger.info("User_A attempting to lock...");
                seatRepository.lockSeats("user_A", Collections.singletonList(TEST_SEAT_ID));
                logger.info("User_A lock SUCCESS");
                return true;
            } catch (Exception e) {
                logger.warn("User_A lock FAILED: {}", e.getMessage());
                return false;
            }
        };

        Callable<Boolean> taskUserB = () -> {
            latch.await(); // wait for start signal
            try {
                logger.info("User_B attempting to lock...");
                seatRepository.lockSeats("user_B", Collections.singletonList(TEST_SEAT_ID));
                logger.info("User_B lock SUCCESS");
                return true;
            } catch (Exception e) {
                logger.warn("User_B lock FAILED: {}", e.getMessage());
                return false;
            }
        };

        Future<Boolean> futureA = executor.submit(taskUserA);
        Future<Boolean> futureB = executor.submit(taskUserB);

        // Start both threads at the exact same moment
        latch.countDown();

        boolean resultA = futureA.get();
        boolean resultB = futureB.get();

        executor.shutdown();

        // XOR: Exactly one must succeed, and exactly one must fail.
        assertTrue(resultA ^ resultB, "Exactly one user must succeed in locking the seat, and the other must fail");

        // Verify database reflects the winner
        DocumentSnapshot snapshot = seatRef.get().get();
        assertEquals("held", snapshot.getString("status"));
        String winner = snapshot.getString("heldBy");
        assertTrue("user_A".equals(winner) || "user_B".equals(winner));
        logger.info("Concurrent test winner: {}", winner);

        logger.info("=== SUCCESS: testConcurrentLocks ===");
    }
}

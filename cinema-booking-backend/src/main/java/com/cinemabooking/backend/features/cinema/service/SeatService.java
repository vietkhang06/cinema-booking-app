package com.cinemabooking.backend.features.cinema.service;

import com.cinemabooking.backend.features.cinema.dto.SeatDTO;
import com.cinemabooking.backend.features.cinema.repository.CinemaRepository;
import com.cinemabooking.backend.features.cinema.repository.SeatRepository;
import com.cinemabooking.backend.features.cinema.repository.ShowtimeRepository;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class SeatService {

    private static final Logger logger = LoggerFactory.getLogger(SeatService.class);

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private CinemaRepository cinemaRepository;

    @Autowired
    private ShowtimeRepository showtimeRepository;

    public List<SeatDTO> getSeatsByShowtimeId(String showtimeId) throws ExecutionException, InterruptedException {
        logger.info("Fetching seats for showtimeId: {}", showtimeId);
        List<QueryDocumentSnapshot> documents = seatRepository.findByShowtimeId(showtimeId);
        List<SeatDTO> seats = documents.stream()
                .map(this::mapToDTO)
                .filter(dto -> dto != null)
                .collect(Collectors.toList());
                
        if (seats.isEmpty()) {
            logger.info("No seats found for showtimeId: {}. Attempting dynamic generation...", showtimeId);
            seats = generateAndSaveSeatsForShowtime(showtimeId);
        }
        
        logger.info("Loaded {} seats for showtime {}", seats.size(), showtimeId);
        return seats;
    }

    private List<SeatDTO> generateAndSaveSeatsForShowtime(String showtimeId) throws ExecutionException, InterruptedException {
        DocumentSnapshot showtimeDoc = showtimeRepository.findById(showtimeId);
        if (!showtimeDoc.exists()) {
            logger.warn("Showtime {} does not exist in database.", showtimeId);
            return new ArrayList<>();
        }
        
        String roomId = showtimeDoc.getString("roomId");
        if (roomId == null || roomId.isEmpty()) {
            logger.warn("Showtime {} has no roomId associated.", showtimeId);
            return new ArrayList<>();
        }
        
        // Fetch room to get rows/cols
        DocumentSnapshot roomDoc = cinemaRepository.findRoomById(roomId);
        int roomRows = 6;
        int roomCols = 12;
        if (roomDoc.exists()) {
            Long rowsVal = roomDoc.getLong("seatRows");
            Long colsVal = roomDoc.getLong("seatCols");
            if (rowsVal != null) roomRows = rowsVal.intValue();
            if (colsVal != null) roomCols = colsVal.intValue();
        }
        
        // Fetch seat templates
        List<QueryDocumentSnapshot> templatesDocs = seatRepository.findTemplatesByRoomId(roomId);
        
        List<SeatDTO> generatedSeats = new ArrayList<>();
        
        if (!templatesDocs.isEmpty()) {
            logger.info("Found {} templates for room {}. Instantiating showtime seats from templates...", templatesDocs.size(), roomId);
            for (DocumentSnapshot doc : templatesDocs) {
                Boolean isEnabledVal = doc.getBoolean("isEnabled");
                boolean isEnabled = isEnabledVal != null ? isEnabledVal : true;
                
                if (isEnabled) {
                    String seatCode = doc.getString("seatCode");
                    String rowName = doc.getString("rowName");
                    Long colNoVal = doc.getLong("columnNo");
                    int colNo = colNoVal != null ? colNoVal.intValue() : 0;
                    String seatType = doc.getString("seatType");
                    
                    SeatDTO seat = SeatDTO.builder()
                            .seatId(showtimeId + "_" + seatCode)
                            .showtimeId(showtimeId)
                            .seatCode(seatCode)
                            .rowName(rowName)
                            .columnNo(colNo)
                            .seatType(seatType != null ? seatType : "STANDARD")
                            .status("available")
                            .heldBy(null)
                            .heldUntil(0L)
                            .bookedBy(null)
                            .bookedAt(0L)
                            .priceOverride(0.0)
                            .build();
                            
                    generatedSeats.add(seat);
                }
            }
        } else {
            logger.info("No templates found for room {}. Creating default seat grid of {}x{}...", roomId, roomRows, roomCols);
            for (int r = 0; r < roomRows; r++) {
                String rowName = String.valueOf((char) ('A' + r));
                for (int c = 1; c <= roomCols; c++) {
                    String seatCode = rowName + String.format(java.util.Locale.getDefault(), "%02d", c);
                    String seatType = (rowName.equals("C") || rowName.equals("D")) ? "VIP" : "STANDARD";
                    
                    SeatDTO seat = SeatDTO.builder()
                            .seatId(showtimeId + "_" + seatCode)
                            .showtimeId(showtimeId)
                            .seatCode(seatCode)
                            .rowName(rowName)
                            .columnNo(c)
                            .seatType(seatType)
                            .status("available")
                            .heldBy(null)
                            .heldUntil(0L)
                            .bookedBy(null)
                            .bookedAt(0L)
                            .priceOverride(0.0)
                            .build();
                            
                    generatedSeats.add(seat);
                }
            }
        }
        
        if (!generatedSeats.isEmpty()) {
            seatRepository.saveSeatsBatch(generatedSeats);
            logger.info("Atomically created and saved {} seats for showtime {}", generatedSeats.size(), showtimeId);
        }
        
        return generatedSeats;
    }

    public void lockSeats(String userId, String showtimeId, List<String> seatIds) throws ExecutionException, InterruptedException {
        seatRepository.lockSeats(userId, showtimeId, seatIds);
    }

    public void releaseSeats(String userId, String showtimeId, List<String> seatIds) throws ExecutionException, InterruptedException {
        seatRepository.releaseSeats(userId, showtimeId, seatIds);
    }

    public void releaseSeatsByStaff(String showtimeId, List<String> seatIds) throws ExecutionException, InterruptedException {
        seatRepository.releaseSeatsByStaff(showtimeId, seatIds);
    }

    private SeatDTO mapToDTO(DocumentSnapshot doc) {
        try {
            return SeatDTO.builder()
                    .seatId(doc.getId())
                    .showtimeId(doc.getString("showtimeId"))
                    .seatCode(doc.getString("seatCode"))
                    .rowName(doc.getString("rowName"))
                    .columnNo(doc.getLong("columnNo") != null ? doc.getLong("columnNo").intValue() : 0)
                    .seatType(doc.getString("seatType"))
                    .status(doc.getString("status"))
                    .heldBy(doc.getString("heldBy"))
                    .heldUntil(doc.getLong("heldUntil") != null ? doc.getLong("heldUntil") : 0L)
                    .bookedBy(doc.getString("bookedBy"))
                    .bookedAt(doc.getLong("bookedAt") != null ? doc.getLong("bookedAt") : 0L)
                    .priceOverride(doc.getDouble("priceOverride") != null ? doc.getDouble("priceOverride") : 0.0)
                    .build();
        } catch (Exception e) {
            logger.warn("Error mapping seat doc {}: {}", doc.getId(), e.getMessage());
            return null;
        }
    }
}

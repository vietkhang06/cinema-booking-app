package com.cinemabooking.backend.features.booking.controller;

import com.cinemabooking.backend.features.booking.dto.BookingDTO;
import com.cinemabooking.backend.features.booking.dto.SnackOrderSnapshot;
import com.cinemabooking.backend.features.booking.request.SeatBookingRequestDTO;
import com.cinemabooking.backend.features.booking.service.BookingService;
import com.cinemabooking.backend.features.cinema.dto.CinemaDTO;
import com.cinemabooking.backend.features.cinema.dto.RoomDTO;
import com.cinemabooking.backend.features.cinema.dto.SeatDTO;
import com.cinemabooking.backend.features.cinema.dto.ShowtimeDTO;
import com.cinemabooking.backend.features.cinema.dto.SnackDTO;
import com.cinemabooking.backend.features.cinema.repository.CinemaRepository;
import com.cinemabooking.backend.features.cinema.repository.SeatRepository;
import com.cinemabooking.backend.features.cinema.service.ShowtimeService;
import com.cinemabooking.backend.features.movie.dto.MovieDTO;
import com.cinemabooking.backend.features.movie.service.MovieService;
import com.cinemabooking.backend.features.payment.service.PaymentService;
import com.cinemabooking.backend.features.user.dto.UserDTO;
import com.cinemabooking.backend.features.user.dto.StaffStatsDTO;
import com.cinemabooking.backend.features.user.service.UserService;
import com.cinemabooking.backend.features.voucher.dto.VoucherDTO;
import com.cinemabooking.backend.features.voucher.service.VoucherService;
import com.cinemabooking.backend.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/bookings")
@Tag(name = "Bookings", description = "Endpoints for advertising booking")
public class BookingController {

    private static final Logger log = LoggerFactory.getLogger(BookingController.class);

    @Autowired private BookingService bookingService;
    @Autowired private ShowtimeService showtimeService;
    @Autowired private UserService userService;
    @Autowired private MovieService movieService;
    @Autowired private PaymentService paymentService;
    @Autowired private VoucherService voucherService;
    @Autowired private CinemaRepository cinemaRepository;
    @Autowired private SeatRepository seatRepository;

    @GetMapping("{id}")
    @Operation(summary = "Get booking detail by id")
    public ApiResponse<BookingDTO> getBookingDetailById(@PathVariable("id") String bookingId) throws ExecutionException, InterruptedException {
        BookingDTO booking = bookingService.getBookingById(bookingId);
        if(booking == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found");

        ShowtimeDTO showTime = showtimeService.getShowtimeById(booking.getShowtimeId());
        UserDTO user = userService.getUserById(booking.getUserId());

        booking.setShowtime(showTime);
        booking.setUser(user);

        return ApiResponse.<BookingDTO>builder()
                .success(true)
                .message("Booking fetched successfully")
                .data(booking)
                .build();
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BookingDTO>> createBooking(
            @AuthenticationPrincipal String userId,
            @RequestBody SeatBookingRequestDTO bookingRequest
    ) throws ExecutionException, InterruptedException {
        SeatBookingRequestDTO data = bookingRequest;
        List<SnackOrderSnapshot> orders = new ArrayList<>();
        if (data.getSnackOrders() != null && !data.getSnackOrders().isEmpty()){
            List<String> snackIds = data.getSnackOrders().stream().map(SeatBookingRequestDTO.SnackOrder::snackId).collect(Collectors.toList());
            List<SnackDTO> snacks = cinemaRepository.findSnacksByIds(snackIds);
            snacks.forEach(snack -> {
                SeatBookingRequestDTO.SnackOrder order = data.getSnackOrders().stream().filter(snackOrder -> snackOrder.snackId().equals(snack.getSnackId())).findFirst().orElse(null);
                if (order != null) {
                    orders.add(
                            SnackOrderSnapshot.builder()
                                    .snackId(snack.getSnackId())
                                    .snackName(snack.getName())
                                    .snackImgURL(snack.getImageUrl())
                                    .price(snack.getPrice())
                                    .quantity(order.quantity())
                                    .build()
                    );
                }
            });
        }

        String uniqueID = UUID.randomUUID().toString();

        ShowtimeDTO showtime = showtimeService.getShowtimeById(data.getShowtimeId());
        MovieDTO movie = movieService.getMovieById(showtime.getMovieId());

        RoomDTO room = cinemaRepository.findRoomById(showtime.getRoomId()).toObject(RoomDTO.class);
        CinemaDTO cinema = cinemaRepository.findCinemaById(showtime.getCinemaId()).toObject(CinemaDTO.class);

        List<SeatDTO> seats = seatRepository.findSeatsByIds(data.getSeatIds());

        // Concurrency and Ownership validation check
        long now = System.currentTimeMillis();
        for (SeatDTO seat : seats) {
            if ("booked".equalsIgnoreCase(seat.getStatus())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Ghế " + seat.getSeatCode() + " đã được đặt trước bởi người khác!");
            }
            if (!"held".equalsIgnoreCase(seat.getStatus()) || !userId.equals(seat.getHeldBy()) || seat.getHeldUntil() < now) {
                log.warn("[SEAT_CONFIRM_FAIL] User {} tried to book seat {} but it is held by user {} until {}",
                        userId, seat.getSeatId(), seat.getHeldBy(), seat.getHeldUntil());
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Ghế " + seat.getSeatCode() + " chưa được giữ bởi bạn hoặc đã hết hạn giữ ghế!");
            }
        }

        double subTotal = showtime.getBasePrice() * seats.size() + orders.stream().mapToDouble(item -> item.getPrice() * item.getQuantity()).sum();
        double discount = 0;

        if (data.getAppliedVoucherCode() != null && !data.getAppliedVoucherCode().isEmpty()) {
            try {
                VoucherDTO voucher = voucherService.validateVoucher(data.getAppliedVoucherCode(), userId);
                discount = subTotal * voucher.getDiscountPercent() / 100.0;
            } catch (Exception e) {
                log.warn("Failed to validate voucher during booking creation: {}", e.getMessage());
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mã voucher không hợp lệ hoặc đã hết hạn.");
            }
        }

        double finalTotal = subTotal - discount;
        if (finalTotal < 0) finalTotal = 0;

        // Prevent client-side manipulation by strictly using our calculated finalTotal
        if (Math.abs(finalTotal - bookingRequest.getTotalPrice()) > 1.0) {
            log.warn("Price mismatch: client sent {}, server calculated {}", bookingRequest.getTotalPrice(), finalTotal);
        }

        String suffix = uniqueID.contains("_") ? uniqueID.substring(uniqueID.indexOf("_") + 1) : uniqueID;
        if (suffix.length() > 8) {
            suffix = suffix.substring(0, 8);
        }
        String paymentCode = ("BK" + suffix).toUpperCase();

        BookingDTO booking = BookingDTO.builder()
                .bookingId(uniqueID)
                .bookingStatus("PENDING")
                .userId(userId)
                .movieId(showtime.getMovieId())
                .showtimeId(data.getShowtimeId())
                .showtimeStartAtSnapshot(showtime.getStartAt())
                .movieTitleSnapshot(movie.getTitle())
                .movieImageUrlSnapshot(movie.getPosterUrl())
                .roomNameSnapshot(room.getName())
                .cinemaNameSnapshot(cinema.getName())
                .seatIds(data.getSeatIds())
                .seatCodes(seats.stream().map(SeatDTO::getSeatCode).collect(Collectors.toList()))
                .snackOrder(orders)
                .subtotal(subTotal)
                .discount(discount)
                .total(finalTotal)
                .appliedVoucherCode(data.getAppliedVoucherCode())
                .paymentMethod(bookingRequest.getPaymentMethod().name())
                .paymentStatus("PENDING")
                .paymentCode(paymentCode)
                .createdAt(System.currentTimeMillis())
                .updatedAt(System.currentTimeMillis())
                .build();

        log.info("[BOOKING_FLOW] Creating booking record: bookingId={}, userId={}, total={}, status=PENDING",
                uniqueID, userId, finalTotal);

        BookingDTO bookingDTO = bookingService.createBooking(booking);

        // Tạo document payment với status = PENDING
        paymentService.createPendingPayment(
                bookingDTO.getBookingId(),
                bookingDTO.getUserId(),
                bookingDTO.getPaymentMethod(),
                bookingDTO.getTotal()
        );

        return ResponseEntity.ok(
                ApiResponse.<BookingDTO>builder()
                        .success(true)
                        .message("Booking fetched successfully")
                        .data(bookingDTO)
                        .build()
        );
    }

    @PutMapping("/payment/{id}/confirmed")
    public ResponseEntity<ApiResponse<?>> updatePaymentStatus(
            @AuthenticationPrincipal String userId,
            @PathVariable("id") String bookingId
    ) throws ExecutionException, InterruptedException {
        BookingDTO booking = bookingService.getBookingById(bookingId);
        if (booking == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy vé đặt.");
        }
        bookingService.updatePaymentStatus(bookingId, "SUCCESS", "CONFIRMED");
        bookingService.confirmBookingSeats(bookingId);

        // Also update payments document status to SUCCESS
        try {
            paymentService.updateStatusByBookingId(bookingId, "SUCCESS");
        } catch (Exception e) {
            log.error("Failed to update payment status to SUCCESS for bookingId: " + bookingId, e);
        }

        return ResponseEntity.ok(
                ApiResponse.<BookingDTO>builder()
                        .success(true)
                        .message("Payment confirmed and seats booked successfully")
                        .build()
        );
    }

    @PutMapping("/payment/{id}/failed")
    public ResponseEntity<ApiResponse<?>> cancelBooking(
            @AuthenticationPrincipal String userId,
            @PathVariable("id") String bookingId
    ) throws ExecutionException, InterruptedException {
        BookingDTO booking = bookingService.getBookingById(bookingId);
        if (booking == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy vé đặt.");
        }
        if (!userId.equals(booking.getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bạn không có quyền hủy vé này.");
        }
        bookingService.updatePaymentStatus(bookingId, "FAILED", "CANCELLED");
        bookingService.releaseBookingSeats(bookingId);

        // Also update payments document status to FAILED
        try {
            paymentService.updateStatusByBookingId(bookingId, "FAILED");
        } catch (Exception e) {
            log.error("Failed to update payment status to FAILED for bookingId: " + bookingId, e);
        }

        return ResponseEntity.ok(
                ApiResponse.<BookingDTO>builder()
                        .success(true)
                        .message("Booking cancelled and seats released successfully")
                        .build()
        );
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<BookingDTO>>> searchBookings(
            @AuthenticationPrincipal String userId,
            @RequestParam("query") String query
    ) throws ExecutionException, InterruptedException {
        UserDTO staffUser = userService.getUserById(userId);
        if (staffUser == null || (!"staff".equalsIgnoreCase(staffUser.getRole()) && !"admin".equalsIgnoreCase(staffUser.getRole()))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bạn không có quyền tìm kiếm vé.");
        }

        List<String> userIds = new ArrayList<>();
        List<UserDTO> users = userService.getAllUsers();
        Map<String, UserDTO> c_users = users.stream().collect(Collectors.toMap(UserDTO::getUid, user -> user));

        for (UserDTO u : users) {
            String name = u.getName();
            String email = u.getEmail();
            String phone = u.getPhone();
            if ((name != null && name.toLowerCase().contains(query.toLowerCase())) ||
                    (email != null && email.toLowerCase().contains(query.toLowerCase())) ||
                    (phone != null && phone.contains(query))) {
                userIds.add(u.getUid());
            }
        }

        List<BookingDTO> results = new ArrayList<>();
        List<BookingDTO> bookings = bookingService.getAllBookings();

        Map<String, ShowtimeDTO> showtimes = showtimeService.getAllShowtimes().stream().collect(Collectors.toMap(ShowtimeDTO::getShowtimeId, showtime -> showtime));

        for (BookingDTO booking : bookings) {
            if (booking != null) {
                boolean matchesUser = userIds.contains(booking.getUserId());
                boolean matchesBookingId = booking.getBookingId().equalsIgnoreCase(query) || booking.getBookingId().toLowerCase().contains(query.toLowerCase());
                boolean matchesPaymentCode = booking.getPaymentCode() != null && booking.getPaymentCode().equalsIgnoreCase(query);
                if (matchesUser || matchesBookingId || matchesPaymentCode) {
                    try {
                        booking.setShowtime(showtimes.get(booking.getShowtimeId()));
                    } catch (Exception ignored) {}
                    try {
                        booking.setUser(c_users.get(booking.getUserId()));
                    } catch (Exception ignored) {}
                    results.add(booking);
                }
            }
        }

        return ResponseEntity.ok(
                ApiResponse.<List<BookingDTO>>builder()
                        .success(true)
                        .message("Bookings found successfully")
                        .data(results)
                        .build()
        );
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<StaffStatsDTO>> getStaffStats(
            @AuthenticationPrincipal String userId
    ) throws ExecutionException, InterruptedException {
        UserDTO staffUser = userService.getUserById(userId);
        if (staffUser == null || (!"staff".equalsIgnoreCase(staffUser.getRole()) && !"admin".equalsIgnoreCase(staffUser.getRole()))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bạn không có quyền truy cập thống kê.");
        }

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long startOfToday = cal.getTimeInMillis();

        int total = 0, paid = 0, pending = 0, failed = 0, cancelled = 0;
        List<BookingDTO> docs = bookingService.getBookingsCreatedAfter(startOfToday);

        for (BookingDTO doc : docs) {
            total++;
            String pStatus = doc.getPaymentStatus();
            String bStatus = doc.getBookingStatus();
            if ("confirmed".equalsIgnoreCase(pStatus) || "paid".equalsIgnoreCase(pStatus)) {
                paid++;
            } else if ("pending".equalsIgnoreCase(pStatus)) {
                pending++;
            } else if ("failed".equalsIgnoreCase(pStatus)) {
                failed++;
            } else if ("cancelled".equalsIgnoreCase(bStatus) || "cancelled".equalsIgnoreCase(pStatus)) {
                cancelled++;
            }
        }

        StaffStatsDTO stats = StaffStatsDTO.builder()
                .totalBookingsToday(total)
                .paidBookingsToday(paid)
                .pendingBookingsToday(pending)
                .failedBookingsToday(failed)
                .cancelledBookingsToday(cancelled)
                .build();

        return ResponseEntity.ok(
                ApiResponse.<StaffStatsDTO>builder()
                        .success(true)
                        .message("Stats fetched successfully")
                        .data(stats)
                        .build()
        );
    }

    @PutMapping("/{id}/checkin")
    public ResponseEntity<ApiResponse<Void>> checkInBooking(
            @AuthenticationPrincipal String userId,
            @PathVariable("id") String bookingId
    ) throws ExecutionException, InterruptedException {
        UserDTO staffUser = userService.getUserById(userId);
        if (staffUser == null || (!"staff".equalsIgnoreCase(staffUser.getRole()) && !"admin".equalsIgnoreCase(staffUser.getRole()))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bạn không có quyền thực hiện check-in.");
        }

        BookingDTO booking = bookingService.getBookingById(bookingId);
        if (booking == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy vé đặt.");
        }
        if (!"confirmed".equalsIgnoreCase(booking.getPaymentStatus()) && !"paid".equalsIgnoreCase(booking.getPaymentStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Chưa thanh toán vé này.");
        }
        if (booking.getCheckInAt() > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vé này đã được check-in trước đó.");
        }

        long now = System.currentTimeMillis();
        if (booking.getShowtimeStartAtSnapshot() + 3 * 3600 * 1000 < now) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Suất chiếu của vé này đã kết thúc.");
        }

        bookingService.updateCheckInTime(bookingId, now);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Check-in thành công")
                        .build()
        );
    }
}

package com.cinemabooking.backend.features.booking.controller;
import com.cinemabooking.backend.features.booking.dto.SnackOrderSnapshot;
import com.cinemabooking.backend.features.booking.repository.BookingRepository;
import com.cinemabooking.backend.features.cinema.repository.CinemaRepository;
import com.cinemabooking.backend.features.cinema.repository.SeatRepository;
import com.cinemabooking.backend.features.user.repository.UserRepository;
import com.cinemabooking.backend.features.payment.repository.PaymentRepository;
import com.cinemabooking.backend.features.voucher.service.VoucherService;
import com.cinemabooking.backend.features.cinema.dto.ShowtimeDTO;
import com.cinemabooking.backend.features.user.dto.AdminStatsDTO;
import com.cinemabooking.backend.features.movie.dto.MovieDTO;
import com.cinemabooking.backend.shared.dto.ApiResponse;
import com.cinemabooking.backend.shared.common.PaymentMethod;
import com.cinemabooking.backend.features.cinema.dto.SeatDTO;
import com.cinemabooking.backend.features.voucher.dto.VoucherDTO;
import com.cinemabooking.backend.features.payment.model.Payment;
import com.cinemabooking.backend.features.user.dto.UserDTO;
import com.cinemabooking.backend.features.cinema.dto.SnackDTO;
import com.cinemabooking.backend.features.cinema.dto.CinemaDTO;
import com.cinemabooking.backend.features.cinema.dto.RoomDTO;


import com.cinemabooking.backend.features.booking.request.SeatBookingRequestDTO;
import com.cinemabooking.backend.features.booking.service.BookingService;
import com.cinemabooking.backend.features.booking.dto.BookingDTO;
import com.cinemabooking.backend.features.movie.service.MovieService;
import com.cinemabooking.backend.features.cinema.service.ShowtimeService;
import com.cinemabooking.backend.features.user.service.UserService;
import com.cinemabooking.backend.features.payment.service.PaymentService;
import com.google.cloud.firestore.DocumentSnapshot;
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

    @Autowired private BookingRepository bookingRepository;
    @Autowired private CinemaRepository cinemaRepository;
    @Autowired private SeatRepository seatRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private PaymentRepository paymentRepository;

    @Autowired private BookingService bookingService;
    @Autowired private ShowtimeService showtimeService;
    @Autowired private UserService userService;
    @Autowired private MovieService movieService;
    @Autowired private PaymentService paymentService;
    @Autowired private VoucherService voucherService;

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
        if (data.getSnackOrders() != null && data.getSnackOrders().size() > 0){
            List<SnackDTO> snacks = cinemaRepository.findSnacksByIds(
                    data.getSnackOrders().stream().map(item -> item.snackId()).collect(Collectors.toList())
            ).stream().map(doc -> doc.toObject(SnackDTO.class)).collect(Collectors.toList());

            snacks.stream().forEach(snack -> {
                SeatBookingRequestDTO.SnackOrder order = data.getSnackOrders().stream().filter(snackOrder -> snackOrder.snackId().equals(snack.snackId)).findFirst().orElse(null);
                orders.add(
                        SnackOrderSnapshot.builder()
                                .snackId(snack.getSnackId())
                                .snackName(snack.getName())
                                .snackImgURL(snack.getImageUrl())
                                .price(snack.getPrice())
                                .quantity(order.quantity())
                                .build()
                );
            });
        }

        String uniqueID = UUID.randomUUID().toString();

        ShowtimeDTO showtime = showtimeService.getShowtimeById(data.getShowtimeId());
        MovieDTO movie = movieService.getMovieById(showtime.getMovieId());

        RoomDTO room = cinemaRepository.findRoomDocumentById(showtime.getRoomId()).toObject(RoomDTO.class);
        CinemaDTO cinema = cinemaRepository.findCinemaDocumentById(showtime.getCinemaId()).toObject(CinemaDTO.class);

        List<SeatDTO> seats = seatRepository.findByIds(data.getSeatIds()).stream()
                .map(doc -> doc.toObject(SeatDTO.class))
                .collect(Collectors.toList());

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

        double subTotal = 0;
        for (SeatDTO seat : seats) {
            double price = "VIP".equalsIgnoreCase(seat.getSeatType()) ? 75000 : 60000;
            subTotal += price;
        }
        subTotal += orders.stream().mapToDouble(item -> item.getPrice() * item.getQuantity()).sum();

        double discount = 0;
        String voucherCode = data.getAppliedVoucherCode();
        if (voucherCode == null || voucherCode.isEmpty()) {
            voucherCode = data.getPromoCode();
        }

        if (voucherCode != null && !voucherCode.isEmpty()) {
            try {
                VoucherDTO voucher = voucherService.validateVoucher(voucherCode, userId);
                discount = subTotal * voucher.getDiscountPercent() / 100.0;
            } catch (Exception e) {
                log.warn("Failed to validate voucher during booking creation: {}", e.getMessage());
                if (data.getDiscountVoucher() > 0) {
                    discount = data.getDiscountVoucher();
                } else {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mã voucher không hợp lệ hoặc đã hết hạn.");
                }
            }
        } else if (data.getDiscountVoucher() > 0) {
            discount = data.getDiscountVoucher();
        }

        if (data.getDiscountVoucher() > discount) {
            discount = data.getDiscountVoucher();
        }
        
        double finalTotal = subTotal - discount;
        if (finalTotal < 0) finalTotal = 0;

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
                .seatCodes(seats.stream().map(seat -> seat.getSeatCode()).collect(Collectors.toList()))
                .snackOrder(orders)
                .subtotal(subTotal)
                .discount(discount)
                .total(finalTotal)
                .appliedVoucherCode(voucherCode)
                // cap nhat payment
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
//        if (!userId.equals(booking.getUserId())) {
//            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bạn không có quyền xác nhận vé này.");
//        }
        bookingService.updatePaymentStatus(bookingId, "SUCCESS", "CONFIRMED");
        bookingService.confirmBookingSeats(bookingId);

        // Also update payments document status to SUCCESS
        try {
            List<com.google.cloud.firestore.QueryDocumentSnapshot> payments = paymentRepository.findByBookingId(bookingId);
            for (com.google.cloud.firestore.QueryDocumentSnapshot paymentDoc : payments) {
                paymentRepository.updateStatus(paymentDoc.getId(), "SUCCESS", System.currentTimeMillis());
            }
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
            List<com.google.cloud.firestore.QueryDocumentSnapshot> payments = paymentRepository.findByBookingId(bookingId);
            for (com.google.cloud.firestore.QueryDocumentSnapshot paymentDoc : payments) {
                paymentRepository.updateStatus(paymentDoc.getId(), "FAILED", System.currentTimeMillis());
            }
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

    // TODO: Optimize search query to query Firestore directly instead of loading collections into memory
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<BookingDTO>>> searchBookings(
            @AuthenticationPrincipal String userId,
            @RequestParam("query") String query
    ) throws ExecutionException, InterruptedException {
        UserDTO adminUser = userService.getUserById(userId);
        if (adminUser == null || !"admin".equalsIgnoreCase(adminUser.getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bạn không có quyền tìm kiếm vé.");
        }

        List<String> userIds = new ArrayList<>();
        List<com.google.cloud.firestore.QueryDocumentSnapshot> users = userRepository.findAll();

        Map<String, UserDTO> c_users = users.stream().map(doc -> doc.toObject(UserDTO.class)).collect(Collectors.toMap(UserDTO::getUid, user -> user));

        for (com.google.cloud.firestore.QueryDocumentSnapshot doc : users) {
            String name = doc.getString("name");
            String email = doc.getString("email");
            String phone = doc.getString("phone");
            if ((name != null && name.toLowerCase().contains(query.toLowerCase())) ||
                    (email != null && email.toLowerCase().contains(query.toLowerCase())) ||
                    (phone != null && phone.contains(query))) {
                userIds.add(doc.getId());
            }
        }

        List<BookingDTO> results = new ArrayList<>();
        List<com.google.cloud.firestore.QueryDocumentSnapshot> bookings = bookingRepository.findAll();

        Map<String, ShowtimeDTO> showtimes = showtimeService.getAllShowtimes().stream().collect(Collectors.toMap(ShowtimeDTO::getShowtimeId, showtime -> showtime));

        for (com.google.cloud.firestore.QueryDocumentSnapshot doc : bookings) {
            BookingDTO booking = doc.toObject(BookingDTO.class);
            if (booking != null) {
                booking.setBookingId(doc.getId());
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
    public ResponseEntity<ApiResponse<AdminStatsDTO>> getStaffStats(
            @AuthenticationPrincipal String userId
    ) throws ExecutionException, InterruptedException {
        UserDTO adminUser = userService.getUserById(userId);
        if (adminUser == null || !"admin".equalsIgnoreCase(adminUser.getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bạn không có quyền truy cập thống kê.");
        }

        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);
        long startOfToday = cal.getTimeInMillis();

        int total = 0, paid = 0, pending = 0, failed = 0, cancelled = 0;
        List<com.google.cloud.firestore.QueryDocumentSnapshot> docs = bookingRepository.findCreatedSince(startOfToday);

        for (com.google.cloud.firestore.QueryDocumentSnapshot doc : docs) {
            total++;
            String pStatus = doc.getString("paymentStatus");
            String bStatus = doc.getString("bookingStatus");
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

        AdminStatsDTO stats = AdminStatsDTO.builder()
                .totalBookingsToday(total)
                .paidBookingsToday(paid)
                .pendingBookingsToday(pending)
                .failedBookingsToday(failed)
                .cancelledBookingsToday(cancelled)
                .build();

        return ResponseEntity.ok(
                ApiResponse.<AdminStatsDTO>builder()
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
        UserDTO adminUser = userService.getUserById(userId);
        if (adminUser == null || !"admin".equalsIgnoreCase(adminUser.getRole())) {
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

package com.cinemabooking.backend.features.chat.service;

import com.cinemabooking.backend.features.chat.dto.ChatMessage;
import com.cinemabooking.backend.features.chat.dto.Conversation;
import com.cinemabooking.backend.features.chat.repository.ChatRepository;
import com.cinemabooking.backend.features.chat.request.SendMessageRequest;
import com.cinemabooking.backend.features.cinema.dto.CinemaDTO;
import com.cinemabooking.backend.features.cinema.dto.ShowtimeDTO;
import com.cinemabooking.backend.features.cinema.service.CinemaService;
import com.cinemabooking.backend.features.cinema.service.ShowtimeService;
import com.cinemabooking.backend.features.movie.dto.MovieDTO;
import com.cinemabooking.backend.features.movie.service.MovieService;
import com.cinemabooking.backend.features.booking.dto.BookingDTO;
import com.cinemabooking.backend.features.booking.service.BookingService;
import com.cinemabooking.backend.features.user.dto.UserDTO;
import com.cinemabooking.backend.features.user.repository.AttendanceRepository;
import com.cinemabooking.backend.features.user.repository.UserRepository;
import com.cinemabooking.backend.features.user.service.UserService;
import com.cinemabooking.backend.features.voucher.dto.VoucherDTO;
import com.cinemabooking.backend.features.voucher.service.VoucherService;
import com.google.cloud.firestore.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ChatService {

    @Autowired private MovieService movieService;
    @Autowired private ShowtimeService showtimeService;
    @Autowired private CinemaService cinemaService;
    @Autowired private BookingService bookingService;
    @Autowired private UserService userService;
    @Autowired private VoucherService voucherService;

    @Autowired private ChatRepository chatRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private AttendanceRepository attendanceRepository;
    @Autowired private ConversationService conversationService;

    public ChatMessage sendMessage(String senderId, SendMessageRequest req)
            throws ExecutionException, InterruptedException {

        String receiverId = req.getReceiverId();
        long now = System.currentTimeMillis();
        Conversation convo = null;

        if ("SUPPORT_BOT".equals(receiverId)) {
            convo = conversationService.getConversationByUserIds(senderId, "SUPPORT_BOT");
            if (convo == null) {
                convo = conversationService.createSupportConversation(senderId, now);
            } else if ("RESOLVED".equals(convo.getStatus()) || "CLOSED".equals(convo.getStatus())) {
                convo.setStatus("BOT_ONLY");
                convo.setAssignedStaffId(null);
                List<String> pIds = new ArrayList<>();
                pIds.add(senderId);
                pIds.add("SUPPORT_BOT");
                convo.setParticipantIds(pIds);

                List<Conversation.UserSnapShot> pSnaps = convo.getParticipants().stream()
                        .filter(p -> p.getUserId().equals(senderId) || p.getUserId().equals("SUPPORT_BOT"))
                        .collect(Collectors.toList());
                convo.setParticipants(pSnaps);

                chatRepository.saveConvo(convo);
            }
        } else {
            convo = conversationService.getConversationByUserIds(senderId, receiverId);
            if (convo == null) {
                convo = conversationService.createNewConversation(Arrays.asList(senderId, receiverId), now);
            }
        }

        ChatMessage message = ChatMessage.builder()
                .convoId(convo.getConvoId())
                .senderId(senderId)
                .receiverId(receiverId)
                .content(req.getContent())
                .imgUrl(req.getImgUrl())
                .sentAt(now)
                .build();

        message = chatRepository.saveMessage(message);

        conversationService.updateConversationAfterMessage(
                convo.getConvoId(), message, receiverId, now);

        if (senderId.equals(convo.getAssignedStaffId()) && "ASSIGNED_TO_STAFF".equals(convo.getStatus())) {
            convo.setStatus("IN_PROGRESS");
            chatRepository.updateConvoStatus(convo.getConvoId(), "IN_PROGRESS");
        }

        log.info("Message {} sent from {} to {} in convo {}", message.getMessageId(), senderId, receiverId, convo.getConvoId());

        if ("SUPPORT_BOT".equals(receiverId) && "BOT_ONLY".equals(convo.getStatus())) {
            handleBotResponse(convo, message);
        }

        return message;
    }

    public List<ChatMessage> getMessages(String convoId, int limit, Long beforeTimestamp) throws ExecutionException, InterruptedException {
        return chatRepository.findMessages(convoId, limit);
    }

    public void deleteAllMessages() {
        chatRepository.deleteAllMessages();
    }

    private boolean matchesAny(String content, String[] keywords) {
        for (String kw : keywords) {
            if (content.contains(kw)) return true;
        }
        return false;
    }

    public void handleBotResponse(Conversation convo, ChatMessage customerMsg) throws ExecutionException, InterruptedException {
        if (customerMsg == null || customerMsg.getContent() == null) {
            return;
        }
        String content = customerMsg.getContent().trim().toLowerCase();
        String customerId = customerMsg.getSenderId();
        long now = System.currentTimeMillis();

        String replyContent = "";
        boolean shouldEscalate = false;
        boolean matchedIntent = false;

        // 1. Kiểm tra xác nhận chuyển sang nhân viên từ câu hỏi không hiểu trước đó
        List<ChatMessage> msgs = getMessages(convo.getConvoId(), 2, null);
        if (msgs.size() >= 2) {
            ChatMessage prevMsg = msgs.get(1);
            if ("SUPPORT_BOT".equals(prevMsg.getSenderId()) &&
                    prevMsg.getContent() != null &&
                    prevMsg.getContent().contains("Bạn có muốn kết nối với nhân viên hỗ trợ không")) {

                // Check confirmation
                boolean isConfirm = false;
                for (String kw : new String[]{"có", "co", "yes", "y", "đúng", "dung", "đồng ý", "dong y", "ok", "oke", "được", "duc"}) {
                    if (content.equals(kw) || content.startsWith(kw + " ") || content.endsWith(" " + kw) || content.contains(" " + kw + " ")) {
                        isConfirm = true;
                        break;
                    }
                }

                if (isConfirm) {
                    replyContent = "Dạ vâng, tôi sẽ chuyển bạn đến nhân viên hỗ trợ ngay lập tức...";
                    shouldEscalate = true;
                    matchedIntent = true;
                } else {
                    // Check rejection
                    boolean isReject = false;
                    for (String kw : new String[]{"không", "khong", "no", "n", "hủy", "huy", "đừng", "dung"}) {
                        if (content.equals(kw) || content.startsWith(kw + " ") || content.endsWith(" " + kw) || content.contains(" " + kw + " ")) {
                            isReject = true;
                            break;
                        }
                    }

                    if (isReject) {
                        replyContent = "Dạ vâng, CineBot sẽ tiếp tục hỗ trợ bạn. Bạn cần hỏi thêm thông tin gì không?";
                        matchedIntent = true;
                    }
                }
            }
        }

        // 2. Phân loại các nhóm Intent nếu chưa được xử lý ở bước xác nhận
        if (!matchedIntent) {
            // Nhóm 8: Hoàn tiền (Auto-escalate)
            String[] refundKws = {"hoàn tiền", "hoan tien", "hoàn vé", "hoan ve", "hủy vé", "huy ve", "trả vé", "tra ve", "hoàn lại", "hoan lai", "refund"};

            // Nhóm 9: Lỗi thanh toán (Auto-escalate)
            String[] payIssueKws = {"lỗi thanh toán", "loi thanh toan", "thanh toán lỗi", "thanh toan loi", "không thanh toán được", "khong thanh toan duoc", "lỗi chuyển tiền", "loi chuyen tien", "bị trừ tiền", "bi tru tien", "lỗi trừ tiền", "loi tru tien", "trừ tiền không có vé", "tru tien khong co ve", "lỗi nạp", "loi nap"};

            // Nhóm 11: Khiếu nại (Auto-escalate)
            String[] complaintKws = {"khiếu nại", "khieu nai", "phản ánh", "phan anh", "tệ quá", "te qua", "không hài lòng", "khong hai long", "báo cáo", "bao cao", "tố cáo", "to cao", "phàn nàn", "phan nan", "chất lượng kém", "chat luong kem"};

            // Nhóm 10: Gặp nhân viên (Auto-escalate)
            String[] contactStaffKws = {"gặp nhân viên", "gap nhan vien", "nhân viên", "nhan vien", "gặp hỗ trợ", "gap ho tro", "hỗ trợ viên", "ho tro vien", "gặp admin", "gap admin", "admin", "staff", "chuyển sang nhân viên", "chuyen sang nhan vien"};

            // Nhóm 3: Suất chiếu hôm nay
            String[] todayShowtimeKws = {"suất chiếu hôm nay", "suat chieu hom nay", "suất hôm nay", "suat hom nay", "lịch chiếu hôm nay", "lich chieu hom nay", "suất chiếu trong ngày", "suat chieu trong ngay"};

            // Nhóm 2: Lịch chiếu
            String[] showtimeKws = {"lịch chiếu", "lich chieu", "lịch phim", "lich phim", "xem lịch", "xem lich", "giờ chiếu", "gio chieu", "suất chiếu", "suat chieu"};

            // Nhóm 1: Phim đang chiếu
            String[] nowShowingKws = {"phim đang chiếu", "phim dang chieu", "đang chiếu", "dang chieu", "phim hot", "phim moi", "phim mới", "phim chieu", "phim chiếu", "danh sách phim", "danh sach phim"};

            // Nhóm 4: Rạp chiếu
            String[] cinemaKws = {"rạp chiếu", "rap chieu", "rạp", "rap", "chi nhánh", "chi nhanh", "địa chỉ", "dia chi", "ở đâu", "o dau", "dia diem", "địa điểm"};

            // Nhóm 5: Vé của tôi / booking của tôi
            String[] myBookingKws = {"vé của tôi", "ve cua toi", "booking của tôi", "booking cua toi", "lịch sử đặt vé", "lich su dat ve", "vé đã đặt", "ve da dat", "my booking", "my ticket", "vé đặt", "ve dat", "lịch sử mua", "lich su mua", "vé của t", "ve cua t", "booking cua t", "booking của t"};

            // Nhóm 6: Voucher của tôi
            String[] myVoucherKws = {"voucher của tôi", "voucher cua toi", "voucher", "mã giảm giá", "ma giam gia", "khuyến mãi", "khuyen mai", "giảm giá", "giam gia", "uu dai", "ưu đãi", "mã khuyến mãi", "ma khuyen mai", "mã ưu đãi", "ma uu dai"};

            // Nhóm 7: Giá vé
            String[] priceKws = {"giá vé", "gia ve", "vé bao nhiêu", "ve bao nhieu", "một vé bao nhiêu", "mot ve bao nhieu", "nhiêu tiền", "nhieu tien", "tiền vé", "tien ve", "giá cả", "gia ca", "bao nhiêu tiền", "bao nhieu tien"};

            // Nhóm 13: Thanh toán
            String[] paymentKws = {"thanh toán", "thanh toan", "cổng thanh toán", "cong thanh toan", "momo", "zalopay", "atm", "visa", "chuyển khoản", "chuyen khoan", "pay", "cách mua vé", "cach mua ve", "đặt vé", "dat ve"};

            if (matchesAny(content, refundKws)) {
                replyContent = "Tôi sẽ hỗ trợ bạn thực hiện yêu cầu hoàn tiền/hủy vé. Để thực hiện nghiệp vụ này, tôi sẽ chuyển bạn đến nhân viên hỗ trợ ngay lập tức. Vui lòng đợi trong giây lát...";
                shouldEscalate = true;
            } else if (matchesAny(content, payIssueKws)) {
                replyContent = "Hệ thống ghi nhận bạn gặp sự cố thanh toán. Tôi sẽ chuyển bạn đến nhân viên hỗ trợ để kiểm tra giao dịch của bạn ngay lập tức...";
                shouldEscalate = true;
            } else if (matchesAny(content, complaintKws)) {
                replyContent = "Chúng tôi rất tiếc vì trải nghiệm không tốt của bạn. Tôi sẽ chuyển tiếp yêu cầu khiếu nại này đến nhân viên hỗ trợ để xử lý trực tiếp ngay lập tức...";
                shouldEscalate = true;
            } else if (matchesAny(content, contactStaffKws)) {
                replyContent = "Tôi đang tiến hành kết nối bạn với nhân viên hỗ trợ. Vui lòng đợi trong giây lát...";
                shouldEscalate = true;
            } else if (matchesAny(content, todayShowtimeKws)) {
                try {
                    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+7"));
                    cal.set(Calendar.HOUR_OF_DAY, 0);
                    cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.SECOND, 0);
                    cal.set(Calendar.MILLISECOND, 0);
                    long todayStart = cal.getTimeInMillis();
                    long todayEnd = todayStart + 24 * 60 * 60 * 1000L - 1;

                    String dateStr = new SimpleDateFormat("yyyy-MM-dd").format(new Date(todayStart));
                    log.info("Chatbot Showtime Query - todayStart: {} ({}), todayEnd: {} ({}), date filtered: {}, condition: deleted=false",
                            todayStart, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(todayStart)),
                            todayEnd, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(todayEnd)), dateStr);

                    List<ShowtimeDTO> showtimes = showtimeService.getAllShowtimes().stream()
                            .filter(s -> s.getStartAt() >= todayStart && s.getStartAt() <= todayEnd)
                            .collect(Collectors.toList());

                    String statuses = showtimes.stream().map(ShowtimeDTO::getStatus).distinct().collect(Collectors.joining(", "));
                    log.info("Chatbot Showtime Result - count: {}, date: {}, status filtered/present: [{}]", showtimes.size(), dateStr, statuses);

                    if (showtimes.isEmpty()) {
                        replyContent = "Hôm nay hiện tại không có suất chiếu nào khả dụng. Bạn vui lòng quay lại sau nhé!";
                    } else {
                        List<MovieDTO> allMovies = movieService.getAllMovies(0, 1000);
                        Map<String, String> movieTitleMap = allMovies.stream()
                                .collect(Collectors.toMap(MovieDTO::getMovieId, MovieDTO::getTitle, (a, b) -> a));
                        List<CinemaDTO> allCinemas = cinemaService.getAllCinemas();
                        Map<String, String> cinemaNameMap = allCinemas.stream()
                                .collect(Collectors.toMap(CinemaDTO::getCinemaId, CinemaDTO::getName, (a, b) -> a));

                        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                        sdf.setTimeZone(TimeZone.getTimeZone("GMT+7"));

                        StringBuilder sb = new StringBuilder("🕒 Danh sách các suất chiếu hôm nay:\n");
                        for (ShowtimeDTO s : showtimes) {
                            String movieTitle = movieTitleMap.getOrDefault(s.getMovieId(), "Phim ẩn");
                            String cinemaName = cinemaNameMap.getOrDefault(s.getCinemaId(), "Rạp ẩn");
                            String timeStr = sdf.format(new Date(s.getStartAt()));
                            sb.append("- 🎬 ").append(movieTitle)
                                    .append(" | 🕒 ").append(timeStr)
                                    .append(" | 📍 ").append(cinemaName).append("\n");
                        }
                        replyContent = sb.toString().trim();
                    }
                } catch (Exception e) {
                    log.error("Error fetching showtimes: ", e);
                    replyContent = "Không thể lấy danh sách suất chiếu lúc này. Bạn vui lòng quay lại sau!";
                }
            } else if (matchesAny(content, showtimeKws)) {
                replyContent = "Để xem lịch chiếu chi tiết, bạn vui lòng chọn mục 'Rạp Phim' hoặc 'Điện Ảnh' ở thanh điều hướng bên dưới, chọn phim và nhấn 'Đặt vé' để xem các suất chiếu khả dụng. Hoặc bạn có thể hỏi tôi 'suất chiếu hôm nay' để xem các suất chiếu trong ngày.";
            } else if (matchesAny(content, nowShowingKws)) {
                try {
                    log.info("Chatbot Movie Query - status: NOW_SHOWING, condition: isActive=true, deleted=false, page: 0, size: 50");
                    List<MovieDTO> nowShowing = movieService.getMoviesByStatus("NOW_SHOWING", 0, 50);
                    log.info("Chatbot Movie Result - count: {}, status filtered: NOW_SHOWING", nowShowing.size());
                    if (nowShowing.isEmpty()) {
                        replyContent = "Hiện tại hệ thống không có phim nào đang chiếu. Bạn vui lòng quay lại sau nhé!";
                    } else {
                        StringBuilder sb = new StringBuilder("🎬 Danh sách phim đang chiếu tại rạp:\n");
                        for (MovieDTO m : nowShowing) {
                            sb.append("- ").append(m.getTitle()).append("\n");
                        }
                        sb.append("Bạn có muốn xem lịch chiếu hôm nay không?");
                        replyContent = sb.toString();
                    }
                } catch (Exception e) {
                    log.error("Error fetching movies: ", e);
                    replyContent = "Không thể tải danh sách phim đang chiếu lúc này. Vui lòng quay lại sau!";
                }
            } else if (matchesAny(content, cinemaKws)) {
                try {
                    List<CinemaDTO> cinemas = cinemaService.getAllCinemas();
                    if (cinemas.isEmpty()) {
                        replyContent = "Hệ thống CinemaBookingApp hiện chưa cấu hình rạp chiếu. Vui lòng quay lại sau!";
                    } else {
                        StringBuilder sb = new StringBuilder("📍 Danh sách các rạp chiếu của chúng tôi:\n");
                        for (CinemaDTO c : cinemas) {
                            sb.append("- ").append(c.getName()).append(": ").append(c.getAddress()).append("\n");
                        }
                        replyContent = sb.toString().trim();
                    }
                } catch (Exception e) {
                    log.error("Error fetching cinemas: ", e);
                    replyContent = "Không thể tải thông tin rạp chiếu lúc này.";
                }
            } else if (matchesAny(content, myBookingKws)) {
                try {
                    List<BookingDTO> userBookings = bookingService.getBookingsByUserId(customerId).stream()
                            .filter(b -> b != null && !b.isDeleted())
                            .sorted((b1, b2) -> Long.compare(b2.getCreatedAt(), b1.getCreatedAt()))
                            .limit(3)
                            .collect(Collectors.toList());

                    if (userBookings.isEmpty()) {
                        replyContent = "Bạn chưa có giao dịch đặt vé nào trên hệ thống.";
                    } else {
                        StringBuilder sb = new StringBuilder("🎟 Danh sách 3 vé đặt gần đây nhất của bạn:\n");
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                        sdf.setTimeZone(TimeZone.getTimeZone("GMT+7"));
                        for (BookingDTO b : userBookings) {
                            sb.append("- Mã vé: #").append(b.getBookingId()).append("\n")
                                    .append("  Phim: ").append(b.getMovieTitleSnapshot()).append("\n")
                                    .append("  Rạp: ").append(b.getCinemaNameSnapshot()).append(" (").append(b.getRoomNameSnapshot()).append(")\n")
                                    .append("  Suất chiếu: ").append(sdf.format(new Date(b.getShowtimeStartAtSnapshot()))).append("\n")
                                    .append("  Ghế: ").append(b.getSeatCodes() != null ? String.join(", ", b.getSeatCodes()) : "").append("\n")
                                    .append("  Tổng tiền: ").append(String.format("%,.0fđ", b.getTotal())).append("\n")
                                    .append("  Trạng thái: ").append(b.getPaymentStatus()).append(" | ").append(b.getBookingStatus()).append("\n\n");
                        }
                        replyContent = sb.toString().trim();
                    }
                } catch (Exception e) {
                    log.error("Error fetching user bookings: ", e);
                    replyContent = "Không thể truy xuất lịch sử đặt vé của bạn lúc này.";
                }
            } else if (matchesAny(content, myVoucherKws)) {
                try {
                    List<VoucherDTO> userVouchers = voucherService.getUserVouchers(customerId);
                    if (userVouchers.isEmpty()) {
                        replyContent = "Bạn chưa có mã giảm giá hoặc voucher nào trong tài khoản.";
                    } else {
                        StringBuilder sb = new StringBuilder("🎁 Danh sách voucher của bạn:\n");
                        for (VoucherDTO v : userVouchers) {
                            String code = v.getCode();
                            double discount = v.getDiscountPercent() != null ? v.getDiscountPercent() : 0.0;
                            String statusStr = "ACTIVE".equals(v.getStatus()) ? "Chưa sử dụng" : ("USED".equals(v.getStatus()) ? "Đã sử dụng" : "Hết hạn");

                            sb.append("- Mã: ").append(code)
                                    .append(" | Giảm: ").append(discount).append("%")
                                    .append(" (").append(statusStr).append(")\n");
                        }
                        replyContent = sb.toString().trim();
                    }
                } catch (Exception e) {
                    log.error("Error fetching vouchers: ", e);
                    replyContent = "Không thể lấy thông tin voucher của bạn lúc này.";
                }
            } else if (matchesAny(content, priceKws)) {
                replyContent = "💵 Giá vé tại CinemaBookingApp dao động từ 70.000đ - 120.000đ tùy theo loại ghế (Thường/VIP), định dạng phim (2D/3D) và khung giờ chiếu (Ngày thường/Cuối tuần/Ngày lễ).\nBạn có thể xem giá chi tiết của từng suất chiếu khi tiến hành đặt vé.";
            } else if (matchesAny(content, paymentKws)) {
                replyContent = "💳 App hỗ trợ thanh toán qua nhiều cổng tiện lợi:\n- Ví điện tử: MoMo, ZaloPay\n- Thẻ nội địa (ATM/Napas)\n- Thẻ quốc tế (Visa/Mastercard)\nKhi đặt vé, bạn chỉ cần chọn phương thức thanh toán phù hợp và làm theo hướng dẫn.";
            } else {
                replyContent = "Tôi chưa hiểu yêu cầu của bạn.\nBạn có muốn kết nối với nhân viên hỗ trợ không?";
            }
        }

        ChatMessage botMessage = ChatMessage.builder()
                .convoId(convo.getConvoId())
                .senderId("SUPPORT_BOT")
                .receiverId(customerId)
                .content(replyContent)
                .sentAt(now)
                .build();

        chatRepository.saveMessage(botMessage);

        conversationService.updateConversationAfterMessage(convo.getConvoId(), botMessage, customerId, now);

        if (shouldEscalate) {
            escalateConversationToStaff(convo.getConvoId());
        }
    }

    public Conversation escalateConversationToStaff(String convoId) throws ExecutionException, InterruptedException {
        long now = System.currentTimeMillis();
        Conversation convo = chatRepository.findConvoById(convoId);
        if (convo == null) return null;

        String customerId = convo.getParticipantIds().stream()
                .filter(id -> !"SUPPORT_BOT".equals(id) && !id.equals(convo.getAssignedStaffId()))
                .findFirst().orElse(null);
        if (customerId == null) return convo;

        String todayDate = new SimpleDateFormat("yyyy-MM-dd") {{
            setTimeZone(TimeZone.getTimeZone("GMT+7"));
        }}.format(new Date(now));

        List<QueryDocumentSnapshot> checkIns = attendanceRepository.findActiveCheckInsByDate(todayDate);

        List<String> onlineStaffIds = checkIns.stream()
                .map(doc -> doc.getString("staffId"))
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        List<UserDTO> candidates = new ArrayList<>();
        if (!onlineStaffIds.isEmpty()) {
            for (String staffId : onlineStaffIds) {
                DocumentSnapshot userDoc = userRepository.findById(staffId);
                if (userDoc.exists()) {
                    UserDTO user = userDoc.toObject(UserDTO.class);
                    if (user != null && "staff".equalsIgnoreCase(user.getRole()) && !"inactive".equalsIgnoreCase(user.getStatus())) {
                        candidates.add(user);
                    }
                }
            }
        }

        if (candidates.isEmpty()) {
            List<QueryDocumentSnapshot> allStaffDocs = userRepository.findAllStaffsAndAdmins();
            for (DocumentSnapshot doc : allStaffDocs) {
                UserDTO user = doc.toObject(UserDTO.class);
                if (user != null && "staff".equalsIgnoreCase(user.getRole()) && !"inactive".equalsIgnoreCase(user.getStatus())) {
                    candidates.add(user);
                }
            }
        }

        UserDTO selectedStaff = null;
        if (!candidates.isEmpty()) {
            int minTickets = Integer.MAX_VALUE;
            for (UserDTO staff : candidates) {
                int activeTickets = chatRepository.countActiveConvosForStaff(staff.getUid());

                if (activeTickets < minTickets) {
                    minTickets = activeTickets;
                    selectedStaff = staff;
                }
            }
        }

        if (selectedStaff != null) {
            String staffId = selectedStaff.getUid();

            List<String> participantIds = new ArrayList<>(convo.getParticipantIds());
            if (!participantIds.contains(staffId)) {
                participantIds.add(staffId);
            }

            List<Conversation.UserSnapShot> participants = new ArrayList<>(convo.getParticipants());
            participants.removeIf(p -> p.getUserId().equals(convo.getAssignedStaffId()));
            participants.add(Conversation.UserSnapShot.builder()
                    .userId(staffId)
                    .name(selectedStaff.getName())
                    .email(selectedStaff.getEmail())
                    .avatarUrl(selectedStaff.getAvatarUrl())
                    .build());

            convo.setParticipantIds(participantIds);
            convo.setParticipants(participants);
            convo.setAssignedStaffId(staffId);
            convo.setStatus("ASSIGNED_TO_STAFF");
            convo.setHadStaff(true);
            convo.setUpdatedAt(now);

            ChatMessage sysMessage = ChatMessage.builder()
                    .convoId(convoId)
                    .senderId("SYSTEM")
                    .receiverId(customerId)
                    .content("Hệ thống đã kết nối bạn với nhân viên hỗ trợ: " + selectedStaff.getName() + ".")
                    .sentAt(now)
                    .build();

            chatRepository.saveMessage(sysMessage);

            convo.setLastMessage(sysMessage);
            convo.setLastMessageAt(now);
            chatRepository.saveConvo(convo);
        } else {
            String oldStatus = convo.getStatus();
            if ("RESOLVED".equals(oldStatus) || "CLOSED".equals(oldStatus) || Boolean.TRUE.equals(convo.getHadStaff())) {
                convo.setStatus("REOPENED");
            } else {
                convo.setStatus("WAITING_STAFF");
            }
            convo.setUpdatedAt(now);

            ChatMessage sysMessage = ChatMessage.builder()
                    .convoId(convoId)
                    .senderId("SYSTEM")
                    .receiverId(customerId)
                    .content("Hiện tại tất cả nhân viên đều đang bận hoặc offline. Yêu cầu của bạn đã được ghi nhận, vui lòng đợi trong giây lát.")
                    .sentAt(now)
                    .build();

            chatRepository.saveMessage(sysMessage);

            convo.setLastMessage(sysMessage);
            convo.setLastMessageAt(now);
            chatRepository.saveConvo(convo);
        }

        return convo;
    }

    public void resolveConversation(String convoId) throws ExecutionException, InterruptedException {
        chatRepository.updateConvoStatus(convoId, "RESOLVED");
    }

    public List<Conversation> getWaitingConversations() throws ExecutionException, InterruptedException {
        return chatRepository.findWaitingConvos();
    }

    public Conversation claimConversation(String convoId, String staffId) throws ExecutionException, InterruptedException {
        long now = System.currentTimeMillis();
        Conversation convo = chatRepository.findConvoById(convoId);
        if (convo == null) return null;

        DocumentSnapshot staffDoc = userRepository.findById(staffId);
        UserDTO staffDTO = staffDoc.toObject(UserDTO.class);
        if (staffDTO == null) return convo;

        List<String> participantIds = new ArrayList<>(convo.getParticipantIds());
        if (!participantIds.contains(staffId)) {
            participantIds.add(staffId);
        }

        List<Conversation.UserSnapShot> participants = new ArrayList<>(convo.getParticipants());
        participants.removeIf(p -> p.getUserId().equals(convo.getAssignedStaffId()));
        participants.add(Conversation.UserSnapShot.builder()
                .userId(staffId)
                .name(staffDTO.getName())
                .email(staffDTO.getEmail())
                .avatarUrl(staffDTO.getAvatarUrl())
                .build());

        convo.setParticipantIds(participantIds);
        convo.setParticipants(participants);
        convo.setAssignedStaffId(staffId);
        convo.setStatus("ASSIGNED_TO_STAFF");
        convo.setHadStaff(true);
        convo.setUpdatedAt(now);

        String customerId = convo.getParticipantIds().stream()
                .filter(id -> !"SUPPORT_BOT".equals(id) && !id.equals(staffId))
                .findFirst().orElse(null);

        ChatMessage sysMessage = ChatMessage.builder()
                .convoId(convoId)
                .senderId("SYSTEM")
                .receiverId(customerId)
                .content("Nhân viên " + staffDTO.getName() + " đã tiếp nhận hỗ trợ cuộc trò chuyện này.")
                .sentAt(now)
                .build();

        chatRepository.saveMessage(sysMessage);

        convo.setLastMessage(sysMessage);
        convo.setLastMessageAt(now);
        chatRepository.saveConvo(convo);

        return convo;
    }

    public void clearConversationMessages(String convoId) throws ExecutionException, InterruptedException {
        chatRepository.deleteMessagesByConvoId(convoId);

        Map<String, Object> updates = new HashMap<>();
        updates.put("lastMessage", null);
        updates.put("lastMessageAt", null);
        chatRepository.updateConvoFields(convoId, updates);
    }

    public Conversation returnToBot(String convoId) throws ExecutionException, InterruptedException {
        long now = System.currentTimeMillis();
        Conversation convo = chatRepository.findConvoById(convoId);
        if (convo == null) return null;

        String customerId = convo.getParticipantIds().stream()
                .filter(id -> !"SUPPORT_BOT".equals(id) && !id.equals(convo.getAssignedStaffId()))
                .findFirst().orElse(null);

        if (customerId == null) {
            customerId = convo.getParticipantIds().stream()
                    .filter(id -> !"SUPPORT_BOT".equals(id))
                    .findFirst().orElse(null);
        }

        convo.setStatus("BOT_ONLY");
        convo.setAssignedStaffId(null);
        convo.setUpdatedAt(now);

        List<String> pIds = new ArrayList<>();
        if (customerId != null) pIds.add(customerId);
        pIds.add("SUPPORT_BOT");
        convo.setParticipantIds(pIds);

        final String finalCustId = customerId;
        List<Conversation.UserSnapShot> pSnaps = convo.getParticipants().stream()
                .filter(p -> p.getUserId().equals(finalCustId) || p.getUserId().equals("SUPPORT_BOT"))
                .collect(Collectors.toList());
        convo.setParticipants(pSnaps);

        // Add system message
        ChatMessage sysMessage = ChatMessage.builder()
                .convoId(convoId)
                .senderId("SYSTEM")
                .receiverId(customerId)
                .content("Hỗ trợ viên đã kết thúc phiên làm việc. Trợ lý ảo sẽ tiếp tục hỗ trợ bạn.")
                .sentAt(now)
                .build();

        chatRepository.saveMessage(sysMessage);

        convo.setLastMessage(sysMessage);
        convo.setLastMessageAt(now);
        chatRepository.saveConvo(convo);

        return convo;
    }

    public void closeConversation(String convoId) throws ExecutionException, InterruptedException {
        long now = System.currentTimeMillis();
        Conversation convo = chatRepository.findConvoById(convoId);
        if (convo == null) return;

        convo.setStatus("CLOSED");
        convo.setUpdatedAt(now);

        String customerId = convo.getParticipantIds().stream()
                .filter(id -> !"SUPPORT_BOT".equals(id) && !id.equals(convo.getAssignedStaffId()))
                .findFirst().orElse(null);

        ChatMessage sysMessage = ChatMessage.builder()
                .convoId(convoId)
                .senderId("SYSTEM")
                .receiverId(customerId)
                .content("Hỗ trợ viên đã đóng cuộc trò chuyện hỗ trợ này.")
                .sentAt(now)
                .build();

        chatRepository.saveMessage(sysMessage);

        convo.setLastMessage(sysMessage);
        convo.setLastMessageAt(now);
        chatRepository.saveConvo(convo);
    }

    public Conversation reopenConversation(String convoId) throws ExecutionException, InterruptedException {
        long now = System.currentTimeMillis();
        Conversation convo = chatRepository.findConvoById(convoId);
        if (convo == null) return null;

        String customerId = convo.getParticipantIds().stream()
                .filter(id -> !"SUPPORT_BOT".equals(id) && !id.equals(convo.getAssignedStaffId()))
                .findFirst().orElse(null);
        if (customerId == null) {
            customerId = convo.getParticipantIds().stream()
                    .filter(id -> !"SUPPORT_BOT".equals(id))
                    .findFirst().orElse(null);
        }

        // Try to allocate staff
        String todayDate = new SimpleDateFormat("yyyy-MM-dd") {{
            setTimeZone(TimeZone.getTimeZone("GMT+7"));
        }}.format(new Date(now));

        List<QueryDocumentSnapshot> checkIns = attendanceRepository.findActiveCheckInsByDate(todayDate);

        List<String> onlineStaffIds = checkIns.stream()
                .map(doc -> doc.getString("staffId"))
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        List<UserDTO> candidates = new ArrayList<>();
        if (!onlineStaffIds.isEmpty()) {
            for (String staffId : onlineStaffIds) {
                DocumentSnapshot userDoc = userRepository.findById(staffId);
                if (userDoc.exists()) {
                    UserDTO user = userDoc.toObject(UserDTO.class);
                    if (user != null && "staff".equalsIgnoreCase(user.getRole()) && !"inactive".equalsIgnoreCase(user.getStatus())) {
                        candidates.add(user);
                    }
                }
            }
        }

        if (candidates.isEmpty()) {
            List<QueryDocumentSnapshot> allStaffDocs = userRepository.findAllStaffsAndAdmins();
            for (DocumentSnapshot doc : allStaffDocs) {
                UserDTO user = doc.toObject(UserDTO.class);
                if (user != null && "staff".equalsIgnoreCase(user.getRole()) && !"inactive".equalsIgnoreCase(user.getStatus())) {
                    candidates.add(user);
                }
            }
        }

        UserDTO selectedStaff = null;
        if (!candidates.isEmpty()) {
            int minTickets = Integer.MAX_VALUE;
            for (UserDTO staff : candidates) {
                int activeTickets = chatRepository.countActiveConvosForStaff(staff.getUid());

                if (activeTickets < minTickets) {
                    minTickets = activeTickets;
                    selectedStaff = staff;
                }
            }
        }

        if (selectedStaff != null) {
            String staffId = selectedStaff.getUid();

            List<String> participantIds = new ArrayList<>(convo.getParticipantIds());
            if (!participantIds.contains(staffId)) {
                participantIds.add(staffId);
            }

            List<Conversation.UserSnapShot> participants = new ArrayList<>(convo.getParticipants());
            participants.removeIf(p -> p.getUserId().equals(convo.getAssignedStaffId()));
            participants.add(Conversation.UserSnapShot.builder()
                    .userId(staffId)
                    .name(selectedStaff.getName())
                    .email(selectedStaff.getEmail())
                    .avatarUrl(selectedStaff.getAvatarUrl())
                    .build());

            convo.setParticipantIds(participantIds);
            convo.setParticipants(participants);
            convo.setAssignedStaffId(staffId);
            convo.setStatus("ASSIGNED_TO_STAFF");
            convo.setHadStaff(true);
            convo.setUpdatedAt(now);

            ChatMessage sysMessage = ChatMessage.builder()
                    .convoId(convoId)
                    .senderId("SYSTEM")
                    .receiverId(customerId)
                    .content("Hệ thống đã kết nối bạn với nhân viên hỗ trợ: " + selectedStaff.getName() + ".")
                    .sentAt(now)
                    .build();

            chatRepository.saveMessage(sysMessage);

            convo.setLastMessage(sysMessage);
            convo.setLastMessageAt(now);
            chatRepository.saveConvo(convo);
        } else {
            convo.setStatus("WAITING_STAFF");
            convo.setUpdatedAt(now);

            ChatMessage sysMessage = ChatMessage.builder()
                    .convoId(convoId)
                    .senderId("SYSTEM")
                    .receiverId(customerId)
                    .content("Hiện tại tất cả nhân viên đều đang bận hoặc offline. Yêu cầu của bạn đã được ghi nhận, vui lòng đợi trong giây lát.")
                    .sentAt(now)
                    .build();

            chatRepository.saveMessage(sysMessage);

            convo.setLastMessage(sysMessage);
            convo.setLastMessageAt(now);
            chatRepository.saveConvo(convo);
        }

        return convo;
    }
}

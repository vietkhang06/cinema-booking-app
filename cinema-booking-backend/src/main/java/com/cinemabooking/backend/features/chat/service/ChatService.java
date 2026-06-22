package com.cinemabooking.backend.features.chat.service;
import com.cinemabooking.backend.features.cinema.service.ShowtimeService;
import com.cinemabooking.backend.features.movie.service.MovieService;
import com.cinemabooking.backend.features.booking.service.BookingService;
import com.cinemabooking.backend.features.cinema.service.CinemaService;

import com.cinemabooking.backend.features.chat.model.ChatMessage;
import com.cinemabooking.backend.features.chat.model.Conversation;
import com.cinemabooking.backend.features.chat.repository.ChatRepository;
import com.cinemabooking.backend.features.chat.request.SendMessageRequest;
import com.cinemabooking.backend.features.user.dto.UserDTO;
import com.cinemabooking.backend.features.user.repository.UserRepository;
import com.cinemabooking.backend.features.booking.repository.BookingRepository;
import com.cinemabooking.backend.features.voucher.repository.VoucherRepository;
import com.cinemabooking.backend.features.movie.dto.MovieDTO;
import com.cinemabooking.backend.features.cinema.dto.ShowtimeDTO;
import com.cinemabooking.backend.features.cinema.dto.CinemaDTO;
import com.cinemabooking.backend.features.booking.dto.BookingDTO;
import com.google.cloud.firestore.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Collectors;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;

@Slf4j
@Service
public class ChatService {

    @Autowired private MovieService movieService;
    @Autowired private ShowtimeService showtimeService;
    @Autowired private CinemaService cinemaService;
    @Autowired private BookingService bookingService;

    @Autowired private ChatRepository chatRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private BookingRepository bookingRepository;
    @Autowired private VoucherRepository voucherRepository;
    @Autowired private ConversationService conversationService;

    public ChatMessage sendMessage(String senderId, SendMessageRequest req)
            throws ExecutionException, InterruptedException {

        String receiverId = "SUPPORT_BOT"; // Chỉ hỗ trợ duy nhất gửi cho bot
        long now = System.currentTimeMillis();
        Conversation convo = conversationService.getConversationByUserIds(senderId, "SUPPORT_BOT");
        
        if (convo == null) {
            convo = conversationService.createSupportConversation(senderId, now);
        } else if ("RESOLVED".equals(convo.getStatus()) || "CLOSED".equals(convo.getStatus()) || "ASSIGNED_TO_STAFF".equals(convo.getStatus())) {
            convo.setStatus("BOT_ONLY");
            convo.setAssignedStaffId(null);
            chatRepository.saveConversation(convo);
        }

        ChatMessage message = ChatMessage.builder()
                .convoId(convo.getConvoId())
                .senderId(senderId)
                .receiverId(receiverId)
                .content(req.getContent())
                .imgUrl(req.getImgUrl())
                .sentAt(now)
                .build();

        message = saveMessage(message);

        conversationService.updateConversationAfterMessage(
                convo.getConvoId(), message, receiverId, now);

        log.info("Bot Message {} sent from {} in convo {}", message.getMessageId(), senderId, convo.getConvoId());

        handleBotResponse(convo, message);

        return message;
    }

    public List<ChatMessage> getMessages(String convoId, int limit, Long beforeTimestamp) throws ExecutionException, InterruptedException {
        return chatRepository.getMessages(convoId, limit);
    }

    private ChatMessage saveMessage(ChatMessage message) throws ExecutionException, InterruptedException {
        chatRepository.saveMessage(message);
        return message;
    }

    public void deleteAllMessages() throws ExecutionException, InterruptedException {
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

        // Phân loại các Intent của Bot
        String[] refundKws = {"hoàn tiền", "hoan tien", "hoàn vé", "hoan ve", "hủy vé", "huy ve", "trả vé", "tra ve"};
        String[] payIssueKws = {"lỗi thanh toán", "loi thanh toan", "thanh toán lỗi", "thanh toan loi"};
        String[] complaintKws = {"khiếu nại", "khieu nai", "phản ánh", "phan anh"};
        String[] todayShowtimeKws = {"suất chiếu hôm nay", "suat chieu hom nay", "suất hôm nay", "suat hom nay", "lịch chiếu hôm nay", "lich chieu hom nay"};
        String[] showtimeKws = {"lịch chiếu", "lich chieu", "giờ chiếu", "gio chieu", "suất chiếu", "suat chieu"};
        String[] nowShowingKws = {"phim đang chiếu", "phim dang chieu", "đang chiếu", "dang chieu", "phim hot"};
        String[] cinemaKws = {"rạp chiếu", "rap chieu", "rạp", "rap", "địa chỉ", "dia chi"};
        String[] myBookingKws = {"vé của tôi", "ve cua toi", "booking của tôi", "booking cua toi", "lịch sử đặt vé", "lich su dat ve"};
        String[] myVoucherKws = {"voucher của tôi", "voucher cua toi", "voucher", "mã giảm giá", "ma giam gia"};
        String[] priceKws = {"giá vé", "gia ve", "vé bao nhiêu", "ve bao nhieu"};
        String[] paymentKws = {"thanh toán", "thanh toan", "momo", "chuyển khoản"};

        if (matchesAny(content, refundKws)) {
            replyContent = "Dạ chào bạn, đối với yêu cầu hủy vé/hoàn tiền, bạn vui lòng liên hệ trực tiếp số hotline: 1900-xxxx để được tổng đài viên xử lý trực tiếp.";
        } else if (matchesAny(content, payIssueKws)) {
            replyContent = "Nếu gặp sự cố giao dịch bị trừ tiền nhưng chưa nhận được mã vé, bạn vui lòng gửi biên lai chuyển tiền qua email support@cinemabooking.com để ban quản trị xử lý.";
        } else if (matchesAny(content, complaintKws)) {
            replyContent = "Rất tiếc về trải nghiệm không tốt của bạn. Ý kiến đóng góp của bạn đã được ghi nhận và gửi đến ban quản lý rạp.";
        } else if (matchesAny(content, todayShowtimeKws)) {
            try {
                java.util.Calendar cal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("GMT+7"));
                cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
                cal.set(java.util.Calendar.MINUTE, 0);
                cal.set(java.util.Calendar.SECOND, 0);
                cal.set(java.util.Calendar.MILLISECOND, 0);
                long todayStart = cal.getTimeInMillis();
                long todayEnd = todayStart + 24 * 60 * 60 * 1000L - 1;

                // TỐI ƯU FIRESTORE READS: Chỉ lấy suất chiếu hôm nay trực tiếp từ DB
                List<ShowtimeDTO> showtimes = showtimeService.getShowtimesBetween(todayStart, todayEnd);

                if (showtimes.isEmpty()) {
                    replyContent = "Hôm nay hiện tại không có suất chiếu nào khả dụng. Bạn vui lòng quay lại sau nhé!";
                } else {
                    List<MovieDTO> allMovies = movieService.getAllMovies(0, 1000);
                    Map<String, String> movieTitleMap = allMovies.stream()
                            .collect(Collectors.toMap(MovieDTO::getMovieId, MovieDTO::getTitle, (a, b) -> a));
                    List<CinemaDTO> allCinemas = cinemaService.getAllCinemas();
                    Map<String, String> cinemaNameMap = allCinemas.stream()
                            .collect(Collectors.toMap(CinemaDTO::getCinemaId, CinemaDTO::getName, (a, b) -> a));

                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm");
                    sdf.setTimeZone(java.util.TimeZone.getTimeZone("GMT+7"));

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
            replyContent = "Để xem lịch chiếu chi tiết, bạn vui lòng chọn mục 'Rạp Phim' hoặc 'Điện Ảnh' ở thanh điều hướng bên dưới, chọn phim và nhấn 'Đặt vé' để xem các suất chiếu khả dụng.";
        } else if (matchesAny(content, nowShowingKws)) {
            try {
                List<MovieDTO> nowShowing = movieService.getMoviesByStatus("NOW_SHOWING", 0, 50);
                if (nowShowing.isEmpty()) {
                    replyContent = "Hiện tại hệ thống không có phim nào đang chiếu. Bạn vui lòng quay lại sau nhé!";
                } else {
                    StringBuilder sb = new StringBuilder("🎬 Danh sách phim đang chiếu tại rạp:\n");
                    for (MovieDTO m : nowShowing) {
                        sb.append("- ").append(m.getTitle()).append("\n");
                    }
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
                    replyContent = "Hệ thống CinemaBookingApp hiện chưa cấu hình rạp chiếu.";
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
                List<QueryDocumentSnapshot> bookingDocs = bookingRepository.findByUserId(customerId);
                List<BookingDTO> userBookings = bookingDocs.stream()
                        .map(doc -> doc.toObject(BookingDTO.class))
                        .filter(b -> b != null && !b.isDeleted())
                        .sorted((b1, b2) -> Long.compare(b2.getCreatedAt(), b1.getCreatedAt()))
                        .limit(3)
                        .collect(Collectors.toList());

                if (userBookings.isEmpty()) {
                    replyContent = "Bạn chưa có giao dịch đặt vé nào trên hệ thống.";
                } else {
                    StringBuilder sb = new StringBuilder("🎟 Danh sách 3 vé đặt gần đây nhất của bạn:\n");
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");
                    sdf.setTimeZone(java.util.TimeZone.getTimeZone("GMT+7"));
                    for (BookingDTO b : userBookings) {
                        sb.append("- Mã vé: #").append(b.getBookingId()).append("\n")
                          .append("  Phim: ").append(b.getMovieTitleSnapshot()).append("\n")
                          .append("  Rạp: ").append(b.getCinemaNameSnapshot()).append(" (").append(b.getRoomNameSnapshot()).append(")\n")
                          .append("  Suất chiếu: ").append(sdf.format(new Date(b.getShowtimeStartAtSnapshot()))).append("\n")
                          .append("  Tổng tiền: ").append(String.format("%,.0fđ", b.getTotal())).append("\n\n");
                    }
                    replyContent = sb.toString().trim();
                }
            } catch (Exception e) {
                log.error("Error fetching user bookings: ", e);
                replyContent = "Không thể truy xuất lịch sử đặt vé của bạn lúc này.";
            }
        } else if (matchesAny(content, myVoucherKws)) {
            try {
                List<QueryDocumentSnapshot> voucherDocs = voucherRepository.findByUserId(customerId);
                List<Map<String, Object>> userVouchers = new ArrayList<>();
                for (DocumentSnapshot doc : voucherDocs) {
                    Map<String, Object> data = doc.getData();
                    if (data != null) {
                        data.put("voucherId", doc.getId());
                        userVouchers.add(data);
                    }
                }
                userVouchers.sort((v1, v2) -> {
                    long t1 = v1.get("createdAt") != null ? ((Number) v1.get("createdAt")).longValue() : 0L;
                    long t2 = v2.get("createdAt") != null ? ((Number) v2.get("createdAt")).longValue() : 0L;
                    return Long.compare(t2, t1);
                });

                if (userVouchers.isEmpty()) {
                    replyContent = "Bạn chưa có mã giảm giá hoặc voucher nào trong tài khoản.";
                } else {
                    StringBuilder sb = new StringBuilder("🎁 Danh sách voucher của bạn:\n");
                    for (Map<String, Object> v : userVouchers) {
                        String vId = (String) v.get("voucherId");
                        Number val = (Number) v.get("discountValue");
                        double discount = val != null ? val.doubleValue() : 0.0;
                        Boolean used = (Boolean) v.get("isUsed");
                        boolean isUsed = used != null && used;
                        String status = isUsed ? "Đã sử dụng" : "Chưa sử dụng";
                        
                        sb.append("- Mã: ").append(vId)
                          .append(" | Giảm: ").append(String.format("%,.0fđ", discount))
                          .append(" (").append(status).append(")\n");
                    }
                    replyContent = sb.toString().trim();
                }
            } catch (Exception e) {
                log.error("Error fetching vouchers: ", e);
                replyContent = "Không thể lấy thông tin voucher của bạn lúc này.";
            }
        } else if (matchesAny(content, priceKws)) {
            replyContent = "💵 Giá vé tại CinemaBookingApp dao động từ 70.000đ - 120.000đ tùy theo loại ghế (Thường/VIP), định dạng phim (2D/3D).";
        } else if (matchesAny(content, paymentKws)) {
            replyContent = "💳 App hỗ trợ thanh toán qua Ví điện tử MoMo, Thẻ ATM/Napas, Visa/Mastercard.";
        } else {
            replyContent = "Xin lỗi, tôi chưa rõ câu hỏi của bạn. Bạn vui lòng thử hỏi về 'suất chiếu hôm nay', 'phim đang chiếu', 'lịch sử đặt vé' hoặc 'voucher' nhé!";
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
    }

    public void clearConversationMessages(String convoId) throws ExecutionException, InterruptedException {
        chatRepository.clearConversationMessages(convoId);
    }
}
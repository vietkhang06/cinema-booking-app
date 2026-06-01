package com.example.cinemabookingapp.ui.customer.transaction;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.cinemabookingapp.R;
import com.example.cinemabookingapp.core.utils.QRCodeUtils;
import com.example.cinemabookingapp.di.ServiceProvider;
import com.example.cinemabookingapp.domain.common.ResultCallback;
import com.example.cinemabookingapp.domain.model.Booking;
import com.example.cinemabookingapp.service.BookingService;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import android.view.View;
import android.graphics.Canvas;
import android.content.ContentValues;
import android.net.Uri;
import android.provider.MediaStore;
import java.io.OutputStream;
import android.os.Build;
import android.os.Environment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

public class TicketDetailActivity extends AppCompatActivity {

    public static final String EXTRA_BOOKING_ID = "extra_booking_id";

    private ImageView imgPoster, imgQr;
    private TextView tvTitle, tvCinema, tvDate, tvTime, tvRoom, tvSeats, tvBookingCode, tvTotal, tvQrHint;
    private MaterialCardView ticketContainer;
    private MaterialButton btnDownload;
    private BookingService bookingService;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    private View layoutHeaderInfo, dividerLine, gridDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket_detail);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        String bookingId = getIntent().getStringExtra(EXTRA_BOOKING_ID);
        if (bookingId == null) {
            Toast.makeText(this, "Không tìm thấy mã vé", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        bookingService = ServiceProvider.getInstance().getBookingService();

        initViews();
        loadBookingDetails(bookingId);
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        imgPoster = findViewById(R.id.img_poster);
        imgQr = findViewById(R.id.img_qr);
        tvTitle = findViewById(R.id.tv_title);
        tvCinema = findViewById(R.id.tv_cinema);
        tvDate = findViewById(R.id.tv_date);
        tvTime = findViewById(R.id.tv_time);
        tvRoom = findViewById(R.id.tv_room);
        tvSeats = findViewById(R.id.tv_seats);
        tvBookingCode = findViewById(R.id.tv_booking_code);
        tvTotal = findViewById(R.id.tv_total);
        tvQrHint = findViewById(R.id.tv_qr_hint);
        ticketContainer = findViewById(R.id.ticket_container);
        btnDownload = findViewById(R.id.btn_download);

        layoutHeaderInfo = findViewById(R.id.layout_header_info);
        dividerLine      = findViewById(R.id.divider_line);
        gridDetails      = findViewById(R.id.grid_details);

        btnDownload.setOnClickListener(v -> downloadTicket());
    }

    private void loadBookingDetails(String bookingId) {
        FirebaseFirestore.getInstance().collection("cine_shop_orders")
                .document(bookingId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        bindCineShopData(doc);
                    } else {
                        loadMovieBookingDetails(bookingId);
                    }
                })
                .addOnFailureListener(e -> {
                    loadMovieBookingDetails(bookingId);
                });
    }

    private void loadMovieBookingDetails(String bookingId) {
        bookingService.getBookingDetails(bookingId, new ResultCallback<Booking>() {
            @Override
            public void onSuccess(Booking booking) {
                boolean isPaid = "paid".equalsIgnoreCase(booking.paymentStatus)
                        || "confirmed".equalsIgnoreCase(booking.bookingStatus)
                        || "success".equalsIgnoreCase(booking.bookingStatus);
                if (isPaid) {
                    bindData(booking);
                } else {
                    Toast.makeText(TicketDetailActivity.this, "Vé chưa được thanh toán!", Toast.LENGTH_LONG).show();
                    finish();
                }
            }

            @Override
            public void onError(String message) {
                Toast.makeText(TicketDetailActivity.this, "Lỗi tải vé: " + message, Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    private void bindCineShopData(DocumentSnapshot doc) {
        String status = doc.getString("status");
        boolean isPaid = "paid".equalsIgnoreCase(status)
                || "confirmed".equalsIgnoreCase(status)
                || "success".equalsIgnoreCase(status);
        if (!isPaid) {
            Toast.makeText(TicketDetailActivity.this, "Đơn hàng chưa được thanh toán!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        String orderId = doc.getId();
        String itemName = doc.getString("itemName");
        String itemImageUrl = doc.getString("itemImageUrl");
        String paymentMethod = doc.getString("paymentMethod");
        Long quantity = doc.getLong("quantity");
        Double totalPrice = doc.getDouble("totalPrice");
        Long createdAt = doc.getLong("createdAt");
        String userId = doc.getString("userId");

        // Keep header info visible, hide grid details and divider line
        if (layoutHeaderInfo != null) layoutHeaderInfo.setVisibility(View.VISIBLE);
        if (dividerLine != null) dividerLine.setVisibility(View.GONE);
        if (gridDetails != null) gridDetails.setVisibility(View.GONE);

        tvTitle.setText(itemName != null ? itemName : "Sản phẩm CineShop");
        tvCinema.setText("CineShop - Nhận tại quầy rạp");

        tvBookingCode.setText("Order ID: #" + orderId);
        tvTotal.setText(String.format("%,.0fđ", totalPrice != null ? totalPrice : 0.0).replace(',', '.'));

        // Generate QR Code containing CineShop details
        String qrContent = String.format("%s|%s|CINE_SHOP|%d",
                orderId,
                userId != null ? userId : "",
                createdAt != null ? createdAt : System.currentTimeMillis());

        new Thread(() -> {
            Bitmap qrBitmap = QRCodeUtils.generateQRCode(qrContent, 500, 500);
            if (qrBitmap != null) {
                runOnUiThread(() -> imgQr.setImageBitmap(qrBitmap));
            }
        }).start();

        Glide.with(this)
                .load(!android.text.TextUtils.isEmpty(itemImageUrl) ? itemImageUrl : R.drawable.bg_banner_placeholder)
                .placeholder(R.drawable.bg_banner_placeholder)
                .error(R.drawable.bg_banner_placeholder)
                .into(imgPoster);
    }

    private void bindData(Booking booking) {
        // Show above-QR layouts for Movie tickets
        if (layoutHeaderInfo != null) layoutHeaderInfo.setVisibility(View.VISIBLE);
        if (dividerLine != null) dividerLine.setVisibility(View.VISIBLE);
        if (gridDetails != null) gridDetails.setVisibility(View.VISIBLE);

        tvTitle.setText(booking.movieTitleSnapshot);
        tvCinema.setText(booking.cinemaNameSnapshot);
        
        if (booking.showtimeStartAtSnapshot > 0) {
            Date date = new Date(booking.showtimeStartAtSnapshot);
            tvDate.setText(dateFormat.format(date));
            tvTime.setText(timeFormat.format(date));
        }

        tvRoom.setText(booking.roomNameSnapshot != null ? booking.roomNameSnapshot : "Chưa xác định");
        
        if (booking.seatCodes != null && !booking.seatCodes.isEmpty()) {
            tvSeats.setText(String.join(", ", booking.seatCodes));
        } else {
            tvSeats.setText("Chưa xác định");
        }

        tvBookingCode.setText("Booking ID: #" + booking.bookingId);
        tvTotal.setText(String.format("%,.0fđ", booking.total).replace(',', '.'));

        boolean allowQR = shouldAllowQR(booking);
        if (allowQR) {
            // Generate QR Code: bookingId | userId | showtimeId | timestamp
            String qrContent = String.format("%s|%s|%s|%d", 
                    booking.bookingId, 
                    booking.userId, 
                    booking.showtimeId, 
                    System.currentTimeMillis());
            
            new Thread(() -> {
                Bitmap qrBitmap = QRCodeUtils.generateQRCode(qrContent, 500, 500);
                if (qrBitmap != null) {
                    runOnUiThread(() -> {
                        imgQr.setImageBitmap(qrBitmap);
                        imgQr.setAlpha(1.0f);
                        if (tvQrHint != null) {
                            tvQrHint.setText("Dùng mã này để nhận vé tại quầy");
                            tvQrHint.setTextColor(0xFF757575);
                        }
                    });
                }
            }).start();
        } else {
            // QR is blocked
            imgQr.setImageResource(R.drawable.square_solid_full);
            imgQr.setAlpha(0.2f);
            if (tvQrHint != null) {
                String bookingStatus = booking.bookingStatus != null ? booking.bookingStatus.toUpperCase() : "PENDING";
                String paymentStatus = booking.paymentStatus != null ? booking.paymentStatus.toUpperCase() : "PENDING";
                long now = System.currentTimeMillis();

                if ("FAILED".equals(bookingStatus) || "CANCELLED".equals(bookingStatus) ||
                    "FAILED".equals(paymentStatus) || "CANCELLED".equals(paymentStatus)) {
                    tvQrHint.setText("Vé đã bị hủy, mã QR đã khóa");
                } else if (booking.checkInAt > 0) {
                    tvQrHint.setText("Vé đã sử dụng, mã QR đã khóa");
                } else if (booking.showtimeStartAtSnapshot > 0 && booking.showtimeStartAtSnapshot < now) {
                    tvQrHint.setText("Vé đã hết hạn, mã QR đã khóa");
                } else {
                    tvQrHint.setText("Mã QR đã bị khóa");
                }
                tvQrHint.setTextColor(0xFFC62828); // Red
            }
        }

        if (!android.text.TextUtils.isEmpty(booking.movieImageUrlSnapshot)) {
            Glide.with(this)
                    .load(booking.movieImageUrlSnapshot)
                    .placeholder(R.drawable.square_solid_full)
                    .error(R.drawable.square_solid_full)
                    .into(imgPoster);
        } else if (!android.text.TextUtils.isEmpty(booking.movieId)) {
            com.example.cinemabookingapp.MyApp app = (com.example.cinemabookingapp.MyApp) getApplicationContext();
            app.getAppContainer().getMovieRepository().getMovieById(booking.movieId, new ResultCallback<com.example.cinemabookingapp.domain.model.Movie>() {
                @Override
                public void onSuccess(com.example.cinemabookingapp.domain.model.Movie movie) {
                    if (movie != null && !android.text.TextUtils.isEmpty(movie.posterUrl)) {
                        booking.movieImageUrlSnapshot = movie.posterUrl;
                        runOnUiThread(() -> Glide.with(TicketDetailActivity.this)
                                .load(movie.posterUrl)
                                .placeholder(R.drawable.square_solid_full)
                                .error(R.drawable.square_solid_full)
                                .into(imgPoster));
                    }
                }
                @Override
                public void onError(String message) {}
            });
        } else {
            Glide.with(this)
                    .load(R.drawable.square_solid_full)
                    .into(imgPoster);
        }
    }

    private void downloadTicket() {
        if (ticketContainer == null || ticketContainer.getWidth() <= 0 || ticketContainer.getHeight() <= 0) {
            Toast.makeText(this, "Đang khởi tạo dữ liệu vé, vui lòng thử lại!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create Bitmap from View
        Bitmap bitmap = Bitmap.createBitmap(ticketContainer.getWidth(), 
                ticketContainer.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        ticketContainer.draw(canvas);

        // Save Bitmap
        String fileName = "Ticket_" + System.currentTimeMillis() + ".jpg";
        OutputStream fos;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/CinemaTickets");
                Uri imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
                fos = getContentResolver().openOutputStream(imageUri);
            } else {
                String imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
                java.io.File image = new java.io.File(imagesDir, fileName);
                fos = new java.io.FileOutputStream(image);
            }

            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            if (fos != null) fos.close();
            
            Toast.makeText(this, "Đã lưu vé vào thư viện ảnh!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi khi lưu vé: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private boolean shouldAllowQR(Booking booking) {
        String paymentStatus = booking.paymentStatus != null ? booking.paymentStatus.toUpperCase() : "PENDING";
        String bookingStatus = booking.bookingStatus != null ? booking.bookingStatus.toUpperCase() : "PENDING";
        boolean isCineShop = (booking.showtimeId == null);

        // 1. Chặn QR khi vé bị huỷ (FAILED hoặc CANCELLED)
        if ("FAILED".equals(bookingStatus) || "CANCELLED".equals(bookingStatus) ||
            "FAILED".equals(paymentStatus) || "CANCELLED".equals(paymentStatus)) {
            return false;
        }

        // 2. Chặn QR khi vé đã sử dụng
        if (booking.checkInAt > 0) {
            return false;
        }

        // 3. Chặn QR khi vé hết hạn (only for movie tickets)
        long now = System.currentTimeMillis();
        if (!isCineShop && booking.showtimeStartAtSnapshot > 0 && booking.showtimeStartAtSnapshot < now) {
            return false;
        }

        // 4. Cho phép xem QR chỉ khi: paymentStatus = SUCCESS (hoặc PAID)
        boolean isPaid = "SUCCESS".equals(paymentStatus) || "PAID".equals(paymentStatus) ||
                         "CONFIRMED".equals(bookingStatus) || "SUCCESS".equals(bookingStatus);
        if (!isPaid) {
            return false;
        }

        return true;
    }
}

package com.example.cinemabookingapp.ui.customer.transaction;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.cinemabookingapp.R;
import com.example.cinemabookingapp.core.utils.QRCodeUtils;
import com.example.cinemabookingapp.di.ServiceProvider;
import com.example.cinemabookingapp.domain.common.ResultCallback;
import com.example.cinemabookingapp.domain.model.Booking;
import com.example.cinemabookingapp.service.BookingService;

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
    private TextView tvTitle, tvCinema, tvDate, tvTime, tvRoom, tvSeats, tvBookingCode, tvTotal;
    private MaterialCardView ticketContainer;
    private MaterialButton btnDownload;
    private BookingService bookingService;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket_detail);

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
        ticketContainer = findViewById(R.id.ticket_container);
        btnDownload = findViewById(R.id.btn_download);

        btnDownload.setOnClickListener(v -> downloadTicket());
    }

    private void loadBookingDetails(String bookingId) {
        bookingService.getBookingDetails(bookingId, new ResultCallback<Booking>() {
            @Override
            public void onSuccess(Booking booking) {
                bindData(booking);
            }

            @Override
            public void onError(String message) {
                Toast.makeText(TicketDetailActivity.this, "Lỗi tải vé: " + message, Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    private void bindData(Booking booking) {
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

        // Generate QR Code: bookingId | userId | showtimeId | timestamp
        String qrContent = String.format("%s|%s|%s|%d", 
                booking.bookingId, 
                booking.userId, 
                booking.showtimeId, 
                System.currentTimeMillis());
        
        new Thread(() -> {
            Bitmap qrBitmap = QRCodeUtils.generateQRCode(qrContent, 500, 500);
            if (qrBitmap != null) {
                runOnUiThread(() -> imgQr.setImageBitmap(qrBitmap));
            }
        }).start();

        Glide.with(this)
                .load(!android.text.TextUtils.isEmpty(booking.movieImageUrlSnapshot) ? booking.movieImageUrlSnapshot : R.drawable.square_solid_full)
                .placeholder(R.drawable.square_solid_full)
                .into(imgPoster);
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
}

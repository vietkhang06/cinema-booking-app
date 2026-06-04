package com.example.cinemabookingapp.ui.admin.showtime;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.cinemabookingapp.R;
import com.example.cinemabookingapp.data.repository.CinemaRepositoryImpl;
import com.example.cinemabookingapp.data.repository.MovieRepositoryImpl;
import com.example.cinemabookingapp.data.repository.RoomRepositoryImpl;
import com.example.cinemabookingapp.data.repository.SeatRepositoryImpl;
import com.example.cinemabookingapp.data.repository.ShowtimeRepositoryImpl;
import com.example.cinemabookingapp.domain.common.ResultCallback;
import com.example.cinemabookingapp.domain.model.Cinema;
import com.example.cinemabookingapp.domain.model.Movie;
import com.example.cinemabookingapp.domain.model.Room;
import com.example.cinemabookingapp.domain.model.SeatTemplate;
import com.example.cinemabookingapp.domain.model.Showtime;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AdminShowtimeAddEditActivity extends AppCompatActivity {

    private MaterialAutoCompleteTextView actvMovie;
    private MaterialAutoCompleteTextView actvCinema;
    private MaterialAutoCompleteTextView actvRoom;
    private MaterialAutoCompleteTextView actvFormat;
    private MaterialAutoCompleteTextView actvLanguage;

    private TextInputEditText tietDate;
    private TextInputEditText tietStartTime;
    private TextInputEditText tietEndTime;
    private TextInputEditText tietBasePrice;

    private android.view.View layoutMovieDetails;
    private android.widget.ImageView imgMoviePoster;
    private TextView tvMovieDuration;
    private TextView tvTimePreview;

    private TextView tvPreviewMovie;
    private TextView tvPreviewCinemaRoom;
    private TextView tvPreviewTime;
    private TextView tvPreviewFormatLang;
    private TextView tvPreviewPrice;

    private ShowtimeRepositoryImpl showtimeRepository;
    private CinemaRepositoryImpl cinemaRepository;
    private MovieRepositoryImpl movieRepository;
    private RoomRepositoryImpl roomRepository;
    private SeatRepositoryImpl seatRepository;

    private boolean isEditMode = false;
    private String currentShowtimeId = null;
    private Showtime currentShowtime = null;

    private final List<Movie> moviesList = new ArrayList<>();
    private final List<Cinema> cinemasList = new ArrayList<>();
    private final List<Room> roomsList = new ArrayList<>();

    private String selectedMovieId = null;
    private String selectedCinemaId = null;
    private String selectedRoomId = null;

    private int selectedYear = -1;
    private int selectedMonth = -1;
    private int selectedDay = -1;
    
    private int selectedStartHour = -1;
    private int selectedStartMinute = -1;
    
    private int selectedEndHour = -1;
    private int selectedEndMinute = -1;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_showtime_add_edit);

        showtimeRepository = new ShowtimeRepositoryImpl();
        cinemaRepository = new CinemaRepositoryImpl();
        movieRepository = new MovieRepositoryImpl(new com.example.cinemabookingapp.data.remote.datasource.MovieRemoteDataSource());
        roomRepository = new RoomRepositoryImpl();
        seatRepository = new SeatRepositoryImpl();

        Intent intent = getIntent();
        if (intent.hasExtra("extra_showtime_id")) {
            isEditMode = true;
            currentShowtimeId = intent.getStringExtra("extra_showtime_id");
        }

        initViews();
        setupDateTimePickers();
        setupStaticDropdowns();
        loadMetaData();
    }

    private void initViews() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnCancel).setOnClickListener(v -> finish());
        findViewById(R.id.btnSave).setOnClickListener(v -> saveShowtime());

        actvMovie = findViewById(R.id.actvMovie);
        actvCinema = findViewById(R.id.actvCinema);
        actvRoom = findViewById(R.id.actvRoom);
        actvFormat = findViewById(R.id.actvFormat);
        actvLanguage = findViewById(R.id.actvLanguage);

        tietDate = findViewById(R.id.tietDate);
        tietStartTime = findViewById(R.id.tietStartTime);
        tietEndTime = findViewById(R.id.tietEndTime);
        tietBasePrice = findViewById(R.id.tietBasePrice);

        tvPreviewMovie = findViewById(R.id.tvPreviewMovie);
        tvPreviewCinemaRoom = findViewById(R.id.tvPreviewCinemaRoom);
        tvPreviewTime = findViewById(R.id.tvPreviewTime);
        tvPreviewFormatLang = findViewById(R.id.tvPreviewFormatLang);
        tvPreviewPrice = findViewById(R.id.tvPreviewPrice);

        layoutMovieDetails = findViewById(R.id.layoutMovieDetails);
        imgMoviePoster = findViewById(R.id.imgMoviePoster);
        tvMovieDuration = findViewById(R.id.tvMovieDuration);
        tvTimePreview = findViewById(R.id.tvTimePreview);

        tietEndTime.setEnabled(false);
        tietEndTime.setFocusable(false);
        tietEndTime.setClickable(false);

        android.widget.TextView tvTitle = findViewById(R.id.tvTitle);
        tvTitle.setText(isEditMode ? "Cập nhật suất chiếu" : "Thêm suất chiếu mới");

        actvMovie.setOnItemClickListener((parent, view, position, id) -> {
            selectedMovieId = moviesList.get(position).movieId;
            calculateAndDisplayEndTime();
            updatePreview();
        });

        actvCinema.setOnItemClickListener((parent, view, position, id) -> {
            selectedCinemaId = cinemasList.get(position).cinemaId;
            selectedRoomId = null;
            actvRoom.setText("", false);
            loadRoomsForCinema(selectedCinemaId);
            updatePreview();
        });

        actvRoom.setOnItemClickListener((parent, view, position, id) -> {
            selectedRoomId = roomsList.get(position).roomId;
            updatePreview();
        });

        actvFormat.setOnItemClickListener((parent, view, position, id) -> updatePreview());
        actvLanguage.setOnItemClickListener((parent, view, position, id) -> updatePreview());

        tietBasePrice.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(android.text.Editable s) {
                updatePreview();
            }
        });
    }

    private void setupDateTimePickers() {
        tietDate.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            if (selectedYear != -1) {
                cal.set(selectedYear, selectedMonth, selectedDay);
            }
            new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                selectedYear = year;
                selectedMonth = month;
                selectedDay = dayOfMonth;
                tietDate.setText(String.format(Locale.getDefault(), "%02d/%02d/%04d", dayOfMonth, month + 1, year));
                calculateAndDisplayEndTime();
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
        });

        tietStartTime.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            if (selectedStartHour != -1) {
                cal.set(Calendar.HOUR_OF_DAY, selectedStartHour);
                cal.set(Calendar.MINUTE, selectedStartMinute);
            }
            new TimePickerDialog(this, (view, hourOfDay, minute) -> {
                selectedStartHour = hourOfDay;
                selectedStartMinute = minute;
                tietStartTime.setText(String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute));
                calculateAndDisplayEndTime();
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show();
        });
    }

    private void setupStaticDropdowns() {
        String[] formats = {"2D", "3D", "IMAX", "4DX"};
        actvFormat.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, formats));

        String[] languages = {"Phụ đề tiếng Việt", "Thuyết minh tiếng Việt", "Lồng tiếng tiếng Việt", "Original English"};
        actvLanguage.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, languages));
    }

    private void loadMetaData() {
        movieRepository.getAllMovies(new ResultCallback<List<Movie>>() {
            @Override
            public void onSuccess(List<Movie> movies) {
                moviesList.clear();
                moviesList.addAll(movies);
                List<String> movieTitles = new ArrayList<>();
                for (Movie m : movies) {
                    movieTitles.add(m.title);
                }
                actvMovie.setAdapter(new ArrayAdapter<>(AdminShowtimeAddEditActivity.this,
                        android.R.layout.simple_list_item_1, movieTitles));

                loadCinemas();
            }

            @Override
            public void onError(String message) {
                showToast("Lỗi tải danh sách phim: " + message);
            }
        });
    }

    private void loadCinemas() {
        cinemaRepository.getAllCinemas(new ResultCallback<List<Cinema>>() {
            @Override
            public void onSuccess(List<Cinema> cinemas) {
                cinemasList.clear();
                cinemasList.addAll(cinemas);
                List<String> cinemaNames = new ArrayList<>();
                for (Cinema c : cinemas) {
                    cinemaNames.add(c.name);
                }
                actvCinema.setAdapter(new ArrayAdapter<>(AdminShowtimeAddEditActivity.this,
                        android.R.layout.simple_list_item_1, cinemaNames));

                if (isEditMode) {
                    loadCurrentShowtime();
                }
            }

            @Override
            public void onError(String message) {
                showToast("Lỗi tải danh sách rạp: " + message);
            }
        });
    }

    private void loadRoomsForCinema(String cinemaId) {
        roomRepository.getRoomsByCinemaId(cinemaId, new ResultCallback<List<Room>>() {
            @Override
            public void onSuccess(List<Room> rooms) {
                roomsList.clear();
                roomsList.addAll(rooms);
                List<String> roomNames = new ArrayList<>();
                for (Room r : rooms) {
                    roomNames.add(r.name);
                }
                actvRoom.setAdapter(new ArrayAdapter<>(AdminShowtimeAddEditActivity.this,
                        android.R.layout.simple_list_item_1, roomNames));

                if (isEditMode && currentShowtime != null && cinemaId.equals(currentShowtime.cinemaId)) {
                    for (int i = 0; i < rooms.size(); i++) {
                        if (rooms.get(i).roomId.equals(currentShowtime.roomId)) {
                            actvRoom.setText(rooms.get(i).name, false);
                            selectedRoomId = rooms.get(i).roomId;
                            break;
                        }
                    }
                }
                updatePreview();
            }

            @Override
            public void onError(String message) {
                showToast("Lỗi tải danh sách phòng: " + message);
            }
        });
    }

    private void loadCurrentShowtime() {
        showtimeRepository.getShowtimeById(currentShowtimeId, new ResultCallback<Showtime>() {
            @Override
            public void onSuccess(Showtime showtime) {
                currentShowtime = showtime;
                selectedMovieId = showtime.movieId;
                selectedCinemaId = showtime.cinemaId;
                selectedRoomId = showtime.roomId;

                // Bind movie
                for (Movie m : moviesList) {
                    if (m.movieId.equals(showtime.movieId)) {
                        actvMovie.setText(m.title, false);
                        break;
                    }
                }

                // Bind cinema
                for (Cinema c : cinemasList) {
                    if (c.cinemaId.equals(showtime.cinemaId)) {
                        actvCinema.setText(c.name, false);
                        break;
                    }
                }

                // Load rooms for the cinema and bind room
                loadRoomsForCinema(showtime.cinemaId);

                // Bind time fields
                Calendar calStart = Calendar.getInstance();
                calStart.setTimeInMillis(showtime.startAt);
                selectedYear = calStart.get(Calendar.YEAR);
                selectedMonth = calStart.get(Calendar.MONTH);
                selectedDay = calStart.get(Calendar.DAY_OF_MONTH);
                selectedStartHour = calStart.get(Calendar.HOUR_OF_DAY);
                selectedStartMinute = calStart.get(Calendar.MINUTE);

                tietDate.setText(dateFormat.format(new Date(showtime.startAt)));
                tietStartTime.setText(timeFormat.format(new Date(showtime.startAt)));

                Calendar calEnd = Calendar.getInstance();
                calEnd.setTimeInMillis(showtime.endAt);
                selectedEndHour = calEnd.get(Calendar.HOUR_OF_DAY);
                selectedEndMinute = calEnd.get(Calendar.MINUTE);
                tietEndTime.setText(timeFormat.format(new Date(showtime.endAt)));

                calculateAndDisplayEndTime();

                // Bind format, lang, price
                actvFormat.setText(showtime.format, false);
                actvLanguage.setText(showtime.language, false);
                tietBasePrice.setText(String.valueOf((int) showtime.basePrice));

                disableEditIfExistBooking();
            }

            @Override
            public void onError(String message) {
                showToast("Lỗi tải thông tin suất chiếu: " + message);
                finish();
            }
        });
    }

    private void saveShowtime() {
        if (selectedMovieId == null || selectedCinemaId == null || selectedRoomId == null) {
            showToast("Vui lòng điền đầy đủ Phim, Rạp và Phòng chiếu!");
            return;
        }

        String format = actvFormat.getText().toString();
        String language = actvLanguage.getText().toString();
        String priceStr = tietBasePrice.getText().toString();

        if (TextUtils.isEmpty(format) || TextUtils.isEmpty(language) || TextUtils.isEmpty(priceStr)) {
            showToast("Vui lòng điền đầy đủ định dạng, ngôn ngữ và giá vé!");
            return;
        }

        if (selectedYear == -1 || selectedStartHour == -1 || selectedEndHour == -1) {
            showToast("Vui lòng điền đầy đủ ngày chiếu và giờ bắt đầu/kết thúc!");
            return;
        }

        double basePrice;
        try {
            basePrice = Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            showToast("Giá vé không hợp lệ!");
            return;
        }

        // Construct timestamps
        Calendar calStart = Calendar.getInstance();
        calStart.set(selectedYear, selectedMonth, selectedDay, selectedStartHour, selectedStartMinute, 0);
        calStart.set(Calendar.MILLISECOND, 0);
        long startAt = calStart.getTimeInMillis();

        // Calculate endAt based on movie duration
        Movie selectedMovie = null;
        for (Movie m : moviesList) {
            if (m.movieId.equals(selectedMovieId)) {
                selectedMovie = m;
                break;
            }
        }

        long endAt;
        if (selectedMovie != null && selectedMovie.durationMinutes > 0) {
            endAt = startAt + (selectedMovie.durationMinutes * 60L * 1000L);
        } else {
            Calendar calEnd = Calendar.getInstance();
            calEnd.set(selectedYear, selectedMonth, selectedDay, selectedEndHour, selectedEndMinute, 0);
            calEnd.set(Calendar.MILLISECOND, 0);
            endAt = calEnd.getTimeInMillis();
        }

        if (endAt <= startAt) {
            showToast("Thời gian kết thúc phải lớn hơn thời gian bắt đầu.");
            return;
        }

        // Validate overlap rule
        validateOverlapAndSave(startAt, endAt, format, language, basePrice);
    }

    private void validateOverlapAndSave(long startAt, long endAt, String format, String language, double basePrice) {
        showtimeRepository.getAllShowtimes(new ResultCallback<List<Showtime>>() {
            @Override
            public void onSuccess(List<Showtime> showtimes) {
                showtimeRepository.getAllShowtimeSchedules(new ResultCallback<List<Showtime>>() {
                    @Override
                    public void onSuccess(List<Showtime> schedules) {
                        List<Showtime> combinedList = new ArrayList<>(showtimes);
                        if (schedules != null) {
                            for (Showtime s : schedules) {
                                if (!s.executed) {
                                    combinedList.add(s);
                                }
                            }
                        }

                        // Check if any active showtime or pending schedule in the same room overlaps
                        for (Showtime s : combinedList) {
                            if (s.roomId.equals(selectedRoomId) && !s.deleted) {
                                // Skip checking current showtime if in edit mode
                                if (isEditMode && s.showtimeId.equals(currentShowtimeId)) {
                                    continue;
                                }

                                // Overlap condition: (startAt < s.endAt) && (endAt > s.startAt)
                                if (startAt < s.endAt && endAt > s.startAt) {
                                    String msg = s.isScheduled ? 
                                            "Phòng chiếu này đã có lịch trình suất chiếu từ " : 
                                            "Phòng chiếu này đã được lên lịch suất chiếu khác từ ";
                                    new AlertDialog.Builder(AdminShowtimeAddEditActivity.this)
                                            .setTitle("Trùng lịch chiếu")
                                            .setMessage(msg +
                                                    timeFormat.format(new Date(s.startAt)) + " đến " +
                                                    timeFormat.format(new Date(s.endAt)) + " cùng ngày.")
                                            .setPositiveButton("Đã hiểu", null)
                                            .show();
                                    return;
                                }
                            }
                        }

                        // Retrieve seat templates count to determine totalSeats
                        seatRepository.getSeatTemplatesByRoomId(selectedRoomId, new ResultCallback<List<SeatTemplate>>() {
                            @Override
                            public void onSuccess(List<SeatTemplate> templates) {
                                int totalSeats = templates.size();
                                if (totalSeats == 0) {
                                    // If no templates configured, fall back to default count (e.g. 64)
                                    totalSeats = 64;
                                }

                                saveToFirestore(startAt, endAt, format, language, basePrice, totalSeats);
                            }

                            @Override
                            public void onError(String message) {
                                // Fall back to default total seats count
                                saveToFirestore(startAt, endAt, format, language, basePrice, 64);
                            }
                        });
                    }

                    @Override
                    public void onError(String message) {
                        showToast("Lỗi kiểm tra trùng lịch trình suất chiếu: " + message);
                    }
                });
            }

            @Override
            public void onError(String message) {
                showToast("Lỗi kiểm tra trùng lịch chiếu: " + message);
            }
        });
    }

    private void saveToFirestore(long startAt, long endAt, String format, String language, double basePrice, int totalSeats) {
        Showtime showtime = isEditMode ? currentShowtime : new Showtime();
        showtime.movieId = selectedMovieId;
        showtime.cinemaId = selectedCinemaId;
        showtime.roomId = selectedRoomId;
        showtime.startAt = startAt;
        showtime.endAt = endAt;
        showtime.format = format;
        showtime.language = language;
        showtime.basePrice = basePrice;
        showtime.totalSeats = totalSeats;

        if (!isEditMode) {
            showtime.status = "active";
            showtime.bookedSeatsCount = 0;
            showtime.isScheduled = false;
            
            showtimeRepository.createShowtime(showtime, new ResultCallback<Showtime>() {
                @Override
                public void onSuccess(Showtime resultShowtime) {
                    // Generate showtime seats automatically!
                    seatRepository.generateSeatsForShowtime(resultShowtime.showtimeId, resultShowtime.roomId, new ResultCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            showToast("Thêm suất chiếu và khởi tạo ghế thành công");
                            setResult(RESULT_OK);
                            finish();
                        }

                        @Override
                        public void onError(String message) {
                            showToast("Tạo suất chiếu thành công nhưng lỗi khởi tạo sơ đồ ghế: " + message);
                            setResult(RESULT_OK);
                            finish();
                        }
                    });
                }

                @Override
                public void onError(String message) {
                    showToast("Thêm suất chiếu thất bại: " + message);
                }
            });
        } else {
            showtimeRepository.updateShowtime(showtime, new ResultCallback<Showtime>() {
                @Override
                public void onSuccess(Showtime resultShowtime) {
                    showToast("Cập nhật suất chiếu thành công");
                    setResult(RESULT_OK);
                    finish();
                }

                @Override
                public void onError(String message) {
                    showToast("Cập nhật suất chiếu thất bại: " + message);
                }
            });
        }
    }

    private void calculateAndDisplayEndTime() {
        updateMovieDetailViews();

        if (selectedMovieId == null || selectedYear == -1 || selectedStartHour == -1 || selectedStartMinute == -1) {
            return;
        }

        Movie selectedMovie = null;
        for (Movie m : moviesList) {
            if (m.movieId.equals(selectedMovieId)) {
                selectedMovie = m;
                break;
            }
        }

        if (selectedMovie == null || selectedMovie.durationMinutes <= 0) {
            return;
        }

        Calendar calStart = Calendar.getInstance();
        calStart.set(selectedYear, selectedMonth, selectedDay, selectedStartHour, selectedStartMinute, 0);
        calStart.set(Calendar.MILLISECOND, 0);

        Calendar calEnd = (Calendar) calStart.clone();
        calEnd.add(Calendar.MINUTE, selectedMovie.durationMinutes);

        selectedEndHour = calEnd.get(Calendar.HOUR_OF_DAY);
        selectedEndMinute = calEnd.get(Calendar.MINUTE);

        tietEndTime.setText(String.format(Locale.getDefault(), "%02d:%02d", selectedEndHour, selectedEndMinute));
        updatePreview();
    }

    private void updateMovieDetailViews() {
        if (selectedMovieId == null) {
            layoutMovieDetails.setVisibility(android.view.View.GONE);
            return;
        }

        Movie selectedMovie = null;
        for (Movie m : moviesList) {
            if (m.movieId.equals(selectedMovieId)) {
                selectedMovie = m;
                break;
            }
        }

        if (selectedMovie == null) {
            layoutMovieDetails.setVisibility(android.view.View.GONE);
            return;
        }

        layoutMovieDetails.setVisibility(android.view.View.VISIBLE);
        tvMovieDuration.setText("Thời lượng: " + selectedMovie.durationMinutes + " phút");

        if (selectedMovie.posterUrl != null && !selectedMovie.posterUrl.isEmpty()) {
            com.bumptech.glide.Glide.with(this)
                    .load(selectedMovie.posterUrl)
                    .placeholder(R.drawable.clapperboard_solid_full)
                    .into(imgMoviePoster);
        } else {
            imgMoviePoster.setImageResource(R.drawable.clapperboard_solid_full);
        }

        updateTimePreviewText(selectedMovie.durationMinutes);
    }

    private void updateTimePreviewText(int durationMinutes) {
        if (selectedStartHour != -1 && selectedStartMinute != -1) {
            String startStr = String.format(Locale.getDefault(), "%02d:%02d", selectedStartHour, selectedStartMinute);
            
            // Calculate end hour and minute
            Calendar calStart = Calendar.getInstance();
            calStart.set(2026, 0, 1, selectedStartHour, selectedStartMinute, 0);
            calStart.add(Calendar.MINUTE, durationMinutes);
            String endStr = String.format(Locale.getDefault(), "%02d:%02d", calStart.get(Calendar.HOUR_OF_DAY), calStart.get(Calendar.MINUTE));

            tvTimePreview.setText("Bắt đầu: " + startStr + " | Kết thúc: " + endStr);
        } else {
            tvTimePreview.setText("Bắt đầu: --:-- | Kết thúc: --:--");
        }
    }

    private void updatePreview() {
        if (tvPreviewMovie == null) return;

        String movie = actvMovie.getText().toString();
        tvPreviewMovie.setText(movie.isEmpty() ? "Chưa chọn phim" : movie);

        String cinema = actvCinema.getText().toString();
        String room = actvRoom.getText().toString();
        String cinemaRoom = "";
        if (!cinema.isEmpty()) cinemaRoom += cinema;
        if (!room.isEmpty()) {
            if (!cinemaRoom.isEmpty()) cinemaRoom += " - ";
            cinemaRoom += room;
        }
        tvPreviewCinemaRoom.setText(cinemaRoom.isEmpty() ? "Chưa chọn rạp & phòng" : cinemaRoom);

        String date = tietDate.getText().toString();
        String start = tietStartTime.getText().toString();
        String end = tietEndTime.getText().toString();
        String timeStr = "";
        if (!date.isEmpty()) timeStr += date;
        if (!start.isEmpty()) {
            if (!timeStr.isEmpty()) timeStr += " | ";
            timeStr += start;
        }
        if (!end.isEmpty()) {
            timeStr += " ~ " + end;
        }
        tvPreviewTime.setText(timeStr.isEmpty() ? "Chưa chọn thời gian" : timeStr);

        String format = actvFormat.getText().toString();
        String lang = actvLanguage.getText().toString();
        String formatLang = "";
        if (!format.isEmpty()) formatLang += format;
        if (!lang.isEmpty()) {
            if (!formatLang.isEmpty()) formatLang += " | ";
            formatLang += lang;
        }
        tvPreviewFormatLang.setText(formatLang.isEmpty() ? "2D | Phụ đề" : formatLang);

        String price = tietBasePrice.getText().toString();
        if (!price.isEmpty()) {
            try {
                double val = Double.parseDouble(price);
                java.text.NumberFormat cf = java.text.NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
                tvPreviewPrice.setText(cf.format(val));
            } catch (Exception e) {
                tvPreviewPrice.setText(price + " đ");
            }
        } else {
            tvPreviewPrice.setText("0 đ");
        }
    }

    void disableEditIfExistBooking(){
        if(currentShowtime.bookedSeatsCount > 0){
            disableFullEdit();
        }
    }

    void disableFullEdit(){
        findViewById(R.id.p_actvMovie).setEnabled(false);
        findViewById(R.id.p_actvCinema).setEnabled(false);
        findViewById(R.id.p_actvRoom).setEnabled(false);
        tietDate.setEnabled(false);
        tietStartTime.setEnabled(false);
        tietEndTime.setEnabled(false);

        showToast("Suất chiếu này đã có khách đặt vé.\n Không thể thay đổi thời gian và phòng chiếu.");
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}

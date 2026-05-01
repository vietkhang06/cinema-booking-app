package com.example.cinemabookingapp.ui.admin.cinema;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;

import com.example.cinemabookingapp.R;
import com.example.cinemabookingapp.core.base.BaseActivity;
import com.example.cinemabookingapp.data.repository.CinemaRepositoryImpl;
import com.example.cinemabookingapp.domain.common.ResultCallback;
import com.example.cinemabookingapp.domain.model.Cinema;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class AdminCinemaFormActivity extends BaseActivity {

    public static final String EXTRA_CINEMA_ID = "extra_cinema_id";

    private TextInputLayout tilName, tilAddress, tilCity, tilDistrict, tilPhone, tilLat, tilLng, tilStatus;
    private TextInputEditText edtName, edtAddress, edtCity, edtDistrict, edtPhone, edtLat, edtLng;
    private MaterialAutoCompleteTextView actvStatus;
    private MaterialButton btnSave;

    private CinemaRepositoryImpl cinemaRepository;
    private Cinema currentCinema;
    private boolean isEditMode = false;
    private String cinemaId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_cinema_form);

        cinemaRepository = new CinemaRepositoryImpl();
        cinemaId = getIntent().getStringExtra(EXTRA_CINEMA_ID);
        isEditMode = !TextUtils.isEmpty(cinemaId);

        initViews();
        setupStatusDropdown();
        bindActions();

        if (isEditMode) {
            btnSave.setText("Cập nhật rạp");
            loadCinema();
        } else {
            btnSave.setText("Thêm rạp");
        }
    }

    private void initViews() {
        tilName = findViewById(R.id.tilName);
        tilAddress = findViewById(R.id.tilAddress);
        tilCity = findViewById(R.id.tilCity);
        tilDistrict = findViewById(R.id.tilDistrict);
        tilPhone = findViewById(R.id.tilPhone);
        tilLat = findViewById(R.id.tilLat);
        tilLng = findViewById(R.id.tilLng);
        tilStatus = findViewById(R.id.tilStatus);

        edtName = findViewById(R.id.edtName);
        edtAddress = findViewById(R.id.edtAddress);
        edtCity = findViewById(R.id.edtCity);
        edtDistrict = findViewById(R.id.edtDistrict);
        edtPhone = findViewById(R.id.edtPhone);
        edtLat = findViewById(R.id.edtLat);
        edtLng = findViewById(R.id.edtLng);

        actvStatus = findViewById(R.id.actvStatus);
        btnSave = findViewById(R.id.btnSave);
    }

    private void setupStatusDropdown() {
        String[] statuses = {"active", "inactive"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                statuses
        );
        actvStatus.setAdapter(adapter);
    }

    private void bindActions() {
        btnSave.setOnClickListener(v -> saveCinema());
    }

    private void loadCinema() {
        cinemaRepository.getCinemaById(cinemaId, new ResultCallback<Cinema>() {
            @Override
            public void onSuccess(Cinema data) {
                if (data == null) {
                    showToast("Không tìm thấy rạp");
                    finish();
                    return;
                }
                currentCinema = data;
                bindCinema(data);
            }

            @Override
            public void onError(String message) {
                showToast(message);
                finish();
            }
        });
    }

    private void bindCinema(Cinema cinema) {
        edtName.setText(cinema.name);
        edtAddress.setText(cinema.address);
        edtCity.setText(cinema.city);
        edtDistrict.setText(cinema.district);
        edtPhone.setText(cinema.phone);
        edtLat.setText(String.valueOf(cinema.latitude));
        edtLng.setText(String.valueOf(cinema.longitude));
        actvStatus.setText(safe(cinema.status), false);
    }

    private void saveCinema() {
        clearErrors();

        String name = getText(edtName);
        String address = getText(edtAddress);
        String city = getText(edtCity);
        String district = getText(edtDistrict);
        String phone = getText(edtPhone);
        String latText = getText(edtLat);
        String lngText = getText(edtLng);
        String status = getText(actvStatus);

        if (TextUtils.isEmpty(name)) {
            tilName.setError("Nhập tên rạp");
            return;
        }

        if (TextUtils.isEmpty(address)) {
            tilAddress.setError("Nhập địa chỉ");
            return;
        }

        if (TextUtils.isEmpty(city)) {
            tilCity.setError("Nhập tỉnh/thành");
            return;
        }

        if (TextUtils.isEmpty(district)) {
            tilDistrict.setError("Nhập quận/huyện");
            return;
        }

        if (TextUtils.isEmpty(phone)) {
            tilPhone.setError("Nhập số điện thoại");
            return;
        }

        double latitude = 0;
        double longitude = 0;

        if (!TextUtils.isEmpty(latText)) {
            try {
                latitude = Double.parseDouble(latText);
            } catch (NumberFormatException e) {
                tilLat.setError("Vĩ độ không hợp lệ");
                return;
            }
        }

        if (!TextUtils.isEmpty(lngText)) {
            try {
                longitude = Double.parseDouble(lngText);
            } catch (NumberFormatException e) {
                tilLng.setError("Kinh độ không hợp lệ");
                return;
            }
        }

        Cinema cinema = (isEditMode && currentCinema != null) ? currentCinema : new Cinema();

        if (isEditMode) {
            cinema.cinemaId = cinemaId;
        }

        cinema.name = name;
        cinema.address = address;
        cinema.city = city;
        cinema.district = district;
        cinema.phone = phone;
        cinema.latitude = latitude;
        cinema.longitude = longitude;
        cinema.status = TextUtils.isEmpty(status) ? "active" : status;

        if (!isEditMode || currentCinema == null) {
            cinema.deleted = false;
            cinema.createdAt = System.currentTimeMillis();
        } else {
            cinema.deleted = currentCinema.deleted;
            cinema.createdAt = currentCinema.createdAt;
        }

        cinema.updatedAt = System.currentTimeMillis();

        btnSave.setEnabled(false);

        if (isEditMode) {
            cinemaRepository.updateCinema(cinema, new ResultCallback<Cinema>() {
                @Override
                public void onSuccess(Cinema data) {
                    showToast("Đã cập nhật rạp");
                    finish();
                }

                @Override
                public void onError(String message) {
                    btnSave.setEnabled(true);
                    showToast(message);
                }
            });
        } else {
            cinemaRepository.createCinema(cinema, new ResultCallback<Cinema>() {
                @Override
                public void onSuccess(Cinema data) {
                    showToast("Đã thêm rạp");
                    finish();
                }

                @Override
                public void onError(String message) {
                    btnSave.setEnabled(true);
                    showToast(message);
                }
            });
        }
    }

    private void clearErrors() {
        tilName.setError(null);
        tilAddress.setError(null);
        tilCity.setError(null);
        tilDistrict.setError(null);
        tilPhone.setError(null);
        tilLat.setError(null);
        tilLng.setError(null);
        tilStatus.setError(null);
    }

    private String getText(TextInputEditText edt) {
        return edt.getText() == null ? "" : edt.getText().toString().trim();
    }

    private String getText(MaterialAutoCompleteTextView edt) {
        return edt.getText() == null ? "" : edt.getText().toString().trim();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
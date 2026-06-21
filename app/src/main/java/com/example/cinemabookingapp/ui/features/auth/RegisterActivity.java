package com.example.cinemabookingapp.ui.features.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.cinemabookingapp.R;
import com.example.cinemabookingapp.config.auth.FacebookAuthProviderConfig;
import com.example.cinemabookingapp.core.base.BaseActivity;
import com.example.cinemabookingapp.core.navigation.AppNavigator;
import com.example.cinemabookingapp.di.ServiceProvider;
import com.example.cinemabookingapp.domain.common.AuthCallback;
import com.example.cinemabookingapp.domain.model.User;
import com.example.cinemabookingapp.service.AuthenticationService;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointBackward;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.util.Calendar;
import java.util.TimeZone;
import java.util.Locale;
import java.text.SimpleDateFormat;

// ✅ Google Sign-In
import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

public class RegisterActivity extends BaseActivity {

    private TextInputLayout tilEmail, tilPassword, tilConfirmPassword, tilPhone, tilName, tilBirthDate;
    private TextInputEditText edtEmail, edtPassword, edtConfirmPassword, edtPhone, edtName, edtBirthDate;

    private MaterialButton btnRegister;
    private MaterialCardView btnBack;
    private TextView tvLogin;

    private MaterialCardView btnFacebook, btnGoogle;

    private android.widget.RadioGroup rgGender;
    private android.widget.RadioButton rbMale, rbFemale, rbOther;

    private AuthenticationService authService;

    // Facebook
    private CallbackManager callbackManager;

    // Google
    private GoogleSignInClient googleSignInClient;
    private static final int RC_SIGN_IN = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        authService = ServiceProvider.getInstance().getAuthenticationService();

        initViews();
        initGoogle();
        initFacebook();
        bindActions();
    }

    private void initViews() {
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);
        tilPhone = findViewById(R.id.tilPhone);
        tilName = findViewById(R.id.tilName);
        tilBirthDate = findViewById(R.id.tilBirthDate);

        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        edtPhone = findViewById(R.id.edtPhone);
        edtName = findViewById(R.id.edtName);
        edtBirthDate = findViewById(R.id.edtBirthDate);

        btnRegister = findViewById(R.id.btnRegister);
        btnBack = findViewById(R.id.btnBack);
        tvLogin = findViewById(R.id.tvLogin);

        btnGoogle = findViewById(R.id.btnGoogle);
        btnFacebook = findViewById(R.id.btnFacebook);

        rgGender = findViewById(R.id.rgGender);
        rbMale = findViewById(R.id.rbMale);
        rbFemale = findViewById(R.id.rbFemale);
        rbOther = findViewById(R.id.rbOther);
    }

    private void initGoogle() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void initFacebook() {
        callbackManager = CallbackManager.Factory.create();

        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult result) {
                authService.handleFacebookAccessToken(result.getAccessToken(), new AuthCallback() {
                    @Override
                    public void onSuccess(User data) {
                        AppNavigator.goToCustomerHome(RegisterActivity.this);
                    }

                    @Override
                    public void onError(String message) {
                        showToast("Facebook login failed");
                    }
                });
            }

            @Override
            public void onCancel() {}

            @Override
            public void onError(FacebookException error) {
                Log.e("FacebookAuth", error.getMessage());
            }
        });
    }

    private void bindActions() {
        btnBack.setOnClickListener(v -> AppNavigator.goToLogin(this));
        tvLogin.setOnClickListener(v -> AppNavigator.goToLogin(this));

        btnRegister.setOnClickListener(v -> attemptRegister());

        btnFacebook.setOnClickListener(v -> signInWithFacebook());

        btnGoogle.setOnClickListener(v -> signInWithGoogle());

        edtBirthDate.setOnClickListener(v -> showDatePicker());
    }

    private void attemptRegister() {
        clearErrors();

        String name = getText(edtName);
        String email = getText(edtEmail);
        String phone = getText(edtPhone);
        String birthDate = getText(edtBirthDate);
        String password = getText(edtPassword);
        String confirmPassword = getText(edtConfirmPassword);

        String gender = null;
        if (rbMale.isChecked()) {
            gender = "Nam";
        } else if (rbFemale.isChecked()) {
            gender = "Nữ";
        } else if (rbOther.isChecked()) {
            gender = "Chưa Xác Định";
        }

        if (TextUtils.isEmpty(name)) {
            tilName.setError("Vui lòng nhập họ và tên");
            return;
        }

        if (TextUtils.isEmpty(email)) {
            tilEmail.setError("Vui lòng nhập email");
            return;
        }

        boolean isValidEmail = Patterns.EMAIL_ADDRESS.matcher(email).matches() && 
                (email.endsWith("@gmail.com") || email.endsWith("@gm.uit.edu.vn") || email.endsWith("@uit.edu.vn"));
        if (!isValidEmail) {
            tilEmail.setError("Email không hợp lệ (chỉ chấp nhận Gmail / email UIT)");
            return;
        }

        if (TextUtils.isEmpty(phone)) {
            tilPhone.setError("Nhập số điện thoại");
            return;
        }

        if (!phone.matches("^0\\d{9}$")) {
            tilPhone.setError("Số điện thoại không hợp lệ (gồm 10 số và bắt đầu bằng 0)");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            tilPassword.setError("Vui lòng nhập mật khẩu");
            return;
        }

        if (password.length() < 6) {
            tilPassword.setError("Mật khẩu >= 6 ký tự");
            return;
        }

        if (!password.equals(confirmPassword)) {
            tilConfirmPassword.setError("Mật khẩu không khớp");
            return;
        }

        btnRegister.setEnabled(false);

        authService.signUpWithEmailAndPassword(email, password, phone, name, gender, birthDate, new AuthCallback() {
            @Override
            public void onSuccess(User data) {
                showToast("Đăng ký thành công");
                AppNavigator.goToCustomerHome(RegisterActivity.this);
            }

            @Override
            public void onError(String message) {
                btnRegister.setEnabled(true);
                showToast(message);
            }
        });
    }

    private void showDatePicker() {
        // 1. Tạo ràng buộc: Chỉ cho chọn ngày cách đây tối đa 15 năm về trước
        Calendar constraintsCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        constraintsCalendar.add(Calendar.YEAR, -15);
        long maxDateInMillis = constraintsCalendar.getTimeInMillis();

        CalendarConstraints constraints = new CalendarConstraints.Builder()
                .setValidator(DateValidatorPointBackward.before(maxDateInMillis))
                .build();

        // 2. Khởi tạo MaterialDatePicker và cấu hình chế độ nhập phím trực tiếp
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Chọn ngày sinh")
                .setCalendarConstraints(constraints)
                .setSelection(maxDateInMillis)
                .setInputMode(MaterialDatePicker.INPUT_MODE_TEXT)
                .build();

        // 3. Lắng nghe sự kiện click nút OK
        datePicker.addOnPositiveButtonClickListener(selection -> {
            SimpleDateFormat sdf = new SimpleDateFormat("d/M/yyyy", Locale.US);
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            String formattedDate = sdf.format(new java.util.Date(selection));
            edtBirthDate.setText(formattedDate);
        });

        datePicker.show(getSupportFragmentManager(), "DATE_PICKER");
    }

    private void signInWithFacebook() {
        FacebookAuthProviderConfig config = new FacebookAuthProviderConfig();

        LoginManager.getInstance().logInWithReadPermissions(
                this,
                config.getFacebookReadPermissions()
        );
    }

    private void signInWithGoogle() {
        Intent intent = googleSignInClient.getSignInIntent();
        startActivityForResult(intent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        callbackManager.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);

                authService.signInWithGoogle(account.getIdToken(), new AuthCallback() {
                    @Override
                    public void onSuccess(User user) {
                        AppNavigator.goToCustomerHome(RegisterActivity.this);
                    }

                    @Override
                    public void onError(String message) {
                        showToast(message);
                    }
                });

            } catch (ApiException e) {
                Log.e("GoogleAuth", "Login failed", e);
            }
        }
    }

    private void clearErrors() {
        tilName.setError(null);
        tilEmail.setError(null);
        tilPhone.setError(null);
        tilBirthDate.setError(null);
        tilPassword.setError(null);
        tilConfirmPassword.setError(null);
    }

    @NonNull
    private String getText(TextInputEditText edt) {
        return edt.getText() == null ? "" : edt.getText().toString().trim();
    }
}
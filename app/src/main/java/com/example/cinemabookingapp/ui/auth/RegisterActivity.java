package com.example.cinemabookingapp.ui.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.cinemabookingapp.R;
import com.example.cinemabookingapp.core.base.BaseActivity;
import com.example.cinemabookingapp.core.navigation.AppNavigator;
import com.example.cinemabookingapp.data.remote.firebase.FirebaseProvider;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends BaseActivity {

    private TextInputLayout tilEmail;
    private TextInputLayout tilPassword;
    private TextInputLayout tilConfirmPassword;
    private TextInputLayout tilPhone;

    private TextInputEditText edtEmail;
    private TextInputEditText edtPassword;
    private TextInputEditText edtConfirmPassword;
    private TextInputEditText edtPhone;

    private MaterialButton btnRegister;
    private TextView tvBack;

    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseProvider.provideFirestore();

        initViews();
        bindActions();
    }

    private void initViews() {
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);
        tilPhone = findViewById(R.id.tilPhone);

        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        edtPhone = findViewById(R.id.edtPhone);

        btnRegister = findViewById(R.id.btnRegister);
        tvBack = findViewById(R.id.tvBack);
    }

    private void bindActions() {
        tvBack.setOnClickListener(v -> AppNavigator.goToLogin(this));
        btnRegister.setOnClickListener(v -> attemptRegister());
    }

    private void attemptRegister() {
        clearErrors();

        String email = getText(edtEmail);
        String password = getText(edtPassword);
        String confirmPassword = getText(edtConfirmPassword);
        String phone = getText(edtPhone);

        if (TextUtils.isEmpty(email)) {
            tilEmail.setError("Vui lòng nhập email");
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Email không hợp lệ");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            tilPassword.setError("Vui lòng nhập mật khẩu");
            return;
        }

        if (password.length() < 6) {
            tilPassword.setError("Mật khẩu phải có ít nhất 6 ký tự");
            return;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            tilConfirmPassword.setError("Vui lòng xác nhận mật khẩu");
            return;
        }

        if (!password.equals(confirmPassword)) {
            tilConfirmPassword.setError("Mật khẩu xác nhận không khớp");
            return;
        }

        if (TextUtils.isEmpty(phone)) {
            tilPhone.setError("Vui lòng nhập số điện thoại");
            return;
        }

        if (phone.length() < 9 || phone.length() > 11) {
            tilPhone.setError("Số điện thoại không hợp lệ");
            return;
        }

        btnRegister.setEnabled(false);

        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {

                    FirebaseUser user = authResult.getUser();
                    if (user == null) {
                        btnRegister.setEnabled(true);
                        showToast("User null");
                        return;
                    }

                    saveUserToFirestore(user.getUid(), email, phone);
                })
                .addOnFailureListener(e -> {
                    btnRegister.setEnabled(true);
                    showToast("Auth lỗi: " + e.getMessage());
                });
    }

    private void saveUserToFirestore(String uid, String email, String phone) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("uid", uid);
        userData.put("name", "");
        userData.put("email", email);
        userData.put("phone", phone);
        userData.put("avatarUrl", "");
        userData.put("role", "customer");
        userData.put("status", "active");
        userData.put("memberLevel", "basic");
        userData.put("points", 0);
        userData.put("fcmToken", "");
        userData.put("createdAt", System.currentTimeMillis());
        userData.put("updatedAt", System.currentTimeMillis());
        userData.put("deleted", false);

        firestore.collection("users")
                .document(uid)
                .set(userData)
                .addOnSuccessListener(unused -> {
                    sessionManager.saveLoginState(true, "customer", uid);
                    sessionManager.saveRememberedEmail(email);
                    btnRegister.setEnabled(true);
                    showToast("Đăng ký thành công");
                    AppNavigator.goToCustomerHome(this);
                })
                .addOnFailureListener(e -> {
                    btnRegister.setEnabled(true);
                    showToast(e.getMessage() != null ? e.getMessage() : "Không lưu được hồ sơ");
                });
    }

    private void clearErrors() {
        tilEmail.setError(null);
        tilPassword.setError(null);
        tilConfirmPassword.setError(null);
        tilPhone.setError(null);
    }

    @NonNull
    private String getText(TextInputEditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }
}
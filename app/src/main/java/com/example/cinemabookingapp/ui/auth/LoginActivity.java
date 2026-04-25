package com.example.cinemabookingapp.ui.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;

import androidx.annotation.NonNull;

import com.example.cinemabookingapp.R;
import com.example.cinemabookingapp.core.base.BaseActivity;
import com.example.cinemabookingapp.core.navigation.AppNavigator;
import com.example.cinemabookingapp.core.session.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends BaseActivity {

    private TextInputLayout tilEmail, tilPassword;
    private TextInputEditText edtEmail, edtPassword;
    private MaterialButton btnLogin;
    private MaterialCheckBox cbRemember;

    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        sessionManager = new SessionManager(this);

        initViews();
        bindActions();
        loadRememberedEmail();
    }

    private void initViews() {
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);

        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);

        btnLogin = findViewById(R.id.btnLogin);
        cbRemember = findViewById(R.id.cbRemember);
    }

    private void bindActions() {
        btnLogin.setOnClickListener(v -> attemptLogin());

        findViewById(R.id.tvRegister)
                .setOnClickListener(v -> AppNavigator.goToRegister(this));

        findViewById(R.id.tvForgotPassword)
                .setOnClickListener(v -> AppNavigator.goToForgotPassword(this));
    }

    private void loadRememberedEmail() {
        String saved = sessionManager.getRememberedEmail();
        if (!saved.isEmpty()) {
            edtEmail.setText(saved);
            cbRemember.setChecked(true);
        }
    }

    private void attemptLogin() {
        clearErrors();

        String email = getText(edtEmail);
        String password = getText(edtPassword);

        if (TextUtils.isEmpty(email)) {
            tilEmail.setError("Nhập email");
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Email không hợp lệ");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            tilPassword.setError("Nhập mật khẩu");
            return;
        }

        btnLogin.setEnabled(false);

        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {

                    String uid = authResult.getUser().getUid();

                    firestore.collection("users")
                            .document(uid)
                            .get()
                            .addOnSuccessListener(doc -> {

                                if (!doc.exists()) {
                                    showToast("Không tìm thấy user");
                                    btnLogin.setEnabled(true);
                                    return;
                                }

                                String role = doc.getString("role");
                                if (role == null) role = "customer";

                                sessionManager.saveLoginState(true, role, uid);

                                if (cbRemember.isChecked()) {
                                    sessionManager.saveRememberedEmail(email);
                                }

                                AppNavigator.goToHomeByRole(this, role);
                            })
                            .addOnFailureListener(e -> {
                                btnLogin.setEnabled(true);
                                showToast("Lỗi lấy dữ liệu");
                            });

                })
                .addOnFailureListener(e -> {
                    btnLogin.setEnabled(true);
                    showToast("Sai email hoặc mật khẩu");
                });
    }

    private void clearErrors() {
        tilEmail.setError(null);
        tilPassword.setError(null);
    }

    @NonNull
    private String getText(TextInputEditText edt) {
        return edt.getText() == null ? "" : edt.getText().toString().trim();
    }
}
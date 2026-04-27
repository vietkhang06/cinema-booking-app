package com.example.cinemabookingapp.ui.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;

import androidx.annotation.NonNull;

import com.example.cinemabookingapp.R;
import com.example.cinemabookingapp.core.base.BaseActivity;
import com.example.cinemabookingapp.core.navigation.AppNavigator;
import com.example.cinemabookingapp.core.session.SessionManager;
import com.example.cinemabookingapp.di.ServiceProvider;
import com.example.cinemabookingapp.domain.common.AuthCallback;
import com.example.cinemabookingapp.domain.common.ResultCallback;
import com.example.cinemabookingapp.domain.model.User;
import com.example.cinemabookingapp.service.AuthenticationService;
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
    private AuthenticationService authService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        authService = ServiceProvider.getInstance().getAuthenticationService();

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

        authService.signInWithEmailAndPassword(email, password, cbRemember.isChecked(),
                new AuthCallback() {
                    @Override
                    public void onSuccess(User user) {
                        AppNavigator.goToHomeByRole(LoginActivity.this, user.role);
                    }

                    @Override
                    public void onError(String message) {
                        btnLogin.setEnabled(true);
                        showToast(message);
                    }
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
package com.example.cinemabookingapp.ui.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.credentials.CredentialManager;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;

import com.example.cinemabookingapp.R;
import com.example.cinemabookingapp.config.auth.GoogleAuthProviderConfig;
import com.example.cinemabookingapp.core.base.BaseActivity;
import com.example.cinemabookingapp.core.navigation.AppNavigator;
import com.example.cinemabookingapp.di.ServiceProvider;
import com.example.cinemabookingapp.domain.common.AuthCallback;
import com.example.cinemabookingapp.domain.common.ResultCallback;
import com.example.cinemabookingapp.domain.model.User;
import com.example.cinemabookingapp.service.AuthenticationService;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

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

    private MaterialCardView btnFacebook, btnGoogle, btnApple;

    private AuthenticationService authService;

    // Facebook
    CallbackManager callbackManager;

    // Google
    CredentialManager credentialManager;
    Executor executor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        authService = ServiceProvider.getInstance().getAuthenticationService();
        credentialManager = CredentialManager.create(this);
        executor = Executors.newSingleThreadExecutor();

        callbackManager = CallbackManager.Factory.create();
        facebookRegisterCallback();

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

        btnGoogle = findViewById(R.id.btnGoogle);
        btnFacebook = findViewById(R.id.btnFacebook);
    }

    private void bindActions() {
        tvBack.setOnClickListener(v -> AppNavigator.goToLogin(this));
        btnRegister.setOnClickListener(v -> attemptRegister());

        btnFacebook.setOnClickListener(v ->
            signInWithFacebook()
        );
        btnGoogle.setOnClickListener(v -> {
            signInWithGoogle();
        });
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

        authService.signUpWithEmailAndPassword(email, password, phone, new AuthCallback() {
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

    private void facebookRegisterCallback(){
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d("FacebookAuth", "facebook:onSuccess:" + loginResult);
                authService.handleFacebookAccessToken(loginResult.getAccessToken(), new AuthCallback() {
                    @Override
                    public void onSuccess(User data) {

                    }

                    @Override
                    public void onError(String message) {

                    }
                });
            }

            @Override
            public void onCancel() {
                Log.d("FacebookAuth", "facebook:onCancel");
            }

            @Override
            public void onError(FacebookException error) {
                Log.d("FacebookAuth", "facebook:onError", error);
            }
        });
    }

    private void signInWithFacebook(){
        LoginManager.getInstance().logInWithReadPermissions(
            this, Arrays.asList("email", "public_profile")
        );
    }

    private void signInWithGoogle(){
        GoogleAuthProviderConfig google = new GoogleAuthProviderConfig();
        // Create the Credential Manager request
        GetCredentialRequest request = google.getCredentialRequest(getString(R.string.default_web_client_id));

        credentialManager.getCredentialAsync(
                this,
                request,
                null,
                executor,
                new androidx.credentials.CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                    @Override
                    public void onResult(GetCredentialResponse result) {
                        Log.i("GoogleAuth", result.toString());
                        authService.signInWithGoogle(result.getCredential(), new AuthCallback() {
                            @Override
                            public void onSuccess(User user) {
                                AppNavigator.goToCustomerHome(RegisterActivity.this);
                            }

                            @Override
                            public void onError(String message) {

                            }
                        });
                    }

                    @Override
                    public void onError(GetCredentialException e) {
                        Log.e("GET CREDENTIAL", e.toString());
                    }
                }
        );
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
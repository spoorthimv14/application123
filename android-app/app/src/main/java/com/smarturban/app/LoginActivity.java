package com.smarturban.app;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.smarturban.app.api.RetrofitClient;
import com.smarturban.app.model.ApiResponse;
import com.smarturban.app.model.AuthResponse;
import com.smarturban.app.model.LoginRequest;
import com.smarturban.app.storage.TokenManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout tilEmail, tilPassword;
    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin;
    private ProgressBar progressBarLogin;
    private TextView tvRegisterLink;
    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        tokenManager = new TokenManager(this);

        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        progressBarLogin = findViewById(R.id.progressBarLogin);
        tvRegisterLink = findViewById(R.id.tvRegisterLink);

        String prefilledEmail = getIntent().getStringExtra("email");
        if (prefilledEmail != null) {
            etEmail.setText(prefilledEmail);
        }

        btnLogin.setOnClickListener(v -> performLogin());

        tvRegisterLink.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            finish();
        });
    }

    private void performLogin() {
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

        tilEmail.setError(null);
        tilPassword.setError(null);

        if (TextUtils.isEmpty(email)) {
            tilEmail.setError("Email is required");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            tilPassword.setError("Password is required");
            return;
        }

        showLoading(true);

        LoginRequest loginRequest = new LoginRequest(email, password);
        RetrofitClient.getApiService(this).login(loginRequest).enqueue(new Callback<ApiResponse<AuthResponse>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<AuthResponse>> call, @NonNull Response<ApiResponse<AuthResponse>> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    AuthResponse authData = response.body().getData();
                    if (authData != null) {
                        tokenManager.saveSession(authData);
                        Toast.makeText(LoginActivity.this, "Welcome back, " + authData.getFullName(), Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
                        finish();
                    }
                } else {
                    String message = "Invalid email or password.";
                    if (response.body() != null && response.body().getMessage() != null) {
                        message = response.body().getMessage();
                    }
                    Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<AuthResponse>> call, @NonNull Throwable t) {
                showLoading(false);
                Toast.makeText(LoginActivity.this, "Unable to connect to server. Please check your internet connection.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            progressBarLogin.setVisibility(View.VISIBLE);
            btnLogin.setEnabled(false);
        } else {
            progressBarLogin.setVisibility(View.GONE);
            btnLogin.setEnabled(true);
        }
    }
}

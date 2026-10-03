package com.smarturban.app;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.smarturban.app.api.RetrofitClient;
import com.smarturban.app.model.ApiResponse;
import com.smarturban.app.model.ResetUserPasswordRequest;
import com.smarturban.app.model.UserResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChangePasswordActivity extends AppCompatActivity {

    private ImageButton btnChangePasswordBack;
    private TextView tvChangePasswordUserInfo;
    private EditText etNewPassword, etConfirmPassword;
    private ProgressBar progressBarChangePassword;
    private Button btnChangePasswordSubmit;

    private Long userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        userId = getIntent().getLongExtra("user_id", -1);
        if (userId == -1) {
            Toast.makeText(this, "Invalid user ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();

        btnChangePasswordBack.setOnClickListener(v -> finish());

        String fullName = getIntent().getStringExtra("full_name");
        String email = getIntent().getStringExtra("email");
        if (fullName != null || email != null) {
            tvChangePasswordUserInfo.setText("Resetting password for: " + (fullName != null ? fullName : "") + " (" + (email != null ? email : "") + ")");
        }

        btnChangePasswordSubmit.setOnClickListener(v -> submitPasswordReset());
    }

    private void initViews() {
        btnChangePasswordBack = findViewById(R.id.btnChangePasswordBack);
        tvChangePasswordUserInfo = findViewById(R.id.tvChangePasswordUserInfo);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        progressBarChangePassword = findViewById(R.id.progressBarChangePassword);
        btnChangePasswordSubmit = findViewById(R.id.btnChangePasswordSubmit);
    }

    private void submitPasswordReset() {
        String newPass = etNewPassword.getText().toString().trim();
        String confirmPass = etConfirmPassword.getText().toString().trim();

        if (newPass.isEmpty() || newPass.length() < 6) {
            etNewPassword.setError("Password must be at least 6 characters");
            etNewPassword.requestFocus();
            return;
        }

        if (!newPass.equals(confirmPass)) {
            etConfirmPassword.setError("Passwords do not match");
            etConfirmPassword.requestFocus();
            return;
        }

        ResetUserPasswordRequest req = new ResetUserPasswordRequest(newPass);

        progressBarChangePassword.setVisibility(View.VISIBLE);
        btnChangePasswordSubmit.setEnabled(false);

        RetrofitClient.getInstance(this).getApi().resetUserPassword(userId, req)
                .enqueue(new Callback<ApiResponse<UserResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<UserResponse>> call, Response<ApiResponse<UserResponse>> response) {
                        progressBarChangePassword.setVisibility(View.GONE);
                        btnChangePasswordSubmit.setEnabled(true);

                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            Toast.makeText(ChangePasswordActivity.this, "Password reset successfully!", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            String message = "Failed to reset password";
                            if (response.body() != null && response.body().getMessage() != null) {
                                message = response.body().getMessage();
                            }
                            Toast.makeText(ChangePasswordActivity.this, message, Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<UserResponse>> call, Throwable t) {
                        progressBarChangePassword.setVisibility(View.GONE);
                        btnChangePasswordSubmit.setEnabled(true);
                        Toast.makeText(ChangePasswordActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

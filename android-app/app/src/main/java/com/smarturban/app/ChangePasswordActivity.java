package com.smarturban.app;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.smarturban.app.api.RetrofitClient;
import com.smarturban.app.model.AdminChangePasswordRequest;
import com.smarturban.app.model.ApiResponse;
import com.smarturban.app.model.User;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChangePasswordActivity extends AppCompatActivity {

    private ImageButton btnBackChangePass;
    private TextView tvChangePassUserHeader;
    private EditText etNewPassword, etConfirmPassword;
    private ProgressBar progressBarChangePass;
    private MaterialButton btnSubmitChangePass;

    private Long userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        userId = getIntent().getLongExtra("user_id", -1);
        if (userId == -1) {
            Toast.makeText(this, "Invalid User ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();

        btnBackChangePass.setOnClickListener(v -> finish());
        btnSubmitChangePass.setOnClickListener(v -> changePassword());

        fetchUserInfo();
    }

    private void initViews() {
        btnBackChangePass = findViewById(R.id.btnBackChangePass);
        tvChangePassUserHeader = findViewById(R.id.tvChangePassUserHeader);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        progressBarChangePass = findViewById(R.id.progressBarChangePass);
        btnSubmitChangePass = findViewById(R.id.btnSubmitChangePass);
    }

    private void fetchUserInfo() {
        RetrofitClient.getInstance(this).getApi().getAdminUserById(userId)
                .enqueue(new Callback<ApiResponse<User>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            User user = response.body().getData();
                            if (user != null) {
                                tvChangePassUserHeader.setText("Reset password for: " + user.getFullName() + " (" + user.getEmail() + ")");
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                        // Silent fail for header text
                    }
                });
    }

    private void changePassword() {
        String newPass = etNewPassword.getText().toString().trim();
        String confirmPass = etConfirmPassword.getText().toString().trim();

        if (newPass.isEmpty() || confirmPass.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newPass.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPass.equals(confirmPass)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBarChangePass.setVisibility(View.VISIBLE);
        btnSubmitChangePass.setEnabled(false);

        AdminChangePasswordRequest request = new AdminChangePasswordRequest(newPass);

        RetrofitClient.getInstance(this).getApi().changeAdminUserPassword(userId, request)
                .enqueue(new Callback<ApiResponse<User>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                        progressBarChangePass.setVisibility(View.GONE);
                        btnSubmitChangePass.setEnabled(true);

                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            Toast.makeText(ChangePasswordActivity.this, "Password updated successfully", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            String err = "Failed to update password";
                            if (response.errorBody() != null) {
                                try {
                                    err = response.errorBody().string();
                                } catch (Exception ignored) {}
                            }
                            Toast.makeText(ChangePasswordActivity.this, err, Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                        progressBarChangePass.setVisibility(View.GONE);
                        btnSubmitChangePass.setEnabled(true);
                        Toast.makeText(ChangePasswordActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

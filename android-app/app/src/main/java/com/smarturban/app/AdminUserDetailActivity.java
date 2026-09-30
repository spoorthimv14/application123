package com.smarturban.app;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.smarturban.app.api.RetrofitClient;
import com.smarturban.app.model.*;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.List;

public class AdminUserDetailActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private EditText etFullName, etEmail, etPhone, etAddress, etNewPassword;
    private Spinner spinnerRole;
    private Button btnUpdateProfile, btnToggleStatus, btnResetPassword;
    private TextView tvStatusLabel;
    private ProgressBar progressBarDetail;

    private Long userId;
    private UserResponse currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_user_detail);

        userId = getIntent().getLongExtra("user_id", -1);
        if (userId == -1) {
            Toast.makeText(this, "Invalid User ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();

        btnBack.setOnClickListener(v -> finish());

        setupRoleSpinner();

        fetchUserDetails();

        btnUpdateProfile.setOnClickListener(v -> updateProfile());
        btnToggleStatus.setOnClickListener(v -> toggleStatus());
        btnResetPassword.setOnClickListener(v -> resetPassword());
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etAddress = findViewById(R.id.etAddress);
        etNewPassword = findViewById(R.id.etNewPassword);
        spinnerRole = findViewById(R.id.spinnerRole);
        btnUpdateProfile = findViewById(R.id.btnUpdateProfile);
        btnToggleStatus = findViewById(R.id.btnToggleStatus);
        btnResetPassword = findViewById(R.id.btnResetPassword);
        tvStatusLabel = findViewById(R.id.tvStatusLabel);
        progressBarDetail = findViewById(R.id.progressBarDetail);
    }

    private void setupRoleSpinner() {
        List<String> roles = new ArrayList<>();
        roles.add("USER");
        roles.add("ADMIN");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, roles);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRole.setAdapter(adapter);
    }

    private void fetchUserDetails() {
        progressBarDetail.setVisibility(View.VISIBLE);
        RetrofitClient.getInstance(this).getApi().getAdminUserById(userId)
                .enqueue(new Callback<ApiResponse<UserResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<UserResponse>> call, Response<ApiResponse<UserResponse>> response) {
                        progressBarDetail.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            currentUser = response.body().getData();
                            populateFields(currentUser);
                        } else {
                            Toast.makeText(AdminUserDetailActivity.this, "Failed to load user details", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<UserResponse>> call, Throwable t) {
                        progressBarDetail.setVisibility(View.GONE);
                        Toast.makeText(AdminUserDetailActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void populateFields(UserResponse user) {
        if (user == null) return;
        etFullName.setText(user.getFullName() != null ? user.getFullName() : "");
        etEmail.setText(user.getEmail() != null ? user.getEmail() : "");
        etPhone.setText(user.getPhone() != null ? user.getPhone() : "");
        etAddress.setText(user.getAddress() != null ? user.getAddress() : "");

        String role = user.getRole() != null ? user.getRole() : "USER";
        if ("ADMIN".equalsIgnoreCase(role)) {
            spinnerRole.setSelection(1);
        } else {
            spinnerRole.setSelection(0);
        }

        updateStatusUI(user.isEnabled());
    }

    private void updateStatusUI(boolean enabled) {
        if (enabled) {
            tvStatusLabel.setText("Current Status: ACTIVE");
            tvStatusLabel.setTextColor(Color.parseColor("#10B981"));
            btnToggleStatus.setText("DEACTIVATE ACCOUNT");
            btnToggleStatus.setBackgroundColor(Color.parseColor("#EF4444"));
        } else {
            tvStatusLabel.setText("Current Status: INACTIVE");
            tvStatusLabel.setTextColor(Color.parseColor("#EF4444"));
            btnToggleStatus.setText("ACTIVATE ACCOUNT");
            btnToggleStatus.setBackgroundColor(Color.parseColor("#10B981"));
        }
    }

    private void updateProfile() {
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String role = (String) spinnerRole.getSelectedItem();
        String address = etAddress.getText().toString().trim();

        if (fullName.isEmpty()) {
            etFullName.setError("Full name is required");
            return;
        }
        if (email.isEmpty()) {
            etEmail.setError("Email is required");
            return;
        }
        if (phone.isEmpty()) {
            etPhone.setError("Phone is required");
            return;
        }

        progressBarDetail.setVisibility(View.VISIBLE);
        UpdateUserRequest request = new UpdateUserRequest(fullName, email, phone, role, address);

        RetrofitClient.getInstance(this).getApi().updateAdminUser(userId, request)
                .enqueue(new Callback<ApiResponse<UserResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<UserResponse>> call, Response<ApiResponse<UserResponse>> response) {
                        progressBarDetail.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            currentUser = response.body().getData();
                            populateFields(currentUser);
                            Toast.makeText(AdminUserDetailActivity.this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                        } else {
                            String msg = "Failed to update profile";
                            if (response.body() != null && response.body().getMessage() != null) {
                                msg = response.body().getMessage();
                            }
                            Toast.makeText(AdminUserDetailActivity.this, msg, Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<UserResponse>> call, Throwable t) {
                        progressBarDetail.setVisibility(View.GONE);
                        Toast.makeText(AdminUserDetailActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void toggleStatus() {
        if (currentUser == null) return;
        boolean newStatus = !currentUser.isEnabled();

        progressBarDetail.setVisibility(View.VISIBLE);
        UpdateUserStatusRequest request = new UpdateUserStatusRequest(newStatus);

        RetrofitClient.getInstance(this).getApi().updateAdminUserStatus(userId, request)
                .enqueue(new Callback<ApiResponse<UserResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<UserResponse>> call, Response<ApiResponse<UserResponse>> response) {
                        progressBarDetail.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            currentUser = response.body().getData();
                            updateStatusUI(currentUser.isEnabled());
                            Toast.makeText(AdminUserDetailActivity.this, "Account status updated!", Toast.LENGTH_SHORT).show();
                        } else {
                            String msg = "Failed to update account status";
                            if (response.body() != null && response.body().getMessage() != null) {
                                msg = response.body().getMessage();
                            }
                            Toast.makeText(AdminUserDetailActivity.this, msg, Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<UserResponse>> call, Throwable t) {
                        progressBarDetail.setVisibility(View.GONE);
                        Toast.makeText(AdminUserDetailActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void resetPassword() {
        String newPassword = etNewPassword.getText().toString().trim();
        if (newPassword.length() < 8) {
            etNewPassword.setError("Password must be at least 8 characters");
            return;
        }

        progressBarDetail.setVisibility(View.VISIBLE);
        ResetUserPasswordRequest request = new ResetUserPasswordRequest(newPassword);

        RetrofitClient.getInstance(this).getApi().resetAdminUserPassword(userId, request)
                .enqueue(new Callback<ApiResponse<UserResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<UserResponse>> call, Response<ApiResponse<UserResponse>> response) {
                        progressBarDetail.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            etNewPassword.setText("");
                            Toast.makeText(AdminUserDetailActivity.this, "Password reset successfully!", Toast.LENGTH_SHORT).show();
                        } else {
                            String msg = "Failed to reset password";
                            if (response.body() != null && response.body().getMessage() != null) {
                                msg = response.body().getMessage();
                            }
                            Toast.makeText(AdminUserDetailActivity.this, msg, Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<UserResponse>> call, Throwable t) {
                        progressBarDetail.setVisibility(View.GONE);
                        Toast.makeText(AdminUserDetailActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

package com.smarturban.app;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.smarturban.app.api.RetrofitClient;
import com.smarturban.app.model.*;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserDetailsActivity extends AppCompatActivity {

    private ImageButton btnUserDetailsBack;
    private TextView tvDetailUserId, tvDetailRole, tvDetailStatus, tvDetailFullName;
    private TextView tvDetailEmail, tvDetailPhone, tvDetailAddress, tvDetailCreatedAt;
    private Button btnEditUser, btnResetPassword, btnToggleStatus;
    private ProgressBar progressBarDetails;

    private long userId = -1;
    private UserAdminResponse currentUser = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_details);

        userId = getIntent().getLongExtra("user_id", -1);
        if (userId == -1) {
            Toast.makeText(this, "Invalid user ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();

        btnUserDetailsBack.setOnClickListener(v -> finish());
        btnEditUser.setOnClickListener(v -> {
            Intent intent = new Intent(UserDetailsActivity.this, AddEditUserActivity.class);
            intent.putExtra("user_id", userId);
            startActivity(intent);
        });
        btnResetPassword.setOnClickListener(v -> showResetPasswordDialog());
        btnToggleStatus.setOnClickListener(v -> toggleUserStatus());

        loadUserDetails();
    }

    private void initViews() {
        btnUserDetailsBack = findViewById(R.id.btnUserDetailsBack);
        tvDetailUserId = findViewById(R.id.tvDetailUserId);
        tvDetailRole = findViewById(R.id.tvDetailRole);
        tvDetailStatus = findViewById(R.id.tvDetailStatus);
        tvDetailFullName = findViewById(R.id.tvDetailFullName);
        tvDetailEmail = findViewById(R.id.tvDetailEmail);
        tvDetailPhone = findViewById(R.id.tvDetailPhone);
        tvDetailAddress = findViewById(R.id.tvDetailAddress);
        tvDetailCreatedAt = findViewById(R.id.tvDetailCreatedAt);
        btnEditUser = findViewById(R.id.btnEditUser);
        btnResetPassword = findViewById(R.id.btnResetPassword);
        btnToggleStatus = findViewById(R.id.btnToggleStatus);
        progressBarDetails = findViewById(R.id.progressBarDetails);
    }

    private void loadUserDetails() {
        progressBarDetails.setVisibility(View.VISIBLE);
        RetrofitClient.getInstance(this).getApi().getUserById(userId).enqueue(new Callback<ApiResponse<UserAdminResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<UserAdminResponse>> call, Response<ApiResponse<UserAdminResponse>> response) {
                progressBarDetails.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    currentUser = response.body().getData();
                    if (currentUser != null) {
                        bindUserData(currentUser);
                    }
                } else {
                    Toast.makeText(UserDetailsActivity.this, "Failed to load user details", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<UserAdminResponse>> call, Throwable t) {
                progressBarDetails.setVisibility(View.GONE);
                Toast.makeText(UserDetailsActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void bindUserData(UserAdminResponse user) {
        tvDetailUserId.setText("User ID: #" + user.getId());
        tvDetailFullName.setText(user.getFullName() != null ? user.getFullName() : "N/A");
        tvDetailEmail.setText(user.getEmail() != null ? user.getEmail() : "N/A");
        tvDetailPhone.setText(user.getPhone() != null ? user.getPhone() : "N/A");
        tvDetailAddress.setText(user.getAddress() != null && !user.getAddress().isEmpty() ? user.getAddress() : "None provided");
        tvDetailCreatedAt.setText(user.getCreatedAt() != null ? user.getCreatedAt().replace("T", " ") : "N/A");

        String role = user.getRole() != null ? user.getRole() : "USER";
        tvDetailRole.setText(role);
        if ("ADMIN".equalsIgnoreCase(role)) {
            tvDetailRole.setBackgroundColor(Color.parseColor("#DBEAFE"));
            tvDetailRole.setTextColor(Color.parseColor("#1E40AF"));
        } else {
            tvDetailRole.setBackgroundColor(Color.parseColor("#F3F4F6"));
            tvDetailRole.setTextColor(Color.parseColor("#374151"));
        }

        if (user.isActive()) {
            tvDetailStatus.setText("ACTIVE");
            tvDetailStatus.setBackgroundColor(Color.parseColor("#D1FAE5"));
            tvDetailStatus.setTextColor(Color.parseColor("#065F46"));

            btnToggleStatus.setText("Deactivate User");
            btnToggleStatus.setBackgroundTintList(getColorStateList(R.color.status_rejected));
        } else {
            tvDetailStatus.setText("INACTIVE");
            tvDetailStatus.setBackgroundColor(Color.parseColor("#FEE2E2"));
            tvDetailStatus.setTextColor(Color.parseColor("#991B1B"));

            btnToggleStatus.setText("Reactivate User");
            btnToggleStatus.setBackgroundTintList(getColorStateList(R.color.secondary_green));
        }
    }

    private void showResetPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Reset User Password");

        final EditText etNewPass = new EditText(this);
        etNewPass.setHint("Enter new password (min 6 chars)");
        etNewPass.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(etNewPass);

        builder.setPositiveButton("Reset Password", (dialog, which) -> {
            String newPassword = etNewPass.getText().toString().trim();
            if (newPassword.isEmpty() || newPassword.length() < 6) {
                Toast.makeText(UserDetailsActivity.this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                return;
            }

            progressBarDetails.setVisibility(View.VISIBLE);
            UserPasswordResetRequest req = new UserPasswordResetRequest(newPassword);
            RetrofitClient.getInstance(UserDetailsActivity.this).getApi()
                    .resetUserPassword(userId, req)
                    .enqueue(new Callback<ApiResponse<UserAdminResponse>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<UserAdminResponse>> call, Response<ApiResponse<UserAdminResponse>> response) {
                            progressBarDetails.setVisibility(View.GONE);
                            if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                                Toast.makeText(UserDetailsActivity.this, "Password reset successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(UserDetailsActivity.this, "Failed to reset password", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResponse<UserAdminResponse>> call, Throwable t) {
                            progressBarDetails.setVisibility(View.GONE);
                            Toast.makeText(UserDetailsActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void toggleUserStatus() {
        if (currentUser == null) return;

        boolean newActive = !currentUser.isActive();
        progressBarDetails.setVisibility(View.VISIBLE);

        UserStatusUpdateRequest request = new UserStatusUpdateRequest(newActive);
        RetrofitClient.getInstance(this).getApi()
                .updateUserStatus(userId, request)
                .enqueue(new Callback<ApiResponse<UserAdminResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<UserAdminResponse>> call, Response<ApiResponse<UserAdminResponse>> response) {
                        progressBarDetails.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            currentUser = response.body().getData();
                            if (currentUser != null) {
                                bindUserData(currentUser);
                                String actionStr = currentUser.isActive() ? "activated" : "deactivated";
                                Toast.makeText(UserDetailsActivity.this, "User " + actionStr + " successfully", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            String msg = "Status update failed";
                            if (response.body() != null && response.body().getMessage() != null) {
                                msg = response.body().getMessage();
                            }
                            Toast.makeText(UserDetailsActivity.this, msg, Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<UserAdminResponse>> call, Throwable t) {
                        progressBarDetails.setVisibility(View.GONE);
                        Toast.makeText(UserDetailsActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserDetails();
    }
}

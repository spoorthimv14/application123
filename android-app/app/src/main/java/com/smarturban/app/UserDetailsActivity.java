package com.smarturban.app;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.smarturban.app.api.RetrofitClient;
import com.smarturban.app.model.ApiResponse;
import com.smarturban.app.model.UserResponse;
import com.smarturban.app.model.UserStatusUpdateRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserDetailsActivity extends AppCompatActivity {

    private ImageButton btnUserDetailBack;
    private TextView tvDetailFullName, tvDetailRoleBadge, tvDetailStatus, tvDetailEmail, tvDetailPhone, tvDetailAddress;
    private MaterialButton btnEditUser, btnChangeUserPassword, btnToggleUserStatus;

    private Long userId;
    private UserResponse currentUser;

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

        btnUserDetailBack.setOnClickListener(v -> finish());

        btnEditUser.setOnClickListener(v -> {
            if (currentUser != null) {
                Intent intent = new Intent(UserDetailsActivity.this, EditUserActivity.class);
                intent.putExtra("user_id", currentUser.getUserId());
                intent.putExtra("full_name", currentUser.getFullName());
                intent.putExtra("email", currentUser.getEmail());
                intent.putExtra("phone", currentUser.getPhone());
                intent.putExtra("role", currentUser.getRole());
                intent.putExtra("address", currentUser.getAddress());
                startActivity(intent);
            }
        });

        btnChangeUserPassword.setOnClickListener(v -> {
            if (currentUser != null) {
                Intent intent = new Intent(UserDetailsActivity.this, ChangePasswordActivity.class);
                intent.putExtra("user_id", currentUser.getUserId());
                intent.putExtra("user_name", currentUser.getFullName());
                startActivity(intent);
            }
        });

        btnToggleUserStatus.setOnClickListener(v -> {
            if (currentUser != null) {
                boolean newStatus = !currentUser.isEnabled();
                toggleUserStatus(newStatus);
            }
        });

        fetchUserDetails();
    }

    private void initViews() {
        btnUserDetailBack = findViewById(R.id.btnUserDetailBack);
        tvDetailFullName = findViewById(R.id.tvDetailFullName);
        tvDetailRoleBadge = findViewById(R.id.tvDetailRoleBadge);
        tvDetailStatus = findViewById(R.id.tvDetailStatus);
        tvDetailEmail = findViewById(R.id.tvDetailEmail);
        tvDetailPhone = findViewById(R.id.tvDetailPhone);
        tvDetailAddress = findViewById(R.id.tvDetailAddress);
        btnEditUser = findViewById(R.id.btnEditUser);
        btnChangeUserPassword = findViewById(R.id.btnChangeUserPassword);
        btnToggleUserStatus = findViewById(R.id.btnToggleUserStatus);
    }

    private void fetchUserDetails() {
        RetrofitClient.getInstance(this).getApi().getUserById(userId)
                .enqueue(new Callback<ApiResponse<UserResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<UserResponse>> call, Response<ApiResponse<UserResponse>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            currentUser = response.body().getData();
                            updateUI();
                        } else {
                            Toast.makeText(UserDetailsActivity.this, "Failed to load user details", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<UserResponse>> call, Throwable t) {
                        Toast.makeText(UserDetailsActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateUI() {
        if (currentUser == null) return;

        tvDetailFullName.setText(currentUser.getFullName());
        tvDetailRoleBadge.setText(currentUser.getRole());
        tvDetailEmail.setText(currentUser.getEmail());
        tvDetailPhone.setText(currentUser.getPhone() != null && !currentUser.getPhone().isEmpty() ? currentUser.getPhone() : "N/A");
        tvDetailAddress.setText(currentUser.getAddress() != null && !currentUser.getAddress().isEmpty() ? currentUser.getAddress() : "N/A");

        if (currentUser.isEnabled()) {
            tvDetailStatus.setText("ACTIVE");
            tvDetailStatus.setTextColor(Color.parseColor("#10B981"));
            btnToggleUserStatus.setText("Deactivate Account");
            btnToggleUserStatus.setBackgroundColor(Color.parseColor("#EF4444"));
        } else {
            tvDetailStatus.setText("DEACTIVATED");
            tvDetailStatus.setTextColor(Color.parseColor("#EF4444"));
            btnToggleUserStatus.setText("Activate Account");
            btnToggleUserStatus.setBackgroundColor(Color.parseColor("#10B981"));
        }
    }

    private void toggleUserStatus(boolean targetStatus) {
        UserStatusUpdateRequest request = new UserStatusUpdateRequest(targetStatus);

        RetrofitClient.getInstance(this).getApi().updateUserStatus(userId, request)
                .enqueue(new Callback<ApiResponse<UserResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<UserResponse>> call, Response<ApiResponse<UserResponse>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            currentUser = response.body().getData();
                            updateUI();
                            String msg = targetStatus ? "User activated successfully" : "User deactivated successfully";
                            Toast.makeText(UserDetailsActivity.this, msg, Toast.LENGTH_SHORT).show();
                        } else {
                            String err = "Failed to update status";
                            if (response.errorBody() != null) {
                                try {
                                    err = response.errorBody().string();
                                } catch (Exception ignored) {}
                            }
                            Toast.makeText(UserDetailsActivity.this, err, Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<UserResponse>> call, Throwable t) {
                        Toast.makeText(UserDetailsActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchUserDetails();
    }
}

package com.smarturban.app;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.smarturban.app.api.RetrofitClient;
import com.smarturban.app.model.ApiResponse;
import com.smarturban.app.model.User;
import com.smarturban.app.model.UserStatusRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserDetailActivity extends AppCompatActivity {

    private ImageButton btnBackUserDetail;
    private ProgressBar progressBarUserDetail;
    private TextView tvDetailFullName, tvDetailRole, tvDetailStatus, tvDetailEmail, tvDetailPhone, tvDetailAddress, tvDetailUserId;
    private MaterialButton btnEditUser, btnToggleStatus, btnChangePassword;

    private Long userId;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_detail);

        userId = getIntent().getLongExtra("user_id", -1);
        if (userId == -1) {
            Toast.makeText(this, "Invalid User ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();

        btnBackUserDetail.setOnClickListener(v -> finish());

        btnEditUser.setOnClickListener(v -> {
            Intent intent = new Intent(UserDetailActivity.this, EditUserActivity.class);
            intent.putExtra("user_id", userId);
            startActivity(intent);
        });

        btnToggleStatus.setOnClickListener(v -> toggleUserStatus());

        btnChangePassword.setOnClickListener(v -> {
            Intent intent = new Intent(UserDetailActivity.this, ChangePasswordActivity.class);
            intent.putExtra("user_id", userId);
            startActivity(intent);
        });

        fetchUserDetails();
    }

    private void initViews() {
        btnBackUserDetail = findViewById(R.id.btnBackUserDetail);
        progressBarUserDetail = findViewById(R.id.progressBarUserDetail);
        tvDetailFullName = findViewById(R.id.tvDetailFullName);
        tvDetailRole = findViewById(R.id.tvDetailRole);
        tvDetailStatus = findViewById(R.id.tvDetailStatus);
        tvDetailEmail = findViewById(R.id.tvDetailEmail);
        tvDetailPhone = findViewById(R.id.tvDetailPhone);
        tvDetailAddress = findViewById(R.id.tvDetailAddress);
        tvDetailUserId = findViewById(R.id.tvDetailUserId);
        btnEditUser = findViewById(R.id.btnEditUser);
        btnToggleStatus = findViewById(R.id.btnToggleStatus);
        btnChangePassword = findViewById(R.id.btnChangePassword);
    }

    private void fetchUserDetails() {
        progressBarUserDetail.setVisibility(View.VISIBLE);
        RetrofitClient.getInstance(this).getApi().getAdminUserById(userId)
                .enqueue(new Callback<ApiResponse<User>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                        progressBarUserDetail.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            currentUser = response.body().getData();
                            displayUser(currentUser);
                        } else {
                            Toast.makeText(UserDetailActivity.this, "Failed to fetch user details", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                        progressBarUserDetail.setVisibility(View.GONE);
                        Toast.makeText(UserDetailActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void displayUser(User user) {
        if (user == null) return;

        tvDetailFullName.setText(user.getFullName());
        tvDetailRole.setText(user.getRole() != null ? user.getRole() : "ROLE_USER");
        tvDetailEmail.setText(user.getEmail());
        tvDetailPhone.setText(user.getPhone() != null ? user.getPhone() : "N/A");
        tvDetailAddress.setText(user.getAddress() != null ? user.getAddress() : "N/A");
        tvDetailUserId.setText("#" + user.getId());

        if (user.isEnabled()) {
            tvDetailStatus.setText("Status: ACTIVE");
            tvDetailStatus.setTextColor(Color.parseColor("#10B981"));
            btnToggleStatus.setText("Deactivate User");
            btnToggleStatus.setBackgroundColor(Color.parseColor("#EF4444"));
        } else {
            tvDetailStatus.setText("Status: INACTIVE");
            tvDetailStatus.setTextColor(Color.parseColor("#EF4444"));
            btnToggleStatus.setText("Activate User");
            btnToggleStatus.setBackgroundColor(Color.parseColor("#10B981"));
        }
    }

    private void toggleUserStatus() {
        if (currentUser == null) return;

        boolean newStatus = !currentUser.isEnabled();
        progressBarUserDetail.setVisibility(View.VISIBLE);

        RetrofitClient.getInstance(this).getApi().updateAdminUserStatus(userId, new UserStatusRequest(newStatus))
                .enqueue(new Callback<ApiResponse<User>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                        progressBarUserDetail.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            currentUser = response.body().getData();
                            displayUser(currentUser);
                            Toast.makeText(UserDetailActivity.this, "User status updated successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            String err = "Failed to update status";
                            if (response.errorBody() != null) {
                                try {
                                    err = response.errorBody().string();
                                } catch (Exception ignored) {}
                            }
                            Toast.makeText(UserDetailActivity.this, err, Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                        progressBarUserDetail.setVisibility(View.GONE);
                        Toast.makeText(UserDetailActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchUserDetails();
    }
}

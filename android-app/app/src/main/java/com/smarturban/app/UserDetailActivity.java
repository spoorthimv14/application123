package com.smarturban.app;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.smarturban.app.api.RetrofitClient;
import com.smarturban.app.model.ApiResponse;
import com.smarturban.app.model.UpdateUserStatusRequest;
import com.smarturban.app.model.UserResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserDetailActivity extends AppCompatActivity {

    private ImageButton btnUserDetailBack;
    private ProgressBar progressBarUserDetail;
    private View layoutUserDetailContent;
    private TextView tvUserDetailName, tvUserDetailRole, tvUserDetailEmail, tvUserDetailPhone, tvUserDetailAddress, tvUserDetailStatus;
    private Button btnUserEdit, btnUserToggleStatus, btnUserResetPassword;

    private Long userId;
    private UserResponse currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_detail);

        userId = getIntent().getLongExtra("user_id", -1);
        if (userId == -1) {
            Toast.makeText(this, "Invalid user ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();

        btnUserDetailBack.setOnClickListener(v -> finish());

        btnUserEdit.setOnClickListener(v -> {
            if (currentUser != null) {
                Intent intent = new Intent(UserDetailActivity.this, EditUserActivity.class);
                intent.putExtra("user_id", userId);
                intent.putExtra("full_name", currentUser.getFullName());
                intent.putExtra("email", currentUser.getEmail());
                intent.putExtra("phone", currentUser.getPhone());
                intent.putExtra("address", currentUser.getAddress());
                intent.putExtra("role", currentUser.getRole());
                startActivity(intent);
            }
        });

        btnUserToggleStatus.setOnClickListener(v -> toggleUserStatus());

        btnUserResetPassword.setOnClickListener(v -> {
            if (currentUser != null) {
                Intent intent = new Intent(UserDetailActivity.this, ChangePasswordActivity.class);
                intent.putExtra("user_id", userId);
                intent.putExtra("full_name", currentUser.getFullName());
                intent.putExtra("email", currentUser.getEmail());
                startActivity(intent);
            }
        });
    }

    private void initViews() {
        btnUserDetailBack = findViewById(R.id.btnUserDetailBack);
        progressBarUserDetail = findViewById(R.id.progressBarUserDetail);
        layoutUserDetailContent = findViewById(R.id.layoutUserDetailContent);
        tvUserDetailName = findViewById(R.id.tvUserDetailName);
        tvUserDetailRole = findViewById(R.id.tvUserDetailRole);
        tvUserDetailEmail = findViewById(R.id.tvUserDetailEmail);
        tvUserDetailPhone = findViewById(R.id.tvUserDetailPhone);
        tvUserDetailAddress = findViewById(R.id.tvUserDetailAddress);
        tvUserDetailStatus = findViewById(R.id.tvUserDetailStatus);
        btnUserEdit = findViewById(R.id.btnUserEdit);
        btnUserToggleStatus = findViewById(R.id.btnUserToggleStatus);
        btnUserResetPassword = findViewById(R.id.btnUserResetPassword);
    }

    private void loadUserDetails() {
        progressBarUserDetail.setVisibility(View.VISIBLE);
        layoutUserDetailContent.setVisibility(View.GONE);

        RetrofitClient.getInstance(this).getApi().getUserById(userId)
                .enqueue(new Callback<ApiResponse<UserResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<UserResponse>> call, Response<ApiResponse<UserResponse>> response) {
                        progressBarUserDetail.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            currentUser = response.body().getData();
                            if (currentUser != null) {
                                displayUser(currentUser);
                                layoutUserDetailContent.setVisibility(View.VISIBLE);
                            }
                        } else {
                            Toast.makeText(UserDetailActivity.this, "User not found or access denied.", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<UserResponse>> call, Throwable t) {
                        progressBarUserDetail.setVisibility(View.GONE);
                        Toast.makeText(UserDetailActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void displayUser(UserResponse user) {
        tvUserDetailName.setText(user.getFullName() != null ? user.getFullName() : "N/A");
        tvUserDetailRole.setText("ROLE: " + (user.getRole() != null ? user.getRole() : "USER"));
        tvUserDetailEmail.setText(user.getEmail() != null ? user.getEmail() : "N/A");
        tvUserDetailPhone.setText(user.getPhone() != null ? user.getPhone() : "N/A");
        tvUserDetailAddress.setText(user.getAddress() != null ? user.getAddress() : "N/A");

        if (user.isEnabled()) {
            tvUserDetailStatus.setText("ENABLED / ACTIVE");
            tvUserDetailStatus.setTextColor(Color.parseColor("#10B981"));
            btnUserToggleStatus.setText("Deactivate Account");
            btnUserToggleStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#EF4444")));
        } else {
            tvUserDetailStatus.setText("DEACTIVATED / DISABLED");
            tvUserDetailStatus.setTextColor(Color.parseColor("#EF4444"));
            btnUserToggleStatus.setText("Activate Account");
            btnUserToggleStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#10B981")));
        }
    }

    private void toggleUserStatus() {
        if (currentUser == null) return;

        boolean newStatus = !currentUser.isEnabled();
        progressBarUserDetail.setVisibility(View.VISIBLE);

        RetrofitClient.getInstance(this).getApi().updateUserStatus(userId, new UpdateUserStatusRequest(newStatus))
                .enqueue(new Callback<ApiResponse<UserResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<UserResponse>> call, Response<ApiResponse<UserResponse>> response) {
                        progressBarUserDetail.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            Toast.makeText(UserDetailActivity.this, "User status updated successfully", Toast.LENGTH_SHORT).show();
                            currentUser = response.body().getData();
                            displayUser(currentUser);
                        } else {
                            String err = "Failed to update status";
                            if (response.body() != null && response.body().getMessage() != null) {
                                err = response.body().getMessage();
                            }
                            Toast.makeText(UserDetailActivity.this, err, Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<UserResponse>> call, Throwable t) {
                        progressBarUserDetail.setVisibility(View.GONE);
                        Toast.makeText(UserDetailActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserDetails();
    }
}

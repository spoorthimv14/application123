package com.smarturban.app;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.smarturban.app.api.RetrofitClient;
import com.smarturban.app.model.AdminChangePasswordRequest;
import com.smarturban.app.model.ApiResponse;
import com.smarturban.app.model.UserResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChangePasswordActivity extends AppCompatActivity {

    private ImageButton btnChangePasswordBack;
    private TextView tvChangePasswordUserLabel;
    private EditText etNewPassword, etConfirmNewPassword;
    private MaterialButton btnSubmitChangePassword;

    private Long userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        userId = getIntent().getLongExtra("user_id", -1);
        String userName = getIntent().getStringExtra("user_name");

        if (userId == -1) {
            Toast.makeText(this, "Invalid user ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();

        btnChangePasswordBack.setOnClickListener(v -> finish());

        if (userName != null) {
            tvChangePasswordUserLabel.setText("User: " + userName);
        }

        btnSubmitChangePassword.setOnClickListener(v -> submitChangePassword());
    }

    private void initViews() {
        btnChangePasswordBack = findViewById(R.id.btnChangePasswordBack);
        tvChangePasswordUserLabel = findViewById(R.id.tvChangePasswordUserLabel);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmNewPassword = findViewById(R.id.etConfirmNewPassword);
        btnSubmitChangePassword = findViewById(R.id.btnSubmitChangePassword);
    }

    private void submitChangePassword() {
        String newPassword = etNewPassword.getText().toString().trim();
        String confirmPassword = etConfirmNewPassword.getText().toString().trim();

        if (newPassword.isEmpty() || newPassword.length() < 6) {
            etNewPassword.setError("Password must be at least 6 characters");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            etConfirmNewPassword.setError("Passwords do not match");
            return;
        }

        AdminChangePasswordRequest request = new AdminChangePasswordRequest(newPassword);

        btnSubmitChangePassword.setEnabled(false);

        RetrofitClient.getInstance(this).getApi().changeUserPassword(userId, request)
                .enqueue(new Callback<ApiResponse<UserResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<UserResponse>> call, Response<ApiResponse<UserResponse>> response) {
                        btnSubmitChangePassword.setEnabled(true);
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            Toast.makeText(ChangePasswordActivity.this, "Password updated successfully", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(ChangePasswordActivity.this, "Failed to update password", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<UserResponse>> call, Throwable t) {
                        btnSubmitChangePassword.setEnabled(true);
                        Toast.makeText(ChangePasswordActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

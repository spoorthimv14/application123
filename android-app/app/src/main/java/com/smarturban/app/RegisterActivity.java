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
import com.smarturban.app.model.RegisterRequest;
import com.smarturban.app.model.UserResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout tilFullName, tilEmail, tilPhone, tilPassword, tilConfirmPassword, tilAddress;
    private TextInputEditText etFullName, etEmail, etPhone, etPassword, etConfirmPassword, etAddress;
    private MaterialButton btnRegister;
    private ProgressBar progressBarRegister;
    private TextView tvLoginLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        tilFullName = findViewById(R.id.tilFullName);
        tilEmail = findViewById(R.id.tilEmail);
        tilPhone = findViewById(R.id.tilPhone);
        tilPassword = findViewById(R.id.tilPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);
        tilAddress = findViewById(R.id.tilAddress);

        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        etAddress = findViewById(R.id.etAddress);

        btnRegister = findViewById(R.id.btnRegister);
        progressBarRegister = findViewById(R.id.progressBarRegister);
        tvLoginLink = findViewById(R.id.tvLoginLink);

        btnRegister.setOnClickListener(v -> performRegistration());
        tvLoginLink.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void performRegistration() {
        clearErrors();

        String fullName = getTextOrEmpty(etFullName);
        String email = getTextOrEmpty(etEmail);
        String phone = getTextOrEmpty(etPhone);
        String password = getTextOrEmpty(etPassword);
        String confirmPassword = getTextOrEmpty(etConfirmPassword);
        String address = getTextOrEmpty(etAddress);

        if (!validateInputs(fullName, email, phone, password, confirmPassword)) {
            return;
        }

        showLoading(true);

        RegisterRequest registerRequest = new RegisterRequest(fullName, email, phone, password, confirmPassword, address);
        RetrofitClient.getApiService(this).register(registerRequest).enqueue(new Callback<ApiResponse<UserResponse>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<UserResponse>> call, @NonNull Response<ApiResponse<UserResponse>> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(RegisterActivity.this, "Registration successful! Please log in.", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                    intent.putExtra("email", email);
                    startActivity(intent);
                    finish();
                } else {
                    String errorMsg = "Registration failed";
                    if (response.code() == 409) {
                        errorMsg = "This email is already registered.";
                        tilEmail.setError(errorMsg);
                    } else if (response.body() != null && response.body().getMessage() != null) {
                        errorMsg = response.body().getMessage();
                    }
                    Toast.makeText(RegisterActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<UserResponse>> call, @NonNull Throwable t) {
                showLoading(false);
                Toast.makeText(RegisterActivity.this, "Unable to connect to server. Please check connection.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private boolean validateInputs(String fullName, String email, String phone, String password, String confirmPassword) {
        boolean valid = true;

        if (TextUtils.isEmpty(fullName) || fullName.length() < 2) {
            tilFullName.setError("Full name must be at least 2 characters");
            valid = false;
        }

        if (TextUtils.isEmpty(email) || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Valid email address required");
            valid = false;
        }

        if (TextUtils.isEmpty(phone) || !phone.matches("^(\\+91[\\-\\s]?)?[0-9]{10}$")) {
            tilPhone.setError("Valid Indian phone number required (+91 9876543210)");
            valid = false;
        }

        if (TextUtils.isEmpty(password) || password.length() < 8) {
            tilPassword.setError("Password must be at least 8 characters");
            valid = false;
        }

        if (!password.equals(confirmPassword)) {
            tilConfirmPassword.setError("Passwords do not match");
            valid = false;
        }

        return valid;
    }

    private void clearErrors() {
        tilFullName.setError(null);
        tilEmail.setError(null);
        tilPhone.setError(null);
        tilPassword.setError(null);
        tilConfirmPassword.setError(null);
        tilAddress.setError(null);
    }

    private String getTextOrEmpty(TextInputEditText editText) {
        return editText.getText() != null ? editText.getText().toString().trim() : "";
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            progressBarRegister.setVisibility(View.VISIBLE);
            btnRegister.setEnabled(false);
        } else {
            progressBarRegister.setVisibility(View.GONE);
            btnRegister.setEnabled(true);
        }
    }
}

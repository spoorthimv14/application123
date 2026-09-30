package com.smarturban.app;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.smarturban.app.api.RetrofitClient;
import com.smarturban.app.model.ApiResponse;
import com.smarturban.app.model.CreateUserRequest;
import com.smarturban.app.model.UserResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.List;

public class AdminAddUserActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private EditText etFullName, etEmail, etPhone, etPassword, etAddress;
    private Spinner spinnerRole;
    private ProgressBar progressBarAddUser;
    private Button btnSubmitUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_add_user);

        initViews();

        btnBack.setOnClickListener(v -> finish());

        List<String> roles = new ArrayList<>();
        roles.add("USER");
        roles.add("ADMIN");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, roles);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRole.setAdapter(adapter);

        btnSubmitUser.setOnClickListener(v -> submitNewUser());
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etPassword = findViewById(R.id.etPassword);
        etAddress = findViewById(R.id.etAddress);
        spinnerRole = findViewById(R.id.spinnerRole);
        progressBarAddUser = findViewById(R.id.progressBarAddUser);
        btnSubmitUser = findViewById(R.id.btnSubmitUser);
    }

    private void submitNewUser() {
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
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
            etPhone.setError("Phone number is required");
            return;
        }
        if (password.length() < 8) {
            etPassword.setError("Password must be at least 8 characters");
            return;
        }

        progressBarAddUser.setVisibility(View.VISIBLE);
        btnSubmitUser.setEnabled(false);

        CreateUserRequest request = new CreateUserRequest(fullName, email, phone, password, role, address);

        RetrofitClient.getInstance(this).getApi().createAdminUser(request)
                .enqueue(new Callback<ApiResponse<UserResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<UserResponse>> call, Response<ApiResponse<UserResponse>> response) {
                        progressBarAddUser.setVisibility(View.GONE);
                        btnSubmitUser.setEnabled(true);
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            Toast.makeText(AdminAddUserActivity.this, "User created successfully!", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            String errorMsg = "Failed to create user";
                            if (response.body() != null && response.body().getMessage() != null) {
                                errorMsg = response.body().getMessage();
                            }
                            Toast.makeText(AdminAddUserActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<UserResponse>> call, Throwable t) {
                        progressBarAddUser.setVisibility(View.GONE);
                        btnSubmitUser.setEnabled(true);
                        Toast.makeText(AdminAddUserActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

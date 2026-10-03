package com.smarturban.app;

import android.os.Bundle;
import android.util.Patterns;
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

public class AddUserActivity extends AppCompatActivity {

    private ImageButton btnAddUserBack;
    private EditText etAddUserFullName, etAddUserEmail, etAddUserPhone, etAddUserPassword, etAddUserAddress;
    private Spinner spinnerAddUserRole;
    private ProgressBar progressBarAddUser;
    private Button btnAddUserSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user);

        initViews();

        btnAddUserBack.setOnClickListener(v -> finish());

        List<String> roles = new ArrayList<>();
        roles.add("USER");
        roles.add("ADMIN");
        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, roles);
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAddUserRole.setAdapter(roleAdapter);

        btnAddUserSubmit.setOnClickListener(v -> submitNewUser());
    }

    private void initViews() {
        btnAddUserBack = findViewById(R.id.btnAddUserBack);
        etAddUserFullName = findViewById(R.id.etAddUserFullName);
        etAddUserEmail = findViewById(R.id.etAddUserEmail);
        etAddUserPhone = findViewById(R.id.etAddUserPhone);
        etAddUserPassword = findViewById(R.id.etAddUserPassword);
        etAddUserAddress = findViewById(R.id.etAddUserAddress);
        spinnerAddUserRole = findViewById(R.id.spinnerAddUserRole);
        progressBarAddUser = findViewById(R.id.progressBarAddUser);
        btnAddUserSubmit = findViewById(R.id.btnAddUserSubmit);
    }

    private void submitNewUser() {
        String fullName = etAddUserFullName.getText().toString().trim();
        String email = etAddUserEmail.getText().toString().trim();
        String phone = etAddUserPhone.getText().toString().trim();
        String password = etAddUserPassword.getText().toString().trim();
        String address = etAddUserAddress.getText().toString().trim();
        String role = (String) spinnerAddUserRole.getSelectedItem();

        if (fullName.isEmpty()) {
            etAddUserFullName.setError("Full name is required");
            etAddUserFullName.requestFocus();
            return;
        }

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etAddUserEmail.setError("Valid email address is required");
            etAddUserEmail.requestFocus();
            return;
        }

        if (phone.isEmpty()) {
            etAddUserPhone.setError("Phone number is required");
            etAddUserPhone.requestFocus();
            return;
        }

        if (password.isEmpty() || password.length() < 6) {
            etAddUserPassword.setError("Password must be at least 6 characters");
            etAddUserPassword.requestFocus();
            return;
        }

        CreateUserRequest req = new CreateUserRequest(fullName, email, phone, password, address, role);

        progressBarAddUser.setVisibility(View.VISIBLE);
        btnAddUserSubmit.setEnabled(false);

        RetrofitClient.getInstance(this).getApi().createUser(req)
                .enqueue(new Callback<ApiResponse<UserResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<UserResponse>> call, Response<ApiResponse<UserResponse>> response) {
                        progressBarAddUser.setVisibility(View.GONE);
                        btnAddUserSubmit.setEnabled(true);

                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            Toast.makeText(AddUserActivity.this, "User created successfully!", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            String message = "Failed to create user";
                            if (response.body() != null && response.body().getMessage() != null) {
                                message = response.body().getMessage();
                            } else if (response.code() == 409) {
                                message = "Email address already exists";
                            }
                            Toast.makeText(AddUserActivity.this, message, Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<UserResponse>> call, Throwable t) {
                        progressBarAddUser.setVisibility(View.GONE);
                        btnAddUserSubmit.setEnabled(true);
                        Toast.makeText(AddUserActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

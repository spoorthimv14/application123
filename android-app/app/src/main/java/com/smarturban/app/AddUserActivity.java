package com.smarturban.app;

import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.smarturban.app.api.RetrofitClient;
import com.smarturban.app.model.AdminCreateUserRequest;
import com.smarturban.app.model.ApiResponse;
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
    private MaterialButton btnSubmitAddUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user);

        initViews();

        btnAddUserBack.setOnClickListener(v -> finish());

        setupRoleSpinner();

        btnSubmitAddUser.setOnClickListener(v -> submitUser());
    }

    private void initViews() {
        btnAddUserBack = findViewById(R.id.btnAddUserBack);
        etAddUserFullName = findViewById(R.id.etAddUserFullName);
        etAddUserEmail = findViewById(R.id.etAddUserEmail);
        etAddUserPhone = findViewById(R.id.etAddUserPhone);
        etAddUserPassword = findViewById(R.id.etAddUserPassword);
        etAddUserAddress = findViewById(R.id.etAddUserAddress);
        spinnerAddUserRole = findViewById(R.id.spinnerAddUserRole);
        btnSubmitAddUser = findViewById(R.id.btnSubmitAddUser);
    }

    private void setupRoleSpinner() {
        List<String> roles = new ArrayList<>();
        roles.add("USER");
        roles.add("ADMIN");

        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, roles);
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAddUserRole.setAdapter(roleAdapter);
    }

    private void submitUser() {
        String fullName = etAddUserFullName.getText().toString().trim();
        String email = etAddUserEmail.getText().toString().trim();
        String phone = etAddUserPhone.getText().toString().trim();
        String password = etAddUserPassword.getText().toString().trim();
        String role = (String) spinnerAddUserRole.getSelectedItem();
        String address = etAddUserAddress.getText().toString().trim();

        if (fullName.isEmpty()) {
            etAddUserFullName.setError("Full name is required");
            return;
        }

        if (email.isEmpty()) {
            etAddUserEmail.setError("Email is required");
            return;
        }

        if (phone.isEmpty()) {
            etAddUserPhone.setError("Phone number is required");
            return;
        }

        if (password.isEmpty() || password.length() < 6) {
            etAddUserPassword.setError("Password must be at least 6 characters");
            return;
        }

        AdminCreateUserRequest request = new AdminCreateUserRequest(fullName, email, phone, password, role, address);

        btnSubmitAddUser.setEnabled(false);

        RetrofitClient.getInstance(this).getApi().createUser(request)
                .enqueue(new Callback<ApiResponse<UserResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<UserResponse>> call, Response<ApiResponse<UserResponse>> response) {
                        btnSubmitAddUser.setEnabled(true);
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            Toast.makeText(AddUserActivity.this, "User created successfully", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            String err = "Failed to create user";
                            if (response.code() == 409) {
                                err = "Email is already registered";
                            }
                            Toast.makeText(AddUserActivity.this, err, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<UserResponse>> call, Throwable t) {
                        btnSubmitAddUser.setEnabled(true);
                        Toast.makeText(AddUserActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

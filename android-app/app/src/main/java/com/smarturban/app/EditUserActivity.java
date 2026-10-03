package com.smarturban.app;

import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.smarturban.app.api.RetrofitClient;
import com.smarturban.app.model.AdminUpdateUserRequest;
import com.smarturban.app.model.ApiResponse;
import com.smarturban.app.model.UserResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.List;

public class EditUserActivity extends AppCompatActivity {

    private ImageButton btnEditUserBack;
    private EditText etEditUserFullName, etEditUserEmail, etEditUserPhone, etEditUserAddress;
    private Spinner spinnerEditUserRole;
    private MaterialButton btnSubmitEditUser;

    private Long userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user);

        userId = getIntent().getLongExtra("user_id", -1);
        if (userId == -1) {
            Toast.makeText(this, "Invalid user ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();

        btnEditUserBack.setOnClickListener(v -> finish());

        setupRoleSpinner();
        populateFields();

        btnSubmitEditUser.setOnClickListener(v -> submitEditUser());
    }

    private void initViews() {
        btnEditUserBack = findViewById(R.id.btnEditUserBack);
        etEditUserFullName = findViewById(R.id.etEditUserFullName);
        etEditUserEmail = findViewById(R.id.etEditUserEmail);
        etEditUserPhone = findViewById(R.id.etEditUserPhone);
        etEditUserAddress = findViewById(R.id.etEditUserAddress);
        spinnerEditUserRole = findViewById(R.id.spinnerEditUserRole);
        btnSubmitEditUser = findViewById(R.id.btnSubmitEditUser);
    }

    private void setupRoleSpinner() {
        List<String> roles = new ArrayList<>();
        roles.add("USER");
        roles.add("ADMIN");

        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, roles);
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEditUserRole.setAdapter(roleAdapter);
    }

    private void populateFields() {
        String fullName = getIntent().getStringExtra("full_name");
        String email = getIntent().getStringExtra("email");
        String phone = getIntent().getStringExtra("phone");
        String role = getIntent().getStringExtra("role");
        String address = getIntent().getStringExtra("address");

        if (fullName != null) etEditUserFullName.setText(fullName);
        if (email != null) etEditUserEmail.setText(email);
        if (phone != null) etEditUserPhone.setText(phone);
        if (address != null) etEditUserAddress.setText(address);

        if ("ADMIN".equalsIgnoreCase(role)) {
            spinnerEditUserRole.setSelection(1);
        } else {
            spinnerEditUserRole.setSelection(0);
        }
    }

    private void submitEditUser() {
        String fullName = etEditUserFullName.getText().toString().trim();
        String email = etEditUserEmail.getText().toString().trim();
        String phone = etEditUserPhone.getText().toString().trim();
        String role = (String) spinnerEditUserRole.getSelectedItem();
        String address = etEditUserAddress.getText().toString().trim();

        if (fullName.isEmpty()) {
            etEditUserFullName.setError("Full name is required");
            return;
        }

        if (email.isEmpty()) {
            etEditUserEmail.setError("Email is required");
            return;
        }

        if (phone.isEmpty()) {
            etEditUserPhone.setError("Phone number is required");
            return;
        }

        AdminUpdateUserRequest request = new AdminUpdateUserRequest(fullName, email, phone, role, address);

        btnSubmitEditUser.setEnabled(false);

        RetrofitClient.getInstance(this).getApi().updateUser(userId, request)
                .enqueue(new Callback<ApiResponse<UserResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<UserResponse>> call, Response<ApiResponse<UserResponse>> response) {
                        btnSubmitEditUser.setEnabled(true);
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            Toast.makeText(EditUserActivity.this, "User updated successfully", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            String err = "Failed to update user";
                            if (response.code() == 409) {
                                err = "Email is already registered";
                            }
                            Toast.makeText(EditUserActivity.this, err, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<UserResponse>> call, Throwable t) {
                        btnSubmitEditUser.setEnabled(true);
                        Toast.makeText(EditUserActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

package com.smarturban.app;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.smarturban.app.api.RetrofitClient;
import com.smarturban.app.model.ApiResponse;
import com.smarturban.app.model.UpdateUserRequest;
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
    private ProgressBar progressBarEditUser;
    private Button btnEditUserSubmit;

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

        List<String> roles = new ArrayList<>();
        roles.add("USER");
        roles.add("ADMIN");
        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, roles);
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEditUserRole.setAdapter(roleAdapter);

        // Pre-fill data
        String fullName = getIntent().getStringExtra("full_name");
        String email = getIntent().getStringExtra("email");
        String phone = getIntent().getStringExtra("phone");
        String address = getIntent().getStringExtra("address");
        String role = getIntent().getStringExtra("role");

        if (fullName != null) etEditUserFullName.setText(fullName);
        if (email != null) etEditUserEmail.setText(email);
        if (phone != null) etEditUserPhone.setText(phone);
        if (address != null) etEditUserAddress.setText(address);

        if (role != null) {
            int pos = roleAdapter.getPosition(role.toUpperCase());
            if (pos >= 0) spinnerEditUserRole.setSelection(pos);
        }

        btnEditUserSubmit.setOnClickListener(v -> submitUserUpdate());
    }

    private void initViews() {
        btnEditUserBack = findViewById(R.id.btnEditUserBack);
        etEditUserFullName = findViewById(R.id.etEditUserFullName);
        etEditUserEmail = findViewById(R.id.etEditUserEmail);
        etEditUserPhone = findViewById(R.id.etEditUserPhone);
        etEditUserAddress = findViewById(R.id.etEditUserAddress);
        spinnerEditUserRole = findViewById(R.id.spinnerEditUserRole);
        progressBarEditUser = findViewById(R.id.progressBarEditUser);
        btnEditUserSubmit = findViewById(R.id.btnEditUserSubmit);
    }

    private void submitUserUpdate() {
        String fullName = etEditUserFullName.getText().toString().trim();
        String email = etEditUserEmail.getText().toString().trim();
        String phone = etEditUserPhone.getText().toString().trim();
        String address = etEditUserAddress.getText().toString().trim();
        String role = (String) spinnerEditUserRole.getSelectedItem();

        if (fullName.isEmpty()) {
            etEditUserFullName.setError("Full name is required");
            etEditUserFullName.requestFocus();
            return;
        }

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEditUserEmail.setError("Valid email address is required");
            etEditUserEmail.requestFocus();
            return;
        }

        if (phone.isEmpty()) {
            etEditUserPhone.setError("Phone number is required");
            etEditUserPhone.requestFocus();
            return;
        }

        UpdateUserRequest req = new UpdateUserRequest(fullName, email, phone, address, role);

        progressBarEditUser.setVisibility(View.VISIBLE);
        btnEditUserSubmit.setEnabled(false);

        RetrofitClient.getInstance(this).getApi().updateUser(userId, req)
                .enqueue(new Callback<ApiResponse<UserResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<UserResponse>> call, Response<ApiResponse<UserResponse>> response) {
                        progressBarEditUser.setVisibility(View.GONE);
                        btnEditUserSubmit.setEnabled(true);

                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            Toast.makeText(EditUserActivity.this, "User updated successfully!", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            String message = "Failed to update user";
                            if (response.body() != null && response.body().getMessage() != null) {
                                message = response.body().getMessage();
                            }
                            Toast.makeText(EditUserActivity.this, message, Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<UserResponse>> call, Throwable t) {
                        progressBarEditUser.setVisibility(View.GONE);
                        btnEditUserSubmit.setEnabled(true);
                        Toast.makeText(EditUserActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

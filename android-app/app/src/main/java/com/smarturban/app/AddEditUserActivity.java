package com.smarturban.app;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.smarturban.app.api.RetrofitClient;
import com.smarturban.app.model.*;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.List;

public class AddEditUserActivity extends AppCompatActivity {

    private ImageButton btnAddEditUserBack;
    private TextView tvAddEditUserTitle;
    private EditText etUserFullName, etUserEmail, etUserPhone, etUserPassword, etUserAddress;
    private LinearLayout layoutPasswordContainer;
    private Spinner spinnerUserRole;
    private SwitchCompat switchUserActive;
    private Button btnSaveUser;
    private ProgressBar progressBarSaveUser;

    private boolean isEditMode = false;
    private long userId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_user);

        initViews();
        setupRoleSpinner();

        if (getIntent().hasExtra("user_id")) {
            isEditMode = true;
            userId = getIntent().getLongExtra("user_id", -1);
            tvAddEditUserTitle.setText("Edit User Profile");
            layoutPasswordContainer.setVisibility(View.GONE);
            switchUserActive.setVisibility(View.VISIBLE);
            loadUserData();
        } else {
            isEditMode = false;
            tvAddEditUserTitle.setText("Add New User");
            layoutPasswordContainer.setVisibility(View.VISIBLE);
            switchUserActive.setVisibility(View.GONE);
        }

        btnAddEditUserBack.setOnClickListener(v -> finish());
        btnSaveUser.setOnClickListener(v -> saveUser());
    }

    private void initViews() {
        btnAddEditUserBack = findViewById(R.id.btnAddEditUserBack);
        tvAddEditUserTitle = findViewById(R.id.tvAddEditUserTitle);
        etUserFullName = findViewById(R.id.etUserFullName);
        etUserEmail = findViewById(R.id.etUserEmail);
        etUserPhone = findViewById(R.id.etUserPhone);
        etUserPassword = findViewById(R.id.etUserPassword);
        etUserAddress = findViewById(R.id.etUserAddress);
        layoutPasswordContainer = findViewById(R.id.layoutPasswordContainer);
        spinnerUserRole = findViewById(R.id.spinnerUserRole);
        switchUserActive = findViewById(R.id.switchUserActive);
        btnSaveUser = findViewById(R.id.btnSaveUser);
        progressBarSaveUser = findViewById(R.id.progressBarSaveUser);
    }

    private void setupRoleSpinner() {
        List<String> roles = new ArrayList<>();
        roles.add("USER");
        roles.add("ADMIN");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, roles);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUserRole.setAdapter(adapter);
    }

    private void loadUserData() {
        progressBarSaveUser.setVisibility(View.VISIBLE);
        RetrofitClient.getInstance(this).getApi().getUserById(userId).enqueue(new Callback<ApiResponse<UserAdminResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<UserAdminResponse>> call, Response<ApiResponse<UserAdminResponse>> response) {
                progressBarSaveUser.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    UserAdminResponse user = response.body().getData();
                    if (user != null) {
                        etUserFullName.setText(user.getFullName());
                        etUserEmail.setText(user.getEmail());
                        etUserPhone.setText(user.getPhone());
                        etUserAddress.setText(user.getAddress());
                        switchUserActive.setChecked(user.isActive());

                        if ("ADMIN".equalsIgnoreCase(user.getRole())) {
                            spinnerUserRole.setSelection(1);
                        } else {
                            spinnerUserRole.setSelection(0);
                        }
                    }
                } else {
                    Toast.makeText(AddEditUserActivity.this, "Failed to fetch user details", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<UserAdminResponse>> call, Throwable t) {
                progressBarSaveUser.setVisibility(View.GONE);
                Toast.makeText(AddEditUserActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void saveUser() {
        String name = etUserFullName.getText().toString().trim();
        String email = etUserEmail.getText().toString().trim();
        String phone = etUserPhone.getText().toString().trim();
        String address = etUserAddress.getText().toString().trim();
        String role = spinnerUserRole.getSelectedItem().toString();

        if (name.isEmpty()) {
            etUserFullName.setError("Full name is required");
            return;
        }
        if (email.isEmpty()) {
            etUserEmail.setError("Email is required");
            return;
        }
        if (phone.isEmpty()) {
            etUserPhone.setError("Phone number is required");
            return;
        }

        progressBarSaveUser.setVisibility(View.VISIBLE);
        btnSaveUser.setEnabled(false);

        if (isEditMode) {
            boolean active = switchUserActive.isChecked();
            UserUpdateRequest request = new UserUpdateRequest(name, email, phone, role, active, address);

            RetrofitClient.getInstance(this).getApi().updateUser(userId, request).enqueue(new Callback<ApiResponse<UserAdminResponse>>() {
                @Override
                public void onResponse(Call<ApiResponse<UserAdminResponse>> call, Response<ApiResponse<UserAdminResponse>> response) {
                    progressBarSaveUser.setVisibility(View.GONE);
                    btnSaveUser.setEnabled(true);
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        Toast.makeText(AddEditUserActivity.this, "User updated successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        String msg = "Update failed";
                        if (response.body() != null && response.body().getMessage() != null) {
                            msg = response.body().getMessage();
                        }
                        Toast.makeText(AddEditUserActivity.this, msg, Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<UserAdminResponse>> call, Throwable t) {
                    progressBarSaveUser.setVisibility(View.GONE);
                    btnSaveUser.setEnabled(true);
                    Toast.makeText(AddEditUserActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            String password = etUserPassword.getText().toString().trim();
            if (password.isEmpty() || password.length() < 6) {
                progressBarSaveUser.setVisibility(View.GONE);
                btnSaveUser.setEnabled(true);
                etUserPassword.setError("Password must be at least 6 characters");
                return;
            }

            UserCreateRequest request = new UserCreateRequest(name, email, phone, password, role, address);

            RetrofitClient.getInstance(this).getApi().createUser(request).enqueue(new Callback<ApiResponse<UserAdminResponse>>() {
                @Override
                public void onResponse(Call<ApiResponse<UserAdminResponse>> call, Response<ApiResponse<UserAdminResponse>> response) {
                    progressBarSaveUser.setVisibility(View.GONE);
                    btnSaveUser.setEnabled(true);
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        Toast.makeText(AddEditUserActivity.this, "User created successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        String msg = "Creation failed";
                        if (response.body() != null && response.body().getMessage() != null) {
                            msg = response.body().getMessage();
                        }
                        Toast.makeText(AddEditUserActivity.this, msg, Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<UserAdminResponse>> call, Throwable t) {
                    progressBarSaveUser.setVisibility(View.GONE);
                    btnSaveUser.setEnabled(true);
                    Toast.makeText(AddEditUserActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}

package com.smarturban.app;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.smarturban.app.api.RetrofitClient;
import com.smarturban.app.model.ApiResponse;
import com.smarturban.app.model.UpdateUserRequest;
import com.smarturban.app.model.User;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.List;

public class EditUserActivity extends AppCompatActivity {

    private ImageButton btnBackEditUser;
    private EditText etEditFullName, etEditEmail, etEditPhone, etEditAddress;
    private Spinner spinnerEditRole;
    private CheckBox cbEditEnabled;
    private ProgressBar progressBarEditUser;
    private MaterialButton btnSubmitEditUser;

    private Long userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user);

        userId = getIntent().getLongExtra("user_id", -1);
        if (userId == -1) {
            Toast.makeText(this, "Invalid User ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();

        btnBackEditUser.setOnClickListener(v -> finish());

        List<String> roles = new ArrayList<>();
        roles.add("USER");
        roles.add("ADMIN");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, roles);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEditRole.setAdapter(adapter);

        btnSubmitEditUser.setOnClickListener(v -> saveUserChanges());

        fetchUserData();
    }

    private void initViews() {
        btnBackEditUser = findViewById(R.id.btnBackEditUser);
        etEditFullName = findViewById(R.id.etEditFullName);
        etEditEmail = findViewById(R.id.etEditEmail);
        etEditPhone = findViewById(R.id.etEditPhone);
        etEditAddress = findViewById(R.id.etEditAddress);
        spinnerEditRole = findViewById(R.id.spinnerEditRole);
        cbEditEnabled = findViewById(R.id.cbEditEnabled);
        progressBarEditUser = findViewById(R.id.progressBarEditUser);
        btnSubmitEditUser = findViewById(R.id.btnSubmitEditUser);
    }

    private void fetchUserData() {
        progressBarEditUser.setVisibility(View.VISIBLE);
        RetrofitClient.getInstance(this).getApi().getAdminUserById(userId)
                .enqueue(new Callback<ApiResponse<User>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                        progressBarEditUser.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            User user = response.body().getData();
                            if (user != null) {
                                etEditFullName.setText(user.getFullName());
                                etEditEmail.setText(user.getEmail());
                                etEditPhone.setText(user.getPhone() != null ? user.getPhone() : "");
                                etEditAddress.setText(user.getAddress() != null ? user.getAddress() : "");
                                cbEditEnabled.setChecked(user.isEnabled());

                                if ("ADMIN".equalsIgnoreCase(user.getRole())) {
                                    spinnerEditRole.setSelection(1);
                                } else {
                                    spinnerEditRole.setSelection(0);
                                }
                            }
                        } else {
                            Toast.makeText(EditUserActivity.this, "Failed to fetch user info", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                        progressBarEditUser.setVisibility(View.GONE);
                        Toast.makeText(EditUserActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveUserChanges() {
        String fullName = etEditFullName.getText().toString().trim();
        String email = etEditEmail.getText().toString().trim();
        String phone = etEditPhone.getText().toString().trim();
        String address = etEditAddress.getText().toString().trim();
        String role = (String) spinnerEditRole.getSelectedItem();
        boolean enabled = cbEditEnabled.isChecked();

        if (fullName.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBarEditUser.setVisibility(View.VISIBLE);
        btnSubmitEditUser.setEnabled(false);

        UpdateUserRequest request = new UpdateUserRequest(fullName, email, phone, address, role, enabled);

        RetrofitClient.getInstance(this).getApi().updateAdminUser(userId, request)
                .enqueue(new Callback<ApiResponse<User>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                        progressBarEditUser.setVisibility(View.GONE);
                        btnSubmitEditUser.setEnabled(true);

                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            Toast.makeText(EditUserActivity.this, "User updated successfully", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            String err = "Failed to update user";
                            if (response.errorBody() != null) {
                                try {
                                    err = response.errorBody().string();
                                } catch (Exception ignored) {}
                            }
                            Toast.makeText(EditUserActivity.this, err, Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                        progressBarEditUser.setVisibility(View.GONE);
                        btnSubmitEditUser.setEnabled(true);
                        Toast.makeText(EditUserActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

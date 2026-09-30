package com.smarturban.app;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.smarturban.app.api.RetrofitClient;
import com.smarturban.app.model.ApiResponse;
import com.smarturban.app.model.CreateUserRequest;
import com.smarturban.app.model.User;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.List;

public class AddUserActivity extends AppCompatActivity {

    private ImageButton btnBackAddUser;
    private EditText etAddFullName, etAddEmail, etAddPhone, etAddPassword, etAddAddress;
    private Spinner spinnerAddRole;
    private CheckBox cbAddEnabled;
    private ProgressBar progressBarAddUser;
    private MaterialButton btnSubmitAddUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user);

        initViews();

        btnBackAddUser.setOnClickListener(v -> finish());

        List<String> roles = new ArrayList<>();
        roles.add("USER");
        roles.add("ADMIN");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, roles);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAddRole.setAdapter(adapter);

        btnSubmitAddUser.setOnClickListener(v -> submitNewUser());
    }

    private void initViews() {
        btnBackAddUser = findViewById(R.id.btnBackAddUser);
        etAddFullName = findViewById(R.id.etAddFullName);
        etAddEmail = findViewById(R.id.etAddEmail);
        etAddPhone = findViewById(R.id.etAddPhone);
        etAddPassword = findViewById(R.id.etAddPassword);
        etAddAddress = findViewById(R.id.etAddAddress);
        spinnerAddRole = findViewById(R.id.spinnerAddRole);
        cbAddEnabled = findViewById(R.id.cbAddEnabled);
        progressBarAddUser = findViewById(R.id.progressBarAddUser);
        btnSubmitAddUser = findViewById(R.id.btnSubmitAddUser);
    }

    private void submitNewUser() {
        String fullName = etAddFullName.getText().toString().trim();
        String email = etAddEmail.getText().toString().trim();
        String phone = etAddPhone.getText().toString().trim();
        String password = etAddPassword.getText().toString().trim();
        String address = etAddAddress.getText().toString().trim();
        String role = (String) spinnerAddRole.getSelectedItem();
        boolean enabled = cbAddEnabled.isChecked();

        if (fullName.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBarAddUser.setVisibility(View.VISIBLE);
        btnSubmitAddUser.setEnabled(false);

        CreateUserRequest request = new CreateUserRequest(fullName, email, phone, password, address, role, enabled);

        RetrofitClient.getInstance(this).getApi().createAdminUser(request)
                .enqueue(new Callback<ApiResponse<User>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                        progressBarAddUser.setVisibility(View.GONE);
                        btnSubmitAddUser.setEnabled(true);

                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            Toast.makeText(AddUserActivity.this, "User created successfully", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            String err = "Failed to create user";
                            if (response.errorBody() != null) {
                                try {
                                    err = response.errorBody().string();
                                } catch (Exception ignored) {}
                            }
                            Toast.makeText(AddUserActivity.this, err, Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                        progressBarAddUser.setVisibility(View.GONE);
                        btnSubmitAddUser.setEnabled(true);
                        Toast.makeText(AddUserActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

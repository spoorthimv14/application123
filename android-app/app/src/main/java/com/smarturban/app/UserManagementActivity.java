package com.smarturban.app;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.smarturban.app.api.RetrofitClient;
import com.smarturban.app.model.ApiResponse;
import com.smarturban.app.model.UserAdminResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.List;

public class UserManagementActivity extends AppCompatActivity {

    private ImageButton btnUserMgmtBack;
    private Button btnAddUser;
    private EditText etUserSearch;
    private Spinner spinnerRoleFilter, spinnerActiveFilter;
    private TextView tvUserCount, tvUsersEmpty;
    private ProgressBar progressBarUsers;
    private RecyclerView recyclerViewUsers;

    private UserAdapter adapter;
    private final List<UserAdminResponse> userList = new ArrayList<>();

    private String currentSearch = null;
    private String currentRoleFilter = null;
    private Boolean currentActiveFilter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_management);

        initViews();
        setupRecyclerView();
        setupFilterSpinners();
        setupSearchInput();

        btnUserMgmtBack.setOnClickListener(v -> finish());
        btnAddUser.setOnClickListener(v -> {
            Intent intent = new Intent(UserManagementActivity.this, AddEditUserActivity.class);
            startActivity(intent);
        });

        fetchUsers();
    }

    private void initViews() {
        btnUserMgmtBack = findViewById(R.id.btnUserMgmtBack);
        btnAddUser = findViewById(R.id.btnAddUser);
        etUserSearch = findViewById(R.id.etUserSearch);
        spinnerRoleFilter = findViewById(R.id.spinnerRoleFilter);
        spinnerActiveFilter = findViewById(R.id.spinnerActiveFilter);
        tvUserCount = findViewById(R.id.tvUserCount);
        tvUsersEmpty = findViewById(R.id.tvUsersEmpty);
        progressBarUsers = findViewById(R.id.progressBarUsers);
        recyclerViewUsers = findViewById(R.id.recyclerViewUsers);
    }

    private void setupRecyclerView() {
        recyclerViewUsers.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UserAdapter(this, userList, user -> {
            Intent intent = new Intent(UserManagementActivity.this, UserDetailsActivity.class);
            intent.putExtra("user_id", user.getId());
            startActivity(intent);
        });
        recyclerViewUsers.setAdapter(adapter);
    }

    private void setupFilterSpinners() {
        // Role Spinner
        List<String> roleOptions = new ArrayList<>();
        roleOptions.add("All Roles");
        roleOptions.add("USER");
        roleOptions.add("ADMIN");

        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, roleOptions);
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRoleFilter.setAdapter(roleAdapter);

        spinnerRoleFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = roleOptions.get(position);
                if ("USER".equalsIgnoreCase(selected)) {
                    currentRoleFilter = "USER";
                } else if ("ADMIN".equalsIgnoreCase(selected)) {
                    currentRoleFilter = "ADMIN";
                } else {
                    currentRoleFilter = null;
                }
                fetchUsers();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Active Status Spinner
        List<String> activeOptions = new ArrayList<>();
        activeOptions.add("All Statuses");
        activeOptions.add("Active");
        activeOptions.add("Inactive");

        ArrayAdapter<String> activeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, activeOptions);
        activeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerActiveFilter.setAdapter(activeAdapter);

        spinnerActiveFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = activeOptions.get(position);
                if ("Active".equalsIgnoreCase(selected)) {
                    currentActiveFilter = true;
                } else if ("Inactive".equalsIgnoreCase(selected)) {
                    currentActiveFilter = false;
                } else {
                    currentActiveFilter = null;
                }
                fetchUsers();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupSearchInput() {
        etUserSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearch = s.toString().trim();
                if (currentSearch.isEmpty()) {
                    currentSearch = null;
                }
                fetchUsers();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void fetchUsers() {
        progressBarUsers.setVisibility(View.VISIBLE);
        RetrofitClient.getInstance(this).getApi()
                .getUsers(currentSearch, currentRoleFilter, currentActiveFilter)
                .enqueue(new Callback<ApiResponse<List<UserAdminResponse>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<UserAdminResponse>>> call, Response<ApiResponse<List<UserAdminResponse>>> response) {
                        progressBarUsers.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            List<UserAdminResponse> fetched = response.body().getData();
                            userList.clear();
                            if (fetched != null && !fetched.isEmpty()) {
                                userList.addAll(fetched);
                                tvUsersEmpty.setVisibility(View.GONE);
                                recyclerViewUsers.setVisibility(View.VISIBLE);
                            } else {
                                tvUsersEmpty.setVisibility(View.VISIBLE);
                                recyclerViewUsers.setVisibility(View.GONE);
                            }
                            tvUserCount.setText("Total Users: " + userList.size());
                            adapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(UserManagementActivity.this, "Failed to load users", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<UserAdminResponse>>> call, Throwable t) {
                        progressBarUsers.setVisibility(View.GONE);
                        Toast.makeText(UserManagementActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchUsers();
    }
}

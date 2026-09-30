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
import com.smarturban.app.model.UserResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.List;

public class AdminUserListActivity extends AppCompatActivity {

    private ImageButton btnBack, btnAddUser;
    private EditText etUserSearch;
    private Spinner spinnerRoleFilter, spinnerStatusFilter;
    private ProgressBar progressBarUsers;
    private TextView tvUsersEmpty;
    private RecyclerView recyclerViewUsers;

    private UserAdapter adapter;
    private List<UserResponse> userList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_user_list);

        initViews();

        btnBack.setOnClickListener(v -> finish());
        btnAddUser.setOnClickListener(v -> {
            Intent intent = new Intent(AdminUserListActivity.this, AdminAddUserActivity.class);
            startActivity(intent);
        });

        setupRecyclerView();
        setupFilters();

        fetchUsers();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnAddUser = findViewById(R.id.btnAddUser);
        etUserSearch = findViewById(R.id.etUserSearch);
        spinnerRoleFilter = findViewById(R.id.spinnerRoleFilter);
        spinnerStatusFilter = findViewById(R.id.spinnerStatusFilter);
        progressBarUsers = findViewById(R.id.progressBarUsers);
        tvUsersEmpty = findViewById(R.id.tvUsersEmpty);
        recyclerViewUsers = findViewById(R.id.recyclerViewUsers);
    }

    private void setupRecyclerView() {
        recyclerViewUsers.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UserAdapter(this, userList, user -> {
            Intent intent = new Intent(AdminUserListActivity.this, AdminUserDetailActivity.class);
            intent.putExtra("user_id", user.getUserId());
            startActivity(intent);
        });
        recyclerViewUsers.setAdapter(adapter);
    }

    private void setupFilters() {
        // Role Spinner Options
        List<String> roles = new ArrayList<>();
        roles.add("All Roles");
        roles.add("USER");
        roles.add("ADMIN");

        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, roles);
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRoleFilter.setAdapter(roleAdapter);

        // Status Spinner Options
        List<String> statuses = new ArrayList<>();
        statuses.add("All Statuses");
        statuses.add("Active");
        statuses.add("Inactive");

        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, statuses);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatusFilter.setAdapter(statusAdapter);

        AdapterView.OnItemSelectedListener filterListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                fetchUsers();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                fetchUsers();
            }
        };

        spinnerRoleFilter.setOnItemSelectedListener(filterListener);
        spinnerStatusFilter.setOnItemSelectedListener(filterListener);

        etUserSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                fetchUsers();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void fetchUsers() {
        String searchQuery = etUserSearch.getText().toString().trim();
        if (searchQuery.isEmpty()) {
            searchQuery = null;
        }

        String selectedRole = (String) spinnerRoleFilter.getSelectedItem();
        String roleParam = ("All Roles".equals(selectedRole)) ? null : selectedRole;

        String selectedStatus = (String) spinnerStatusFilter.getSelectedItem();
        Boolean statusParam = null;
        if ("Active".equals(selectedStatus)) {
            statusParam = true;
        } else if ("Inactive".equals(selectedStatus)) {
            statusParam = false;
        }

        progressBarUsers.setVisibility(View.VISIBLE);
        RetrofitClient.getInstance(this).getApi().getAdminUsers(searchQuery, roleParam, statusParam)
                .enqueue(new Callback<ApiResponse<List<UserResponse>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<UserResponse>>> call, Response<ApiResponse<List<UserResponse>>> response) {
                        progressBarUsers.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            List<UserResponse> fetched = response.body().getData();
                            userList.clear();
                            if (fetched != null && !fetched.isEmpty()) {
                                userList.addAll(fetched);
                                tvUsersEmpty.setVisibility(View.GONE);
                                recyclerViewUsers.setVisibility(View.VISIBLE);
                            } else {
                                tvUsersEmpty.setVisibility(View.VISIBLE);
                                recyclerViewUsers.setVisibility(View.GONE);
                            }
                            adapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(AdminUserListActivity.this, "Failed to load users.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<UserResponse>>> call, Throwable t) {
                        progressBarUsers.setVisibility(View.GONE);
                        Toast.makeText(AdminUserListActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchUsers();
    }
}

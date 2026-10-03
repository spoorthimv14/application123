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

import com.google.android.material.button.MaterialButton;
import com.smarturban.app.api.RetrofitClient;
import com.smarturban.app.model.ApiResponse;
import com.smarturban.app.model.UserResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.List;

public class UserListActivity extends AppCompatActivity {

    private ImageButton btnUserListBack, btnAddUserHeader;
    private EditText etUserSearch;
    private Spinner spinnerRoleFilter, spinnerStatusFilter;
    private ProgressBar progressBarUsers;
    private TextView tvUsersEmpty;
    private RecyclerView recyclerViewUsers;
    private MaterialButton btnCreateNewUser;

    private UserAdapter adapter;
    private List<UserResponse> userList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);

        initViews();

        btnUserListBack.setOnClickListener(v -> finish());

        View.OnClickListener openAddUserListener = v -> {
            Intent intent = new Intent(UserListActivity.this, AddUserActivity.class);
            startActivity(intent);
        };
        btnAddUserHeader.setOnClickListener(openAddUserListener);
        btnCreateNewUser.setOnClickListener(openAddUserListener);

        setupRecyclerView();
        setupFilters();
        setupSearch();
    }

    private void initViews() {
        btnUserListBack = findViewById(R.id.btnUserListBack);
        btnAddUserHeader = findViewById(R.id.btnAddUserHeader);
        etUserSearch = findViewById(R.id.etUserSearch);
        spinnerRoleFilter = findViewById(R.id.spinnerRoleFilter);
        spinnerStatusFilter = findViewById(R.id.spinnerStatusFilter);
        progressBarUsers = findViewById(R.id.progressBarUsers);
        tvUsersEmpty = findViewById(R.id.tvUsersEmpty);
        recyclerViewUsers = findViewById(R.id.recyclerViewUsers);
        btnCreateNewUser = findViewById(R.id.btnCreateNewUser);
    }

    private void setupRecyclerView() {
        recyclerViewUsers.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UserAdapter(this, userList, user -> {
            Intent intent = new Intent(UserListActivity.this, UserDetailsActivity.class);
            intent.putExtra("user_id", user.getUserId());
            startActivity(intent);
        });
        recyclerViewUsers.setAdapter(adapter);
    }

    private void setupFilters() {
        List<String> roleOptions = new ArrayList<>();
        roleOptions.add("All Roles");
        roleOptions.add("USER");
        roleOptions.add("ADMIN");

        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, roleOptions);
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRoleFilter.setAdapter(roleAdapter);

        List<String> statusOptions = new ArrayList<>();
        statusOptions.add("All Statuses");
        statusOptions.add("Active");
        statusOptions.add("Deactivated");

        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, statusOptions);
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
    }

    private void setupSearch() {
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
        String roleFilter = ("All Roles".equals(selectedRole)) ? null : selectedRole;

        String selectedStatus = (String) spinnerStatusFilter.getSelectedItem();
        Boolean enabledFilter = null;
        if ("Active".equalsIgnoreCase(selectedStatus)) {
            enabledFilter = true;
        } else if ("Deactivated".equalsIgnoreCase(selectedStatus)) {
            enabledFilter = false;
        }

        progressBarUsers.setVisibility(View.VISIBLE);

        RetrofitClient.getInstance(this).getApi().getAllUsers(searchQuery, roleFilter, enabledFilter)
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
                            Toast.makeText(UserListActivity.this, "Failed to load users", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<UserResponse>>> call, Throwable t) {
                        progressBarUsers.setVisibility(View.GONE);
                        Toast.makeText(UserListActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchUsers();
    }
}

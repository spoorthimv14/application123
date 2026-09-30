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

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.smarturban.app.api.RetrofitClient;
import com.smarturban.app.model.ApiResponse;
import com.smarturban.app.model.User;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.List;

public class UserListActivity extends AppCompatActivity {

    private ImageButton btnBackUsers, btnAddUserHeader;
    private EditText etUserSearch;
    private Spinner spinnerRoleFilter, spinnerStatusUserFilter;
    private ProgressBar progressBarUsers;
    private TextView tvUsersEmpty;
    private RecyclerView recyclerViewUsers;
    private FloatingActionButton fabAddUser;

    private UserAdapter adapter;
    private List<User> userList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);

        initViews();

        btnBackUsers.setOnClickListener(v -> finish());
        View.OnClickListener addListener = v -> {
            Intent intent = new Intent(UserListActivity.this, AddUserActivity.class);
            startActivity(intent);
        };
        btnAddUserHeader.setOnClickListener(addListener);
        fabAddUser.setOnClickListener(addListener);

        setupRecyclerView();
        setupFilters();
    }

    private void initViews() {
        btnBackUsers = findViewById(R.id.btnBackUsers);
        btnAddUserHeader = findViewById(R.id.btnAddUserHeader);
        etUserSearch = findViewById(R.id.etUserSearch);
        spinnerRoleFilter = findViewById(R.id.spinnerRoleFilter);
        spinnerStatusUserFilter = findViewById(R.id.spinnerStatusUserFilter);
        progressBarUsers = findViewById(R.id.progressBarUsers);
        tvUsersEmpty = findViewById(R.id.tvUsersEmpty);
        recyclerViewUsers = findViewById(R.id.recyclerViewUsers);
        fabAddUser = findViewById(R.id.fabAddUser);
    }

    private void setupRecyclerView() {
        recyclerViewUsers.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UserAdapter(this, userList, user -> {
            Intent intent = new Intent(UserListActivity.this, UserDetailActivity.class);
            intent.putExtra("user_id", user.getId());
            startActivity(intent);
        });
        recyclerViewUsers.setAdapter(adapter);
    }

    private void setupFilters() {
        // Role Spinner
        List<String> roles = new ArrayList<>();
        roles.add("All Roles");
        roles.add("ADMIN");
        roles.add("USER");

        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, roles);
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRoleFilter.setAdapter(roleAdapter);

        // Status Spinner
        List<String> statuses = new ArrayList<>();
        statuses.add("All Statuses");
        statuses.add("Active");
        statuses.add("Inactive");

        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, statuses);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatusUserFilter.setAdapter(statusAdapter);

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
        spinnerStatusUserFilter.setOnItemSelectedListener(filterListener);

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
        progressBarUsers.setVisibility(View.VISIBLE);

        String search = etUserSearch.getText().toString().trim();
        if (search.isEmpty()) search = null;

        String selectedRole = (String) spinnerRoleFilter.getSelectedItem();
        String roleParam = null;
        if ("ADMIN".equalsIgnoreCase(selectedRole) || "USER".equalsIgnoreCase(selectedRole)) {
            roleParam = selectedRole;
        }

        String selectedStatus = (String) spinnerStatusUserFilter.getSelectedItem();
        Boolean enabledParam = null;
        if ("Active".equalsIgnoreCase(selectedStatus)) {
            enabledParam = true;
        } else if ("Inactive".equalsIgnoreCase(selectedStatus)) {
            enabledParam = false;
        }

        RetrofitClient.getInstance(this).getApi().getAdminUsers(search, roleParam, enabledParam)
                .enqueue(new Callback<ApiResponse<List<User>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<User>>> call, Response<ApiResponse<List<User>>> response) {
                        progressBarUsers.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            List<User> fetched = response.body().getData();
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
                    public void onFailure(Call<ApiResponse<List<User>>> call, Throwable t) {
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

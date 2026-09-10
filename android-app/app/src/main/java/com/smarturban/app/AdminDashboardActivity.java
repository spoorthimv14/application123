package com.smarturban.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.smarturban.app.api.RetrofitClient;
import com.smarturban.app.model.ApiResponse;
import com.smarturban.app.model.Complaint;
import com.smarturban.app.model.ComplaintStats;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AdminDashboardActivity extends AppCompatActivity {

    private ImageButton btnAdminBack;
    private TextView tvAdminTotal, tvAdminPending, tvAdminAssigned, tvAdminInProgress, tvAdminResolved, tvAdminRejected;
    private Spinner spinnerStatusFilter;
    private ProgressBar progressBarAdmin;
    private TextView tvAdminEmpty;
    private RecyclerView recyclerViewAdminComplaints;

    private ComplaintAdapter adapter;
    private List<Complaint> complaintList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        initViews();

        btnAdminBack.setOnClickListener(v -> finish());

        setupRecyclerView();
        setupFilterSpinner();

        fetchAdminStats();
        fetchAdminComplaints(null);
    }

    private void initViews() {
        btnAdminBack = findViewById(R.id.btnAdminBack);
        tvAdminTotal = findViewById(R.id.tvAdminTotal);
        tvAdminPending = findViewById(R.id.tvAdminPending);
        tvAdminAssigned = findViewById(R.id.tvAdminAssigned);
        tvAdminInProgress = findViewById(R.id.tvAdminInProgress);
        tvAdminResolved = findViewById(R.id.tvAdminResolved);
        tvAdminRejected = findViewById(R.id.tvAdminRejected);
        spinnerStatusFilter = findViewById(R.id.spinnerStatusFilter);
        progressBarAdmin = findViewById(R.id.progressBarAdmin);
        tvAdminEmpty = findViewById(R.id.tvAdminEmpty);
        recyclerViewAdminComplaints = findViewById(R.id.recyclerViewAdminComplaints);
    }

    private void setupRecyclerView() {
        recyclerViewAdminComplaints.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ComplaintAdapter(this, complaintList, complaint -> {
            Intent intent = new Intent(AdminDashboardActivity.this, ComplaintDetailActivity.class);
            intent.putExtra("complaint_id", complaint.getId());
            intent.putExtra("is_admin_mode", true);
            startActivity(intent);
        });
        recyclerViewAdminComplaints.setAdapter(adapter);
    }

    private void setupFilterSpinner() {
        List<String> filters = Arrays.asList("All Statuses", "PENDING", "ASSIGNED", "IN_PROGRESS", "RESOLVED", "REJECTED");
        ArrayAdapter<String> filterAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, filters);
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatusFilter.setAdapter(filterAdapter);

        spinnerStatusFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = filters.get(position);
                if ("All Statuses".equalsIgnoreCase(selected)) {
                    fetchAdminComplaints(null);
                } else {
                    fetchAdminComplaints(selected);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                fetchAdminComplaints(null);
            }
        });
    }

    private void fetchAdminStats() {
        RetrofitClient.getInstance(this).getApi().getAdminStats().enqueue(new Callback<ApiResponse<ComplaintStats>>() {
            @Override
            public void onResponse(Call<ApiResponse<ComplaintStats>> call, Response<ApiResponse<ComplaintStats>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    ComplaintStats stats = response.body().getData();
                    if (stats != null) {
                        tvAdminTotal.setText(String.valueOf(stats.getTotal()));
                        tvAdminPending.setText(String.valueOf(stats.getPending()));
                        tvAdminAssigned.setText(String.valueOf(stats.getAssigned()));
                        tvAdminInProgress.setText(String.valueOf(stats.getInProgress()));
                        tvAdminResolved.setText(String.valueOf(stats.getResolved()));
                        tvAdminRejected.setText(String.valueOf(stats.getRejected()));
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<ComplaintStats>> call, Throwable t) {
                // Ignore silent error
            }
        });
    }

    private void fetchAdminComplaints(String statusFilter) {
        progressBarAdmin.setVisibility(View.VISIBLE);
        RetrofitClient.getInstance(this).getApi().getAdminComplaints(statusFilter).enqueue(new Callback<ApiResponse<List<Complaint>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Complaint>>> call, Response<ApiResponse<List<Complaint>>> response) {
                progressBarAdmin.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<Complaint> fetched = response.body().getData();
                    complaintList.clear();
                    if (fetched != null && !fetched.isEmpty()) {
                        complaintList.addAll(fetched);
                        tvAdminEmpty.setVisibility(View.GONE);
                        recyclerViewAdminComplaints.setVisibility(View.VISIBLE);
                    } else {
                        tvAdminEmpty.setVisibility(View.VISIBLE);
                        recyclerViewAdminComplaints.setVisibility(View.GONE);
                    }
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(AdminDashboardActivity.this, "Access denied or failed to load complaints.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Complaint>>> call, Throwable t) {
                progressBarAdmin.setVisibility(View.GONE);
                Toast.makeText(AdminDashboardActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchAdminStats();
        int pos = spinnerStatusFilter.getSelectedItemPosition();
        if (pos > 0) {
            String filter = (String) spinnerStatusFilter.getSelectedItem();
            fetchAdminComplaints(filter);
        } else {
            fetchAdminComplaints(null);
        }
    }
}

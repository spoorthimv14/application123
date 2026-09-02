package com.smarturban.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.smarturban.app.api.RetrofitClient;
import com.smarturban.app.model.ApiResponse;
import com.smarturban.app.model.AuthResponse;
import com.smarturban.app.model.Complaint;
import com.smarturban.app.model.ComplaintStats;
import com.smarturban.app.storage.TokenManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.List;

public class DashboardActivity extends AppCompatActivity {

    private TextView tvWelcomeUser;
    private TextView tvTotalCount, tvPendingCount, tvResolvedCount;
    private ImageButton btnLogout;
    private MaterialButton btnReportAction;
    private BottomNavigationView bottomNavigation;
    private RecyclerView recyclerViewRecentComplaints;
    private View cardEmptyRecent;
    private ProgressBar progressBarRecent;

    private ComplaintAdapter recentAdapter;
    private List<Complaint> recentComplaintList = new ArrayList<>();
    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        tokenManager = new TokenManager(this);

        if (!tokenManager.isLoggedIn()) {
            redirectToLogin();
            return;
        }

        initViews();
        setupRecentRecyclerView();

        String userName = tokenManager.getUserName();
        tvWelcomeUser.setText("Hello, " + userName + " 👋");

        btnLogout.setOnClickListener(v -> performLogout());

        btnReportAction.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, ReportComplaintActivity.class);
            startActivity(intent);
        });

        setupBottomNavigation();
        fetchUserProfile();
        fetchDashboardData();
    }

    private void initViews() {
        tvWelcomeUser = findViewById(R.id.tvWelcomeUser);
        tvTotalCount = findViewById(R.id.tvTotalCount);
        tvPendingCount = findViewById(R.id.tvPendingCount);
        tvResolvedCount = findViewById(R.id.tvResolvedCount);
        btnLogout = findViewById(R.id.btnLogout);
        btnReportAction = findViewById(R.id.btnReportAction);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        recyclerViewRecentComplaints = findViewById(R.id.recyclerViewRecentComplaints);
        cardEmptyRecent = findViewById(R.id.cardEmptyRecent);
        progressBarRecent = findViewById(R.id.progressBarRecent);
    }

    private void setupRecentRecyclerView() {
        recyclerViewRecentComplaints.setLayoutManager(new LinearLayoutManager(this));
        recentAdapter = new ComplaintAdapter(this, recentComplaintList, complaint -> {
            Intent intent = new Intent(DashboardActivity.this, ComplaintDetailActivity.class);
            intent.putExtra("complaint_id", complaint.getId());
            startActivity(intent);
        });
        recyclerViewRecentComplaints.setAdapter(recentAdapter);
    }

    private void setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                return true;
            } else if (itemId == R.id.nav_report) {
                startActivity(new Intent(DashboardActivity.this, ReportComplaintActivity.class));
                return true;
            } else if (itemId == R.id.nav_complaints) {
                startActivity(new Intent(DashboardActivity.this, MyComplaintsActivity.class));
                return true;
            } else if (itemId == R.id.nav_notifications) {
                Toast.makeText(DashboardActivity.this, "No new notifications.", Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.nav_profile) {
                String name = tokenManager.getUserName();
                String email = tokenManager.getUserEmail();
                Toast.makeText(DashboardActivity.this, "Profile: " + name + " (" + email + ")", Toast.LENGTH_LONG).show();
                return true;
            }
            return false;
        });
    }

    private void fetchUserProfile() {
        RetrofitClient.getInstance(this).getApi().getCurrentUser().enqueue(new Callback<ApiResponse<AuthResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<AuthResponse>> call, Response<ApiResponse<AuthResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    AuthResponse auth = response.body().getData();
                    if (auth != null) {
                        if (auth.getToken() == null) {
                            auth.setToken(tokenManager.getToken());
                        }
                        tokenManager.saveSession(auth);
                        if (auth.getFullName() != null) {
                            tvWelcomeUser.setText("Hello, " + auth.getFullName() + " 👋");
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<AuthResponse>> call, Throwable t) {
                // Keep local user name from token manager if network fetch fails
            }
        });
    }

    private void fetchDashboardData() {
        // Fetch Complaint Statistics
        RetrofitClient.getInstance(this).getApi().getMyComplaintStats().enqueue(new Callback<ApiResponse<ComplaintStats>>() {
            @Override
            public void onResponse(Call<ApiResponse<ComplaintStats>> call, Response<ApiResponse<ComplaintStats>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    ComplaintStats stats = response.body().getData();
                    if (stats != null) {
                        tvTotalCount.setText(String.valueOf(stats.getTotal()));
                        tvPendingCount.setText(String.valueOf(stats.getPending()));
                        tvResolvedCount.setText(String.valueOf(stats.getResolved()));
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<ComplaintStats>> call, Throwable t) {
                // Ignore silent stats error
            }
        });

        // Fetch Recent Complaints
        if (progressBarRecent != null) progressBarRecent.setVisibility(View.VISIBLE);
        RetrofitClient.getInstance(this).getApi().getMyComplaints().enqueue(new Callback<ApiResponse<List<Complaint>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Complaint>>> call, Response<ApiResponse<List<Complaint>>> response) {
                if (progressBarRecent != null) progressBarRecent.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<Complaint> allComplaints = response.body().getData();
                    recentComplaintList.clear();
                    if (allComplaints != null && !allComplaints.isEmpty()) {
                        // Display top 3 recent complaints on home dashboard
                        int limit = Math.min(allComplaints.size(), 3);
                        recentComplaintList.addAll(allComplaints.subList(0, limit));
                        cardEmptyRecent.setVisibility(View.GONE);
                        recyclerViewRecentComplaints.setVisibility(View.VISIBLE);
                    } else {
                        cardEmptyRecent.setVisibility(View.VISIBLE);
                        recyclerViewRecentComplaints.setVisibility(View.GONE);
                    }
                    recentAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Complaint>>> call, Throwable t) {
                if (progressBarRecent != null) progressBarRecent.setVisibility(View.GONE);
            }
        });
    }

    private void performLogout() {
        tokenManager.clearSession();
        Toast.makeText(this, "Logged out successfully.", Toast.LENGTH_SHORT).show();
        redirectToLogin();
    }

    private void redirectToLogin() {
        Intent intent = new Intent(DashboardActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchUserProfile();
        fetchDashboardData();
    }
}

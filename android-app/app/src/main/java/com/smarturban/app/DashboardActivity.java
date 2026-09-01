package com.smarturban.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.smarturban.app.api.RetrofitClient;
import com.smarturban.app.model.ApiResponse;
import com.smarturban.app.model.ComplaintStats;
import com.smarturban.app.storage.TokenManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardActivity extends AppCompatActivity {

    private TextView tvWelcomeUser;
    private TextView tvTotalCount, tvPendingCount, tvResolvedCount;
    private ImageButton btnLogout;
    private MaterialButton btnReportAction;
    private BottomNavigationView bottomNavigation;
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

        String userName = tokenManager.getUserName();
        tvWelcomeUser.setText("Hello, " + userName + " 👋");

        btnLogout.setOnClickListener(v -> performLogout());

        btnReportAction.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, ReportComplaintActivity.class);
            startActivity(intent);
        });

        setupBottomNavigation();
        fetchComplaintStats();
    }

    private void initViews() {
        tvWelcomeUser = findViewById(R.id.tvWelcomeUser);
        tvTotalCount = findViewById(R.id.tvTotalCount);
        tvPendingCount = findViewById(R.id.tvPendingCount);
        tvResolvedCount = findViewById(R.id.tvResolvedCount);
        btnLogout = findViewById(R.id.btnLogout);
        btnReportAction = findViewById(R.id.btnReportAction);
        bottomNavigation = findViewById(R.id.bottomNavigation);
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
                Toast.makeText(DashboardActivity.this, "Notifications feature coming soon.", Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.nav_profile) {
                Toast.makeText(DashboardActivity.this, "Profile feature coming soon.", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });
    }

    private void fetchComplaintStats() {
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
                // Ignore silent stats error on home dashboard
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
        fetchComplaintStats();
    }
}

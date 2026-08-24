package com.smarturban.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.smarturban.app.storage.TokenManager;

public class DashboardActivity extends AppCompatActivity {

    private TextView tvWelcomeUser;
    private ImageButton btnLogout;
    private MaterialButton btnReportAction;
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

        tvWelcomeUser = findViewById(R.id.tvWelcomeUser);
        btnLogout = findViewById(R.id.btnLogout);
        btnReportAction = findViewById(R.id.btnReportAction);

        String userName = tokenManager.getUserName();
        tvWelcomeUser.setText("Hello, " + userName + " 👋");

        btnLogout.setOnClickListener(v -> performLogout());

        btnReportAction.setOnClickListener(v ->
            Toast.makeText(DashboardActivity.this, "Complaint submission will be available in Phase 3.", Toast.LENGTH_SHORT).show()
        );
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
}

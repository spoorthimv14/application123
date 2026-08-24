package com.smarturban.app;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;
import com.smarturban.app.storage.TokenManager;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 1500;
    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        tokenManager = new TokenManager(this);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent;
            if (tokenManager.isLoggedIn()) {
                intent = new Intent(SplashActivity.this, DashboardActivity.class);
            } else {
                intent = new Intent(SplashActivity.this, LoginActivity.class);
            }
            startActivity(intent);
            finish();
        }, SPLASH_DELAY);
    }
}

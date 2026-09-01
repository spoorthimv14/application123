package com.smarturban.app;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.smarturban.app.api.RetrofitClient;
import com.smarturban.app.model.ApiResponse;
import com.smarturban.app.model.Complaint;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ComplaintDetailActivity extends AppCompatActivity implements OnMapReadyCallback {

    private ImageButton btnBackDetail;
    private ProgressBar progressBarDetail;
    private LinearLayout layoutDetailContent;
    private TextView tvDetailNumber, tvDetailStatusBadge, tvDetailCategory, tvDetailTitle, tvDetailDescription;
    private TextView tvPhotoLabel, tvDetailCoordinates, tvDetailTimestamps;
    private ImageView imgDetailPhoto;
    private MapView detailMapView;

    private GoogleMap googleMap;
    private Complaint currentComplaint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complaint_detail);

        initViews();

        btnBackDetail.setOnClickListener(v -> finish());

        detailMapView.onCreate(savedInstanceState);
        detailMapView.getMapAsync(this);

        long complaintId = getIntent().getLongExtra("complaint_id", -1);
        if (complaintId != -1) {
            fetchComplaintDetails(complaintId);
        } else {
            Toast.makeText(this, "Invalid complaint ID", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initViews() {
        btnBackDetail = findViewById(R.id.btnBackDetail);
        progressBarDetail = findViewById(R.id.progressBarDetail);
        layoutDetailContent = findViewById(R.id.layoutDetailContent);
        tvDetailNumber = findViewById(R.id.tvDetailNumber);
        tvDetailStatusBadge = findViewById(R.id.tvDetailStatusBadge);
        tvDetailCategory = findViewById(R.id.tvDetailCategory);
        tvDetailTitle = findViewById(R.id.tvDetailTitle);
        tvDetailDescription = findViewById(R.id.tvDetailDescription);
        tvPhotoLabel = findViewById(R.id.tvPhotoLabel);
        tvDetailCoordinates = findViewById(R.id.tvDetailCoordinates);
        tvDetailTimestamps = findViewById(R.id.tvDetailTimestamps);
        imgDetailPhoto = findViewById(R.id.imgDetailPhoto);
        detailMapView = findViewById(R.id.detailMapView);
    }

    private void fetchComplaintDetails(long id) {
        progressBarDetail.setVisibility(View.VISIBLE);
        RetrofitClient.getInstance(this).getApi().getComplaintById(id).enqueue(new Callback<ApiResponse<Complaint>>() {
            @Override
            public void onResponse(Call<ApiResponse<Complaint>> call, Response<ApiResponse<Complaint>> response) {
                progressBarDetail.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    currentComplaint = response.body().getData();
                    bindComplaintData();
                } else {
                    Toast.makeText(ComplaintDetailActivity.this, "Unable to fetch complaint details", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Complaint>> call, Throwable t) {
                progressBarDetail.setVisibility(View.GONE);
                Toast.makeText(ComplaintDetailActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void bindComplaintData() {
        if (currentComplaint == null) return;

        layoutDetailContent.setVisibility(View.VISIBLE);
        tvDetailNumber.setText(currentComplaint.getComplaintNumber());
        tvDetailCategory.setText(currentComplaint.getCategory());
        tvDetailTitle.setText(currentComplaint.getTitle());
        tvDetailDescription.setText(currentComplaint.getDescription());

        String status = currentComplaint.getStatus() != null ? currentComplaint.getStatus() : "PENDING";
        tvDetailStatusBadge.setText(status);

        switch (status) {
            case "PENDING":
                tvDetailStatusBadge.setBackgroundColor(Color.parseColor("#F59E0B"));
                break;
            case "IN_PROGRESS":
                tvDetailStatusBadge.setBackgroundColor(Color.parseColor("#3B82F6"));
                break;
            case "RESOLVED":
                tvDetailStatusBadge.setBackgroundColor(Color.parseColor("#10B981"));
                break;
            case "REJECTED":
                tvDetailStatusBadge.setBackgroundColor(Color.parseColor("#EF4444"));
                break;
            default:
                tvDetailStatusBadge.setBackgroundColor(Color.parseColor("#6B7280"));
                break;
        }

        if (currentComplaint.getLatitude() != null && currentComplaint.getLongitude() != null) {
            tvDetailCoordinates.setText(String.format("Latitude: %.6f, Longitude: %.6f",
                    currentComplaint.getLatitude(), currentComplaint.getLongitude()));
            updateMapLocation();
        }

        String createdAtStr = currentComplaint.getCreatedAt() != null ? currentComplaint.getCreatedAt().replace("T", " ") : "";
        tvDetailTimestamps.setText("Submitted on: " + createdAtStr);
    }

    private void updateMapLocation() {
        if (googleMap != null && currentComplaint != null &&
                currentComplaint.getLatitude() != null && currentComplaint.getLongitude() != null) {
            LatLng pos = new LatLng(currentComplaint.getLatitude(), currentComplaint.getLongitude());
            googleMap.clear();
            googleMap.addMarker(new MarkerOptions().position(pos).title(currentComplaint.getComplaintNumber()));
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 15f));
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        updateMapLocation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        detailMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        detailMapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        detailMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        detailMapView.onLowMemory();
    }
}

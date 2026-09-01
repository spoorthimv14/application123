package com.smarturban.app;

import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.smarturban.app.api.RetrofitClient;
import com.smarturban.app.model.ApiResponse;
import com.smarturban.app.model.Complaint;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ComplaintDetailActivity extends AppCompatActivity {

    private ImageButton btnBackDetail;
    private ProgressBar progressBarDetail;
    private LinearLayout layoutDetailContent;
    private TextView tvDetailNumber, tvDetailStatusBadge, tvDetailCategory, tvDetailTitle, tvDetailDescription;
    private TextView tvPhotoLabel, tvDetailCoordinates, tvDetailTimestamps;
    private ImageView imgDetailPhoto;
    private MapView detailMapView;

    private Complaint currentComplaint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Configuration.getInstance().load(getApplicationContext(), PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));

        setContentView(R.layout.activity_complaint_detail);

        initViews();

        btnBackDetail.setOnClickListener(v -> finish());

        setupMap();

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

    private void setupMap() {
        detailMapView.setTileSource(TileSourceFactory.MAPNIK);
        detailMapView.setMultiTouchControls(true);
        detailMapView.getController().setZoom(15.0);
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
        if (currentComplaint != null && currentComplaint.getLatitude() != null && currentComplaint.getLongitude() != null) {
            GeoPoint point = new GeoPoint(currentComplaint.getLatitude(), currentComplaint.getLongitude());
            detailMapView.getOverlays().clear();

            Marker marker = new Marker(detailMapView);
            marker.setPosition(point);
            marker.setTitle(currentComplaint.getComplaintNumber() + " (" + currentComplaint.getCategory() + ")");
            detailMapView.getOverlays().add(marker);

            detailMapView.getController().setCenter(point);
            detailMapView.invalidate();
        }
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
}

package com.smarturban.app;

import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.smarturban.app.api.RetrofitClient;
import com.smarturban.app.model.*;
import com.smarturban.app.storage.TokenManager;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ComplaintDetailActivity extends AppCompatActivity {

    private ImageButton btnBackDetail;
    private ProgressBar progressBarDetail;
    private LinearLayout layoutDetailContent;
    private TextView tvDetailNumber, tvDetailStatusBadge, tvDetailDepartment, tvDetailCategory, tvDetailTitle, tvDetailDescription;
    private TextView tvPhotoLabel, tvDetailCoordinates, tvDetailTimestamps;
    private ImageView imgDetailPhoto;
    private MapView detailMapView;
    private RecyclerView recyclerViewStatusHistory;

    // Admin Controls
    private MaterialCardView cardAdminControls;
    private Spinner spinnerDepartments, spinnerUpdateStatus;
    private TextInputEditText etStatusRemarks;
    private MaterialButton btnAssignDept, btnUpdateStatusAction;

    private StatusHistoryAdapter historyAdapter;
    private List<ComplaintStatusHistory> historyList = new ArrayList<>();
    private List<Department> departmentList = new ArrayList<>();

    private Complaint currentComplaint;
    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Configuration.getInstance().load(getApplicationContext(), PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));

        setContentView(R.layout.activity_complaint_detail);

        tokenManager = new TokenManager(this);

        initViews();

        btnBackDetail.setOnClickListener(v -> finish());

        setupMap();
        setupHistoryRecyclerView();

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
        tvDetailDepartment = findViewById(R.id.tvDetailDepartment);
        tvDetailCategory = findViewById(R.id.tvDetailCategory);
        tvDetailTitle = findViewById(R.id.tvDetailTitle);
        tvDetailDescription = findViewById(R.id.tvDetailDescription);
        tvPhotoLabel = findViewById(R.id.tvPhotoLabel);
        tvDetailCoordinates = findViewById(R.id.tvDetailCoordinates);
        tvDetailTimestamps = findViewById(R.id.tvDetailTimestamps);
        imgDetailPhoto = findViewById(R.id.imgDetailPhoto);
        detailMapView = findViewById(R.id.detailMapView);
        recyclerViewStatusHistory = findViewById(R.id.recyclerViewStatusHistory);

        cardAdminControls = findViewById(R.id.cardAdminControls);
        spinnerDepartments = findViewById(R.id.spinnerDepartments);
        spinnerUpdateStatus = findViewById(R.id.spinnerUpdateStatus);
        etStatusRemarks = findViewById(R.id.etStatusRemarks);
        btnAssignDept = findViewById(R.id.btnAssignDept);
        btnUpdateStatusAction = findViewById(R.id.btnUpdateStatusAction);
    }

    private void setupMap() {
        detailMapView.setTileSource(TileSourceFactory.MAPNIK);
        detailMapView.setMultiTouchControls(true);
        detailMapView.getController().setZoom(15.0);
    }

    private void setupHistoryRecyclerView() {
        recyclerViewStatusHistory.setLayoutManager(new LinearLayoutManager(this));
        historyAdapter = new StatusHistoryAdapter(this, historyList);
        recyclerViewStatusHistory.setAdapter(historyAdapter);
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
            case "ASSIGNED":
                tvDetailStatusBadge.setBackgroundColor(Color.parseColor("#8B5CF6"));
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

        if (currentComplaint.getDepartmentName() != null && !currentComplaint.getDepartmentName().isEmpty()) {
            tvDetailDepartment.setVisibility(View.VISIBLE);
            tvDetailDepartment.setText("Assigned: " + currentComplaint.getDepartmentName());
        } else {
            tvDetailDepartment.setVisibility(View.GONE);
        }

        if (currentComplaint.getLatitude() != null && currentComplaint.getLongitude() != null) {
            tvDetailCoordinates.setText(String.format("Latitude: %.6f, Longitude: %.6f",
                    currentComplaint.getLatitude(), currentComplaint.getLongitude()));
            updateMapLocation();
        }

        if (currentComplaint.getImagePath() != null && !currentComplaint.getImagePath().isEmpty()) {
            tvPhotoLabel.setVisibility(View.VISIBLE);
            imgDetailPhoto.setVisibility(View.VISIBLE);
            String imagePath = currentComplaint.getImagePath();
            String fullImageUrl = imagePath.startsWith("http") ? imagePath : RetrofitClient.getBaseUrl() + (imagePath.startsWith("/") ? imagePath.substring(1) : imagePath);
            Glide.with(this)
                    .load(fullImageUrl)
                    .centerCrop()
                    .placeholder(R.drawable.smarturban_logo)
                    .error(R.drawable.smarturban_logo)
                    .into(imgDetailPhoto);
        } else {
            tvPhotoLabel.setVisibility(View.GONE);
            imgDetailPhoto.setVisibility(View.GONE);
        }

        // Bind Status History Timeline
        historyList.clear();
        if (currentComplaint.getStatusHistory() != null) {
            historyList.addAll(currentComplaint.getStatusHistory());
        }
        historyAdapter.notifyDataSetChanged();

        String createdAtStr = currentComplaint.getCreatedAt() != null ? currentComplaint.getCreatedAt().replace("T", " ") : "";
        tvDetailTimestamps.setText("Submitted on: " + createdAtStr);

        setupAdminControlsIfAuthorized();
    }

    private void setupAdminControlsIfAuthorized() {
        boolean isAdmin = "ADMIN".equalsIgnoreCase(tokenManager.getUserRole());

        // Show admin controls if authenticated user has ADMIN role or is opened in admin mode
        if (isAdmin || getIntent().getBooleanExtra("is_admin_mode", false)) {
            cardAdminControls.setVisibility(View.VISIBLE);
            setupAdminSpinnersAndButtons();
        } else {
            cardAdminControls.setVisibility(View.GONE);
        }
    }

    private void setupAdminSpinnersAndButtons() {
        fetchStatusesAndSetupSpinner();
        fetchDepartments();

        btnAssignDept.setOnClickListener(v -> {
            int selectedPos = spinnerDepartments.getSelectedItemPosition();
            if (selectedPos >= 0 && selectedPos < departmentList.size()) {
                Department dept = departmentList.get(selectedPos);
                String remarks = etStatusRemarks.getText().toString().trim();
                assignDepartment(dept.getId(), remarks);
            } else {
                Toast.makeText(this, "Please select a department", Toast.LENGTH_SHORT).show();
            }
        });

        btnUpdateStatusAction.setOnClickListener(v -> {
            String selectedStatus = (String) spinnerUpdateStatus.getSelectedItem();
            String remarks = etStatusRemarks.getText().toString().trim();
            updateStatus(selectedStatus, remarks);
        });
    }

    private void fetchStatusesAndSetupSpinner() {
        List<String> statuses = new ArrayList<>();
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, statuses);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUpdateStatus.setAdapter(statusAdapter);

        RetrofitClient.getInstance(this).getApi().getStatuses().enqueue(new Callback<ApiResponse<List<String>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<String>>> call, Response<ApiResponse<List<String>>> response) {
                statuses.clear();
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess() && response.body().getData() != null) {
                    statuses.addAll(response.body().getData());
                } else {
                    statuses.addAll(Arrays.asList("PENDING", "ASSIGNED", "IN_PROGRESS", "RESOLVED", "REJECTED"));
                }
                statusAdapter.notifyDataSetChanged();
                if (currentComplaint != null && currentComplaint.getStatus() != null) {
                    int pos = statuses.indexOf(currentComplaint.getStatus());
                    if (pos >= 0) spinnerUpdateStatus.setSelection(pos);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<String>>> call, Throwable t) {
                statuses.clear();
                statuses.addAll(Arrays.asList("PENDING", "ASSIGNED", "IN_PROGRESS", "RESOLVED", "REJECTED"));
                statusAdapter.notifyDataSetChanged();
            }
        });
    }

    private void fetchDepartments() {
        RetrofitClient.getInstance(this).getApi().getDepartments().enqueue(new Callback<ApiResponse<List<Department>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Department>>> call, Response<ApiResponse<List<Department>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<Department> fetched = response.body().getData();
                    departmentList.clear();
                    List<String> names = new ArrayList<>();
                    if (fetched != null) {
                        departmentList.addAll(fetched);
                        for (Department d : fetched) {
                            names.add(d.getName() + " (" + d.getCode() + ")");
                        }
                    }
                    ArrayAdapter<String> deptAdapter = new ArrayAdapter<>(ComplaintDetailActivity.this, android.R.layout.simple_spinner_item, names);
                    deptAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerDepartments.setAdapter(deptAdapter);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Department>>> call, Throwable t) {
                Toast.makeText(ComplaintDetailActivity.this, "Failed to load departments", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void assignDepartment(Long deptId, String remarks) {
        if (currentComplaint == null) return;
        btnAssignDept.setEnabled(false);
        AssignDepartmentRequest req = new AssignDepartmentRequest(deptId, remarks);

        RetrofitClient.getInstance(this).getApi().assignDepartment(currentComplaint.getId(), req).enqueue(new Callback<ApiResponse<Complaint>>() {
            @Override
            public void onResponse(Call<ApiResponse<Complaint>> call, Response<ApiResponse<Complaint>> response) {
                btnAssignDept.setEnabled(true);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(ComplaintDetailActivity.this, "Department assigned successfully ✓", Toast.LENGTH_SHORT).show();
                    currentComplaint = response.body().getData();
                    bindComplaintData();
                } else {
                    Toast.makeText(ComplaintDetailActivity.this, "Assignment failed.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Complaint>> call, Throwable t) {
                btnAssignDept.setEnabled(true);
                Toast.makeText(ComplaintDetailActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateStatus(String newStatus, String remarks) {
        if (currentComplaint == null) return;
        btnUpdateStatusAction.setEnabled(false);
        StatusUpdateRequest req = new StatusUpdateRequest(newStatus, remarks);

        RetrofitClient.getInstance(this).getApi().updateComplaintStatus(currentComplaint.getId(), req).enqueue(new Callback<ApiResponse<Complaint>>() {
            @Override
            public void onResponse(Call<ApiResponse<Complaint>> call, Response<ApiResponse<Complaint>> response) {
                btnUpdateStatusAction.setEnabled(true);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(ComplaintDetailActivity.this, "Status updated successfully ✓", Toast.LENGTH_SHORT).show();
                    currentComplaint = response.body().getData();
                    bindComplaintData();
                } else {
                    Toast.makeText(ComplaintDetailActivity.this, "Status update failed.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Complaint>> call, Throwable t) {
                btnUpdateStatusAction.setEnabled(true);
                Toast.makeText(ComplaintDetailActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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

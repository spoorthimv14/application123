package com.smarturban.app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.smarturban.app.api.RetrofitClient;
import com.smarturban.app.model.ApiResponse;
import com.smarturban.app.model.Complaint;
import com.smarturban.app.model.ComplaintRequest;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ReportComplaintActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int PERMISSION_LOCATION_REQUEST_CODE = 1001;
    private static final int PERMISSION_CAMERA_REQUEST_CODE = 1002;
    private static final int REQUEST_CAMERA_CAPTURE = 2001;
    private static final int REQUEST_GALLERY_PICK = 2002;

    private Spinner spinnerCategory;
    private TextInputEditText etTitle, etDescription;
    private MaterialButton btnCamera, btnGallery, btnGetLocation, btnSubmit;
    private RelativeLayout layoutImagePreview;
    private ImageView imgPreview;
    private ImageButton btnRemoveImage, btnBack;
    private TextView tvLocationStatus, tvCoordinates;
    private FrameLayout mapContainer;
    private MapView mapView;
    private ProgressBar progressBarReport;

    private FusedLocationProviderClient fusedLocationClient;
    private GoogleMap googleMap;
    private Double selectedLatitude = null;
    private Double selectedLongitude = null;

    private byte[] imageBytes = null;
    private String selectedCategory = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_complaint);

        initViews();
        setupCategorySpinner();
        setupListeners();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        etTitle = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        btnCamera = findViewById(R.id.btnCamera);
        btnGallery = findViewById(R.id.btnGallery);
        layoutImagePreview = findViewById(R.id.layoutImagePreview);
        imgPreview = findViewById(R.id.imgPreview);
        btnRemoveImage = findViewById(R.id.btnRemoveImage);
        tvLocationStatus = findViewById(R.id.tvLocationStatus);
        tvCoordinates = findViewById(R.id.tvCoordinates);
        mapContainer = findViewById(R.id.mapContainer);
        mapView = findViewById(R.id.mapView);
        btnGetLocation = findViewById(R.id.btnGetLocation);
        btnSubmit = findViewById(R.id.btnSubmit);
        progressBarReport = findViewById(R.id.progressBarReport);
    }

    private void setupCategorySpinner() {
        List<String> categories = new ArrayList<>();
        categories.add("Select Category");
        categories.addAll(Arrays.asList(
                "Road/Pothole", "Garbage/Waste", "Street Light", "Water Supply",
                "Drainage", "Traffic", "Public Toilet", "Park", "Electricity", "Other"
        ));

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);

        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    selectedCategory = categories.get(position);
                } else {
                    selectedCategory = "";
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedCategory = "";
            }
        });
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnCamera.setOnClickListener(v -> checkCameraPermissionAndOpen());

        btnGallery.setOnClickListener(v -> openGallery());

        btnRemoveImage.setOnClickListener(v -> {
            imageBytes = null;
            imgPreview.setImageDrawable(null);
            layoutImagePreview.setVisibility(View.GONE);
        });

        btnGetLocation.setOnClickListener(v -> checkLocationPermissionAndGet());

        btnSubmit.setOnClickListener(v -> submitComplaint());
    }

    private void checkCameraPermissionAndOpen() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSION_CAMERA_REQUEST_CODE);
        } else {
            openCamera();
        }
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_CAMERA_CAPTURE);
        } else {
            Toast.makeText(this, "Camera not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_GALLERY_PICK);
    }

    private void checkLocationPermissionAndGet() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_LOCATION_REQUEST_CODE);
        } else {
            getCurrentLocation();
        }
    }

    private void getCurrentLocation() {
        tvLocationStatus.setText("Detecting location...");
        try {
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    updateLocationUI(location.getLatitude(), location.getLongitude());
                } else {
                    tvLocationStatus.setText("Unable to detect location. Please ensure GPS is enabled.");
                }
            }).addOnFailureListener(e -> tvLocationStatus.setText("Location error: " + e.getMessage()));
        } catch (SecurityException e) {
            tvLocationStatus.setText("Location permission denied");
        }
    }

    private void updateLocationUI(double lat, double lng) {
        selectedLatitude = lat;
        selectedLongitude = lng;
        tvLocationStatus.setText("Location detected ✓");
        tvCoordinates.setText(String.format("Lat: %.6f, Long: %.6f", lat, lng));
        mapContainer.setVisibility(View.VISIBLE);

        if (googleMap != null) {
            LatLng pos = new LatLng(lat, lng);
            googleMap.clear();
            googleMap.addMarker(new MarkerOptions().position(pos).title("Complaint Location"));
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 15f));
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        if (selectedLatitude != null && selectedLongitude != null) {
            updateLocationUI(selectedLatitude, selectedLongitude);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CAMERA_CAPTURE && data != null) {
                Bundle extras = data.getExtras();
                if (extras != null && extras.get("data") instanceof Bitmap) {
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    imgPreview.setImageBitmap(imageBitmap);
                    layoutImagePreview.setVisibility(View.VISIBLE);

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    imageBitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos);
                    imageBytes = baos.toByteArray();
                }
            } else if (requestCode == REQUEST_GALLERY_PICK && data != null && data.getData() != null) {
                Uri imageUri = data.getData();
                try {
                    imgPreview.setImageURI(imageUri);
                    layoutImagePreview.setVisibility(View.VISIBLE);

                    InputStream is = getContentResolver().openInputStream(imageUri);
                    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                    int nRead;
                    byte[] dataBytes = new byte[16384];
                    while ((nRead = is.read(dataBytes, 0, dataBytes.length)) != -1) {
                        buffer.write(dataBytes, 0, nRead);
                    }
                    imageBytes = buffer.toByteArray();
                } catch (Exception e) {
                    Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                tvLocationStatus.setText("Location permission denied. Cannot capture location.");
            }
        } else if (requestCode == PERMISSION_CAMERA_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Camera permission denied.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void submitComplaint() {
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        if (selectedCategory.isEmpty()) {
            Toast.makeText(this, "Please select a complaint category", Toast.LENGTH_SHORT).show();
            return;
        }

        if (title.isEmpty()) {
            etTitle.setError("Title is required");
            etTitle.requestFocus();
            return;
        }

        if (description.isEmpty()) {
            etDescription.setError("Description is required");
            etDescription.requestFocus();
            return;
        }

        if (selectedLatitude == null || selectedLongitude == null) {
            Toast.makeText(this, "Please capture location before submitting", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSubmit.setEnabled(false);
        progressBarReport.setVisibility(View.VISIBLE);

        ComplaintRequest requestObj = new ComplaintRequest(selectedCategory, title, description, selectedLatitude, selectedLongitude, "Detected Location");
        String jsonStr = new Gson().toJson(requestObj);
        RequestBody dataPart = RequestBody.create(MediaType.parse("application/json"), jsonStr);

        MultipartBody.Part imagePart = null;
        if (imageBytes != null && imageBytes.length > 0) {
            RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), imageBytes);
            imagePart = MultipartBody.Part.createFormData("image", "complaint.jpg", requestFile);
        }

        RetrofitClient.getInstance(this).getApi().createComplaint(dataPart, imagePart).enqueue(new Callback<ApiResponse<Complaint>>() {
            @Override
            public void onResponse(Call<ApiResponse<Complaint>> call, Response<ApiResponse<Complaint>> response) {
                progressBarReport.setVisibility(View.GONE);
                btnSubmit.setEnabled(true);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Complaint complaint = response.body().getData();
                    Toast.makeText(ReportComplaintActivity.this, "Complaint Submitted! #" + complaint.getComplaintNumber(), Toast.LENGTH_LONG).show();

                    Intent intent = new Intent(ReportComplaintActivity.this, ComplaintDetailActivity.class);
                    intent.putExtra("complaint_id", complaint.getId());
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(ReportComplaintActivity.this, "Submission failed. Please try again.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Complaint>> call, Throwable t) {
                progressBarReport.setVisibility(View.GONE);
                btnSubmit.setEnabled(true);
                Toast.makeText(ReportComplaintActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}

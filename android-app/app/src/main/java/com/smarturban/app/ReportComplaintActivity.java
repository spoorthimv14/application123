package com.smarturban.app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.smarturban.app.api.RetrofitClient;
import com.smarturban.app.model.ApiResponse;
import com.smarturban.app.model.Complaint;
import com.smarturban.app.model.ComplaintRequest;

import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;

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

public class ReportComplaintActivity extends AppCompatActivity {

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
    private LocationCallback locationCallback;
    private Marker currentMarker;

    private Double selectedLatitude = null;
    private Double selectedLongitude = null;

    private byte[] imageBytes = null;
    private String selectedCategory = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Configuration.getInstance().load(getApplicationContext(), PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));

        setContentView(R.layout.activity_report_complaint);

        initViews();
        setupCategorySpinner();
        setupListeners();
        setupMap();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
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

    private void setupMap() {
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(15.0);

        MapEventsOverlay mapEventsOverlay = new MapEventsOverlay(new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                updateLocationUI(p.getLatitude(), p.getLongitude(), "Selected location on map ✓");
                return true;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                return false;
            }
        });
        mapView.getOverlays().add(0, mapEventsOverlay);
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
            requestFreshLocation();
        }
    }

    private void requestFreshLocation() {
        tvLocationStatus.setText("Requesting fresh GPS location...");
        btnGetLocation.setEnabled(false);

        if (locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }

        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000)
                .setMinUpdateIntervalMillis(1000)
                .setMaxUpdates(1)
                .build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                btnGetLocation.setEnabled(true);
                if (locationResult.getLastLocation() != null) {
                    Location loc = locationResult.getLastLocation();
                    updateLocationUI(loc.getLatitude(), loc.getLongitude(), "Fresh location detected ✓");
                } else {
                    fallbackToLastLocation();
                }
            }
        };

        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                btnGetLocation.setEnabled(true);
                if (selectedLatitude == null) {
                    fallbackToLastLocation();
                }
            }, 10000);

        } catch (SecurityException e) {
            btnGetLocation.setEnabled(true);
            tvLocationStatus.setText("Location permission denied. Please grant permission.");
        }
    }

    private void fallbackToLastLocation() {
        try {
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    updateLocationUI(location.getLatitude(), location.getLongitude(), "Location detected ✓");
                } else {
                    tvLocationStatus.setText("Location unavailable. Please enable GPS and try again or tap the map.");
                    mapContainer.setVisibility(View.VISIBLE);
                }
            }).addOnFailureListener(e -> {
                tvLocationStatus.setText("GPS error: " + e.getMessage());
                mapContainer.setVisibility(View.VISIBLE);
            });
        } catch (SecurityException e) {
            tvLocationStatus.setText("Location permission denied.");
        }
    }

    private void updateLocationUI(double lat, double lng, String statusMsg) {
        selectedLatitude = lat;
        selectedLongitude = lng;
        tvLocationStatus.setText(statusMsg);
        tvCoordinates.setText(String.format("Lat: %.6f, Long: %.6f", lat, lng));
        mapContainer.setVisibility(View.VISIBLE);

        GeoPoint point = new GeoPoint(lat, lng);

        if (currentMarker != null) {
            mapView.getOverlays().remove(currentMarker);
        }

        currentMarker = new Marker(mapView);
        currentMarker.setPosition(point);
        currentMarker.setTitle("Selected Complaint Location");
        currentMarker.setDraggable(true);
        currentMarker.setOnMarkerDragListener(new Marker.OnMarkerDragListener() {
            @Override
            public void onMarkerDrag(Marker marker) {}

            @Override
            public void onMarkerDragEnd(Marker marker) {
                GeoPoint newPos = marker.getPosition();
                updateLocationUI(newPos.getLatitude(), newPos.getLongitude(), "Marker dragged to new location ✓");
            }

            @Override
            public void onMarkerDragStart(Marker marker) {}
        });

        mapView.getOverlays().add(currentMarker);
        mapView.getController().setCenter(point);
        mapView.invalidate();
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
                requestFreshLocation();
            } else {
                tvLocationStatus.setText("Location permission denied. Cannot auto-detect location.");
                mapContainer.setVisibility(View.VISIBLE);
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
            Toast.makeText(this, "Please select/capture complaint location before submitting", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(ReportComplaintActivity.this, "Submission failed. Please check image or details.", Toast.LENGTH_SHORT).show();
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
        if (locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }
}

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

import com.smarturban.app.api.RetrofitClient;
import com.smarturban.app.model.ApiResponse;
import com.smarturban.app.model.Complaint;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.List;

public class MyComplaintsActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private RecyclerView recyclerViewComplaints;
    private ProgressBar progressBarMyComplaints;
    private TextView tvEmptyState;

    private ComplaintAdapter adapter;
    private List<Complaint> complaintList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_complaints);

        btnBack = findViewById(R.id.btnBack);
        recyclerViewComplaints = findViewById(R.id.recyclerViewComplaints);
        progressBarMyComplaints = findViewById(R.id.progressBarMyComplaints);
        tvEmptyState = findViewById(R.id.tvEmptyState);

        btnBack.setOnClickListener(v -> finish());

        recyclerViewComplaints.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ComplaintAdapter(this, complaintList, complaint -> {
            Intent intent = new Intent(MyComplaintsActivity.this, ComplaintDetailActivity.class);
            intent.putExtra("complaint_id", complaint.getId());
            startActivity(intent);
        });
        recyclerViewComplaints.setAdapter(adapter);

        fetchMyComplaints();
    }

    private void fetchMyComplaints() {
        progressBarMyComplaints.setVisibility(View.VISIBLE);
        RetrofitClient.getInstance(this).getApi().getMyComplaints().enqueue(new Callback<ApiResponse<List<Complaint>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Complaint>>> call, Response<ApiResponse<List<Complaint>>> response) {
                progressBarMyComplaints.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<Complaint> fetched = response.body().getData();
                    complaintList.clear();
                    if (fetched != null && !fetched.isEmpty()) {
                        complaintList.addAll(fetched);
                        tvEmptyState.setVisibility(View.GONE);
                        recyclerViewComplaints.setVisibility(View.VISIBLE);
                    } else {
                        tvEmptyState.setVisibility(View.VISIBLE);
                        recyclerViewComplaints.setVisibility(View.GONE);
                    }
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(MyComplaintsActivity.this, "Failed to load complaints.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Complaint>>> call, Throwable t) {
                progressBarMyComplaints.setVisibility(View.GONE);
                Toast.makeText(MyComplaintsActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchMyComplaints();
    }
}

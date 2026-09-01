package com.smarturban.app;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.smarturban.app.model.Complaint;

import java.util.List;

public class ComplaintAdapter extends RecyclerView.Adapter<ComplaintAdapter.ComplaintViewHolder> {

    public interface OnComplaintClickListener {
        void onComplaintClick(Complaint complaint);
    }

    private final Context context;
    private final List<Complaint> complaintList;
    private final OnComplaintClickListener listener;

    public ComplaintAdapter(Context context, List<Complaint> complaintList, OnComplaintClickListener listener) {
        this.context = context;
        this.complaintList = complaintList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ComplaintViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_complaint, parent, false);
        return new ComplaintViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ComplaintViewHolder holder, int position) {
        Complaint complaint = complaintList.get(position);
        holder.tvComplaintNumber.setText(complaint.getComplaintNumber());
        holder.tvCategory.setText(complaint.getCategory());
        holder.tvTitle.setText(complaint.getTitle());

        String dateStr = complaint.getCreatedAt() != null ? complaint.getCreatedAt().split("T")[0] : "";
        holder.tvDate.setText("Created: " + dateStr);

        String status = complaint.getStatus() != null ? complaint.getStatus() : "PENDING";
        holder.tvStatusBadge.setText(status);

        switch (status) {
            case "PENDING":
                holder.tvStatusBadge.setBackgroundColor(Color.parseColor("#F59E0B"));
                break;
            case "IN_PROGRESS":
                holder.tvStatusBadge.setBackgroundColor(Color.parseColor("#3B82F6"));
                break;
            case "RESOLVED":
                holder.tvStatusBadge.setBackgroundColor(Color.parseColor("#10B981"));
                break;
            case "REJECTED":
                holder.tvStatusBadge.setBackgroundColor(Color.parseColor("#EF4444"));
                break;
            default:
                holder.tvStatusBadge.setBackgroundColor(Color.parseColor("#6B7280"));
                break;
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onComplaintClick(complaint);
            }
        });
    }

    @Override
    public int getItemCount() {
        return complaintList.size();
    }

    static class ComplaintViewHolder extends RecyclerView.ViewHolder {
        TextView tvComplaintNumber, tvStatusBadge, tvCategory, tvTitle, tvDate;

        public ComplaintViewHolder(@NonNull View itemView) {
            super(itemView);
            tvComplaintNumber = itemView.findViewById(R.id.tvComplaintNumber);
            tvStatusBadge = itemView.findViewById(R.id.tvStatusBadge);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDate = itemView.findViewById(R.id.tvDate);
        }
    }
}

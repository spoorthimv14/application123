package com.smarturban.app;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.smarturban.app.model.ComplaintStatusHistory;

import java.util.List;

public class StatusHistoryAdapter extends RecyclerView.Adapter<StatusHistoryAdapter.HistoryViewHolder> {

    private final Context context;
    private final List<ComplaintStatusHistory> historyList;

    public StatusHistoryAdapter(Context context, List<ComplaintStatusHistory> historyList) {
        this.context = context;
        this.historyList = historyList;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_status_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        ComplaintStatusHistory item = historyList.get(position);

        String status = item.getNewStatus() != null ? item.getNewStatus() : "PENDING";
        holder.tvHistoryStatusBadge.setText(status);

        switch (status) {
            case "PENDING":
                holder.tvHistoryStatusBadge.setBackgroundColor(Color.parseColor("#F59E0B"));
                break;
            case "ASSIGNED":
                holder.tvHistoryStatusBadge.setBackgroundColor(Color.parseColor("#8B5CF6"));
                break;
            case "IN_PROGRESS":
                holder.tvHistoryStatusBadge.setBackgroundColor(Color.parseColor("#3B82F6"));
                break;
            case "RESOLVED":
                holder.tvHistoryStatusBadge.setBackgroundColor(Color.parseColor("#10B981"));
                break;
            case "REJECTED":
                holder.tvHistoryStatusBadge.setBackgroundColor(Color.parseColor("#EF4444"));
                break;
            default:
                holder.tvHistoryStatusBadge.setBackgroundColor(Color.parseColor("#6B7280"));
                break;
        }

        String timeStr = item.getCreatedAt() != null ? item.getCreatedAt().replace("T", " ") : "";
        if (timeStr.contains(".")) {
            timeStr = timeStr.substring(0, timeStr.lastIndexOf("."));
        }
        holder.tvHistoryDate.setText(timeStr);

        holder.tvHistoryUpdatedBy.setText("Updated by: " + (item.getUpdatedBy() != null ? item.getUpdatedBy() : "System"));
        holder.tvHistoryRemarks.setText(item.getRemarks() != null ? item.getRemarks() : "Status updated");
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvHistoryStatusBadge, tvHistoryDate, tvHistoryUpdatedBy, tvHistoryRemarks;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHistoryStatusBadge = itemView.findViewById(R.id.tvHistoryStatusBadge);
            tvHistoryDate = itemView.findViewById(R.id.tvHistoryDate);
            tvHistoryUpdatedBy = itemView.findViewById(R.id.tvHistoryUpdatedBy);
            tvHistoryRemarks = itemView.findViewById(R.id.tvHistoryRemarks);
        }
    }
}

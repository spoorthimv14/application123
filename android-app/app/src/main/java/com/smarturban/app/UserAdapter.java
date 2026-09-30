package com.smarturban.app;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.smarturban.app.model.UserResponse;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    public interface OnUserClickListener {
        void onUserClick(UserResponse user);
    }

    private Context context;
    private List<UserResponse> userList;
    private OnUserClickListener listener;

    public UserAdapter(Context context, List<UserResponse> userList, OnUserClickListener listener) {
        this.context = context;
        this.userList = userList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        UserResponse user = userList.get(position);
        holder.tvUserName.setText(user.getFullName() != null ? user.getFullName() : "Unknown");
        holder.tvUserEmail.setText(user.getEmail() != null ? user.getEmail() : "");
        holder.tvUserPhone.setText(user.getPhone() != null ? user.getPhone() : "");

        String role = user.getRole() != null ? user.getRole() : "USER";
        holder.tvUserRoleBadge.setText(role);
        if ("ADMIN".equalsIgnoreCase(role)) {
            holder.tvUserRoleBadge.setBackgroundColor(Color.parseColor("#F97316")); // Accent Orange
        } else {
            holder.tvUserRoleBadge.setBackgroundColor(Color.parseColor("#0F2537")); // Deep Navy
        }

        if (user.isEnabled()) {
            holder.tvUserStatusBadge.setText("ACTIVE");
            holder.tvUserStatusBadge.setTextColor(Color.parseColor("#10B981")); // Green
        } else {
            holder.tvUserStatusBadge.setText("INACTIVE");
            holder.tvUserStatusBadge.setTextColor(Color.parseColor("#EF4444")); // Red
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onUserClick(user);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList != null ? userList.size() : 0;
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserName, tvUserRoleBadge, tvUserEmail, tvUserPhone, tvUserStatusBadge;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvUserRoleBadge = itemView.findViewById(R.id.tvUserRoleBadge);
            tvUserEmail = itemView.findViewById(R.id.tvUserEmail);
            tvUserPhone = itemView.findViewById(R.id.tvUserPhone);
            tvUserStatusBadge = itemView.findViewById(R.id.tvUserStatusBadge);
        }
    }
}

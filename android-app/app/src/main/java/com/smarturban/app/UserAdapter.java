package com.smarturban.app;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.smarturban.app.model.UserAdminResponse;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    public interface OnUserClickListener {
        void onUserClick(UserAdminResponse user);
    }

    private final Context context;
    private final List<UserAdminResponse> userList;
    private final OnUserClickListener listener;

    public UserAdapter(Context context, List<UserAdminResponse> userList, OnUserClickListener listener) {
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
        UserAdminResponse user = userList.get(position);

        holder.tvItemUserId.setText("#ID: " + user.getId());
        holder.tvItemUserName.setText(user.getFullName() != null ? user.getFullName() : "N/A");
        holder.tvItemUserEmail.setText(user.getEmail() != null ? user.getEmail() : "");
        holder.tvItemUserPhone.setText(user.getPhone() != null ? user.getPhone() : "");

        String role = user.getRole() != null ? user.getRole() : "USER";
        holder.tvItemUserRole.setText(role);
        if ("ADMIN".equalsIgnoreCase(role)) {
            holder.tvItemUserRole.setBackgroundColor(Color.parseColor("#DBEAFE"));
            holder.tvItemUserRole.setTextColor(Color.parseColor("#1E40AF"));
        } else {
            holder.tvItemUserRole.setBackgroundColor(Color.parseColor("#F3F4F6"));
            holder.tvItemUserRole.setTextColor(Color.parseColor("#374151"));
        }

        if (user.isActive()) {
            holder.tvItemUserStatus.setText("ACTIVE");
            holder.tvItemUserStatus.setBackgroundColor(Color.parseColor("#D1FAE5"));
            holder.tvItemUserStatus.setTextColor(Color.parseColor("#065F46"));
        } else {
            holder.tvItemUserStatus.setText("INACTIVE");
            holder.tvItemUserStatus.setBackgroundColor(Color.parseColor("#FEE2E2"));
            holder.tvItemUserStatus.setTextColor(Color.parseColor("#991B1B"));
        }

        String created = user.getCreatedAt();
        if (created != null && created.length() >= 10) {
            created = created.substring(0, 10);
        }
        holder.tvItemUserCreated.setText("Created: " + (created != null ? created : "N/A"));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onUserClick(user);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvItemUserId, tvItemUserName, tvItemUserEmail, tvItemUserPhone, tvItemUserRole, tvItemUserStatus, tvItemUserCreated;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvItemUserId = itemView.findViewById(R.id.tvItemUserId);
            tvItemUserName = itemView.findViewById(R.id.tvItemUserName);
            tvItemUserEmail = itemView.findViewById(R.id.tvItemUserEmail);
            tvItemUserPhone = itemView.findViewById(R.id.tvItemUserPhone);
            tvItemUserRole = itemView.findViewById(R.id.tvItemUserRole);
            tvItemUserStatus = itemView.findViewById(R.id.tvItemUserStatus);
            tvItemUserCreated = itemView.findViewById(R.id.tvItemUserCreated);
        }
    }
}

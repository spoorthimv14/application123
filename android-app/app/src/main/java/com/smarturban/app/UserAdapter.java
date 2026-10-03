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

        holder.tvItemUserName.setText(user.getFullName() != null ? user.getFullName() : "N/A");
        holder.tvItemUserEmail.setText(user.getEmail() != null ? user.getEmail() : "N/A");
        holder.tvItemUserPhone.setText(user.getPhone() != null ? user.getPhone() : "N/A");

        String role = user.getRole() != null ? user.getRole().toUpperCase() : "USER";
        holder.tvItemUserRole.setText(role);

        if (user.isEnabled()) {
            holder.tvItemUserStatus.setText("ACTIVE");
            holder.tvItemUserStatus.setTextColor(Color.parseColor("#10B981"));
        } else {
            holder.tvItemUserStatus.setText("DEACTIVATED");
            holder.tvItemUserStatus.setTextColor(Color.parseColor("#EF4444"));
        }

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

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvItemUserName, tvItemUserEmail, tvItemUserPhone, tvItemUserRole, tvItemUserStatus;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvItemUserName = itemView.findViewById(R.id.tvItemUserName);
            tvItemUserEmail = itemView.findViewById(R.id.tvItemUserEmail);
            tvItemUserPhone = itemView.findViewById(R.id.tvItemUserPhone);
            tvItemUserRole = itemView.findViewById(R.id.tvItemUserRole);
            tvItemUserStatus = itemView.findViewById(R.id.tvItemUserStatus);
        }
    }
}

package com.smarturban.app;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.smarturban.app.model.User;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    public interface OnUserClickListener {
        void onUserClick(User user);
    }

    private final Context context;
    private final List<User> userList;
    private final OnUserClickListener listener;

    public UserAdapter(Context context, List<User> userList, OnUserClickListener listener) {
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
        User user = userList.get(position);
        holder.tvUserName.setText(user.getFullName());
        holder.tvUserEmail.setText(user.getEmail());
        holder.tvUserPhone.setText(user.getPhone() != null ? user.getPhone() : "");
        holder.tvUserRole.setText(user.getRole() != null ? user.getRole() : "ROLE_USER");

        if (user.isEnabled()) {
            holder.tvUserStatus.setText("Active");
            holder.tvUserStatus.setTextColor(Color.parseColor("#10B981"));
        } else {
            holder.tvUserStatus.setText("Inactive");
            holder.tvUserStatus.setTextColor(Color.parseColor("#EF4444"));
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
        TextView tvUserName, tvUserRole, tvUserEmail, tvUserPhone, tvUserStatus;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvUserRole = itemView.findViewById(R.id.tvUserRole);
            tvUserEmail = itemView.findViewById(R.id.tvUserEmail);
            tvUserPhone = itemView.findViewById(R.id.tvUserPhone);
            tvUserStatus = itemView.findViewById(R.id.tvUserStatus);
        }
    }
}

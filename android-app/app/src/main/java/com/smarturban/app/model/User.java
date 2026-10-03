package com.smarturban.app.model;

import com.google.gson.annotations.SerializedName;

public class User {
    @SerializedName("userId")
    private Long userId;

    @SerializedName("id")
    private Long id;

    @SerializedName("fullName")
    private String fullName;

    @SerializedName("email")
    private String email;

    @SerializedName("phone")
    private String phone;

    @SerializedName("role")
    private String role;

    @SerializedName("address")
    private String address;

    @SerializedName("enabled")
    private boolean enabled = true;

    public User() {
    }

    public User(Long id, String fullName, String email, String phone, String role, String address, boolean enabled) {
        this.id = id;
        this.userId = id;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.address = address;
        this.enabled = enabled;
    }

    public Long getId() {
        return id != null ? id : userId;
    }

    public Long getUserId() {
        return userId != null ? userId : id;
    }

    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getRole() { return role; }
    public String getAddress() { return address; }
    public boolean isEnabled() { return enabled; }
}

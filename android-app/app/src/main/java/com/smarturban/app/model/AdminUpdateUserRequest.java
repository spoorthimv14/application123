package com.smarturban.app.model;

import com.google.gson.annotations.SerializedName;

public class AdminUpdateUserRequest {
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

    public AdminUpdateUserRequest(String fullName, String email, String phone, String role, String address) {
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.address = address;
    }

    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getRole() { return role; }
    public String getAddress() { return address; }
}

package com.smarturban.app.model;

import com.google.gson.annotations.SerializedName;

public class AdminCreateUserRequest {
    @SerializedName("fullName")
    private String fullName;

    @SerializedName("email")
    private String email;

    @SerializedName("phone")
    private String phone;

    @SerializedName("password")
    private String password;

    @SerializedName("role")
    private String role;

    @SerializedName("address")
    private String address;

    public AdminCreateUserRequest(String fullName, String email, String phone, String password, String role, String address) {
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.password = password;
        this.role = role;
        this.address = address;
    }

    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getPassword() { return password; }
    public String getRole() { return role; }
    public String getAddress() { return address; }
}

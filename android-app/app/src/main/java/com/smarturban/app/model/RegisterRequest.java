package com.smarturban.app.model;

import com.google.gson.annotations.SerializedName;

public class RegisterRequest {
    @SerializedName("fullName")
    private String fullName;

    @SerializedName("email")
    private String email;

    @SerializedName("phone")
    private String phone;

    @SerializedName("password")
    private String password;

    @SerializedName("confirmPassword")
    private String confirmPassword;

    @SerializedName("address")
    private String address;

    public RegisterRequest(String fullName, String email, String phone, String password, String confirmPassword, String address) {
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.password = password;
        this.confirmPassword = confirmPassword;
        this.address = address;
    }

    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getPassword() { return password; }
    public String getConfirmPassword() { return confirmPassword; }
    public String getAddress() { return address; }
}

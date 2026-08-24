package com.smarturban.dto;

public class AuthResponse {

    private String token;
    private Long userId;
    private String fullName;
    private String email;
    private String phone;
    private String role;
    private String address;

    public AuthResponse() {
    }

    public AuthResponse(String token, Long userId, String fullName, String email, String phone, String role, String address) {
        this.token = token;
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.address = address;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}

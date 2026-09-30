package com.smarturban.app.model;

public class UpdateUserRequest {
    private String fullName;
    private String email;
    private String phone;
    private String address;
    private String role;
    private Boolean enabled;

    public UpdateUserRequest() {
    }

    public UpdateUserRequest(String fullName, String email, String phone, String address, String role, Boolean enabled) {
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.role = role;
        this.enabled = enabled;
    }

    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getAddress() { return address; }
    public String getRole() { return role; }
    public Boolean getEnabled() { return enabled; }

    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setEmail(String email) { this.email = email; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setAddress(String address) { this.address = address; }
    public void setRole(String role) { this.role = role; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
}

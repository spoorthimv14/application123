package com.smarturban.app.model;

public class User {
    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private String role;
    private String address;
    private boolean enabled;
    private String createdAt;
    private String updatedAt;

    public User() {
    }

    public User(Long id, String fullName, String email, String phone, String role, String address, boolean enabled, String createdAt, String updatedAt) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.address = address;
        this.enabled = enabled;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getRole() { return role; }
    public String getAddress() { return address; }
    public boolean isEnabled() { return enabled; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }

    public void setId(Long id) { this.id = id; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setEmail(String email) { this.email = email; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setRole(String role) { this.role = role; }
    public void setAddress(String address) { this.address = address; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}

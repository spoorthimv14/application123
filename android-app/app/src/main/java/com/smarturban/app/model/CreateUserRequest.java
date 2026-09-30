package com.smarturban.app.model;

public class CreateUserRequest {
    private String fullName;
    private String email;
    private String phone;
    private String password;
    private String address;
    private String role;
    private Boolean enabled;

    public CreateUserRequest() {
    }

    public CreateUserRequest(String fullName, String email, String phone, String password, String address, String role, Boolean enabled) {
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.password = password;
        this.address = address;
        this.role = role;
        this.enabled = enabled;
    }

    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getPassword() { return password; }
    public String getAddress() { return address; }
    public String getRole() { return role; }
    public Boolean getEnabled() { return enabled; }

    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setEmail(String email) { this.email = email; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setPassword(String password) { this.password = password; }
    public void setAddress(String address) { this.address = address; }
    public void setRole(String role) { this.role = role; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
}

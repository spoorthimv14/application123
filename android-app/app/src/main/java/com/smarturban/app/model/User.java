package com.smarturban.app.model;

public class User {
    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private String role;
    private String address;

    public User(Long id, String fullName, String email, String phone, String role, String address) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.address = address;
    }

    public Long getId() { return id; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getRole() { return role; }
    public String getAddress() { return address; }
}

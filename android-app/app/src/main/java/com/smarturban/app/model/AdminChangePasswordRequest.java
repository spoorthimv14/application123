package com.smarturban.app.model;

public class AdminChangePasswordRequest {
    private String newPassword;

    public AdminChangePasswordRequest() {
    }

    public AdminChangePasswordRequest(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
}

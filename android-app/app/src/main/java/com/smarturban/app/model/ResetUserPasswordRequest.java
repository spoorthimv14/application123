package com.smarturban.app.model;

public class ResetUserPasswordRequest {

    private String newPassword;

    public ResetUserPasswordRequest() {
    }

    public ResetUserPasswordRequest(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}

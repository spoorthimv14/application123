package com.smarturban.app.model;

import com.google.gson.annotations.SerializedName;

public class UserPasswordResetRequest {

    @SerializedName("newPassword")
    private String newPassword;

    public UserPasswordResetRequest() {
    }

    public UserPasswordResetRequest(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}

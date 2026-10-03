package com.smarturban.app.model;

import com.google.gson.annotations.SerializedName;

public class AdminChangePasswordRequest {
    @SerializedName("newPassword")
    private String newPassword;

    public AdminChangePasswordRequest(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getNewPassword() { return newPassword; }
}

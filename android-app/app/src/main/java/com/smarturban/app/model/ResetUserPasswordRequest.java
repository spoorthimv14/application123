package com.smarturban.app.model;

import com.google.gson.annotations.SerializedName;

public class ResetUserPasswordRequest {
    @SerializedName("newPassword")
    private String newPassword;

    public ResetUserPasswordRequest(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getNewPassword() { return newPassword; }
}

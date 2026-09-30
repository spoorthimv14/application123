package com.smarturban.app.model;

import com.google.gson.annotations.SerializedName;

public class UpdateUserStatusRequest {
    @SerializedName("enabled")
    private Boolean enabled;

    public UpdateUserStatusRequest(Boolean enabled) {
        this.enabled = enabled;
    }

    public Boolean getEnabled() { return enabled; }
}

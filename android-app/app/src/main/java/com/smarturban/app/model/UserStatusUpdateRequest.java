package com.smarturban.app.model;

import com.google.gson.annotations.SerializedName;

public class UserStatusUpdateRequest {
    @SerializedName("enabled")
    private Boolean enabled;

    public UserStatusUpdateRequest(Boolean enabled) {
        this.enabled = enabled;
    }

    public Boolean getEnabled() { return enabled; }
}

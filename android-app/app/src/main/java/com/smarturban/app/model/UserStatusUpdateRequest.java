package com.smarturban.app.model;

import com.google.gson.annotations.SerializedName;

public class UserStatusUpdateRequest {

    @SerializedName("active")
    private Boolean active;

    public UserStatusUpdateRequest() {
    }

    public UserStatusUpdateRequest(Boolean active) {
        this.active = active;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}

package com.smarturban.app.model;

public class UserStatusRequest {
    private Boolean enabled;

    public UserStatusRequest() {
    }

    public UserStatusRequest(Boolean enabled) {
        this.enabled = enabled;
    }

    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
}

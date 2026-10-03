package com.smarturban.app.model;

public class UpdateUserStatusRequest {

    private boolean enabled;

    public UpdateUserStatusRequest() {
    }

    public UpdateUserStatusRequest(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}

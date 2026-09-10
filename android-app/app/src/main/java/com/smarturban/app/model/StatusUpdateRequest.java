package com.smarturban.app.model;

import com.google.gson.annotations.SerializedName;

public class StatusUpdateRequest {

    @SerializedName("status")
    private String status;

    @SerializedName("remarks")
    private String remarks;

    public StatusUpdateRequest() {}

    public StatusUpdateRequest(String status, String remarks) {
        this.status = status;
        this.remarks = remarks;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}

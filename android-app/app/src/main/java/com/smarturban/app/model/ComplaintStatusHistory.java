package com.smarturban.app.model;

import com.google.gson.annotations.SerializedName;

public class ComplaintStatusHistory {

    @SerializedName("id")
    private Long id;

    @SerializedName("oldStatus")
    private String oldStatus;

    @SerializedName("newStatus")
    private String newStatus;

    @SerializedName("updatedBy")
    private String updatedBy;

    @SerializedName("remarks")
    private String remarks;

    @SerializedName("createdAt")
    private String createdAt;

    public ComplaintStatusHistory() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOldStatus() {
        return oldStatus;
    }

    public void setOldStatus(String oldStatus) {
        this.oldStatus = oldStatus;
    }

    public String getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(String newStatus) {
        this.newStatus = newStatus;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}

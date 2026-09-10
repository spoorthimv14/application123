package com.smarturban.app.model;

import com.google.gson.annotations.SerializedName;

public class AssignDepartmentRequest {

    @SerializedName("departmentId")
    private Long departmentId;

    @SerializedName("remarks")
    private String remarks;

    public AssignDepartmentRequest() {}

    public AssignDepartmentRequest(Long departmentId, String remarks) {
        this.departmentId = departmentId;
        this.remarks = remarks;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}

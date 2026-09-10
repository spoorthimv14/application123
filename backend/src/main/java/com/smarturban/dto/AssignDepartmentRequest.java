package com.smarturban.dto;

import jakarta.validation.constraints.NotNull;

public class AssignDepartmentRequest {

    @NotNull(message = "Department ID is required")
    private Long departmentId;

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

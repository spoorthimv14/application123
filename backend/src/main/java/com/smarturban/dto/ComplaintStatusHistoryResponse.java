package com.smarturban.dto;

import com.smarturban.entity.ComplaintStatus;
import com.smarturban.entity.ComplaintStatusHistory;

import java.time.LocalDateTime;

public class ComplaintStatusHistoryResponse {

    private Long id;
    private ComplaintStatus oldStatus;
    private ComplaintStatus newStatus;
    private String updatedBy;
    private String remarks;
    private LocalDateTime createdAt;

    public ComplaintStatusHistoryResponse() {}

    public static ComplaintStatusHistoryResponse fromEntity(ComplaintStatusHistory history) {
        ComplaintStatusHistoryResponse dto = new ComplaintStatusHistoryResponse();
        dto.setId(history.getId());
        dto.setOldStatus(history.getOldStatus());
        dto.setNewStatus(history.getNewStatus());
        dto.setUpdatedBy(history.getUpdatedBy());
        dto.setRemarks(history.getRemarks());
        dto.setCreatedAt(history.getCreatedAt());
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ComplaintStatus getOldStatus() {
        return oldStatus;
    }

    public void setOldStatus(ComplaintStatus oldStatus) {
        this.oldStatus = oldStatus;
    }

    public ComplaintStatus getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(ComplaintStatus newStatus) {
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

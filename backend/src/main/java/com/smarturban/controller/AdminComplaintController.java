package com.smarturban.controller;

import com.smarturban.dto.*;
import com.smarturban.entity.ComplaintStatus;
import com.smarturban.service.ComplaintService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/complaints")
@PreAuthorize("hasRole('ADMIN')")
public class AdminComplaintController {

    private final ComplaintService complaintService;

    public AdminComplaintController(ComplaintService complaintService) {
        this.complaintService = complaintService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ComplaintResponse>>> getAllComplaints(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) ComplaintStatus status) {

        List<ComplaintResponse> complaints = complaintService.getAllComplaintsForAdmin(status);
        return ResponseEntity.ok(ApiResponse.success("Admin complaints list retrieved successfully", complaints));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<ComplaintStatsResponse>> getAdminStats(
            @AuthenticationPrincipal UserDetails userDetails) {

        ComplaintStatsResponse stats = complaintService.getAdminComplaintStats();
        return ResponseEntity.ok(ApiResponse.success("Admin stats retrieved successfully", stats));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ComplaintResponse>> getComplaintById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        ComplaintResponse complaint = complaintService.getComplaintById(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Complaint details retrieved successfully", complaint));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<ComplaintResponse>> updateStatus(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody StatusUpdateRequest request) {

        ComplaintResponse response = complaintService.updateComplaintStatusByAdmin(
                id, userDetails.getUsername(), request.getStatus(), request.getRemarks());

        return ResponseEntity.ok(ApiResponse.success("Complaint status updated successfully", response));
    }

    @PutMapping("/{id}/assign")
    public ResponseEntity<ApiResponse<ComplaintResponse>> assignDepartment(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody AssignDepartmentRequest request) {

        ComplaintResponse response = complaintService.assignDepartmentByAdmin(
                id, userDetails.getUsername(), request.getDepartmentId(), request.getRemarks());

        return ResponseEntity.ok(ApiResponse.success("Department assigned successfully", response));
    }
}

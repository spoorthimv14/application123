package com.smarturban.controller;

import com.smarturban.dto.ApiResponse;
import com.smarturban.dto.ComplaintRequest;
import com.smarturban.dto.ComplaintResponse;
import com.smarturban.dto.ComplaintStatsResponse;
import com.smarturban.entity.ComplaintStatus;
import com.smarturban.service.ComplaintService;
import com.smarturban.service.FileStorageService;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/complaints")
public class ComplaintController {

    private final ComplaintService complaintService;
    private final FileStorageService fileStorageService;

    public ComplaintController(ComplaintService complaintService, FileStorageService fileStorageService) {
        this.complaintService = complaintService;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<String>>> getCategories() {
        List<String> categories = Arrays.asList(
                "Road/Pothole",
                "Garbage/Waste",
                "Street Light",
                "Water Supply",
                "Drainage",
                "Traffic",
                "Public Toilet",
                "Park",
                "Electricity",
                "Other"
        );
        return ResponseEntity.ok(ApiResponse.success("Categories retrieved successfully", categories));
    }

    @PostMapping(consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<ApiResponse<ComplaintResponse>> createComplaint(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestPart("data") @Valid ComplaintRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image) {

        ComplaintResponse response = complaintService.createComplaint(userDetails.getUsername(), request, image);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Complaint submitted successfully", response));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<ComplaintResponse>>> getMyComplaints(@AuthenticationPrincipal UserDetails userDetails) {
        List<ComplaintResponse> complaints = complaintService.getMyComplaints(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("User complaints retrieved successfully", complaints));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<ComplaintStatsResponse>> getMyComplaintStats(@AuthenticationPrincipal UserDetails userDetails) {
        ComplaintStatsResponse stats = complaintService.getMyComplaintStats(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Complaint statistics retrieved successfully", stats));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ComplaintResponse>>> getAllComplaints(@AuthenticationPrincipal UserDetails userDetails) {
        List<ComplaintResponse> complaints = complaintService.getMyComplaints(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Complaints retrieved successfully", complaints));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ComplaintResponse>> getComplaintById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        ComplaintResponse complaint = complaintService.getComplaintById(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Complaint details retrieved successfully", complaint));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ComplaintResponse>> updateComplaintStatus(
            @PathVariable Long id,
            @RequestParam ComplaintStatus status) {

        ComplaintResponse updated = complaintService.updateComplaintStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("Complaint status updated successfully", updated));
    }

    @GetMapping("/images/{filename:.+}")
    public ResponseEntity<Resource> getComplaintImage(@PathVariable String filename) {
        Resource file = fileStorageService.loadFileAsResource(filename);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getFilename() + "\"")
                .contentType(MediaType.IMAGE_JPEG)
                .body(file);
    }
}

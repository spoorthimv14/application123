package com.smarturban.service;

import com.smarturban.dto.*;
import com.smarturban.entity.*;
import com.smarturban.repository.ComplaintRepository;
import com.smarturban.repository.ComplaintStatusHistoryRepository;
import com.smarturban.repository.DepartmentRepository;
import com.smarturban.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Random;

@Service
@Transactional
public class ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final DepartmentRepository departmentRepository;
    private final ComplaintStatusHistoryRepository statusHistoryRepository;

    public ComplaintService(ComplaintRepository complaintRepository,
                            UserRepository userRepository,
                            FileStorageService fileStorageService,
                            DepartmentRepository departmentRepository,
                            ComplaintStatusHistoryRepository statusHistoryRepository) {
        this.complaintRepository = complaintRepository;
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
        this.departmentRepository = departmentRepository;
        this.statusHistoryRepository = statusHistoryRepository;
    }

    public ComplaintResponse createComplaint(String email, ComplaintRequest request, MultipartFile image) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        String imagePath = null;
        if (image != null && !image.isEmpty()) {
            String storedFileName = fileStorageService.storeFile(image);
            imagePath = "/api/complaints/images/" + storedFileName;
        }

        String complaintNumber = generateComplaintNumber();

        Complaint complaint = new Complaint(
                complaintNumber,
                user,
                request.getCategory(),
                request.getTitle(),
                request.getDescription(),
                imagePath,
                request.getLatitude(),
                request.getLongitude(),
                request.getAddress(),
                ComplaintStatus.PENDING
        );

        Complaint saved = complaintRepository.save(complaint);

        // Record initial status history entry
        ComplaintStatusHistory initialHistory = new ComplaintStatusHistory(
                saved,
                null,
                ComplaintStatus.PENDING,
                user.getFullName() + " (" + user.getEmail() + ")",
                "Complaint submitted by citizen"
        );
        statusHistoryRepository.save(initialHistory);

        List<ComplaintStatusHistoryResponse> historyDtos = statusHistoryRepository.findByComplaintOrderByCreatedAtAsc(saved)
                .stream().map(ComplaintStatusHistoryResponse::fromEntity).toList();

        return ComplaintResponse.fromEntity(saved, historyDtos);
    }

    public List<ComplaintResponse> getMyComplaints(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        return complaintRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(c -> {
                    List<ComplaintStatusHistoryResponse> history = statusHistoryRepository.findByComplaintOrderByCreatedAtAsc(c)
                            .stream().map(ComplaintStatusHistoryResponse::fromEntity).toList();
                    return ComplaintResponse.fromEntity(c, history);
                })
                .toList();
    }

    public ComplaintResponse getComplaintById(Long id, String email) {
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Complaint not found with id: " + id));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // Normal users can only view their own complaints, ADMIN can view any
        if (!complaint.getUser().getId().equals(user.getId()) && user.getRole() != Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to this complaint");
        }

        List<ComplaintStatusHistoryResponse> history = statusHistoryRepository.findByComplaintOrderByCreatedAtAsc(complaint)
                .stream().map(ComplaintStatusHistoryResponse::fromEntity).toList();

        return ComplaintResponse.fromEntity(complaint, history);
    }

    public List<ComplaintResponse> getAllComplaintsForAdmin(ComplaintStatus statusFilter) {
        List<Complaint> complaints;
        if (statusFilter != null) {
            complaints = complaintRepository.findAllByOrderByCreatedAtDesc()
                    .stream().filter(c -> c.getStatus() == statusFilter).toList();
        } else {
            complaints = complaintRepository.findAllByOrderByCreatedAtDesc();
        }

        return complaints.stream().map(c -> {
            List<ComplaintStatusHistoryResponse> history = statusHistoryRepository.findByComplaintOrderByCreatedAtAsc(c)
                    .stream().map(ComplaintStatusHistoryResponse::fromEntity).toList();
            return ComplaintResponse.fromEntity(c, history);
        }).toList();
    }

    public ComplaintResponse updateComplaintStatusByAdmin(Long id, String adminEmail, ComplaintStatus newStatus, String remarks) {
        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Admin user not found"));

        if (admin.getRole() != Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Normal users are not authorized to update complaint status");
        }

        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Complaint not found with id: " + id));

        ComplaintStatus oldStatus = complaint.getStatus();

        // Idempotency check: If status is already current status, do not record duplicate status history
        if (oldStatus == newStatus) {
            List<ComplaintStatusHistoryResponse> historyList = statusHistoryRepository.findByComplaintOrderByCreatedAtAsc(complaint)
                    .stream().map(ComplaintStatusHistoryResponse::fromEntity).toList();
            return ComplaintResponse.fromEntity(complaint, historyList);
        }

        complaint.setStatus(newStatus);
        Complaint saved = complaintRepository.save(complaint);

        String remarkText = (remarks != null && !remarks.trim().isEmpty()) ? remarks : "Status updated to " + newStatus;
        ComplaintStatusHistory historyEntry = new ComplaintStatusHistory(
                saved,
                oldStatus,
                newStatus,
                admin.getFullName() + " [ADMIN]",
                remarkText
        );
        statusHistoryRepository.save(historyEntry);

        List<ComplaintStatusHistoryResponse> historyList = statusHistoryRepository.findByComplaintOrderByCreatedAtAsc(saved)
                .stream().map(ComplaintStatusHistoryResponse::fromEntity).toList();

        return ComplaintResponse.fromEntity(saved, historyList);
    }

    public ComplaintResponse assignDepartmentByAdmin(Long id, String adminEmail, Long departmentId, String remarks) {
        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Admin user not found"));

        if (admin.getRole() != Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Normal users are not authorized to assign departments");
        }

        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Complaint not found with id: " + id));

        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Department not found with id: " + departmentId));

        // Idempotency check: If department is already assigned, do not record duplicate assignment history
        if (complaint.getDepartment() != null && complaint.getDepartment().getId().equals(department.getId())) {
            List<ComplaintStatusHistoryResponse> historyList = statusHistoryRepository.findByComplaintOrderByCreatedAtAsc(complaint)
                    .stream().map(ComplaintStatusHistoryResponse::fromEntity).toList();
            return ComplaintResponse.fromEntity(complaint, historyList);
        }

        ComplaintStatus oldStatus = complaint.getStatus();
        complaint.setDepartment(department);
        if (complaint.getStatus() == ComplaintStatus.PENDING) {
            complaint.setStatus(ComplaintStatus.ASSIGNED);
        }
        Complaint saved = complaintRepository.save(complaint);

        String remarkText = "Assigned to " + department.getName() + (remarks != null && !remarks.trim().isEmpty() ? ". " + remarks : "");
        ComplaintStatusHistory historyEntry = new ComplaintStatusHistory(
                saved,
                oldStatus,
                saved.getStatus(),
                admin.getFullName() + " [ADMIN]",
                remarkText
        );
        statusHistoryRepository.save(historyEntry);

        List<ComplaintStatusHistoryResponse> historyList = statusHistoryRepository.findByComplaintOrderByCreatedAtAsc(saved)
                .stream().map(ComplaintStatusHistoryResponse::fromEntity).toList();

        return ComplaintResponse.fromEntity(saved, historyList);
    }

    public ComplaintStatsResponse getAdminComplaintStats() {
        List<Complaint> all = complaintRepository.findAll();
        long total = all.size();
        long pending = all.stream().filter(c -> c.getStatus() == ComplaintStatus.PENDING).count();
        long assigned = all.stream().filter(c -> c.getStatus() == ComplaintStatus.ASSIGNED).count();
        long inProgress = all.stream().filter(c -> c.getStatus() == ComplaintStatus.IN_PROGRESS).count();
        long resolved = all.stream().filter(c -> c.getStatus() == ComplaintStatus.RESOLVED).count();
        long rejected = all.stream().filter(c -> c.getStatus() == ComplaintStatus.REJECTED).count();

        return new ComplaintStatsResponse(total, pending, inProgress, resolved, rejected, assigned);
    }

    public ComplaintStatsResponse getMyComplaintStats(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        long total = complaintRepository.countByUser(user);
        long pending = complaintRepository.countByUserAndStatus(user, ComplaintStatus.PENDING);
        long assigned = complaintRepository.countByUserAndStatus(user, ComplaintStatus.ASSIGNED);
        long inProgress = complaintRepository.countByUserAndStatus(user, ComplaintStatus.IN_PROGRESS);
        long resolved = complaintRepository.countByUserAndStatus(user, ComplaintStatus.RESOLVED);
        long rejected = complaintRepository.countByUserAndStatus(user, ComplaintStatus.REJECTED);

        return new ComplaintStatsResponse(total, pending, inProgress, resolved, rejected, assigned);
    }

    public List<DepartmentResponse> getAllActiveDepartments() {
        return departmentRepository.findByActiveTrueOrderByNameAsc()
                .stream().map(DepartmentResponse::fromEntity).toList();
    }

    private synchronized String generateComplaintNumber() {
        int year = LocalDate.now().getYear();
        long count = complaintRepository.count() + 1;
        int randomDigits = new Random().nextInt(9000) + 1000;
        return String.format("SU-%d-%04d%d", year, count, randomDigits);
    }
}

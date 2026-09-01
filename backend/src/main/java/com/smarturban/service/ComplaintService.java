package com.smarturban.service;

import com.smarturban.dto.ComplaintRequest;
import com.smarturban.dto.ComplaintResponse;
import com.smarturban.dto.ComplaintStatsResponse;
import com.smarturban.entity.Complaint;
import com.smarturban.entity.ComplaintStatus;
import com.smarturban.entity.Role;
import com.smarturban.entity.User;
import com.smarturban.repository.ComplaintRepository;
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

    public ComplaintService(ComplaintRepository complaintRepository, UserRepository userRepository, FileStorageService fileStorageService) {
        this.complaintRepository = complaintRepository;
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
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
        return ComplaintResponse.fromEntity(saved);
    }

    public List<ComplaintResponse> getMyComplaints(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        return complaintRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(ComplaintResponse::fromEntity)
                .toList();
    }

    public ComplaintResponse getComplaintById(Long id, String email) {
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Complaint not found with id: " + id));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // Normal users can only view their own complaints
        if (!complaint.getUser().getId().equals(user.getId()) && user.getRole() != Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to this complaint");
        }

        return ComplaintResponse.fromEntity(complaint);
    }

    public List<ComplaintResponse> getAllComplaints() {
        return complaintRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(ComplaintResponse::fromEntity)
                .toList();
    }

    public ComplaintResponse updateComplaintStatus(Long id, String email, ComplaintStatus status) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (user.getRole() != Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Normal users are not authorized to update complaint statuses");
        }

        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Complaint not found with id: " + id));

        complaint.setStatus(status);
        Complaint updated = complaintRepository.save(complaint);
        return ComplaintResponse.fromEntity(updated);
    }

    public ComplaintStatsResponse getMyComplaintStats(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        long total = complaintRepository.countByUser(user);
        long pending = complaintRepository.countByUserAndStatus(user, ComplaintStatus.PENDING);
        long inProgress = complaintRepository.countByUserAndStatus(user, ComplaintStatus.IN_PROGRESS);
        long resolved = complaintRepository.countByUserAndStatus(user, ComplaintStatus.RESOLVED);
        long rejected = complaintRepository.countByUserAndStatus(user, ComplaintStatus.REJECTED);

        return new ComplaintStatsResponse(total, pending, inProgress, resolved, rejected);
    }

    private synchronized String generateComplaintNumber() {
        int year = LocalDate.now().getYear();
        long count = complaintRepository.count() + 1;
        int randomDigits = new Random().nextInt(9000) + 1000;
        return String.format("SU-%d-%04d%d", year, count, randomDigits);
    }
}

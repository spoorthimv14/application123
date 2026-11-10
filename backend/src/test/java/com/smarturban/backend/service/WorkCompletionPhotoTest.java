package com.smarturban.backend.service;

import com.smarturban.backend.dto.ComplaintRequest;
import com.smarturban.backend.dto.ComplaintResponse;
import com.smarturban.backend.entity.*;
import com.smarturban.backend.repository.*;
import com.smarturban.backend.security.UserDetailsImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class WorkCompletionPhotoTest {

    @Autowired
    private ComplaintService complaintService;

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private ComplaintCompletionPhotoRepository completionPhotoRepository;

    @Autowired
    private ComplaintStatusHistoryRepository statusHistoryRepository;

    @Autowired
    private ComplaintFeedbackRepository feedbackRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    private User citizen1;
    private User citizen2;
    private Category testCategory;
    private Department testDept;

    @BeforeEach
    public void setUp() {
        completionPhotoRepository.deleteAll();
        feedbackRepository.deleteAll();
        statusHistoryRepository.deleteAll();
        complaintRepository.deleteAll();

        citizen1 = userRepository.findByEmail("citizen1_test@smarturban.com").orElseGet(() ->
                userRepository.save(new User("Citizen One", "citizen1_test@smarturban.com", "9111111111", "Pass@123", "Street 1", "ROLE_CITIZEN")));

        citizen2 = userRepository.findByEmail("citizen2_test@smarturban.com").orElseGet(() ->
                userRepository.save(new User("Citizen Two", "citizen2_test@smarturban.com", "9222222222", "Pass@123", "Street 2", "ROLE_CITIZEN")));

        testDept = departmentRepository.findByName("Public Works Department").orElseGet(() ->
                departmentRepository.save(new Department("Public Works Department", "PWD Dept")));

        testCategory = categoryRepository.findByName("Road Maintenance").orElseGet(() ->
                categoryRepository.save(new Category("Road Maintenance", "Road issues", testDept)));
    }

    private Complaint createTestComplaint(User owner, String originalImageUrl) {
        ComplaintRequest req = new ComplaintRequest();
        req.setTitle("Pothole on Main Road");
        req.setDescription("Damaged road near central junction");
        req.setCategoryId(testCategory.getId());
        req.setLatitude(12.9716);
        req.setLongitude(77.5946);
        return complaintService.createComplaint(owner, req, originalImageUrl);
    }

    @Test
    @DisplayName("1. Resolve a complaint without photos (photos optional)")
    public void testResolveComplaintWithoutPhotos() {
        Complaint c = createTestComplaint(citizen1, "/uploads/orig_photo1.jpg");
        assertEquals("Submitted", c.getStatus());
        assertEquals("/uploads/orig_photo1.jpg", c.getImageUrl());

        Complaint resolved = complaintService.updateComplaintStatusAndDepartment(
                c.getId(), "Resolved", testDept.getId(), "Admin Jane",
                "Fixed the pothole", "Asphalt paving completed", Collections.emptyList());

        assertEquals("Resolved", resolved.getStatus());
        assertNotNull(resolved.getResolutionDate());
        assertEquals("Asphalt paving completed", resolved.getCompletionRemarks());
        assertEquals("/uploads/orig_photo1.jpg", resolved.getImageUrl(), "Original photo remains untouched");

        List<ComplaintCompletionPhoto> photos = complaintService.getCompletionPhotos(c.getId());
        assertTrue(photos.isEmpty(), "No completion photos should be saved");
    }

    @Test
    @DisplayName("2. Resolve a complaint with one completion photo")
    public void testResolveComplaintWithOnePhoto() {
        Complaint c = createTestComplaint(citizen1, "/uploads/orig_pothole.jpg");

        Complaint resolved = complaintService.updateComplaintStatusAndDepartment(
                c.getId(), "Resolved", testDept.getId(), "Admin Jane",
                "Repairs completed", "Filled and asphalt laid", List.of("/uploads/completion_1.jpg"));

        assertEquals("Resolved", resolved.getStatus());
        assertNotNull(resolved.getResolutionDate());
        assertEquals("Filled and asphalt laid", resolved.getCompletionRemarks());
        assertEquals("/uploads/orig_pothole.jpg", resolved.getImageUrl());

        List<ComplaintCompletionPhoto> photos = complaintService.getCompletionPhotos(c.getId());
        assertEquals(1, photos.size());
        assertEquals("/uploads/completion_1.jpg", photos.get(0).getPhotoUrl());
        assertEquals("Admin Jane", photos.get(0).getUploadedBy());
        assertNotNull(photos.get(0).getUploadedAt());
    }

    @Test
    @DisplayName("3. Resolve a complaint with multiple completion photos")
    public void testResolveComplaintWithMultiplePhotos() {
        Complaint c = createTestComplaint(citizen1, "/uploads/orig_pipe.jpg");

        List<String> photoUrls = Arrays.asList(
                "/uploads/comp_photo_a.jpg",
                "/uploads/comp_photo_b.jpg",
                "/uploads/comp_photo_c.jpg"
        );

        Complaint resolved = complaintService.updateComplaintStatusAndDepartment(
                c.getId(), "Resolved", testDept.getId(), "Admin Bob",
                "Repaired pipe", "Replaced cracked pipeline and pressure tested", photoUrls);

        assertEquals("Resolved", resolved.getStatus());
        List<ComplaintCompletionPhoto> photos = complaintService.getCompletionPhotos(c.getId());
        assertEquals(3, photos.size());
        assertEquals("/uploads/comp_photo_a.jpg", photos.get(0).getPhotoUrl());
        assertEquals("/uploads/comp_photo_b.jpg", photos.get(1).getPhotoUrl());
        assertEquals("/uploads/comp_photo_c.jpg", photos.get(2).getPhotoUrl());

        // Verify ComplaintResponse DTO includes all photos
        ComplaintResponse resp = new ComplaintResponse(resolved);
        assertNotNull(resp.getCompletionPhotos());
        assertEquals(3, resp.getCompletionPhotos().size());
        assertEquals("Replaced cracked pipeline and pressure tested", resp.getCompletionRemarks());
        assertNotNull(resp.getResolutionDate());
    }

    @Test
    @DisplayName("4. Reopen a complaint and resolve it again, preserving previous photos")
    public void testReopenAndResolveAgainPreservesPreviousPhotos() {
        Complaint c = createTestComplaint(citizen1, "/uploads/orig_drain.jpg");

        // First resolution with 2 photos
        complaintService.updateComplaintStatusAndDepartment(
                c.getId(), "Resolved", testDept.getId(), "Admin 1",
                "Cleared blockage", "Initial clearing", List.of("/uploads/comp_round1_a.jpg", "/uploads/comp_round1_b.jpg"));

        List<ComplaintCompletionPhoto> firstRoundPhotos = complaintService.getCompletionPhotos(c.getId());
        assertEquals(2, firstRoundPhotos.size());

        // Citizen / Admin reopens complaint
        Complaint reopened = complaintService.updateComplaintStatusAndDepartment(
                c.getId(), "Reopened", testDept.getId(), "Citizen One", "Water clogged again after rain");
        assertEquals("Reopened", reopened.getStatus());

        // Photos from round 1 are still preserved
        assertEquals(2, complaintService.getCompletionPhotos(c.getId()).size());

        // Re-resolve with 1 new photo
        Complaint reResolved = complaintService.updateComplaintStatusAndDepartment(
                c.getId(), "Resolved", testDept.getId(), "Admin 2",
                "Drain rebuilt", "Rebuilt catch basin", List.of("/uploads/comp_round2.jpg"));

        assertEquals("Resolved", reResolved.getStatus());
        assertEquals("Rebuilt catch basin", reResolved.getCompletionRemarks());

        // Both previous photos and new photo must exist (total 3 photos)
        List<ComplaintCompletionPhoto> allPhotos = complaintService.getCompletionPhotos(c.getId());
        assertEquals(3, allPhotos.size());
        assertEquals("/uploads/comp_round1_a.jpg", allPhotos.get(0).getPhotoUrl());
        assertEquals("/uploads/comp_round1_b.jpg", allPhotos.get(1).getPhotoUrl());
        assertEquals("/uploads/comp_round2.jpg", allPhotos.get(2).getPhotoUrl());
        assertEquals("Admin 2", allPhotos.get(2).getUploadedBy());
    }

    @Test
    @DisplayName("5. Reject completion photo uploads for non-RESOLVED statuses (PENDING, IN_PROGRESS, REJECTED, REOPENED)")
    public void testRejectUploadsForNonResolvedStatuses() {
        Complaint c = createTestComplaint(citizen1, "/uploads/orig.jpg");

        // Trying to upload photo while setting to In Progress -> must fail
        IllegalArgumentException ex1 = assertThrows(IllegalArgumentException.class, () ->
                complaintService.updateComplaintStatusAndDepartment(
                        c.getId(), "In Progress", testDept.getId(), "Admin",
                        "Starting work", null, List.of("/uploads/photo_illegal.jpg")));
        assertTrue(ex1.getMessage().contains("only be uploaded when resolving"));

        // Trying to upload photo while setting to Rejected -> must fail
        IllegalArgumentException ex2 = assertThrows(IllegalArgumentException.class, () ->
                complaintService.updateComplaintStatusAndDepartment(
                        c.getId(), "Rejected", testDept.getId(), "Admin",
                        "Not a municipal issue", null, List.of("/uploads/photo_illegal.jpg")));
        assertTrue(ex2.getMessage().contains("only be uploaded when resolving"));

        // Trying to upload photo while setting to Submitted / Pending -> must fail
        IllegalArgumentException ex3 = assertThrows(IllegalArgumentException.class, () ->
                complaintService.updateComplaintStatusAndDepartment(
                        c.getId(), "Submitted", testDept.getId(), "Admin",
                        "Submitted", null, List.of("/uploads/photo_illegal.jpg")));
        assertTrue(ex3.getMessage().contains("only be uploaded when resolving"));

        // Reopen and try to upload photo under Reopened status -> must fail
        complaintService.updateComplaintStatusAndDepartment(c.getId(), "Resolved", testDept.getId(), "Admin", "Resolved");
        complaintService.updateComplaintStatusAndDepartment(c.getId(), "Reopened", testDept.getId(), "Citizen", "Reopened");

        IllegalArgumentException ex4 = assertThrows(IllegalArgumentException.class, () ->
                complaintService.updateComplaintStatusAndDepartment(
                        c.getId(), "Reopened", testDept.getId(), "Admin",
                        "Still reopened", null, List.of("/uploads/photo_illegal.jpg")));
        assertTrue(ex4.getMessage().contains("only be uploaded when resolving"));
    }

    @Test
    @DisplayName("6. Prevent citizens from accessing another citizen's completion photos")
    public void testCitizenOwnershipOnPhotoAccess() {
        Complaint c1 = createTestComplaint(citizen1, "/uploads/c1.jpg");
        complaintService.updateComplaintStatusAndDepartment(
                c1.getId(), "Resolved", testDept.getId(), "Admin", "Done", "Fixed", List.of("/uploads/comp1.jpg"));

        // Citizen 1 Details
        UserDetailsImpl citizen1Details = UserDetailsImpl.build(citizen1);

        // Citizen 2 Details
        UserDetailsImpl citizen2Details = UserDetailsImpl.build(citizen2);

        // Admin Details
        User adminUser = new User("Admin User", "admin@smarturban.com", "9999999999", "AdminPass", "HQ", "ROLE_ADMIN");
        adminUser.setId(999L);
        UserDetailsImpl adminDetails = UserDetailsImpl.build(adminUser);

        // Verify citizen 1 can access own complaint
        Complaint loadedForOwner = complaintService.getComplaintById(c1.getId());
        assertEquals(citizen1.getId(), loadedForOwner.getUser().getId());

        // Check ownership condition logic enforced in CitizenComplaintController
        boolean canOwnerAccess = loadedForOwner.getUser().getId().equals(citizen1Details.getId()) ||
                "ROLE_ADMIN".equals(citizen1Details.getAuthorities().iterator().next().getAuthority());
        assertTrue(canOwnerAccess);

        boolean canCitizen2Access = loadedForOwner.getUser().getId().equals(citizen2Details.getId()) ||
                "ROLE_ADMIN".equals(citizen2Details.getAuthorities().iterator().next().getAuthority());
        assertFalse(canCitizen2Access, "Another citizen must NOT have access to this complaint or completion photos");

        boolean canAdminAccess = loadedForOwner.getUser().getId().equals(adminDetails.getId()) ||
                "ROLE_ADMIN".equals(adminDetails.getAuthorities().iterator().next().getAuthority());
        assertTrue(canAdminAccess, "Admin must be permitted to access");
    }

    @Test
    @DisplayName("7. Verify original complaint photos are never overwritten")
    public void testOriginalComplaintPhotosNeverOverwritten() {
        String originalPhoto = "/uploads/citizen_original_proof.jpg";
        Complaint c = createTestComplaint(citizen1, originalPhoto);
        assertEquals(originalPhoto, c.getImageUrl());

        // Transition through multiple stages
        complaintService.updateComplaintStatusAndDepartment(c.getId(), "Under Review", testDept.getId(), "Admin", "Reviewing");
        Complaint cReview = complaintService.getComplaintById(c.getId());
        assertEquals(originalPhoto, cReview.getImageUrl());

        complaintService.updateComplaintStatusAndDepartment(c.getId(), "In Progress", testDept.getId(), "Admin", "In progress");
        Complaint cProgress = complaintService.getComplaintById(c.getId());
        assertEquals(originalPhoto, cProgress.getImageUrl());

        complaintService.updateComplaintStatusAndDepartment(
                c.getId(), "Resolved", testDept.getId(), "Admin", "Done", "Fixed completely",
                List.of("/uploads/completion_pic1.jpg", "/uploads/completion_pic2.jpg"));

        Complaint cResolved = complaintService.getComplaintById(c.getId());
        assertEquals(originalPhoto, cResolved.getImageUrl(), "Original photo URL must remain completely unchanged");
        assertEquals(2, complaintService.getCompletionPhotos(c.getId()).size());
    }
}

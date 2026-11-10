package com.smarturban.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smarturban.backend.dto.ComplaintRequest;
import com.smarturban.backend.dto.ComplaintResponse;
import com.smarturban.backend.entity.*;
import com.smarturban.backend.repository.*;
import com.smarturban.backend.security.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class WorkCompletionPhotoTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtUtils jwtUtils;

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
    private User adminUser;
    private Category testCategory;
    private Department testDept;

    private String citizen1Token;
    private String citizen2Token;
    private String adminToken;

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

        adminUser = userRepository.findByEmail("admin_auth_test@smarturban.com").orElseGet(() ->
                userRepository.save(new User("Admin Officer", "admin_auth_test@smarturban.com", "9999999999", "Admin@12345", "HQ", "ROLE_ADMIN")));

        testDept = departmentRepository.findByName("Public Works Department").orElseGet(() ->
                departmentRepository.save(new Department("Public Works Department", "PWD Dept")));

        testCategory = categoryRepository.findByName("Road Maintenance").orElseGet(() ->
                categoryRepository.save(new Category("Road Maintenance", "Road issues", testDept)));

        citizen1Token = jwtUtils.generateTokenFromUsername(citizen1.getEmail());
        citizen2Token = jwtUtils.generateTokenFromUsername(citizen2.getEmail());
        adminToken = jwtUtils.generateTokenFromUsername(adminUser.getEmail());
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
                c.getId(), "Resolved", testDept.getId(), adminUser.getFullName(),
                "Fixed the pothole", "Asphalt paving completed", Collections.emptyList());

        assertEquals("Resolved", resolved.getStatus());
        assertNotNull(resolved.getResolutionDate());
        assertEquals("Asphalt paving completed", resolved.getCompletionRemarks());
        assertEquals("/uploads/orig_photo1.jpg", resolved.getImageUrl(), "Original photo remains untouched");

        List<ComplaintCompletionPhoto> photos = complaintService.getCompletionPhotos(c.getId());
        assertTrue(photos.isEmpty(), "No completion photos should be saved");
    }

    @Test
    @DisplayName("2. Resolve a complaint with one completion photo via multipart API")
    public void testResolveComplaintWithOnePhotoApi() throws Exception {
        Complaint c = createTestComplaint(citizen1, "/uploads/orig_pothole.jpg");

        // Valid JPEG content (magic bytes FF D8 FF)
        byte[] validJpeg = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0x00, 0x10, 'J', 'F', 'I', 'F'};
        MockMultipartFile photoPart = new MockMultipartFile(
                "photos", "comp1.jpg", "image/jpeg", validJpeg);

        mockMvc.perform(multipart("/api/admin/complaints/" + c.getId() + "/resolve")
                        .file(photoPart)
                        .param("remarks", "Repairs completed")
                        .param("completionRemarks", "Filled and asphalt laid")
                        .param("departmentId", testDept.getId().toString())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("Resolved"))
                .andExpect(jsonPath("$.completionRemarks").value("Filled and asphalt laid"))
                .andExpect(jsonPath("$.completionPhotos", hasSize(1)))
                .andExpect(jsonPath("$.completionPhotos[0].uploadedBy").value(adminUser.getFullName()))
                .andExpect(jsonPath("$.imageUrl").value("/uploads/orig_pothole.jpg"));

        List<ComplaintCompletionPhoto> photos = complaintService.getCompletionPhotos(c.getId());
        assertEquals(1, photos.size());
        assertEquals(adminUser.getFullName(), photos.get(0).getUploadedBy());
    }

    @Test
    @DisplayName("3. Resolve a complaint with multiple completion photos via API")
    public void testResolveComplaintWithMultiplePhotosApi() throws Exception {
        Complaint c = createTestComplaint(citizen1, "/uploads/orig_pipe.jpg");

        byte[] validPng = new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0x00, 0x00};
        MockMultipartFile photo1 = new MockMultipartFile("photos", "comp1.png", "image/png", validPng);
        MockMultipartFile photo2 = new MockMultipartFile("photos", "comp2.png", "image/png", validPng);
        MockMultipartFile photo3 = new MockMultipartFile("photos", "comp3.png", "image/png", validPng);

        mockMvc.perform(multipart("/api/admin/complaints/" + c.getId() + "/resolve")
                        .file(photo1)
                        .file(photo2)
                        .file(photo3)
                        .param("remarks", "Multiple steps completed")
                        .param("completionRemarks", "Excavation, replacement and road resurfacing done")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("Resolved"))
                .andExpect(jsonPath("$.completionPhotos", hasSize(3)))
                .andExpect(jsonPath("$.imageUrl").value("/uploads/orig_pipe.jpg"));

        List<ComplaintCompletionPhoto> photos = complaintService.getCompletionPhotos(c.getId());
        assertEquals(3, photos.size());
    }

    @Test
    @DisplayName("4. Reopen a complaint and resolve it again, preserving previous photos")
    public void testReopenAndResolveAgainPreservesPreviousPhotos() {
        Complaint c = createTestComplaint(citizen1, "/uploads/orig_drain.jpg");

        // First resolution with 2 photos
        complaintService.updateComplaintStatusAndDepartment(
                c.getId(), "Resolved", testDept.getId(), adminUser.getFullName(),
                "Cleared blockage", "Initial clearing", List.of("/uploads/comp_round1_a.jpg", "/uploads/comp_round1_b.jpg"));

        List<ComplaintCompletionPhoto> firstRoundPhotos = complaintService.getCompletionPhotos(c.getId());
        assertEquals(2, firstRoundPhotos.size());

        // Reopen complaint
        Complaint reopened = complaintService.updateComplaintStatusAndDepartment(
                c.getId(), "Reopened", testDept.getId(), "Citizen One", "Water clogged again after rain");
        assertEquals("Reopened", reopened.getStatus());

        // Photos from round 1 are preserved
        assertEquals(2, complaintService.getCompletionPhotos(c.getId()).size());

        // Re-resolve with 1 new photo
        Complaint reResolved = complaintService.updateComplaintStatusAndDepartment(
                c.getId(), "Resolved", testDept.getId(), "Admin Two",
                "Drain rebuilt", "Rebuilt catch basin", List.of("/uploads/comp_round2.jpg"));

        assertEquals("Resolved", reResolved.getStatus());
        assertEquals("Rebuilt catch basin", reResolved.getCompletionRemarks());

        // Both previous photos and new photo must exist (total 3 photos)
        List<ComplaintCompletionPhoto> allPhotos = complaintService.getCompletionPhotos(c.getId());
        assertEquals(3, allPhotos.size());
        assertEquals("/uploads/comp_round1_a.jpg", allPhotos.get(0).getPhotoUrl());
        assertEquals("/uploads/comp_round1_b.jpg", allPhotos.get(1).getPhotoUrl());
        assertEquals("/uploads/comp_round2.jpg", allPhotos.get(2).getPhotoUrl());
    }

    @Test
    @DisplayName("5. Reject completion photo uploads for non-RESOLVED statuses (PENDING, IN_PROGRESS, REJECTED, REOPENED)")
    public void testRejectUploadsForNonResolvedStatuses() {
        Complaint c = createTestComplaint(citizen1, "/uploads/orig.jpg");

        // In Progress
        IllegalArgumentException ex1 = assertThrows(IllegalArgumentException.class, () ->
                complaintService.updateComplaintStatusAndDepartment(
                        c.getId(), "In Progress", testDept.getId(), "Admin",
                        "Starting work", null, List.of("/uploads/photo_illegal.jpg")));
        assertTrue(ex1.getMessage().contains("only be uploaded when resolving"));

        // Rejected
        IllegalArgumentException ex2 = assertThrows(IllegalArgumentException.class, () ->
                complaintService.updateComplaintStatusAndDepartment(
                        c.getId(), "Rejected", testDept.getId(), "Admin",
                        "Not a municipal issue", null, List.of("/uploads/photo_illegal.jpg")));
        assertTrue(ex2.getMessage().contains("only be uploaded when resolving"));

        // Submitted / Pending
        IllegalArgumentException ex3 = assertThrows(IllegalArgumentException.class, () ->
                complaintService.updateComplaintStatusAndDepartment(
                        c.getId(), "Submitted", testDept.getId(), "Admin",
                        "Submitted", null, List.of("/uploads/photo_illegal.jpg")));
        assertTrue(ex3.getMessage().contains("only be uploaded when resolving"));

        // Reopened
        complaintService.updateComplaintStatusAndDepartment(c.getId(), "Resolved", testDept.getId(), "Admin", "Resolved");
        complaintService.updateComplaintStatusAndDepartment(c.getId(), "Reopened", testDept.getId(), "Citizen", "Reopened");

        IllegalArgumentException ex4 = assertThrows(IllegalArgumentException.class, () ->
                complaintService.updateComplaintStatusAndDepartment(
                        c.getId(), "Reopened", testDept.getId(), "Admin",
                        "Still reopened", null, List.of("/uploads/photo_illegal.jpg")));
        assertTrue(ex4.getMessage().contains("only be uploaded when resolving"));
    }

    @Test
    @DisplayName("6. Reject unauthorized admin uploads (Citizen cannot call resolve/upload endpoint)")
    public void testRejectUnauthorizedAdminUploads() throws Exception {
        Complaint c = createTestComplaint(citizen1, "/uploads/c1.jpg");

        byte[] validJpeg = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0x00, 0x10, 'J', 'F', 'I', 'F'};
        MockMultipartFile photo = new MockMultipartFile("photos", "comp.jpg", "image/jpeg", validJpeg);

        // Citizen token attempting to call Admin resolve endpoint -> 403 Forbidden
        mockMvc.perform(multipart("/api/admin/complaints/" + c.getId() + "/resolve")
                        .file(photo)
                        .header("Authorization", "Bearer " + citizen1Token))
                .andExpect(status().isForbidden());

        // Unauthenticated request -> 401 Unauthorized
        mockMvc.perform(multipart("/api/admin/complaints/" + c.getId() + "/resolve")
                        .file(photo))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("7. Prevent citizens from accessing another citizen's completion photos via API")
    public void testCitizenOwnershipOnPhotoAccessApi() throws Exception {
        Complaint c1 = createTestComplaint(citizen1, "/uploads/c1.jpg");
        complaintService.updateComplaintStatusAndDepartment(
                c1.getId(), "Resolved", testDept.getId(), adminUser.getFullName(),
                "Done", "Fixed", List.of("/uploads/comp1.jpg"));

        // Owner (citizen1) can access complaint details and completion photos -> 200 OK
        mockMvc.perform(get("/api/citizen/complaints/" + c1.getId())
                        .header("Authorization", "Bearer " + citizen1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(c1.getId()))
                .andExpect(jsonPath("$.completionPhotos", hasSize(1)));

        mockMvc.perform(get("/api/citizen/complaints/" + c1.getId() + "/completion-photos")
                        .header("Authorization", "Bearer " + citizen1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        // Non-owner citizen (citizen2) attempting to access -> 403 Forbidden
        mockMvc.perform(get("/api/citizen/complaints/" + c1.getId())
                        .header("Authorization", "Bearer " + citizen2Token))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/citizen/complaints/" + c1.getId() + "/completion-photos")
                        .header("Authorization", "Bearer " + citizen2Token))
                .andExpect(status().isForbidden());

        // Admin can access citizen's complaint -> 200 OK
        mockMvc.perform(get("/api/citizen/complaints/" + c1.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("8. Verify original complaint photos are never overwritten")
    public void testOriginalComplaintPhotosNeverOverwritten() {
        String originalPhoto = "/uploads/citizen_original_proof.jpg";
        Complaint c = createTestComplaint(citizen1, originalPhoto);
        assertEquals(originalPhoto, c.getImageUrl());

        complaintService.updateComplaintStatusAndDepartment(c.getId(), "Under Review", testDept.getId(), "Admin", "Reviewing");
        assertEquals(originalPhoto, complaintService.getComplaintById(c.getId()).getImageUrl());

        complaintService.updateComplaintStatusAndDepartment(c.getId(), "In Progress", testDept.getId(), "Admin", "Working");
        assertEquals(originalPhoto, complaintService.getComplaintById(c.getId()).getImageUrl());

        complaintService.updateComplaintStatusAndDepartment(
                c.getId(), "Resolved", testDept.getId(), "Admin", "Done", "Fixed completely",
                List.of("/uploads/completion_pic1.jpg", "/uploads/completion_pic2.jpg"));

        Complaint resolved = complaintService.getComplaintById(c.getId());
        assertEquals(originalPhoto, resolved.getImageUrl(), "Original photo URL must remain completely untouched");
        assertEquals(2, complaintService.getCompletionPhotos(c.getId()).size());
    }
}

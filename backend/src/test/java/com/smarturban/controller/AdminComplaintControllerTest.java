package com.smarturban.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smarturban.dto.AssignDepartmentRequest;
import com.smarturban.dto.ComplaintRequest;
import com.smarturban.dto.RegisterRequest;
import com.smarturban.dto.StatusUpdateRequest;
import com.smarturban.entity.ComplaintStatus;
import com.smarturban.entity.Department;
import com.smarturban.entity.Role;
import com.smarturban.entity.User;
import com.smarturban.repository.ComplaintRepository;
import com.smarturban.repository.ComplaintStatusHistoryRepository;
import com.smarturban.repository.DepartmentRepository;
import com.smarturban.repository.UserRepository;
import com.smarturban.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AdminComplaintControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private ComplaintStatusHistoryRepository statusHistoryRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private JwtService jwtService;

    private String userToken;
    private String adminToken;
    private User normalUser;
    private User adminUser;
    private Department pwdDepartment;

    @BeforeEach
    void setUp() throws Exception {
        statusHistoryRepository.deleteAll();
        complaintRepository.deleteAll();
        userRepository.deleteAll();
        departmentRepository.deleteAll();

        // Save PWD department
        pwdDepartment = departmentRepository.save(new Department("Public Works Department", "PWD", true));

        // Register Citizen User
        RegisterRequest regUser = new RegisterRequest("Citizen User", "citizen@smarturban.com", "9876543210", "Password123!", "Password123!", "Main St");
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(regUser)))
                .andExpect(status().isCreated());

        normalUser = userRepository.findByEmail("citizen@smarturban.com").orElseThrow();
        userToken = jwtService.generateToken(normalUser.getId(), normalUser.getEmail(), normalUser.getRole().name());

        // Register Admin User
        RegisterRequest regAdmin = new RegisterRequest("Admin User", "admin@smarturban.com", "9876543211", "Password123!", "Password123!", "City Hall");
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(regAdmin)))
                .andExpect(status().isCreated());

        adminUser = userRepository.findByEmail("admin@smarturban.com").orElseThrow();
        adminUser.setRole(Role.ADMIN);
        userRepository.save(adminUser);
        adminToken = jwtService.generateToken(adminUser.getId(), adminUser.getEmail(), adminUser.getRole().name());
    }

    @Test
    void testNormalUserBlockedFromAdminEndpoints() throws Exception {
        mockMvc.perform(get("/api/admin/complaints")
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/admin/complaints/stats")
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void testAdminDepartmentAssignmentAndStatusUpdateTimeline() throws Exception {
        // 1. Citizen creates a complaint
        ComplaintRequest req = new ComplaintRequest("Road/Pothole", "Broken Asphalt", "Huge pothole on 5th Ave", 12.9716, 77.5946, "5th Avenue");
        MockMultipartFile dataPart = new MockMultipartFile("data", "", "application/json", objectMapper.writeValueAsBytes(req));

        String responseContent = mockMvc.perform(multipart("/api/complaints")
                .file(dataPart)
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long complaintId = objectMapper.readTree(responseContent).get("data").get("id").asLong();

        // 2. Admin assigns department
        AssignDepartmentRequest assignReq = new AssignDepartmentRequest(pwdDepartment.getId(), "Assigned to PWD team for asphalt patch");
        mockMvc.perform(put("/api/admin/complaints/" + complaintId + "/assign")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(assignReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.departmentName").value("Public Works Department"))
                .andExpect(jsonPath("$.data.status").value("ASSIGNED"));

        // 3. Admin updates status to IN_PROGRESS
        StatusUpdateRequest statusReq1 = new StatusUpdateRequest(ComplaintStatus.IN_PROGRESS, "Work order dispatched");
        mockMvc.perform(put("/api/admin/complaints/" + complaintId + "/status")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(statusReq1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"));

        // 4. Citizen fetches complaint details and verifies complete status history timeline
        mockMvc.perform(get("/api/complaints/" + complaintId)
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.data.departmentName").value("Public Works Department"))
                .andExpect(jsonPath("$.data.statusHistory.length()").value(3));
    }
}

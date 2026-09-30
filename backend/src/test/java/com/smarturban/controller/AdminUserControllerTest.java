package com.smarturban.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smarturban.dto.*;
import com.smarturban.entity.Role;
import com.smarturban.entity.User;
import com.smarturban.repository.ComplaintRepository;
import com.smarturban.repository.ComplaintStatusHistoryRepository;
import com.smarturban.repository.UserRepository;
import com.smarturban.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AdminUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ComplaintStatusHistoryRepository statusHistoryRepository;

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private JwtService jwtService;

    private String adminToken;
    private String userToken;
    private User adminUser;
    private User normalUser;

    @BeforeEach
    void setUp() {
        statusHistoryRepository.deleteAll();
        complaintRepository.deleteAll();
        userRepository.deleteAll();

        // Create Admin User
        adminUser = new User("System Admin", "admin@smarturban.com", "+91 9999999999", "Password123!", "HQ", Role.ADMIN);
        adminUser = userRepository.save(adminUser);
        adminToken = jwtService.generateToken(adminUser.getId(), adminUser.getEmail(), adminUser.getRole().name());

        // Create Normal User
        normalUser = new User("John Doe", "john@example.com", "+91 9876543210", "Password123!", "123 Main St", Role.USER);
        normalUser = userRepository.save(normalUser);
        userToken = jwtService.generateToken(normalUser.getId(), normalUser.getEmail(), normalUser.getRole().name());
    }

    @Test
    void testNormalUserBlockedFromAdminUserEndpoints() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/admin/users/" + normalUser.getId())
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void testGetAllUsersAndFiltering() throws Exception {
        // Fetch all users
        mockMvc.perform(get("/api/admin/users")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(2)));

        // Filter by search keyword
        mockMvc.perform(get("/api/admin/users")
                .param("search", "John")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].email").value("john@example.com"));

        // Filter by role
        mockMvc.perform(get("/api/admin/users")
                .param("role", "ADMIN")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].email").value("admin@smarturban.com"));
    }

    @Test
    void testGetUserById() throws Exception {
        mockMvc.perform(get("/api/admin/users/" + normalUser.getId())
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.fullName").value("John Doe"))
                .andExpect(jsonPath("$.data.email").value("john@example.com"));
    }

    @Test
    void testCreateUser() throws Exception {
        CreateUserRequest request = new CreateUserRequest("Jane Smith", "jane@example.com", "+91 9876543211", "NewPass123!", Role.USER, "456 Oak St");

        mockMvc.perform(post("/api/admin/users")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("jane@example.com"))
                .andExpect(jsonPath("$.data.role").value("USER"));
    }

    @Test
    void testUpdateUser() throws Exception {
        UpdateUserRequest request = new UpdateUserRequest("Johnathan Doe", "john.doe@example.com", "+91 9876543210", Role.USER, "789 Pine St");

        mockMvc.perform(put("/api/admin/users/" + normalUser.getId())
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.fullName").value("Johnathan Doe"))
                .andExpect(jsonPath("$.data.email").value("john.doe@example.com"));
    }

    @Test
    void testUpdateUserStatusAndSafeguardLastActiveAdmin() throws Exception {
        // Deactivate normal user
        UpdateUserStatusRequest disableReq = new UpdateUserStatusRequest(false);
        mockMvc.perform(put("/api/admin/users/" + normalUser.getId() + "/status")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(disableReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.enabled").value(false));

        // Attempt to deactivate the LAST active ADMIN -> Should fail
        mockMvc.perform(put("/api/admin/users/" + adminUser.getId() + "/status")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(disableReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message", containsString("Cannot deactivate the last active ADMIN account")));
    }

    @Test
    void testSafeguardDemotingLastActiveAdmin() throws Exception {
        // Attempt to demote the LAST active ADMIN to USER -> Should fail
        UpdateUserRequest demoteReq = new UpdateUserRequest("System Admin", "admin@smarturban.com", "+91 9999999999", Role.USER, "HQ");
        mockMvc.perform(put("/api/admin/users/" + adminUser.getId())
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(demoteReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message", containsString("Cannot demote the last active ADMIN account")));
    }

    @Test
    void testResetUserPassword() throws Exception {
        ResetUserPasswordRequest resetReq = new ResetUserPasswordRequest("UpdatedPass123!");

        mockMvc.perform(put("/api/admin/users/" + normalUser.getId() + "/password")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(resetReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}

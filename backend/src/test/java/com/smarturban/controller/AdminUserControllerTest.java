package com.smarturban.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smarturban.dto.*;
import com.smarturban.entity.Role;
import com.smarturban.entity.User;
import com.smarturban.repository.UserRepository;
import com.smarturban.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AdminUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminToken;
    private User adminUser;
    private User regularUser;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        adminUser = new User(
                "Admin One",
                "admin1@smarturban.com",
                "+1111111111",
                passwordEncoder.encode("Password@123"),
                "Admin HQ",
                Role.ADMIN
        );
        adminUser = userRepository.save(adminUser);

        regularUser = new User(
                "John Citizen",
                "john@citizen.com",
                "+2222222222",
                passwordEncoder.encode("Password@123"),
                "Citizen Address",
                Role.USER
        );
        regularUser = userRepository.save(regularUser);

        adminToken = jwtService.generateToken(adminUser.getId(), adminUser.getEmail(), adminUser.getRole().name());
    }

    @Test
    void testGetAllUsers() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(2)));
    }

    @Test
    void testSearchAndFilterUsers() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                .header("Authorization", "Bearer " + adminToken)
                .param("search", "John")
                .param("role", "USER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].fullName", is("John Citizen")));
    }

    @Test
    void testGetUserById() throws Exception {
        mockMvc.perform(get("/api/admin/users/" + regularUser.getId())
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.email", is("john@citizen.com")));
    }

    @Test
    void testCreateUser() throws Exception {
        AdminCreateUserRequest request = new AdminCreateUserRequest(
                "New Admin",
                "newadmin@smarturban.com",
                "+3333333333",
                "SecurePass123",
                Role.ADMIN,
                "New Admin Address"
        );

        mockMvc.perform(post("/api/admin/users")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.email", is("newadmin@smarturban.com")))
                .andExpect(jsonPath("$.data.role", is("ADMIN")));
    }

    @Test
    void testCreateUserDuplicateEmailFailure() throws Exception {
        AdminCreateUserRequest request = new AdminCreateUserRequest(
                "Duplicate User",
                "john@citizen.com",
                "+3333333333",
                "SecurePass123",
                Role.USER,
                "Address"
        );

        mockMvc.perform(post("/api/admin/users")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void testUpdateUser() throws Exception {
        AdminUpdateUserRequest request = new AdminUpdateUserRequest(
                "John Citizen Updated",
                "john.updated@citizen.com",
                "+2222222222",
                Role.USER,
                "Updated Address"
        );

        mockMvc.perform(put("/api/admin/users/" + regularUser.getId())
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.fullName", is("John Citizen Updated")))
                .andExpect(jsonPath("$.data.email", is("john.updated@citizen.com")));
    }

    @Test
    void testUpdateUserStatus() throws Exception {
        UserStatusUpdateRequest request = new UserStatusUpdateRequest(false);

        mockMvc.perform(put("/api/admin/users/" + regularUser.getId() + "/status")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.enabled", is(false)));

        User fetched = userRepository.findById(regularUser.getId()).orElseThrow();
        assertFalse(fetched.isEnabled());
    }

    @Test
    void testPreventDeactivatingLastActiveAdmin() throws Exception {
        UserStatusUpdateRequest request = new UserStatusUpdateRequest(false);

        mockMvc.perform(put("/api/admin/users/" + adminUser.getId() + "/status")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Cannot deactivate the last active ADMIN user")));
    }

    @Test
    void testChangeUserPassword() throws Exception {
        AdminChangePasswordRequest request = new AdminChangePasswordRequest("BrandNewPass123");

        mockMvc.perform(put("/api/admin/users/" + regularUser.getId() + "/password")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));

        User fetched = userRepository.findById(regularUser.getId()).orElseThrow();
        assertTrue(passwordEncoder.matches("BrandNewPass123", fetched.getPassword()));
    }
}

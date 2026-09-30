package com.smarturban.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smarturban.dto.AdminChangePasswordRequest;
import com.smarturban.dto.CreateUserRequest;
import com.smarturban.dto.UpdateUserRequest;
import com.smarturban.dto.UserStatusRequest;
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
import static org.junit.jupiter.api.Assertions.*;
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
    private String userToken;
    private User adminUser;
    private User normalUser;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        adminUser = new User(
                "Admin One",
                "admin1@test.com",
                "+1111111111",
                passwordEncoder.encode("Password@123"),
                "Admin HQ",
                Role.ADMIN
        );
        adminUser = userRepository.save(adminUser);
        adminToken = jwtService.generateToken(adminUser.getId(), adminUser.getEmail(), adminUser.getRole().name());

        normalUser = new User(
                "User One",
                "user1@test.com",
                "+2222222222",
                passwordEncoder.encode("Password@123"),
                "User Home",
                Role.USER
        );
        normalUser = userRepository.save(normalUser);
        userToken = jwtService.generateToken(normalUser.getId(), normalUser.getEmail(), normalUser.getRole().name());
    }

    @Test
    void adminCanListUsers() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].password").doesNotExist())
                .andExpect(jsonPath("$.data[0].passwordHash").doesNotExist());
    }

    @Test
    void adminCanFilterUsersBySearchRoleAndStatus() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                .param("search", "User")
                .param("role", "USER")
                .param("enabled", "true")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].email").value("user1@test.com"));
    }

    @Test
    void adminCanViewUser() throws Exception {
        mockMvc.perform(get("/api/admin/users/" + normalUser.getId())
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(normalUser.getId()))
                .andExpect(jsonPath("$.data.email").value("user1@test.com"))
                .andExpect(jsonPath("$.data.password").doesNotExist())
                .andExpect(jsonPath("$.data.passwordHash").doesNotExist());
    }

    @Test
    void adminCanCreateUser() throws Exception {
        CreateUserRequest request = new CreateUserRequest(
                "New Citizen",
                "newcitizen@test.com",
                "+3333333333",
                "Password@123",
                "City Center",
                Role.USER,
                true
        );

        mockMvc.perform(post("/api/admin/users")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.email").value("newcitizen@test.com"))
                .andExpect(jsonPath("$.data.password").doesNotExist())
                .andExpect(jsonPath("$.data.passwordHash").doesNotExist());

        User saved = userRepository.findByEmail("newcitizen@test.com").orElse(null);
        assertNotNull(saved);
        assertTrue(passwordEncoder.matches("Password@123", saved.getPassword()));
    }

    @Test
    void duplicateEmailIsRejectedOnCreate() throws Exception {
        CreateUserRequest request = new CreateUserRequest(
                "Duplicate Email User",
                "user1@test.com",
                "+3333333333",
                "Password@123",
                "City Center",
                Role.USER,
                true
        );

        mockMvc.perform(post("/api/admin/users")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void adminCanUpdateUser() throws Exception {
        UpdateUserRequest request = new UpdateUserRequest(
                "User One Updated",
                "user1.updated@test.com",
                "+2222229999",
                "New Address",
                Role.USER,
                true
        );

        mockMvc.perform(put("/api/admin/users/" + normalUser.getId())
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fullName").value("User One Updated"))
                .andExpect(jsonPath("$.data.email").value("user1.updated@test.com"));
    }

    @Test
    void adminCanActivateDeactivateUser() throws Exception {
        UserStatusRequest statusRequest = new UserStatusRequest(false);

        mockMvc.perform(put("/api/admin/users/" + normalUser.getId() + "/status")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(statusRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.enabled").value(false));

        User updated = userRepository.findById(normalUser.getId()).orElse(null);
        assertNotNull(updated);
        assertFalse(updated.isEnabled());
    }

    @Test
    void adminCanChangePassword() throws Exception {
        AdminChangePasswordRequest request = new AdminChangePasswordRequest("NewSecretPass123");

        mockMvc.perform(put("/api/admin/users/" + normalUser.getId() + "/password")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.password").doesNotExist())
                .andExpect(jsonPath("$.data.passwordHash").doesNotExist());

        User updated = userRepository.findById(normalUser.getId()).orElse(null);
        assertNotNull(updated);
        assertTrue(passwordEncoder.matches("NewSecretPass123", updated.getPassword()));
    }

    @Test
    void normalUserCannotAccessAdminApis() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void lastActiveAdminCannotBeDeactivatedOrDemoted() throws Exception {
        // Attempt to deactivate last active admin
        UserStatusRequest statusRequest = new UserStatusRequest(false);

        mockMvc.perform(put("/api/admin/users/" + adminUser.getId() + "/status")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(statusRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("last active ADMIN")));

        // Attempt to demote last active admin
        UpdateUserRequest updateRequest = new UpdateUserRequest(
                adminUser.getFullName(),
                adminUser.getEmail(),
                adminUser.getPhone(),
                adminUser.getAddress(),
                Role.USER,
                true
        );

        mockMvc.perform(put("/api/admin/users/" + adminUser.getId())
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("last active ADMIN")));
    }
}

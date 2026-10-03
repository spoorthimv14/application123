package com.smarturban.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smarturban.dto.*;
import com.smarturban.entity.Role;
import com.smarturban.entity.User;
import com.smarturban.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestPropertySource(locations = "classpath:application-test.properties")
public class AdminUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String adminToken;
    private String userToken;
    private User adminUser;
    private User normalUser;

    @BeforeEach
    void setUp() throws Exception {
        adminUser = userRepository.findByEmail("admin@smarturban.com").orElseGet(() -> {
            User u = new User("System Admin", "admin@smarturban.com", "+919999999999", passwordEncoder.encode("Admin@12345"), "HQ Address", Role.ADMIN);
            return userRepository.save(u);
        });

        normalUser = userRepository.findByEmail("john@smarturban.com").orElseGet(() -> {
            User u = new User("John Doe", "john@smarturban.com", "+918888888888", passwordEncoder.encode("User@12345"), "City Address", Role.USER);
            return userRepository.save(u);
        });

        // Get Tokens
        adminToken = obtainToken("admin@smarturban.com", "Admin@12345");
        userToken = obtainToken("john@smarturban.com", "User@12345");
    }

    private String obtainToken(String email, String password) throws Exception {
        LoginRequest req = new LoginRequest(email, password);
        MvcResult res = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(res.getResponse().getContentAsString()).path("data").path("token").asText();
    }

    @Test
    void testAdminListUsersSuccess() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].password").doesNotExist())
                .andExpect(jsonPath("$.data[0].passwordHash").doesNotExist());
    }

    @Test
    void testAdminListUsersWithSearchAndFilter() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .param("search", "john")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].email").value("john@smarturban.com"));

        mockMvc.perform(get("/api/admin/users")
                        .param("role", "ADMIN")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].email").value("admin@smarturban.com"));
    }

    @Test
    void testAdminGetUserByIdSuccess() throws Exception {
        mockMvc.perform(get("/api/admin/users/" + normalUser.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("john@smarturban.com"))
                .andExpect(jsonPath("$.data.password").doesNotExist());
    }

    @Test
    void testAdminCreateUserSuccess() throws Exception {
        CreateUserRequest req = new CreateUserRequest("Jane Smith", "jane@smarturban.com", "+917777777777", "Secret@123", "Main St", Role.USER);

        MvcResult res = mockMvc.perform(post("/api/admin/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("jane@smarturban.com"))
                .andExpect(jsonPath("$.data.password").doesNotExist())
                .andExpect(jsonPath("$.data.passwordHash").doesNotExist())
                .andReturn();

        // Verify password in DB is BCrypt hashed
        User savedUser = userRepository.findByEmail("jane@smarturban.com").orElseThrow();
        assertTrue(passwordEncoder.matches("Secret@123", savedUser.getPassword()));
    }

    @Test
    void testAdminCreateUserDuplicateEmailRejected() throws Exception {
        CreateUserRequest req = new CreateUserRequest("Duplicate User", "john@smarturban.com", "+917777777777", "Secret@123", "Main St", Role.USER);

        mockMvc.perform(post("/api/admin/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void testAdminUpdateUserSuccess() throws Exception {
        UpdateUserRequest req = new UpdateUserRequest("John Updated", "john.updated@smarturban.com", "+918888888888", "New St", Role.USER);

        mockMvc.perform(put("/api/admin/users/" + normalUser.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fullName").value("John Updated"))
                .andExpect(jsonPath("$.data.email").value("john.updated@smarturban.com"));
    }

    @Test
    void testAdminUpdateUserStatusSuccessAndDeactivate() throws Exception {
        UpdateUserStatusRequest req = new UpdateUserStatusRequest(false);

        mockMvc.perform(put("/api/admin/users/" + normalUser.getId() + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.enabled").value(false));

        User updatedUser = userRepository.findById(normalUser.getId()).orElseThrow();
        assertFalse(updatedUser.isEnabled());
    }

    @Test
    void testAdminResetPasswordSuccess() throws Exception {
        ResetUserPasswordRequest req = new ResetUserPasswordRequest("NewPass@123");

        mockMvc.perform(put("/api/admin/users/" + normalUser.getId() + "/password")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.password").doesNotExist());

        User updatedUser = userRepository.findById(normalUser.getId()).orElseThrow();
        assertTrue(passwordEncoder.matches("NewPass@123", updatedUser.getPassword()));
    }

    @Test
    void testNormalUserAccessDeniedToAdminUserApi() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void testInvalidUserIdHandled() throws Exception {
        mockMvc.perform(get("/api/admin/users/999999")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void testPreventDeactivateLastAdminAccount() throws Exception {
        UpdateUserStatusRequest req = new UpdateUserStatusRequest(false);

        mockMvc.perform(put("/api/admin/users/" + adminUser.getId() + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void testPreventDemoteLastAdminAccount() throws Exception {
        UpdateUserRequest req = new UpdateUserRequest("Admin Demoted", "admin@smarturban.com", "+919999999999", "HQ", Role.USER);

        mockMvc.perform(put("/api/admin/users/" + adminUser.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
}

package com.smarturban.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smarturban.dto.*;
import com.smarturban.entity.User;
import com.smarturban.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
public class AdminUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    private String getAdminToken() throws Exception {
        LoginRequest adminLogin = new LoginRequest("admin@smarturban.com", "Admin@12345");
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminLogin)))
                .andExpect(status().isOk())
                .andReturn();
        String json = result.getResponse().getContentAsString();
        return objectMapper.readTree(json).path("data").path("path").isMissingNode()
                ? objectMapper.readTree(json).path("data").path("token").asText()
                : objectMapper.readTree(json).path("data").path("token").asText();
    }

    private String getUserToken(String email, String password) throws Exception {
        LoginRequest userLogin = new LoginRequest(email, password);
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userLogin)))
                .andExpect(status().isOk())
                .andReturn();
        String json = result.getResponse().getContentAsString();
        return objectMapper.readTree(json).path("data").path("token").asText();
    }

    @Test
    public void testUserManagementFullLifecycle() throws Exception {
        String adminToken = getAdminToken();

        // Register a normal user for role testing
        RegisterRequest regReq = new RegisterRequest(
                "Normal User",
                "normaluser@smarturban.com",
                "+919111111111",
                "Password@123",
                "Password@123",
                "12 Main St"
        );
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(regReq)))
                .andExpect(status().isCreated());

        String userToken = getUserToken("normaluser@smarturban.com", "Password@123");

        // 1. Normal USER is denied access to /api/admin/users -> 403 Forbidden
        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());

        // 2. ADMIN can view user list -> 200 OK
        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());

        // 3. ADMIN creates a new user
        UserCreateRequest createReq = new UserCreateRequest(
                "Managed User",
                "managed@smarturban.com",
                "+919222222222",
                "Secret@123",
                "USER",
                "456 Park Ave"
        );
        MvcResult createResult = mockMvc.perform(post("/api/admin/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.fullName").value("Managed User"))
                .andExpect(jsonPath("$.data.email").value("managed@smarturban.com"))
                .andExpect(jsonPath("$.data.role").value("USER"))
                .andExpect(jsonPath("$.data.active").value(true))
                .andExpect(jsonPath("$.data.password").doesNotExist())
                .andExpect(jsonPath("$.data.passwordHash").doesNotExist())
                .andReturn();

        String createJson = createResult.getResponse().getContentAsString();
        long createdUserId = objectMapper.readTree(createJson).path("data").path("id").asLong();

        // 4. ADMIN creates duplicate email -> 409 Conflict
        mockMvc.perform(post("/api/admin/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isConflict());

        // 5. ADMIN fetches single user by ID -> 200 OK
        mockMvc.perform(get("/api/admin/users/" + createdUserId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(createdUserId))
                .andExpect(jsonPath("$.data.password").doesNotExist());

        // 6. ADMIN updates user details
        UserUpdateRequest updateReq = new UserUpdateRequest(
                "Managed User Updated",
                "managed@smarturban.com",
                "+919222222222",
                "USER",
                true,
                "Updated Address"
        );
        mockMvc.perform(put("/api/admin/users/" + createdUserId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.fullName").value("Managed User Updated"));

        // 7. ADMIN resets user password
        UserPasswordResetRequest resetReq = new UserPasswordResetRequest("NewPassword@123");
        mockMvc.perform(put("/api/admin/users/" + createdUserId + "/password")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resetReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // User can now log in with the new password
        getUserToken("managed@smarturban.com", "NewPassword@123");

        // 8. ADMIN deactivates user (soft delete)
        UserStatusUpdateRequest statusReq = new UserStatusUpdateRequest(false);
        mockMvc.perform(put("/api/admin/users/" + createdUserId + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.active").value(false));

        // Deactivated user cannot log in -> 401 Unauthorized
        LoginRequest deactivatedLogin = new LoginRequest("managed@smarturban.com", "NewPassword@123");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deactivatedLogin)))
                .andExpect(status().isUnauthorized());

        // 9. ADMIN reactivates user
        statusReq = new UserStatusUpdateRequest(true);
        mockMvc.perform(put("/api/admin/users/" + createdUserId + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.active").value(true));

        // Reactivated user can log in again
        getUserToken("managed@smarturban.com", "NewPassword@123");

        // 10. Protection for last active ADMIN
        User adminUser = userRepository.findByEmail("admin@smarturban.com").orElseThrow();

        // Attempting to deactivate the last active ADMIN -> 400 Bad Request
        statusReq = new UserStatusUpdateRequest(false);
        mockMvc.perform(put("/api/admin/users/" + adminUser.getId() + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        // Attempting to demote the last active ADMIN to USER -> 400 Bad Request
        UserUpdateRequest demoteReq = new UserUpdateRequest(
                adminUser.getFullName(),
                adminUser.getEmail(),
                adminUser.getPhone(),
                "USER",
                true,
                adminUser.getAddress()
        );
        mockMvc.perform(put("/api/admin/users/" + adminUser.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(demoteReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        // 11. Search & Filtering
        mockMvc.perform(get("/api/admin/users?search=Managed")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].fullName").value("Managed User Updated"));

        mockMvc.perform(get("/api/admin/users?role=ADMIN")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].role").value("ADMIN"));
    }
}

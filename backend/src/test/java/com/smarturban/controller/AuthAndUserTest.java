package com.smarturban.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smarturban.dto.LoginRequest;
import com.smarturban.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
public class AuthAndUserTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testFullAuthenticationFlow() throws Exception {
        // 1. Register User
        RegisterRequest registerRequest = new RegisterRequest(
                "Test Citizen",
                "citizen@smarturban.com",
                "+919876543210",
                "Password@123",
                "Password@123",
                "123 Smart Street"
        );

        MvcResult regResult = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("citizen@smarturban.com"))
                .andExpect(jsonPath("$.data.token").exists())
                .andReturn();

        // 2. Duplicate Registration Fail
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));

        // 3. Login
        LoginRequest loginRequest = new LoginRequest("citizen@smarturban.com", "Password@123");
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").exists())
                .andReturn();

        String responseString = loginResult.getResponse().getContentAsString();
        String token = objectMapper.readTree(responseString).path("data").path("token").asText();

        // 4. Invalid Password Login Fail
        LoginRequest badLogin = new LoginRequest("citizen@smarturban.com", "WrongPassword");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badLogin)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));

        // 5. Access Protected Endpoint without JWT -> 403 Forbidden (or 401 depending on Security setup)
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isForbidden());

        // 6. Access Protected Endpoint with valid JWT -> 200 OK
        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("citizen@smarturban.com"))
                .andExpect(jsonPath("$.data.fullName").value("Test Citizen"));
    }
}

package com.smarturban.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smarturban.dto.ComplaintRequest;
import com.smarturban.dto.RegisterRequest;
import com.smarturban.entity.User;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ComplaintControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private com.smarturban.repository.ComplaintRepository complaintRepository;

    @Autowired
    private JwtService jwtService;

    private String user1Token;
    private String user2Token;
    private User user1;
    private User user2;

    @BeforeEach
    void setUp() throws Exception {
        complaintRepository.deleteAll();
        userRepository.deleteAll();

        // Register user 1
        RegisterRequest reg1 = new RegisterRequest("Citizen One", "citizen1@smarturban.com", "9876543210", "Password123!", "Password123!", "Main Street");
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reg1)))
                .andExpect(status().isCreated());

        user1 = userRepository.findByEmail("citizen1@smarturban.com").orElseThrow();
        user1Token = jwtService.generateToken(user1.getId(), user1.getEmail(), user1.getRole().name());

        // Register user 2
        RegisterRequest reg2 = new RegisterRequest("Citizen Two", "citizen2@smarturban.com", "9876543211", "Password123!", "Password123!", "Side Street");
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reg2)))
                .andExpect(status().isCreated());

        user2 = userRepository.findByEmail("citizen2@smarturban.com").orElseThrow();
        user2Token = jwtService.generateToken(user2.getId(), user2.getEmail(), user2.getRole().name());
    }

    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder post(String url) {
        return org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post(url);
    }

    @Test
    void testCreateComplaintAndGetMyComplaints() throws Exception {
        ComplaintRequest req = new ComplaintRequest("Road/Pothole", "Pothole on Main St", "Deep pothole causing traffic", 12.9716, 77.5946, "Main Street, City");
        MockMultipartFile dataPart = new MockMultipartFile("data", "", "application/json", objectMapper.writeValueAsBytes(req));

        String resultStr = mockMvc.perform(multipart("/api/complaints")
                .file(dataPart)
                .header("Authorization", "Bearer " + user1Token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Pothole on Main St"))
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.data.complaintNumber").exists())
                .andReturn().getResponse().getContentAsString();

        // Fetch User 1 complaints
        mockMvc.perform(get("/api/complaints/my")
                .header("Authorization", "Bearer " + user1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].title").value("Pothole on Main St"));

        // User 2 should have 0 complaints
        mockMvc.perform(get("/api/complaints/my")
                .header("Authorization", "Bearer " + user2Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    void testUserCannotAccessOtherUsersComplaint() throws Exception {
        ComplaintRequest req = new ComplaintRequest("Garbage/Waste", "Dumped Trash", "Uncollected garbage near park", 12.9720, 77.5950, "Park Ave");
        MockMultipartFile dataPart = new MockMultipartFile("data", "", "application/json", objectMapper.writeValueAsBytes(req));

        String responseContent = mockMvc.perform(multipart("/api/complaints")
                .file(dataPart)
                .header("Authorization", "Bearer " + user1Token))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long complaintId = objectMapper.readTree(responseContent).get("data").get("id").asLong();

        // User 1 can access complaint
        mockMvc.perform(get("/api/complaints/" + complaintId)
                .header("Authorization", "Bearer " + user1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Dumped Trash"));

        // User 2 trying to access User 1 complaint should get 403 Forbidden
        mockMvc.perform(get("/api/complaints/" + complaintId)
                .header("Authorization", "Bearer " + user2Token))
                .andExpect(status().isForbidden());
    }

    @Test
    void testUnauthenticatedAccessFails() throws Exception {
        mockMvc.perform(get("/api/complaints/my"))
                .andExpect(status().isUnauthorized());
    }
}

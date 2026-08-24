package com.smarturban.controller;

import com.smarturban.dto.ApiResponse;
import com.smarturban.dto.AuthResponse;
import com.smarturban.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<AuthResponse>> getCurrentUser() {
        AuthResponse userResponse = userService.getCurrentUser();
        return ResponseEntity.ok(ApiResponse.success("User retrieved successfully", userResponse));
    }
}

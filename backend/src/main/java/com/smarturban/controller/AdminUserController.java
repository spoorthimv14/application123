package com.smarturban.controller;

import com.smarturban.dto.*;
import com.smarturban.entity.Role;
import com.smarturban.service.AdminUserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) Boolean enabled) {

        List<UserResponse> users = adminUserService.getAllUsers(search, role, enabled);
        return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully", users));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        UserResponse user = adminUserService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success("User details retrieved successfully", user));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@Valid @RequestBody AdminCreateUserRequest request) {
        UserResponse user = adminUserService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User created successfully", user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody AdminUpdateUserRequest request) {

        UserResponse user = adminUserService.updateUser(id, request);
        return ResponseEntity.ok(ApiResponse.success("User updated successfully", user));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserStatus(
            @PathVariable Long id,
            @Valid @RequestBody UserStatusUpdateRequest request) {

        UserResponse user = adminUserService.updateUserStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success("User status updated successfully", user));
    }

    @PutMapping("/{id}/password")
    public ResponseEntity<ApiResponse<UserResponse>> changeUserPassword(
            @PathVariable Long id,
            @Valid @RequestBody AdminChangePasswordRequest request) {

        UserResponse user = adminUserService.changeUserPassword(id, request);
        return ResponseEntity.ok(ApiResponse.success("User password changed successfully", user));
    }
}

package com.smarturban.controller;

import com.smarturban.dto.*;
import com.smarturban.service.AdminUserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserAdminResponse>>> getAllUsers(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "role", required = false) String role,
            @RequestParam(value = "active", required = false) Boolean active
    ) {
        List<UserAdminResponse> users = adminUserService.getAllUsers(search, role, active);
        return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully", users));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserAdminResponse>> getUserById(@PathVariable("id") Long id) {
        UserAdminResponse user = adminUserService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success("User retrieved successfully", user));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UserAdminResponse>> createUser(@Valid @RequestBody UserCreateRequest request) {
        UserAdminResponse createdUser = adminUserService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User created successfully", createdUser));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserAdminResponse>> updateUser(
            @PathVariable("id") Long id,
            @Valid @RequestBody UserUpdateRequest request
    ) {
        UserAdminResponse updatedUser = adminUserService.updateUser(id, request);
        return ResponseEntity.ok(ApiResponse.success("User updated successfully", updatedUser));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<UserAdminResponse>> updateUserStatus(
            @PathVariable("id") Long id,
            @Valid @RequestBody UserStatusUpdateRequest request
    ) {
        UserAdminResponse updatedUser = adminUserService.updateUserStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success("User status updated successfully", updatedUser));
    }

    @PutMapping("/{id}/password")
    public ResponseEntity<ApiResponse<UserAdminResponse>> resetPassword(
            @PathVariable("id") Long id,
            @Valid @RequestBody UserPasswordResetRequest request
    ) {
        UserAdminResponse updatedUser = adminUserService.resetPassword(id, request);
        return ResponseEntity.ok(ApiResponse.success("Password reset successfully", updatedUser));
    }
}

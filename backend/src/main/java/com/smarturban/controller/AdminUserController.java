package com.smarturban.controller;

import com.smarturban.dto.*;
import com.smarturban.entity.Role;
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
    public ResponseEntity<ApiResponse<List<AdminUserResponse>>> getAllUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) Boolean enabled
    ) {
        List<AdminUserResponse> users = adminUserService.getAllUsers(search, role, enabled);
        return ResponseEntity.ok(ApiResponse.success("Users fetched successfully", users));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AdminUserResponse>> getUserById(@PathVariable Long id) {
        AdminUserResponse user = adminUserService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success("User fetched successfully", user));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AdminUserResponse>> createUser(@Valid @RequestBody CreateUserRequest request) {
        AdminUserResponse createdUser = adminUserService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User created successfully", createdUser));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AdminUserResponse>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request
    ) {
        AdminUserResponse updatedUser = adminUserService.updateUser(id, request);
        return ResponseEntity.ok(ApiResponse.success("User updated successfully", updatedUser));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<AdminUserResponse>> updateUserStatus(
            @PathVariable Long id,
            @Valid @RequestBody UserStatusRequest request
    ) {
        AdminUserResponse updatedUser = adminUserService.updateUserStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success("User status updated successfully", updatedUser));
    }

    @PutMapping("/{id}/password")
    public ResponseEntity<ApiResponse<AdminUserResponse>> changePassword(
            @PathVariable Long id,
            @Valid @RequestBody AdminChangePasswordRequest request
    ) {
        AdminUserResponse updatedUser = adminUserService.changePassword(id, request);
        return ResponseEntity.ok(ApiResponse.success("User password updated successfully", updatedUser));
    }
}

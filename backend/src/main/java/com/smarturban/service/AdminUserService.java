package com.smarturban.service;

import com.smarturban.dto.*;
import com.smarturban.entity.Role;
import com.smarturban.entity.User;
import com.smarturban.exception.DuplicateEmailException;
import com.smarturban.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminUserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserAdminResponse> getAllUsers(String search, String roleStr, Boolean active) {
        Role role = null;
        if (roleStr != null && !roleStr.trim().isEmpty() && !"ALL".equalsIgnoreCase(roleStr)) {
            try {
                role = Role.valueOf(roleStr.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                // Ignore invalid role filter or treat as null
            }
        }

        List<User> users = userRepository.searchAndFilterUsers(search, role, active);
        return users.stream()
                .map(UserAdminResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public UserAdminResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with ID: " + id));
        return UserAdminResponse.fromEntity(user);
    }

    public UserAdminResponse createUser(UserCreateRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException("Email is already registered: " + request.getEmail());
        }

        Role role;
        try {
            role = Role.valueOf(request.getRole().trim().toUpperCase());
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid user role: " + request.getRole());
        }

        User user = new User(
                request.getFullName(),
                request.getEmail(),
                request.getPhone(),
                passwordEncoder.encode(request.getPassword()),
                request.getAddress(),
                role
        );
        user.setEnabled(true);

        User savedUser = userRepository.save(user);
        return UserAdminResponse.fromEntity(savedUser);
    }

    public UserAdminResponse updateUser(Long id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with ID: " + id));

        if (userRepository.existsByEmailAndIdNot(request.getEmail(), id)) {
            throw new DuplicateEmailException("Email is already registered by another user: " + request.getEmail());
        }

        Role targetRole;
        try {
            targetRole = Role.valueOf(request.getRole().trim().toUpperCase());
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid user role: " + request.getRole());
        }

        boolean willBeActive = request.getActive() != null ? request.getActive() : user.isEnabled();

        // Prevent removing or demoting the last active ADMIN
        if (user.getRole() == Role.ADMIN && user.isEnabled()) {
            if (targetRole != Role.ADMIN || !willBeActive) {
                long activeAdminCount = userRepository.countByRoleAndEnabled(Role.ADMIN, true);
                if (activeAdminCount <= 1) {
                    throw new IllegalArgumentException("Cannot deactivate or demote the last active ADMIN user.");
                }
            }
        }

        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setRole(targetRole);
        user.setEnabled(willBeActive);
        user.setAddress(request.getAddress());

        User updatedUser = userRepository.save(user);
        return UserAdminResponse.fromEntity(updatedUser);
    }

    public UserAdminResponse updateUserStatus(Long id, UserStatusUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with ID: " + id));

        boolean newActiveStatus = Boolean.TRUE.equals(request.getActive());

        // Prevent removing or demoting the last active ADMIN
        if (user.getRole() == Role.ADMIN && user.isEnabled() && !newActiveStatus) {
            long activeAdminCount = userRepository.countByRoleAndEnabled(Role.ADMIN, true);
            if (activeAdminCount <= 1) {
                throw new IllegalArgumentException("Cannot deactivate or demote the last active ADMIN user.");
            }
        }

        user.setEnabled(newActiveStatus);
        User updatedUser = userRepository.save(user);
        return UserAdminResponse.fromEntity(updatedUser);
    }

    public UserAdminResponse resetPassword(Long id, UserPasswordResetRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with ID: " + id));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        User updatedUser = userRepository.save(user);
        return UserAdminResponse.fromEntity(updatedUser);
    }
}

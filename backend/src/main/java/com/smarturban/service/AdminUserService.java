package com.smarturban.service;

import com.smarturban.dto.*;
import com.smarturban.entity.Role;
import com.smarturban.entity.User;
import com.smarturban.exception.DuplicateEmailException;
import com.smarturban.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public List<AdminUserResponse> getAllUsers(String search, Role role, Boolean enabled) {
        String querySearch = (search != null && !search.trim().isEmpty()) ? search.trim() : null;
        List<User> users = userRepository.searchAndFilterUsers(querySearch, role, enabled);
        return users.stream()
                .map(AdminUserResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public AdminUserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
        return AdminUserResponse.fromEntity(user);
    }

    @Transactional
    public AdminUserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException("Email address is already in use: " + request.getEmail());
        }

        User user = new User(
                request.getFullName(),
                request.getEmail(),
                request.getPhone(),
                passwordEncoder.encode(request.getPassword()),
                request.getAddress(),
                request.getRole() != null ? request.getRole() : Role.USER
        );

        if (request.getEnabled() != null) {
            user.setEnabled(request.getEnabled());
        }

        User savedUser = userRepository.save(user);
        return AdminUserResponse.fromEntity(savedUser);
    }

    @Transactional
    public AdminUserResponse updateUser(Long id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));

        if (!user.getEmail().equalsIgnoreCase(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException("Email address is already in use: " + request.getEmail());
        }

        // Check last active ADMIN safeguard if role or status is being changed
        boolean isCurrentlyActiveAdmin = user.getRole() == Role.ADMIN && user.isEnabled();
        boolean demotingRole = request.getRole() != Role.ADMIN;
        boolean deactivating = request.getEnabled() != null && !request.getEnabled();

        if (isCurrentlyActiveAdmin && (demotingRole || deactivating)) {
            long activeAdminCount = userRepository.countByRoleAndEnabled(Role.ADMIN, true);
            if (activeAdminCount <= 1) {
                throw new IllegalStateException("Cannot demote or deactivate the last active ADMIN user.");
            }
        }

        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setAddress(request.getAddress());
        user.setRole(request.getRole());
        if (request.getEnabled() != null) {
            user.setEnabled(request.getEnabled());
        }

        User updatedUser = userRepository.save(user);
        return AdminUserResponse.fromEntity(updatedUser);
    }

    @Transactional
    public AdminUserResponse updateUserStatus(Long id, UserStatusRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));

        boolean isCurrentlyActiveAdmin = user.getRole() == Role.ADMIN && user.isEnabled();
        boolean deactivating = request.getEnabled() != null && !request.getEnabled();

        if (isCurrentlyActiveAdmin && deactivating) {
            long activeAdminCount = userRepository.countByRoleAndEnabled(Role.ADMIN, true);
            if (activeAdminCount <= 1) {
                throw new IllegalStateException("Cannot deactivate the last active ADMIN user.");
            }
        }

        if (request.getEnabled() != null) {
            user.setEnabled(request.getEnabled());
        }

        User updatedUser = userRepository.save(user);
        return AdminUserResponse.fromEntity(updatedUser);
    }

    @Transactional
    public AdminUserResponse changePassword(Long id, AdminChangePasswordRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        User updatedUser = userRepository.save(user);
        return AdminUserResponse.fromEntity(updatedUser);
    }
}

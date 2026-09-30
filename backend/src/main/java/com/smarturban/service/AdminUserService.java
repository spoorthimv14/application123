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

    public List<UserResponse> getUsers(String search, Role role, Boolean enabled) {
        String cleanSearch = (search != null && !search.trim().isEmpty()) ? search.trim() : null;
        List<User> users = userRepository.searchAndFilterUsers(cleanSearch, role, enabled);
        return users.stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
        return mapToUserResponse(user);
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException("Email is already registered: " + request.getEmail());
        }

        Role role = request.getRole() != null ? request.getRole() : Role.USER;
        User user = new User(
                request.getFullName(),
                request.getEmail(),
                request.getPhone(),
                passwordEncoder.encode(request.getPassword()),
                request.getAddress(),
                role
        );

        User savedUser = userRepository.save(user);
        return mapToUserResponse(savedUser);
    }

    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));

        if (!user.getEmail().equalsIgnoreCase(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException("Email is already registered: " + request.getEmail());
        }

        // Check last active ADMIN safeguard if role is changed from ADMIN to USER
        if (user.getRole() == Role.ADMIN && request.getRole() != Role.ADMIN) {
            long activeAdminCount = userRepository.countByRoleAndEnabledTrue(Role.ADMIN);
            if (user.isEnabled() && activeAdminCount <= 1) {
                throw new IllegalStateException("Cannot demote the last active ADMIN account.");
            }
        }

        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }
        user.setAddress(request.getAddress());

        User updatedUser = userRepository.save(user);
        return mapToUserResponse(updatedUser);
    }

    @Transactional
    public UserResponse updateUserStatus(Long id, UpdateUserStatusRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));

        boolean newEnabled = Boolean.TRUE.equals(request.getEnabled());

        if (user.getRole() == Role.ADMIN && !newEnabled && user.isEnabled()) {
            long activeAdminCount = userRepository.countByRoleAndEnabledTrue(Role.ADMIN);
            if (activeAdminCount <= 1) {
                throw new IllegalStateException("Cannot deactivate the last active ADMIN account.");
            }
        }

        user.setEnabled(newEnabled);
        User updatedUser = userRepository.save(user);
        return mapToUserResponse(updatedUser);
    }

    @Transactional
    public UserResponse resetUserPassword(Long id, ResetUserPasswordRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        User updatedUser = userRepository.save(user);
        return mapToUserResponse(updatedUser);
    }

    private UserResponse mapToUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getPhone(),
                user.getRole().name(),
                user.getAddress(),
                user.isEnabled(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}

package com.smarturban.service;

import com.smarturban.dto.*;
import com.smarturban.entity.Role;
import com.smarturban.entity.User;
import com.smarturban.exception.DuplicateEmailException;
import com.smarturban.repository.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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

    public List<UserResponse> getAllUsers(String search, Role role, Boolean enabled) {
        List<User> users = userRepository.searchUsers(search, role, enabled);
        return users.stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));
        return mapToUserResponse(user);
    }

    public UserResponse createUser(AdminCreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException("Email is already registered: " + request.getEmail());
        }

        User user = new User(
                request.getFullName(),
                request.getEmail(),
                request.getPhone(),
                passwordEncoder.encode(request.getPassword()),
                request.getAddress(),
                request.getRole()
        );

        User savedUser = userRepository.save(user);
        return mapToUserResponse(savedUser);
    }

    public UserResponse updateUser(Long id, AdminUpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));

        if (!user.getEmail().equalsIgnoreCase(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException("Email is already registered: " + request.getEmail());
        }

        // Prevent demoting the last active ADMIN user
        if (user.getRole() == Role.ADMIN && request.getRole() != Role.ADMIN && user.isEnabled()) {
            long activeAdminCount = userRepository.countByRoleAndEnabledTrue(Role.ADMIN);
            if (activeAdminCount <= 1) {
                throw new IllegalArgumentException("Cannot change role of the last active ADMIN user");
            }
        }

        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setRole(request.getRole());
        user.setAddress(request.getAddress());

        User updatedUser = userRepository.save(user);
        return mapToUserResponse(updatedUser);
    }

    public UserResponse updateUserStatus(Long id, UserStatusUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));

        // Prevent deactivating the last active ADMIN user
        if (user.getRole() == Role.ADMIN && user.isEnabled() && Boolean.FALSE.equals(request.getEnabled())) {
            long activeAdminCount = userRepository.countByRoleAndEnabledTrue(Role.ADMIN);
            if (activeAdminCount <= 1) {
                throw new IllegalArgumentException("Cannot deactivate the last active ADMIN user");
            }
        }

        user.setEnabled(request.getEnabled());
        User updatedUser = userRepository.save(user);
        return mapToUserResponse(updatedUser);
    }

    public UserResponse changeUserPassword(Long id, AdminChangePasswordRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));

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
                user.isEnabled()
        );
    }
}

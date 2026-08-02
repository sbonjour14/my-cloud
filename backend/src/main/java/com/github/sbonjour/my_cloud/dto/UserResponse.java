package com.github.sbonjour.my_cloud.dto;

import com.github.sbonjour.my_cloud.entity.User;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String email,
        String displayName,
        User.Role role,
        Instant createdAt) {
    public static UserResponse fromEntity(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getDisplayName(),
                user.getRole(),
                user.getCreatedAt());
    }
}
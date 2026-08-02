package com.github.sbonjour.my_cloud.controller;

import com.github.sbonjour.my_cloud.dto.UserResponse;
import com.github.sbonjour.my_cloud.entity.User;
import com.github.sbonjour.my_cloud.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();          // (1)

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User introuvable"));

        return ResponseEntity.ok(UserResponse.fromEntity(user));
    }
}
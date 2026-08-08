package com.github.sbonjour.my_cloud.controller;

import com.github.sbonjour.my_cloud.dto.UserResponse;
import com.github.sbonjour.my_cloud.entity.User;
import com.github.sbonjour.my_cloud.service.AuthService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        User user = authService.register(request.email(), request.password(), request.displayName());
        return ResponseEntity.ok(UserResponse.fromEntity(user));
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@Valid @RequestBody LoginRequest request) {
        String token = authService.login(request.email(), request.password());
        return ResponseEntity.ok(token);
    }

    public record RegisterRequest(
            @NotBlank(message = "Email is required") @Email(message = "Invalid email format") String email,

            @NotBlank(message = "Password is required") @Size(min = 8, message = "Password must be at least 8 characters") String password,

            @NotBlank(message = "Display name is required") String displayName) {
    }

    public record LoginRequest(
        @NotBlank(message = "Email is required") @Email(message = "Invalid email format") String email,
        @NotBlank(message = "Password is required") String password) {
    }
}
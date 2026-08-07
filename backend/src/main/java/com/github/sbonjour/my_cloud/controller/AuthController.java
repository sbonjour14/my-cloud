package com.github.sbonjour.my_cloud.controller;

import com.github.sbonjour.my_cloud.dto.UserResponse;
import com.github.sbonjour.my_cloud.entity.User;
import com.github.sbonjour.my_cloud.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@RequestBody RegisterRequest request) {
        if(request == null || request.email() == null || request.password() == null || request.displayName() == null) {
            return ResponseEntity.badRequest().build();
        }
        User user = authService.register(request.email(), request.password(), request.displayName());
        return ResponseEntity.ok(UserResponse.fromEntity(user));
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest request) {
        if (request == null || request.email() == null || request.password() == null) {
            return ResponseEntity.badRequest().build();
        }
        String token = authService.login(request.email(), request.password());
        return ResponseEntity.ok(token);
    }

    public record RegisterRequest(String email, String password, String displayName) {
    }

    public record LoginRequest(String email, String password) {
    }
}
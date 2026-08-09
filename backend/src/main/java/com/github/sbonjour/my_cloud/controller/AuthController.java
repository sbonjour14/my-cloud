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

    /**
     * Registers a new user account.
     *
     * The request body is validated via Bean Validation (@Valid) before
     * reaching this method, ensuring email, password, and displayName
     * meet the required format and constraints. Delegates the actual
     * account creation (uniqueness checks, password hashing) to AuthService.
     *
     * @param request the registration payload (email, password, displayName)
     * @return 200 OK with the created user's public representation
     */

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        User user = authService.register(request.email(), request.password(), request.displayName());
        return ResponseEntity.ok(UserResponse.fromEntity(user));
    }

    /**
     * Authenticates a user and returns a JWT token.
     *
     * The request body is validated via Bean Validation (@Valid) before
     * reaching this method, ensuring email and password are provided.
     * Delegates the actual authentication (credential verification,
     * token generation) to AuthService.
     *
     * @param request the login payload (email, password)
     * @return 200 OK with the generated JWT token
     */

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        String token = authService.login(request.email(), request.password());
        return ResponseEntity.ok(new LoginResponse(token));
    }

    /**
     * Request payload for user registration.
     *
     * Includes validation annotations to ensure the email is in a valid
     * format, the password meets complexity requirements, and the display
     * name is not blank. These constraints are enforced automatically by
     * Spring's validation framework when @Valid is used in controller methods.
     */

    public record RegisterRequest(
            @NotBlank(message = "Email is required") @Email(message = "Invalid email format") String email,
            @NotBlank(message = "Password is required") @Size(min = 8, message = "Password must be at least 8 characters") String password,
            @NotBlank(message = "Display name is required") String displayName) {
    }

    /**
     * Request payload for user login.
     *
     * Includes validation annotations to ensure the email is in a valid
     * format and the password is not blank. These constraints are enforced
     * automatically by Spring's validation framework when @Valid is used
     * in controller methods.
     */
    public record LoginRequest(
        @NotBlank(message = "Email is required") @Email(message = "Invalid email format") String email,
        @NotBlank(message = "Password is required") String password) {
    }


    public record LoginResponse(String token) {
    }
}
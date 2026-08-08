package com.github.sbonjour.my_cloud.controller;

import com.github.sbonjour.my_cloud.dto.UserResponse;
import com.github.sbonjour.my_cloud.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController {

    /**
     * Retrieves the currently authenticated user's information.
     *
     * The user is obtained from the Spring Security Authentication object,
     * which is automatically populated based on the JWT token provided in
     * the request. The response includes a public representation of the user.
     *
     * @param authentication the Spring Security Authentication object
     * @return 200 OK with UserResponse containing user details
     */

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(Authentication authentication) {
        User user = (User) authentication.getPrincipal();

        return ResponseEntity.ok(UserResponse.fromEntity(user));
    }
}
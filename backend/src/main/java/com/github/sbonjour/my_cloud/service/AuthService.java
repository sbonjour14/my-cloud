package com.github.sbonjour.my_cloud.service;

import com.github.sbonjour.my_cloud.entity.User;
import com.github.sbonjour.my_cloud.exception.EmailAlreadyUsedException;
import com.github.sbonjour.my_cloud.exception.InvalidCredentialsException;
import com.github.sbonjour.my_cloud.exception.InvalidInputException;
import com.github.sbonjour.my_cloud.repository.UserRepository;
import com.github.sbonjour.my_cloud.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public User register(String email, String rawPassword, String displayName) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new EmailAlreadyUsedException("this email address is already used");
        }
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new InvalidInputException("invalid email address");
        }
        if (!rawPassword.matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=.!])(?=\\S+$).{8,}$")) {
            throw new InvalidInputException("invalid password");
        }

        User newUser = User.builder()
                .email(email)
                .displayName(displayName)
                .password(passwordEncoder.encode(rawPassword))
                .build();
        return userRepository.save(newUser);
    }

    public String login(String email, String rawPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException("invalid email or password"));
        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new InvalidCredentialsException("invalid email or password");
        }
        return jwtService.generateToken(user);
    }
}
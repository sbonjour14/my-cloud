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

    /**
     * Password must contain at least one digit, one lowercase, one uppercase,
     * one special character (@#$%^&+=.!), and be at least 8 characters long,
     * with no whitespace.
     */
    private static final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=.!])(?=\\S+$).{8,}$";

    private void validatePassword(String password) {
        if (!password.matches(PASSWORD_PATTERN)) {
            throw new InvalidInputException("Password must contain at least one digit, one lowercase, one uppercase, one special character (@#$%^&+=.!), and be at least 8 characters long, with no whitespace.");
        }
    }
    public User register(String email, String rawPassword, String displayName) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new EmailAlreadyUsedException("this email address is already used");
        }
        validatePassword(rawPassword);

        User newUser = User.builder()
                .email(email)
                .displayName(displayName)
                .password(passwordEncoder.encode(rawPassword))
                .build();
        return userRepository.save(newUser);
    }

    public String login(String email, String rawPassword) {
        User user = userRepository
            .findByEmail(email)
            .orElseThrow(() -> new InvalidCredentialsException("invalid email or password"));
            
        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new InvalidCredentialsException("invalid email or password");
        }
        return jwtService.generateToken(user);
    }
}
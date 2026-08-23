package com.github.sbonjour.my_cloud.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.github.sbonjour.my_cloud.entity.User;
import com.github.sbonjour.my_cloud.entity.User.Role;
import com.github.sbonjour.my_cloud.repository.UserRepository;


@Configuration
public class DataInitializer {
    @Value("root.email")
    private String rootEmail;
    @Value("root.displayName")
    private String rootDisplayName;
    @Value("root.password")
    private String rootPassword;



    @Bean
    CommandLineRunner initUsers(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if(userRepository.findByEmail(rootEmail).isEmpty()) {
                User root = User.builder()
                .displayName(rootDisplayName)
                .email(rootEmail)
                .password(passwordEncoder.encode(rootPassword))
                .role(Role.ADMIN)
                .build();

                userRepository.save(root);
            }
        };
    }
    
}

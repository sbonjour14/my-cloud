package com.github.sbonjour.my_cloud.config;

import com.github.sbonjour.my_cloud.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Provides the password encoder used for hashing and verifying
     * passwords during registration and login. BCrypt includes automatic
     * salting and a configurable cost factor, making it resistant to
     * brute-force attacks.
     *
     * @return a BCryptPasswordEncoder instance
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configures the application's security filter chain.
     *
     * Authentication is stateless (JWT-based, no server-side session),
     * so CSRF is disabled and no session is created. Authentication
     * endpoints (/auth/**) and the error page (/error) are public;
     * everything else requires an authenticated user via JwtAuthenticationFilter,
     * which runs before the standard UsernamePasswordAuthenticationFilter.
     *
     * @param http the HTTP security builder provided by Spring Security
     * @return the built security filter chain
     * @throws Exception if the configuration fails to build
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/auth/**").permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
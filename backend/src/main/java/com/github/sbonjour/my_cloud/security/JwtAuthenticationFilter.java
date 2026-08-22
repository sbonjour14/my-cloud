package com.github.sbonjour.my_cloud.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import com.github.sbonjour.my_cloud.entity.User;
import com.github.sbonjour.my_cloud.repository.UserRepository;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private static final String INTERNAL_PATH_PATTERN = "/media/*/thumbnail-ready";

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return pathMatcher.match(INTERNAL_PATH_PATTERN, request.getRequestURI());
    }


    /**
     * Filters incoming HTTP requests to authenticate users based on JWT tokens.
     *
     * This filter checks for the presence of a JWT token in the Authorization header.
     * If a valid token is found, it extracts the user ID, retrieves the corresponding
     * User entity from the database, and sets the authentication in the security context.
     *
     * @param request the incoming HTTP request
     * @param response the HTTP response
     * @param filterChain the filter chain to pass control to the next filter
     * @throws ServletException if an error occurs during filtering
     * @throws IOException if an I/O error occurs during filtering
     */

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        if (jwtService.isTokenValid(token)) {
            UUID userId = jwtService.extractUserId(token);

            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                filterChain.doFilter(request, response);
                return;
            }

            // Add the user's role as a Spring Security authority, prefixed with "ROLE_"
            var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));

            var authentication = new UsernamePasswordAuthenticationToken(
                    user, null, authorities);

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }
}
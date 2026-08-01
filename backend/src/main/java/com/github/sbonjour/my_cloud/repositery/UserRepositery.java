package com.github.sbonjour.my_cloud.repositery;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.github.sbonjour.my_cloud.entity.User;

public interface UserRepositery extends JpaRepository<User, UUID>{
    Optional<User> findByEmail(String email);
}

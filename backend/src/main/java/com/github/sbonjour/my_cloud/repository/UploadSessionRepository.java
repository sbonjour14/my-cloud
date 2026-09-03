package com.github.sbonjour.my_cloud.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.github.sbonjour.my_cloud.entity.UploadSession;
import com.github.sbonjour.my_cloud.entity.User;

public interface UploadSessionRepository extends JpaRepository<UploadSession, UUID>{
    
    public Optional<UploadSession> findByChecksumAndUser(String checksum, User user);
    
}

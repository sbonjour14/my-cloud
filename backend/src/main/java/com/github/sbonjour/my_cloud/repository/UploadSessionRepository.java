package com.github.sbonjour.my_cloud.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.github.sbonjour.my_cloud.entity.UploadSession;

public interface UploadSessionRepository extends JpaRepository<UploadSession, UUID>{
    
    public Optional<UploadSession> findById(UUID id);

    public Optional<UploadSession> findByChecksum(String checksum);

    
}

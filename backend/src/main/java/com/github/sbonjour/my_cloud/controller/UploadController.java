package com.github.sbonjour.my_cloud.controller;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.sbonjour.my_cloud.entity.Upload;
import com.github.sbonjour.my_cloud.entity.User;
import com.github.sbonjour.my_cloud.service.UploadService;

import lombok.AllArgsConstructor;

@RestController 
@AllArgsConstructor 
public class UploadController {
    private final UploadService service;

    @GetMapping("/uploads")
    public ResponseEntity<List<UploadResponse>> getAll(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(service.findByUser(user).stream().map(UploadResponse::from).toList());
    }

    public record UploadResponse(
    UUID id,
    String filename,
    long totalSize,
    Instant createdAt
) {
    public static UploadResponse from(Upload u) {
        return new UploadResponse(u.getId(), u.getFilename(), u.getTotalSize(), u.getCreatedAt());
    }
}
    
}

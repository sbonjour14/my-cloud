package com.github.sbonjour.my_cloud.controller;

import java.util.UUID;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.github.sbonjour.my_cloud.dto.MediaAssetResponse;
import com.github.sbonjour.my_cloud.entity.MediaAsset;
import com.github.sbonjour.my_cloud.entity.User;
import com.github.sbonjour.my_cloud.service.MediaAssetService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class MediaAssetController {
    private final MediaAssetService mediaAssetService;

    @PostMapping("/mediaAssets")
    public ResponseEntity<MediaAssetResponse> uploadMediaAsset(@RequestParam("file") MultipartFile file, @AuthenticationPrincipal User user) {
        MediaAsset mediaAsset = mediaAssetService.uploadFile(file, user);
        return ResponseEntity.ok(MediaAssetResponse.fromEntity(mediaAsset));
    }

    @GetMapping("/media/{id}")
    public ResponseEntity<Resource> getMediaAsset(@PathVariable("id") UUID id, @AuthenticationPrincipal User user) {
        MediaAsset mediaAsset = mediaAssetService.getMediaAsset(id, user);

        Resource file = new FileSystemResource(mediaAsset.getStoredFile().getStoragePath());

        return ResponseEntity
                .ok()
                .contentType(MediaType.parseMediaType(mediaAsset.getStoredFile().getMimeType()))
                .body(file);
    }

    @GetMapping("/mediaAssets/{id}")
    public ResponseEntity<MediaAssetResponse> getMediaAssetInfo(@PathVariable("id") UUID id,
            @AuthenticationPrincipal User user) {
        MediaAsset mediaAsset = mediaAssetService.getMediaAsset(id, user);
        return ResponseEntity.ok(MediaAssetResponse.fromEntity(mediaAsset));
    }
}

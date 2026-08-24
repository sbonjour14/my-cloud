package com.github.sbonjour.my_cloud.dto;

import java.time.Instant;
import java.util.UUID;

import com.github.sbonjour.my_cloud.entity.StoredFile.MediaType;

public record MediaAssetResponse(
    UUID id,
    String filename,
    String url,
    MediaType mediaType,
    Instant createdAt
) {
    public static MediaAssetResponse fromEntity(com.github.sbonjour.my_cloud.entity.MediaAsset mediaAsset) {
        return new MediaAssetResponse(
            mediaAsset.getId(),
            mediaAsset.getFilename(),
            "/media/" + mediaAsset.getId(),
            mediaAsset.getStoredFile().getMediaType(),
            mediaAsset.getCreatedAt()
        );
    }
    
}

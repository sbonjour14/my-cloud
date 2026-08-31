package com.github.sbonjour.my_cloud.dto;

import java.time.Instant;

public record MediaAssetResponse(
        String filename,
        String url,
        String thumbnailUrl,
        Instant createdAt,
        boolean hasThumbnail) {
    public static MediaAssetResponse fromEntity(com.github.sbonjour.my_cloud.entity.MediaAsset mediaAsset) {
        return new MediaAssetResponse(
                mediaAsset.getFilename(),
                "/media/" + mediaAsset.getId(),
                "/media/" + mediaAsset.getId() + "/thumbnail",
                mediaAsset.getCreatedAt(),
                mediaAsset.getStoredFile().isHasThumbnail());
    }

}

package com.github.sbonjour.my_cloud.dto;

import java.time.Instant;
import java.util.UUID;

import com.github.sbonjour.my_cloud.FileType;

public record MediaAssetDetailResponse(
        UUID id,
        String filename,
        String url,
        String thumbnailUrl,
        Instant createdAt,
        Instant takenAt,
        boolean hasThumbnail,
        FileType type) {
    public static MediaAssetDetailResponse fromEntity(com.github.sbonjour.my_cloud.entity.MediaAsset mediaAsset) {
        return new MediaAssetDetailResponse(
                mediaAsset.getId(),
                mediaAsset.getFilename(),
                "/media/" + mediaAsset.getId(),
                "/media/" + mediaAsset.getId() + "/thumbnail",
                mediaAsset.getCreatedAt(),
                mediaAsset.getTakenAt(),
                mediaAsset.getStoredFile().isHasThumbnail(),
                mediaAsset.getFileType()
            );
    }

}

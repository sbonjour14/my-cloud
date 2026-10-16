package com.github.sbonjour.my_cloud.dto;

import java.time.Instant;
import java.util.UUID;

import com.github.sbonjour.my_cloud.FileType;
import com.github.sbonjour.my_cloud.entity.MediaAsset;

public record MediaAssetSummaryResponse(
    UUID id,
    FileType type,
    boolean hasThumbnail,
    Instant takenAt
) {
public static MediaAssetSummaryResponse fromEntity(MediaAsset ma){
    return new MediaAssetSummaryResponse(ma.getId(), ma.getFileType(), ma.getStoredFile().isHasThumbnail(), ma.getTakenAt());
}}

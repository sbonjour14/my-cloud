package com.github.sbonjour.my_cloud.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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

    /**
     * Uploads a media asset for the authenticated user.
     *
     * The uploaded file is validated and stored, and a MediaAsset entity
     * is created to represent it. The response includes the public
     * representation of the newly created media asset.
     *
     * @param file the uploaded file
     * @param user the authenticated user (injected by Spring Security)
     * @return 200 OK with the created MediaAssetResponse
     */

    @PostMapping("/mediaAssets")
    public ResponseEntity<MediaAssetResponse> uploadMediaAsset(@RequestParam("file") MultipartFile file, @AuthenticationPrincipal User user) {
        MediaAsset mediaAsset = mediaAssetService.uploadFile(file, user);
        return ResponseEntity.ok(MediaAssetResponse.fromEntity(mediaAsset));
    }

    /**
     * Retrieves all media assets for the authenticated user.
     *
     * The response includes a list of public representations of the
     * user's media assets.
     *
     * @param user the authenticated user (injected by Spring Security)
     * @return 200 OK with a list of MediaAssetResponse
     */

    @GetMapping("/mediaAssets")
    public ResponseEntity<List<MediaAssetResponse>> getMediaAssets(@AuthenticationPrincipal User user) {
        List<MediaAsset> mediaAssets = mediaAssetService.getAll(user);

        List<MediaAssetResponse> response = mediaAssets.stream()
                .map(MediaAssetResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(response);
    }
    @PatchMapping("/media/{id}/thumbnail-ready")
    public ResponseEntity<?> markThumbnailReady(@PathVariable("id") UUID id) {
        mediaAssetService.markThumbnailReady(id);
        return ResponseEntity.ok(null);
    }

    /**
     * Retrieves the actual media file for a given media asset ID.
     *
     * The file is served with the appropriate MIME type. Access is
     * restricted to the owner of the media asset.
     *
     * @param id   the UUID of the media asset
     * @param user the authenticated user (injected by Spring Security)
     * @return 200 OK with the file as a Resource, or 404 if not found
     */
    @GetMapping("/media/{id}")
    public ResponseEntity<Resource> getMediaAsset(@PathVariable("id") UUID id, @AuthenticationPrincipal User user) {
        MediaAsset mediaAsset = mediaAssetService.getMediaAsset(id, user);

        Resource file = new FileSystemResource(mediaAsset.getStoredFile().getStoragePath());

        return ResponseEntity
                .ok()
                .contentType(MediaType.parseMediaType(mediaAsset.getStoredFile().getMimeType()))
                .body(file);
    }

    @GetMapping("/media/{id}/thumbnail")
    public ResponseEntity<Resource> getMediaAssetThumbnail(@PathVariable("id") UUID id, @AuthenticationPrincipal User user) {
        MediaAsset mediaAsset = mediaAssetService.getMediaAsset(id, user);

        Resource file = new FileSystemResource(mediaAsset.getStoredFile().getThumbnailPath());

        return ResponseEntity
                .ok()
                .contentType(MediaType.parseMediaType("image/webp"))
                .body(file);
    }

    /**
     * Retrieves the metadata for a given media asset ID.
     *
     * The response includes the public representation of the media asset.
     * Access is restricted to the owner of the media asset.
     *
     * @param id   the UUID of the media asset
     * @param user the authenticated user (injected by Spring Security)
     * @return 200 OK with MediaAssetResponse, or 404 if not found
     */
    @GetMapping("/mediaAssets/{id}")
    public ResponseEntity<MediaAssetResponse> getMediaAssetInfo(@PathVariable("id") UUID id,
            @AuthenticationPrincipal User user) {
        MediaAsset mediaAsset = mediaAssetService.getMediaAsset(id, user);
        return ResponseEntity.ok(MediaAssetResponse.fromEntity(mediaAsset));
    }

    /**
     * Deletes a media asset for the authenticated user.
     *
     * The media asset is removed from the database and the associated
     * stored file is deleted if it is no longer referenced by any other
     * media assets. Access is restricted to the owner of the media asset.
     *
     * @param id   the UUID of the media asset to delete
     * @param user the authenticated user (injected by Spring Security)
     * @return 204 No Content if deletion was successful, or 404 if not found
     */
    @DeleteMapping("/mediaAssets/{id}")
    public ResponseEntity<Void> deleteMediaAsset(@PathVariable("id") UUID id, @AuthenticationPrincipal User user) {
        mediaAssetService.deleteMediaAsset(id, user);
        return ResponseEntity.noContent().build();
    }

    /**
     * Updates the name of a media asset for the authenticated user.
     *
     * The media asset's name is updated in the database. Access is
     * restricted to the owner of the media asset.
     *
     * @param id   the UUID of the media asset to update
     * @param name the new name for the media asset
     * @param user the authenticated user (injected by Spring Security)
     * @return 200 OK with the updated MediaAssetResponse, or 404 if not found
     */
    @PatchMapping("/mediaAssets/{id}")
    public ResponseEntity<MediaAssetResponse> updateMediaAsset(@PathVariable("id") UUID id, @RequestParam("name") String name, @AuthenticationPrincipal User user) {
        MediaAsset mediaAsset = mediaAssetService.updateMediaAsset(id, name, user);
        return ResponseEntity.ok(MediaAssetResponse.fromEntity(mediaAsset));
    }

    /**
     * Retrieves the total storage size used by the authenticated user's media assets.
     * @param user the authenticated user (injected by Spring Security)
     * @return 200 OK with the total size in bytes
     */
    @GetMapping("/mediaAssets/size")
    public ResponseEntity<Long> getUserMediaAssetsSize(@AuthenticationPrincipal User user) {
        Long size = mediaAssetService.getUserMediaAssetSize(user);

        return ResponseEntity.ok(size);
    }
}

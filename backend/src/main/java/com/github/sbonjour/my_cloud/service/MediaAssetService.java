package com.github.sbonjour.my_cloud.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.github.sbonjour.my_cloud.entity.MediaAsset;
import com.github.sbonjour.my_cloud.entity.StoredFile;
import com.github.sbonjour.my_cloud.entity.User;
import com.github.sbonjour.my_cloud.entity.StoredFile.MediaType;
import com.github.sbonjour.my_cloud.exception.ConflictException;
import com.github.sbonjour.my_cloud.exception.InternalServerErrorException;
import com.github.sbonjour.my_cloud.exception.InvalidFileTypeException;
import com.github.sbonjour.my_cloud.exception.NotFoundException;
import com.github.sbonjour.my_cloud.repository.MediaAssetRepository;
import com.github.sbonjour.my_cloud.repository.StoredFileRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MediaAssetService {

    private final MediaAssetRepository mediaAssetRepository;
    private final StoredFileRepository storedFileRepository;

    @Value("${file.storage.path:/app/uploads}")
    private String uploadPath;

    private MediaType getMediaType(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null)
            throw new InvalidFileTypeException("File type is not supported");

        if (contentType.startsWith("image"))
            return MediaType.IMAGE;
        else if (contentType.startsWith("video"))
            return MediaType.VIDEO;
        else
            throw new InvalidFileTypeException("File type is not supported");
    }

    private String calculateChecksum(byte[] fileBytes) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(fileBytes);

            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new InternalServerErrorException("Algorithme SHA-256 not found");
        }

    }

    public MediaAsset uploadFile(MultipartFile file, User owner) {
        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw new InternalServerErrorException("Error while accessing the file");
        }

        if (mediaAssetRepository.findByOwnerAndFileNameIgnoringCase(owner, file.getOriginalFilename()).isPresent()) {
            throw new ConflictException("A media asset with the same name already exists for this user");
        }
        String checksum = calculateChecksum(bytes);

        StoredFile sf = storedFileRepository.findByChecksum(checksum).orElse(null);

        if (sf != null && mediaAssetRepository.findByOwnerAndStoredFile(owner, sf).isPresent()) {
            throw new ConflictException("A media asset with the same file already exists for this user");
        }

        if (sf == null) {
            String storagePath = uploadPath + "/" + checksum;
            try {
                file.transferTo(new java.io.File(storagePath));
            } catch (IOException e) {
                throw new InternalServerErrorException("Error while saving the file");
            }

            sf = StoredFile.builder()
                    .checksum(checksum)
                    .storagePath(storagePath)
                    .mimeType(file.getContentType())
                    .sizeBytes(file.getSize())
                    .mediaType(getMediaType(file))
                    .build();
            sf = storedFileRepository.save(sf);
        }

        MediaAsset mediaAsset = MediaAsset.builder()
                .owner(owner)
                .fileName(file.getOriginalFilename())
                .storedFile(sf)
                .build();

        return mediaAssetRepository.save(mediaAsset);
    }

    public MediaAsset getMediaAsset(UUID id, User user) {
        MediaAsset mediaAsset = mediaAssetRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Media asset not found"));

        if (!mediaAsset.getOwner().equals(user)) {
            throw new AccessDeniedException("You don't have access to this media asset");
        }

        return mediaAsset;
    }

    public void deleteMediaAsset(UUID id, User user) {
        MediaAsset mediaAsset = mediaAssetRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Media asset not found"));

        if (!mediaAsset.getOwner().equals(user)) {
            throw new AccessDeniedException("You don't have access to this media asset");
        }

        StoredFile storedFile = mediaAsset.getStoredFile();
        mediaAssetRepository.delete(mediaAsset);

        List<MediaAsset> remaining = mediaAssetRepository.findByStoredFile(storedFile);

        if(remaining.isEmpty()) {
            try {
                Files.deleteIfExists(Path.of(storedFile.getStoragePath()));
                
            } catch (IOException e) {
                throw new InternalServerErrorException("Error while deleting the file");
            }
            storedFileRepository.delete(storedFile);
        }
    }

    public List<MediaAsset> getAll(User user) {
        return mediaAssetRepository.findByOwner(user);
    }

    public MediaAsset updateMediaAsset(UUID id, String name, User user) {
        MediaAsset mediaAsset = mediaAssetRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Media asset not found"));

        if (!mediaAsset.getOwner().equals(user)) {
            throw new AccessDeniedException("You don't have access to this media asset");
        }

        if (mediaAssetRepository.findByOwnerAndFileNameIgnoringCase(user, name).isPresent()) {
            throw new ConflictException("A media asset with the same name already exists for this user");
        }

        mediaAsset.setFileName(name);
        return mediaAssetRepository.save(mediaAsset);
    }

}

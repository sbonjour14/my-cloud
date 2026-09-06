package com.github.sbonjour.my_cloud.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.github.sbonjour.my_cloud.config.RabbitMQConfig;
import com.github.sbonjour.my_cloud.entity.MediaAsset;
import com.github.sbonjour.my_cloud.entity.StoredFile;
import com.github.sbonjour.my_cloud.entity.User;
import com.github.sbonjour.my_cloud.exception.ConflictException;
import com.github.sbonjour.my_cloud.exception.InternalServerErrorException;
import com.github.sbonjour.my_cloud.exception.NotFoundException;
import com.github.sbonjour.my_cloud.repository.MediaAssetRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MediaAssetService {

    private final MediaAssetRepository mediaAssetRepository;
    private final StoredFileService storedFileService;
    private final RabbitTemplate rabbitTemplate;
    private final FileService fileService;

    @Value("${file.storage.path:/app/uploads}")
    private String uploadPath;



    public MediaAsset uploadFile(MultipartFile file, User owner) {
        if (mediaAssetRepository.findByOwnerAndFilenameIgnoringCase(owner, file.getOriginalFilename()).isPresent()) {
            throw new ConflictException("A media asset with the same name already exists for this user");
        }
        String checksum = fileService.calculateChecksum(file);

        StoredFile sf = storedFileService.findByChecksum(checksum);

        if (sf != null && mediaAssetRepository.findByOwnerAndStoredFile(owner, sf).isPresent()) {
            throw new ConflictException("A media asset with the same file already exists for this user");
        }

        if(sf == null) {
            String storagePath = uploadPath + "/" + checksum;
            try {
                sf = storedFileService.save(fileService.write(file, storagePath, checksum, fileService.getFileType(file)));
            } catch (IOException e) {
                throw new InternalServerErrorException("Error while saving the file");
            }

        }
        MediaAsset mediaAsset = MediaAsset.builder()
                .owner(owner)
                .filename(file.getOriginalFilename())
                .storedFile(sf)
                .build();

        mediaAsset = mediaAssetRepository.save(mediaAsset);
        if (!sf.isHasThumbnail()) {
            publishThumbnailGenerationMessage(mediaAsset);
        }
        return mediaAsset;
    }


    public MediaAsset createMediaAsset(User owner, String filename, StoredFile sf) {
        MediaAsset ma = mediaAssetRepository.save(MediaAsset.builder()
            .owner(owner)
            .filename(filename)
            .storedFile(sf)
            .build()
        );
        if(!sf.isHasThumbnail())
            publishThumbnailGenerationMessage(ma);
        return ma;
    }

    private void publishThumbnailGenerationMessage(MediaAsset mediaAsset) {
        Map<String, String> message = Map.of(
                "mediaAssetId", mediaAsset.getId().toString(),
                "storagePath", mediaAsset.getStoredFile().getStoragePath(),
                "fileType", mediaAsset.getStoredFile().getFileType().toString());

        rabbitTemplate.convertAndSend(RabbitMQConfig.THUMBNAIL_QUEUE, message);
    }

    public MediaAsset getMediaAsset(UUID id) {
        MediaAsset mediaAsset = mediaAssetRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Media asset not found"));
        return mediaAsset;
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
            storedFileService.delete(storedFile);
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

        if (mediaAssetRepository.findByOwnerAndFilenameIgnoringCase(user, name).isPresent()) {
            throw new ConflictException("A media asset with the same name already exists for this user");
        }

        mediaAsset.setFilename(name);
        return mediaAssetRepository.save(mediaAsset);
    }

    public Long getUserMediaAssetSize(User user) {
        return mediaAssetRepository.getUserTotalStorageUsed(user);
    }

    public MediaAsset findByOwnerAndStoredFile(User owner, StoredFile sf) {
        return mediaAssetRepository.findByOwnerAndStoredFile(owner, sf).orElse(null);
    }

    public void markThumbnailReady(UUID id) {
        MediaAsset ma = getMediaAsset(id);
        StoredFile sf = ma.getStoredFile();
        sf.setHasThumbnail(true);
        storedFileService.save(sf);
    }

}

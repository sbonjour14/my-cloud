package com.github.sbonjour.my_cloud.service;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.github.sbonjour.my_cloud.entity.MediaAsset;
import com.github.sbonjour.my_cloud.entity.StoredFile;
import com.github.sbonjour.my_cloud.entity.User;
import com.github.sbonjour.my_cloud.entity.StoredFile.MediaType;
import com.github.sbonjour.my_cloud.exception.ConflictException;
import com.github.sbonjour.my_cloud.exception.InternalServerErrorException;
import com.github.sbonjour.my_cloud.exception.InvalidFileTypeException;
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

        // Compute checksum 
        byte[] bytes;

        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw new InternalServerErrorException("Error while accessing the file");

        }

        String checksum = calculateChecksum(bytes);

        // check if storedFile already exists
        StoredFile sf = storedFileRepository.findByChecksum(checksum).orElse(null);
        if(sf == null) {
            // create file in storage
            String storagePath = uploadPath + "/" + checksum;
            try {
                file.transferTo(new java.io.File(storagePath));
            } catch (IOException e) {
                throw new InternalServerErrorException("Error while saving the file");
            }

            // Create new storedFile
            sf = StoredFile.builder().checksum(checksum).storagePath(storagePath).mimeType(file.getContentType()).sizeBytes(file.getSize()).mediaType(getMediaType(file)).build();
        } 
        // Save storedFile
        sf = storedFileRepository.save(sf);

        // check if mediaAsset already exists for this user and fileName
        MediaAsset existingMediaAsset = mediaAssetRepository.findByOwnerAndFileNameIgnoringCase(owner, file.getOriginalFilename()).orElse(null);
        if(existingMediaAsset != null) {
            throw new ConflictException("A media asset with the same name already exists for this user");
        }

        existingMediaAsset = mediaAssetRepository.findByOwnerAndStoredFile(owner, sf).orElse(null);
        if(existingMediaAsset != null) {
            throw new ConflictException("A media asset with the same file already exists for this user");
        }


        MediaAsset mediaAsset = MediaAsset.builder().owner(owner).fileName(file.getOriginalFilename()).storedFile(sf).build();

        return mediaAssetRepository.save(mediaAsset);
    }


    
}

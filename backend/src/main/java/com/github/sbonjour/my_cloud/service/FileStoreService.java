package com.github.sbonjour.my_cloud.service;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.github.sbonjour.my_cloud.entity.StoredFile;
import com.github.sbonjour.my_cloud.entity.StoredFile.MediaType;
import com.github.sbonjour.my_cloud.exception.InternalServerErrorException;

@Service
public class FileStoreService {

    public StoredFile write(MultipartFile file, String storagePath, String checksum, MediaType mediaType) throws IOException {

        file.transferTo(new java.io.File(storagePath));

        StoredFile sf = StoredFile.builder()
                .checksum(checksum)
                .storagePath(storagePath)
                .mimeType(file.getContentType())
                .sizeBytes(file.getSize())
                .mediaType(mediaType)
                .build();
        return sf;

    }


    public String calculateChecksum(byte[] fileBytes) {
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
}

package com.github.sbonjour.my_cloud.service;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.github.sbonjour.my_cloud.entity.StoredFile;
import com.github.sbonjour.my_cloud.entity.StoredFile.FileType;
import com.github.sbonjour.my_cloud.exception.InternalServerErrorException;
import com.github.sbonjour.my_cloud.exception.InvalidFileTypeException;

@Service
public class FileService {

    public StoredFile write(MultipartFile file, String storagePath, String checksum, FileType mediaType)
            throws IOException {

        file.transferTo(new java.io.File(storagePath));

        StoredFile sf = StoredFile.builder()
                .checksum(checksum)
                .storagePath(storagePath)
                .mediaType(file.getContentType())
                .sizeBytes(file.getSize())
                .fileType(mediaType)
                .build();
        return sf;

    }

    public String calculateChecksum(MultipartFile file) {
        MessageDigest digest;
        try (InputStream is = file.getInputStream()) {
            digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[65536];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }
        } catch (NoSuchAlgorithmException e) {
            throw new InternalServerErrorException("Error Upload the file");
        } catch (IOException e) {
            throw new InternalServerErrorException("Error accessing the file");
        }
        byte[] hash = digest.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : hash) {
            sb.append(String.format("%02x", b));
        }

        return sb.toString();

    }
    
    public FileType getFileType(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null)
            throw new InvalidFileTypeException("File type is not supported");

        if (contentType.startsWith("image"))
            return FileType.IMAGE;
        else if (contentType.startsWith("video"))
            return FileType.VIDEO;
        else
            throw new InvalidFileTypeException("File type is not supported");
    }
}

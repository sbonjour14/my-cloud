package com.github.sbonjour.my_cloud.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

import org.springframework.http.MediaType;
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
    public String calculateChecksum(Path path) throws IOException{
        InputStream is = Files.newInputStream(path);
        return calculateChecksum(is);
    }
    public String calculateChecksum(InputStream is) throws IOException {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[65536];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }
        } catch (NoSuchAlgorithmException e) {
            throw new InternalServerErrorException("Error Upload the file");
        }
        byte[] hash = digest.digest();
        return HexFormat.of().formatHex(hash);
    }

    public String calculateChecksum(MultipartFile file) {
        try (InputStream is = file.getInputStream()) {
            return calculateChecksum(is);
        } catch (IOException e) {
           throw new InternalServerErrorException("Error accessing the file");
        }
    }
    
    public FileType getFileType(MultipartFile file) {
        return getFileType(file.getContentType());
    }
    
    public FileType getFileType(MediaType mediaType) {
        return getFileType(mediaType.getType());
    }

    public FileType getFileType(String contentType) {
        if (contentType == null)
            throw new InvalidFileTypeException("File type is not supported");

        if (contentType.startsWith("image"))
            return FileType.IMAGE;
        else if (contentType.startsWith("video"))
            return FileType.VIDEO;
        else
            throw new InvalidFileTypeException("File type is not supported");
    }


    public boolean writeChunk(MultipartFile file, long position, Path tempFilePath) {
        try (FileChannel channel = FileChannel.open(tempFilePath, StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
            ByteBuffer byteBuffer = ByteBuffer.wrap(file.getBytes());
            channel.position(position);
            while (byteBuffer.hasRemaining())
                channel.write(byteBuffer);
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    public void renameFile(Path filePath, Path target) {
        try {
            if (Files.exists(target)) {
                Files.deleteIfExists(filePath);
                return;
            }
            Files.move(filePath, target);
        } catch (IOException e) {
            throw new InternalServerErrorException(
                    "An error occurred while moving the file: " + filePath + " to: " + target);
        }
    }
}

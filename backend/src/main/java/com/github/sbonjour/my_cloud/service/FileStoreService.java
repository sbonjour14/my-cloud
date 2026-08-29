package com.github.sbonjour.my_cloud.service;

import java.io.IOException;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.github.sbonjour.my_cloud.entity.StoredFile;
import com.github.sbonjour.my_cloud.entity.StoredFile.MediaType;

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
}

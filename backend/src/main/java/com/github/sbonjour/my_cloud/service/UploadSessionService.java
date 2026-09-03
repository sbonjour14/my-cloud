package com.github.sbonjour.my_cloud.service;

import java.util.HashSet;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.github.sbonjour.my_cloud.entity.MediaAsset;
import com.github.sbonjour.my_cloud.entity.StoredFile;
import com.github.sbonjour.my_cloud.entity.UploadSession;
import com.github.sbonjour.my_cloud.entity.User;
import com.github.sbonjour.my_cloud.entity.StoredFile.FileType;
import com.github.sbonjour.my_cloud.repository.UploadSessionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UploadSessionService {

    private final UploadSessionRepository repository;
    private final MediaAssetService mediaAssetService;
    private final StoredFileService storedFileService;
    private final FileService fileService;

    private long imageChunkSize = 1024*1024*2;
    private long videoChunkSize = 1024*1024*10;

    @Value("${file.storage.path:/app/uploads}")
    private String uploadPath;

    public UploadSession findByChecksumAndUser(String checksum, User user){
        return repository.findByChecksumAndUser(checksum, user).orElse(null);
    }


    public InitUploadResult init(String filename, MediaType mediaType, FileType fileType, long totalSize, int totalChunks, String checksum, User user) {
        UploadSession us = findByChecksumAndUser(checksum, user);
        if(us != null) 
            return InitUploadResult.from(us);
        
        StoredFile sf = storedFileService.findByChecksum(checksum);
        if(sf != null) {

            MediaAsset ma = mediaAssetService.findByOwnerAndStoredFile(user, sf);
            if(ma != null)
                return InitUploadResult.from(ma);

            ma = mediaAssetService.createMediaAsset(user, filename, sf);
            return InitUploadResult.from(ma);
        }

        UploadSession uploadSession = UploadSession.builder()
            .filename(filename)
            .mediaType(mediaType.toString())
            .fileType(fileType)
            .totalSize(totalSize)
            .totalChunks(totalChunks)
            .checksum(checksum)
            .user(user)
            .uploadedChunks(new HashSet<>())
            .tempFilePath(uploadPath + "/" + checksum + ".tmp")
            .build();
        repository.save(uploadSession);
        
        return InitUploadResult.from(uploadSession);
    }

    public void chunk(UUID id, MultipartFile file, int chunkindex){
        // TODO
    }

    public UploadSession getUploadSession(UUID id){
        return null;
    }



    public record InitUploadResult(boolean fileAlreadyExists, UploadSession uploadSession, MediaAsset mediaAsset) {
        protected static InitUploadResult from(MediaAsset ma){
            return new InitUploadResult(true, null, ma);
        }
        protected static InitUploadResult from(UploadSession us){
            return new InitUploadResult(false, us, null);
        }
    }


    
}

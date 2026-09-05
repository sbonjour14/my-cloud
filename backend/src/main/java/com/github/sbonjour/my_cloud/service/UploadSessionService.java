package com.github.sbonjour.my_cloud.service;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.github.sbonjour.my_cloud.entity.MediaAsset;
import com.github.sbonjour.my_cloud.entity.StoredFile;
import com.github.sbonjour.my_cloud.entity.UploadSession;
import com.github.sbonjour.my_cloud.entity.User;
import com.github.sbonjour.my_cloud.entity.StoredFile.FileType;
import com.github.sbonjour.my_cloud.entity.UploadSession.BytesRange;
import com.github.sbonjour.my_cloud.entity.UploadSession.UploadSessionStatus;
import com.github.sbonjour.my_cloud.exception.InternalServerErrorException;
import com.github.sbonjour.my_cloud.exception.InvalidInputException;
import com.github.sbonjour.my_cloud.exception.NotFoundException;
import com.github.sbonjour.my_cloud.repository.UploadSessionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UploadSessionService {

    private final UploadSessionRepository repository;
    private final MediaAssetService mediaAssetService;
    private final StoredFileService storedFileService;
    private final FileService fileService;

    @Value("${upload.chunk-size.image}:2097152")
    private long imageChunkSize;
    @Value("${upload.chunk-size.video}:10485760")
    private long videoChunkSize;

    @Value("${file.storage.path:/app/uploads}")
    private String uploadPath;

    public UploadSession findByChecksumAndUser(String checksum, User user) {
        return repository.findByChecksumAndUser(checksum, user).orElse(null);
    }

    public InitUploadResult init(String filename, MediaType mediaType, FileType fileType, long totalSize,
            String checksum, User user) {
        UploadSession us = findByChecksumAndUser(checksum, user);
        if (us != null)
            return InitUploadResult.from(us);

        StoredFile sf = storedFileService.findByChecksum(checksum);
        if (sf != null) {

            MediaAsset ma = mediaAssetService.findByOwnerAndStoredFile(user, sf);
            if (ma != null)
                return InitUploadResult.from(ma);

            ma = mediaAssetService.createMediaAsset(user, filename, sf);
            return InitUploadResult.from(ma);
        }

        UploadSession uploadSession = UploadSession.builder()
                .filename(filename)
                .mediaType(mediaType.toString())
                .fileType(fileType)
                .totalSize(totalSize)
                .checksum(checksum)
                .user(user)
                .uploadedRanges(new HashSet<BytesRange>())
                .tempFilePath(uploadPath + "/" + checksum + ".tmp")
                .build();
        repository.save(uploadSession);

        return InitUploadResult.from(uploadSession);
    }

    private WriteChunkResult setPausedSaveAndReturn(UploadSession us, HttpStatus status, String message) {
        us.setStatus(UploadSessionStatus.PAUSED);
        return WriteChunkResult.from(repository.save(us), status, message);
    }


    public WriteChunkResult writeChunk(UUID id, MultipartFile file, long start, long end, User user) {
        long fileSize = file.getSize();

        if(end - start + 1 != fileSize)
            throw new InvalidInputException("the chunk size should be equal to " + (end - start + 1));

        
        UploadSession uploadSession = repository.findById(id).orElseThrow(() -> new NotFoundException("The uploadSession with id: " + id.toString() + " not found"));

        if(!uploadSession.getUser().getId().equals(user.getId()))
            throw new AccessDeniedException("You are not allowed to have access to this resource");

        if(!uploadSession.isRangeValid(start, end))
            throw new InvalidInputException("the range must be between 0 and " + (uploadSession.getTotalSize()-1));

        long chunkSize = fileService.getFileType(file) == FileType.IMAGE ? imageChunkSize : videoChunkSize;
        boolean isLastChunk = end == uploadSession.getTotalSize() - 1;

        if(fileSize != chunkSize && !isLastChunk)
            return setPausedSaveAndReturn(uploadSession, HttpStatus.CONFLICT, "the chunk size shoud be equal to " + chunkSize);

        if(isLastChunk && fileSize > chunkSize)
            return setPausedSaveAndReturn(uploadSession, HttpStatus.CONFLICT, "the chunk size shoud less than or equal to " + chunkSize);

        if(uploadSession.hasOverlapWith(start, end))
            return setPausedSaveAndReturn(uploadSession,HttpStatus.CONFLICT, uploadPath);


        boolean success = fileService.writeChunk(file, start, Path.of(uploadSession.getTempFilePath()));
        
        if(!success)
            throw new InternalServerErrorException("error while uploading the chunk");

        uploadSession.addRange(start, end);;
        uploadSession.addUploadedSize(fileSize);

        repository.save(uploadSession);

        return WriteChunkResult.from(uploadSession, HttpStatus.CREATED, "OK");
    }

    public UploadSession getUploadSession(UUID id) {
        return null;
    }

    public record InitUploadResult(boolean fileAlreadyExists, UploadSession uploadSession, MediaAsset mediaAsset) {
        private static InitUploadResult from(MediaAsset ma) {
            return new InitUploadResult(true, null, ma);
        }

        protected static InitUploadResult from(UploadSession us) {
            return new InitUploadResult(false, us, null);
        }
    }

    public record WriteChunkResult(UploadSession uploadSession, HttpStatus status, String message) {
        private static WriteChunkResult from(UploadSession us, HttpStatus status, String message) {
            return new WriteChunkResult(us, status, message);
        }
    }

}

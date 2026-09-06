package com.github.sbonjour.my_cloud.controller;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.github.sbonjour.my_cloud.entity.StoredFile.FileType;
import com.github.sbonjour.my_cloud.entity.UploadSession.BytesRange;
import com.github.sbonjour.my_cloud.entity.UploadSession.UploadSessionStatus;
import com.github.sbonjour.my_cloud.exception.InvalidInputException;
import com.github.sbonjour.my_cloud.entity.UploadSession;
import com.github.sbonjour.my_cloud.entity.User;
import com.github.sbonjour.my_cloud.service.FileService;
import com.github.sbonjour.my_cloud.service.UploadSessionService;
import com.github.sbonjour.my_cloud.service.UploadSessionService.InitUploadResult;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class UploadSessionController {
    private final UploadSessionService uploadSessionService;
    private final FileService fileService;

    @PostMapping("/uploads/init")
    public ResponseEntity<InitUploadSessionResult> initUploadSession(
            @Valid @RequestBody InitUploadSessionRequest request, @AuthenticationPrincipal User user) {
        FileType fileType = fileService.getFileType(request.mediaType.toString());

        InitUploadResult result = uploadSessionService.init(request.filename, request.mediaType, fileType,
                request.totalSize, request.checksum, user);

        return ResponseEntity.status(HttpStatus.CREATED).body(InitUploadSessionResult.from(result));
    }

    @GetMapping("/uploads/{id}")
    public ResponseEntity<UploadSessionResponse> getUploadSession(@PathVariable UUID id, @AuthenticationPrincipal User user) {
        UploadSession us = uploadSessionService.findById(id, user);
        return ResponseEntity.ok(UploadSessionResponse.from(us));
    }

    @PostMapping("/uploads/{id}")
    public ResponseEntity<String> writeChunk(@PathVariable UUID id, @Valid @RequestBody MultipartFile chunk,
            @RequestHeader("Content-Range") String contentRange, @AuthenticationPrincipal User user) {
        Matcher matcher = Pattern.compile("bytes (\\d+)-(\\d+)/(\\d+)").matcher(contentRange);
        if (!matcher.matches()) {
            throw new InvalidInputException("Content-Range header is malformed");
        }
        long start = Long.parseLong(matcher.group(1));
        long end = Long.parseLong(matcher.group(2));

        UploadSession us = uploadSessionService.writeChunk(id, chunk, start, end, user);

        if(us.isComplete())
            return ResponseEntity.status(HttpStatus.CREATED).body(us.getUrl());
        return ResponseEntity.ok(null);
    }

    private record InitUploadSessionRequest(
            @NotBlank(message = "filename is required") String filename,
            @NotNull(message = "media type is required") MediaType mediaType,
            @Positive(message = "total size is required") long totalSize,
            @NotBlank(message = "checksum is required") String checksum) {
    }

    private record InitUploadSessionResult(boolean fileAlreadyExists, String url) {
        private static InitUploadSessionResult from(InitUploadResult result) {
            if (result.fileAlreadyExists())
                return new InitUploadSessionResult(true, result.mediaAsset().getUrl());
            return new InitUploadSessionResult(false, result.uploadSession().getUrl());
        }
    }

    private record UploadSessionResponse(
            UploadSessionStatus status,
            long totalSize,
            long uploadedSize,
            List<BytesRange> uploadedRanges) {
        private static UploadSessionResponse from(UploadSession us) {
            List<BytesRange> bytesRanges = us.getUploadedRanges().stream()
                    .sorted(Comparator.comparingLong(BytesRange::byteStart)).toList();
            return new UploadSessionResponse(us.getStatus(), us.getTotalSize(), us.getUploadedSize(), bytesRanges);
        }
    }
}

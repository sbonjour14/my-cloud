package com.github.sbonjour.my_cloud.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;

import com.github.sbonjour.my_cloud.entity.MediaAsset;
import com.github.sbonjour.my_cloud.entity.StoredFile;
import com.github.sbonjour.my_cloud.entity.UploadSession;
import com.github.sbonjour.my_cloud.entity.User;
import com.github.sbonjour.my_cloud.entity.StoredFile.FileType;
import com.github.sbonjour.my_cloud.entity.UploadSession.UploadSessionStatus;
import com.github.sbonjour.my_cloud.repository.UploadSessionRepository;
import com.github.sbonjour.my_cloud.service.UploadSessionService.InitUploadResult;

@ExtendWith(MockitoExtension.class)
public class UploadSessionServiceTest {
    @InjectMocks
    private UploadSessionService service;

    @Mock
    private UploadSessionRepository repository;
    @Mock
    private MediaAssetService mediaAssetService;
    @Mock
    private StoredFileService storedFileService;
    @Mock
    private FileService fileService;

    private String uploadPath = "/uploads";


    @Nested
    class Init {

        User user;

        @BeforeEach
        void setUp() {
            ReflectionTestUtils.setField(service, "uploadPath", uploadPath);
            user = User.builder()
                .displayName("test")
                .email("test@example.com")
                .id(UUID.randomUUID())
                .password("hashedPassword")
                .build();
        }
        
        @Test
        void createsNewSession_whenNoExistingSessionOrAsset() {

            UploadSession us = UploadSession.builder()
                .id(UUID.randomUUID())
                .filename("test.jpg")
                .tempFilePath(uploadPath + "/" + "checksum.tmp")
                .mediaType(MediaType.IMAGE_JPEG_VALUE)
                .fileType(FileType.IMAGE)
                .totalSize(10)
                .totalChunks(5)
                .checksum("checksum")
                .status(UploadSessionStatus.UPLOADING)
                .build();
            

            when(repository.findByChecksumAndUser(anyString(), any(User.class))).thenReturn(Optional.empty());
            when(storedFileService.findByChecksum(anyString())).thenReturn(null);

            when(repository.save(any(UploadSession.class))).thenAnswer(invocation -> { 
                UploadSession res = invocation.getArgument(0);
                res.setId(us.getId());
                return res;
            });

            InitUploadResult result = service.init("test.jpg", MediaType.IMAGE_JPEG, FileType.IMAGE, 10L, 5, "checksum", user);


            verify(repository).findByChecksumAndUser(eq("checksum"), eq(user));
            verify(storedFileService).findByChecksum(eq("checksum"));
            verify(repository).save(any());

            assertThat(result.fileAlreadyExists()).isEqualTo(false);
            assertThat(result.mediaAsset()).isNull();
            assertThat(result.uploadSession()).isNotNull();
            assertThat(result.uploadSession().getUploadedChunks()).isEmpty();
            assertThat(result.uploadSession().getUploadedSize()).isEqualTo(0);
            assertThat(result.uploadSession().getTempFilePath()).isEqualTo(us.getTempFilePath());
            assertThat(result.uploadSession().getMediaType()).isEqualTo(us.getMediaType());
            assertThat(result.uploadSession().getStatus()).isEqualTo(us.getStatus());
            assertThat(result.uploadSession().getFileType()).isEqualTo(us.getFileType());
        }

        @Test
        void returnsExistingSession_whenSessionAlreadyExists() {
            UploadSession us = UploadSession.builder()
                .id(UUID.randomUUID())
                .filename("test.jpg")
                .tempFilePath(uploadPath + "/" + "checksum.tmp")
                .mediaType(MediaType.IMAGE_JPEG_VALUE)
                .fileType(FileType.IMAGE)
                .totalSize(10)
                .totalChunks(5)
                .checksum("checksum")
                .status(UploadSessionStatus.UPLOADING)
                .build();
            when(repository.findByChecksumAndUser(anyString(), any(User.class))).thenReturn(Optional.of(us));

            InitUploadResult result = service.init("test.jpg", MediaType.IMAGE_JPEG, FileType.IMAGE, 10L, 5, "checksum", user);

            verify(repository).findByChecksumAndUser(eq("checksum"), eq(user));

            assertThat(result.fileAlreadyExists()).isEqualTo(false);
            assertThat(result.mediaAsset()).isNull();
            assertThat(result.uploadSession()).isEqualTo(us);
        }

        @Test
        void returnsAssetId_whenChecksumMatchesExistingStoredFile() {

            StoredFile storedFile = StoredFile.builder()
                .fileType(FileType.IMAGE)
                .hasThumbnail(true)
                .id(UUID.randomUUID())
                .mediaType(MediaType.IMAGE_JPEG_VALUE)
                .storagePath(uploadPath + "/" + "checksum")
                .checksum("checksum")
                .build();

            MediaAsset expected = MediaAsset.builder()
                .storedFile(storedFile)
                .owner(user)
                .filename("test.jpg")
                .build();

            when(repository.findByChecksumAndUser("checksum", user)).thenReturn(Optional.empty());
            when(storedFileService.findByChecksum(anyString())).thenReturn(storedFile);


            when(mediaAssetService.findByOwnerAndStoredFile(any(), any())).thenReturn(null);
            when(mediaAssetService.createMediaAsset(any(), anyString(), any())).thenAnswer(invocation -> {
                User usr = invocation.getArgument(0);
                String filename = invocation.getArgument(1);
                StoredFile sf = invocation.getArgument(2);

                return MediaAsset.builder()
                    .id(expected.getId())
                    .filename(filename)
                    .owner(usr)
                    .storedFile(sf)
                    .build();
            });


            InitUploadResult result = service.init("test.jpg", MediaType.IMAGE_JPEG, FileType.IMAGE, 10, 10, "checksum", user);


            verify(mediaAssetService).createMediaAsset(eq(user), eq("test.jpg"), eq(storedFile));
            verify(mediaAssetService).findByOwnerAndStoredFile(eq(user), eq(storedFile));

            assertThat(result).isNotNull();
            assertThat(result.fileAlreadyExists()).isTrue();
            assertThat(result.uploadSession()).isNull();
            assertThat(result.mediaAsset()).isNotNull();
            assertThat(result.mediaAsset().getId()).isEqualTo(expected.getId());
            assertThat(result.mediaAsset().getStoredFile()).isEqualTo(expected.getStoredFile());
            assertThat(result.mediaAsset().getOwner()).isEqualTo(expected.getOwner());
            assertThat(result.mediaAsset().getFilename()).isEqualTo(expected.getFilename());
        }
        @Test
        void returnsAssetId_whenChecksumMatchesExistingMediaAsset() {

            StoredFile storedFile = StoredFile.builder()
                .fileType(FileType.IMAGE)
                .hasThumbnail(true)
                .id(UUID.randomUUID())
                .mediaType(MediaType.IMAGE_JPEG_VALUE)
                .storagePath(uploadPath + "/" + "checksum")
                .checksum("checksum")
                .build();

            MediaAsset ma = MediaAsset.builder()
                .storedFile(storedFile)
                .owner(user)
                .filename("test.jpg")
                .build();

            when(repository.findByChecksumAndUser("checksum", user)).thenReturn(Optional.empty());
            when(storedFileService.findByChecksum(anyString())).thenReturn(storedFile);


            when(mediaAssetService.findByOwnerAndStoredFile(any(), any())).thenReturn(ma);

            InitUploadResult result = service.init("test.jpg", MediaType.IMAGE_JPEG, FileType.IMAGE, 10, 10, "checksum", user);

            verify(mediaAssetService).findByOwnerAndStoredFile(eq(user), eq(storedFile));

            assertThat(result).isNotNull();
            assertThat(result.fileAlreadyExists()).isTrue();
            assertThat(result.uploadSession()).isNull();
            assertThat(result.mediaAsset()).isNotNull();
        }
    }
    
}

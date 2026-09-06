package com.github.sbonjour.my_cloud.service.uploadSession;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.anyString;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import com.github.sbonjour.my_cloud.entity.MediaAsset;
import com.github.sbonjour.my_cloud.entity.StoredFile;
import com.github.sbonjour.my_cloud.entity.StoredFile.FileType;
import com.github.sbonjour.my_cloud.entity.UploadSession;
import com.github.sbonjour.my_cloud.entity.UploadSession.UploadSessionStatus;
import com.github.sbonjour.my_cloud.entity.User;
import com.github.sbonjour.my_cloud.service.UploadSessionService.InitUploadResult;

public class UploadSessionServiceInitTest extends UploadSessionServiceTest {

        @Test
        void createsNewSession_whenNoExistingSessionOrAsset() {

                UploadSession us = UploadSession.builder()
                                .id(UUID.randomUUID())
                                .filename("test.jpg")
                                .tempFilePath(uploadPath + "/" + "checksum.tmp")
                                .mediaType(MediaType.IMAGE_JPEG_VALUE)
                                .fileType(FileType.IMAGE)
                                .totalSize(10)
                                .checksum("checksum")
                                .status(UploadSessionStatus.UPLOADING)
                                .build();

                when(repository.findByChecksumAndUser(anyString(), any(User.class))).thenReturn(Optional.empty());
                when(storedFileService.findByChecksum(anyString())).thenReturn(null);

                when(repository.save(any(UploadSession.class))).thenAnswer(invocation -> {
                        UploadSession res = invocation.getArgument(0);
                        return UploadSession.builder()
                                        .id(us.getId())
                                        .checksum(res.getChecksum())
                                        .fileType(res.getFileType())
                                        .filename(res.getFilename())
                                        .user(res.getUser())
                                        .mediaType(res.getMediaType())
                                        .status(res.getStatus())
                                        .tempFilePath(res.getTempFilePath())
                                        .totalSize(res.getTotalSize())
                                        .uploadedRanges(res.getUploadedRanges())
                                        .uploadedSize(res.getUploadedSize())
                                        .build();
                });

                InitUploadResult result = service.init("test.jpg", MediaType.IMAGE_JPEG, FileType.IMAGE, 10L,
                                "checksum",
                                user);

                verify(repository).findByChecksumAndUser(eq("checksum"), eq(user));
                verify(storedFileService).findByChecksum(eq("checksum"));
                verify(repository).save(any());

                assertThat(result.fileAlreadyExists()).isEqualTo(false);
                assertThat(result.mediaAsset()).isNull();
                assertThat(result.uploadSession()).isNotNull();
                assertThat(result.uploadSession().getUploadedRanges()).isEmpty();
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
                                .checksum("checksum")
                                .status(UploadSessionStatus.UPLOADING)
                                .build();
                when(repository.findByChecksumAndUser(anyString(), any(User.class))).thenReturn(Optional.of(us));

                InitUploadResult result = service.init("test.jpg", MediaType.IMAGE_JPEG, FileType.IMAGE, 10L,
                                "checksum",
                                user);

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

                InitUploadResult result = service.init("test.jpg", MediaType.IMAGE_JPEG, FileType.IMAGE, 10L,
                                "checksum",
                                user);

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

                InitUploadResult result = service.init("test.jpg", MediaType.IMAGE_JPEG, FileType.IMAGE, 10L,
                                "checksum",
                                user);

                verify(mediaAssetService).findByOwnerAndStoredFile(eq(user), eq(storedFile));

                assertThat(result).isNotNull();
                assertThat(result.fileAlreadyExists()).isTrue();
                assertThat(result.uploadSession()).isNull();
                assertThat(result.mediaAsset()).isNotNull();
        }

}

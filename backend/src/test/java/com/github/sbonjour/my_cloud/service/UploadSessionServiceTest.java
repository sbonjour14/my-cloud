package com.github.sbonjour.my_cloud.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import com.github.sbonjour.my_cloud.entity.MediaAsset;
import com.github.sbonjour.my_cloud.entity.StoredFile;
import com.github.sbonjour.my_cloud.entity.UploadSession;
import com.github.sbonjour.my_cloud.entity.User;
import com.github.sbonjour.my_cloud.entity.StoredFile.FileType;
import com.github.sbonjour.my_cloud.entity.UploadSession.BytesRange;
import com.github.sbonjour.my_cloud.entity.UploadSession.UploadSessionStatus;
import com.github.sbonjour.my_cloud.exception.ConflictException;
import com.github.sbonjour.my_cloud.exception.InvalidInputException;
import com.github.sbonjour.my_cloud.exception.NotFoundException;
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

            InitUploadResult result = service.init("test.jpg", MediaType.IMAGE_JPEG, FileType.IMAGE, 10L, "checksum",
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

            InitUploadResult result = service.init("test.jpg", MediaType.IMAGE_JPEG, FileType.IMAGE, 10L, "checksum",
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

            InitUploadResult result = service.init("test.jpg", MediaType.IMAGE_JPEG, FileType.IMAGE, 10L, "checksum",
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

            InitUploadResult result = service.init("test.jpg", MediaType.IMAGE_JPEG, FileType.IMAGE, 10L, "checksum",
                    user);

            verify(mediaAssetService).findByOwnerAndStoredFile(eq(user), eq(storedFile));

            assertThat(result).isNotNull();
            assertThat(result.fileAlreadyExists()).isTrue();
            assertThat(result.uploadSession()).isNull();
            assertThat(result.mediaAsset()).isNotNull();
        }
    }

    @Nested
    class WriteChunk {

        @Nested
        class Image {

            UploadSession us;
            User user;

            @BeforeEach
            void setUp() {
                lenient().when(fileService.getFileType(any(MultipartFile.class))).thenReturn(FileType.IMAGE);
                ReflectionTestUtils.setField(service, "imageChunkSize", 10L);

                user = User.builder()
                    .displayName("test")
                    .password("hashedPassword")
                    .email("test@example.com")
                    .id(UUID.randomUUID())
                    .build();

                us = UploadSession.builder()
                        .id(UUID.randomUUID())
                        .user(user)
                        .checksum("checksum")
                        .filename("test.jpg")
                        .tempFilePath(uploadPath + "/" + "checksum.tmp")
                        .fileType(FileType.IMAGE)
                        .status(UploadSessionStatus.UPLOADING)
                        .mediaType(MediaType.IMAGE_JPEG_VALUE)
                        .totalSize(99L)
                        .uploadedRanges(new HashSet<BytesRange>())
                        .build();
            }

            @Test
            void shouldWriteChunk_whenRangeStartsAtZero() {
                MockMultipartFile mockFile = new MockMultipartFile("file", "test.jpg", MediaType.IMAGE_JPEG_VALUE,
                        "test-chunk".getBytes());
                when(repository.findById(any())).thenReturn(Optional.of(us));
                when(repository.save(any())).thenAnswer(invocation -> invocation.<UploadSession>getArgument(0));
                when(fileService.writeChunk(any(), anyLong(), any(Path.class))).thenReturn(true);

                UploadSession result = service.writeChunk(us.getId(), mockFile, 0L, 9L, user);

                verify(fileService).writeChunk(any(), anyLong(), any()); 
                verify(repository).findById(eq(us.getId()));

                assertThat(result).isNotNull();
                assertThat(result.getId()).isEqualTo(us.getId());
                assertThat(result.getUploadedRanges()).containsExactlyInAnyOrder(new BytesRange(0, 9));
                assertThat(result.getStatus()).isEqualTo(UploadSessionStatus.UPLOADING);
                assertThat(result.getUploadedSize()).isEqualTo(mockFile.getSize());
            }

            @Test
            void shouldWriteChunk_whenRangeIsInTheMiddle() {
                MockMultipartFile mockFile = new MockMultipartFile("file", "test.jpg", MediaType.IMAGE_JPEG_VALUE,
                        "otherChunk".getBytes());

                when(repository.findById(any())).thenReturn(Optional.of(us));
                when(repository.save(any())).thenAnswer(invocation -> invocation.<UploadSession>getArgument(0));
                when(fileService.writeChunk(any(), anyLong(), any(Path.class))).thenReturn(true);

                UploadSession result = service.writeChunk(us.getId(), mockFile, 20L, 29L, user);

                verify(fileService).writeChunk(eq(mockFile), eq(20L), eq(Path.of(us.getTempFilePath())));
                verify(repository).findById(eq(us.getId()));

                assertThat(result).isNotNull();
                assertThat(result.getId()).isEqualTo(us.getId());
                assertThat(result.getUploadedRanges()).containsExactlyInAnyOrder(new BytesRange(20L, 29L));
                assertThat(result.getStatus()).isEqualTo(UploadSessionStatus.UPLOADING);
                assertThat(result.getUploadedSize()).isEqualTo(mockFile.getSize());
            }

            @Test
            void shouldWriteChunk_whenRangeEndsAtTotalSize() {
                MockMultipartFile mockFile = new MockMultipartFile("file", "test.jpg", MediaType.IMAGE_JPEG_VALUE,
                        "lastChunk".getBytes());
                when(repository.findById(any())).thenReturn(Optional.of(us));
                when(repository.save(any())).thenAnswer(invocation -> invocation.<UploadSession>getArgument(0));
                when(fileService.writeChunk(any(), anyLong(), any(Path.class))).thenReturn(true);

                UploadSession result = service.writeChunk(us.getId(), mockFile, 90L, 98L, user);

                verify(fileService).writeChunk(eq(mockFile), eq(90L), eq(Path.of(us.getTempFilePath())));
                verify(repository).findById(eq(us.getId()));

                assertThat(result).isNotNull();
                assertThat(result.getId()).isEqualTo(us.getId());
                assertThat(result.getUploadedRanges()).containsExactlyInAnyOrder(new BytesRange(90L, 98L));
                assertThat(result.getStatus()).isEqualTo(UploadSessionStatus.UPLOADING);
                assertThat(result.getUploadedSize()).isEqualTo(9L);
            }

            @Test
            void shouldNotWriteChunk_whenRangeOverlapsExistingRange() {
                    MockMultipartFile mockFile = new MockMultipartFile("file", "test.jpg", MediaType.IMAGE_JPEG_VALUE,
                                    "otherChunk".getBytes());
                    us.addRange(0L, 9L);

                    when(repository.findById(any())).thenReturn(Optional.of(us));
                    when(repository.save(any())).thenAnswer(invocation -> invocation.<UploadSession>getArgument(0));

                    assertThatThrownBy(() -> service.writeChunk(us.getId(), mockFile, 5L, 14L, user))
                                    .isInstanceOf(ConflictException.class);

                    verify(fileService, never()).writeChunk(any(), anyLong(), any());
                    verify(repository).findById(eq(us.getId()));

                    ArgumentCaptor<UploadSession> captor = ArgumentCaptor.forClass(UploadSession.class);
                    verify(repository).save(captor.capture());
                    assertThat(captor.getValue().getStatus()).isEqualTo(UploadSessionStatus.PAUSED);
                    assertThat(captor.getValue().getUploadedRanges()).containsExactly(new BytesRange(0L, 9L));
            }

            @Test
            void shouldNotWriteChunk_whenRangeExceedsChunkSize() {
                    when(fileService.getFileType(any(MultipartFile.class))).thenReturn(FileType.IMAGE);
                    MockMultipartFile mockFile = new MockMultipartFile("file", "test.jpg", MediaType.IMAGE_JPEG_VALUE,
                                    "otherChunkTooLong".getBytes());

                    when(repository.findById(any())).thenReturn(Optional.of(us));
                    when(repository.save(any())).thenAnswer(invocation -> invocation.<UploadSession>getArgument(0));

                    assertThatThrownBy(() -> service.writeChunk(us.getId(), mockFile, 0L, 16L, user))
                                    .isInstanceOf(InvalidInputException.class);

                    verify(fileService, never()).writeChunk(any(), anyLong(), any());
                    verify(repository).findById(eq(us.getId()));

                    ArgumentCaptor<UploadSession> captor = ArgumentCaptor.forClass(UploadSession.class);
                    verify(repository).save(captor.capture());
                    assertThat(captor.getValue().getStatus()).isEqualTo(UploadSessionStatus.PAUSED);
                    assertThat(captor.getValue().getUploadedRanges()).isEmpty();
            }

            @Test
            void shouldNotWriteChunk_whenUploadSessionNotFound() {
                MockMultipartFile mockFile = new MockMultipartFile("file", "test.jpg", MediaType.IMAGE_JPEG_VALUE,
                        "otherChunk".getBytes());

                when(repository.findById(any())).thenReturn(Optional.empty());

                assertThatThrownBy(() -> service.writeChunk(us.getId(), mockFile, 10L, 19L, user))
                        .isInstanceOf(NotFoundException.class);

                verify(repository).findById(eq(us.getId()));
            }

            @Test
            void shouldWriteChunk_whenSomeChunksAlreadyUploaded() {
                MockMultipartFile mockFile = new MockMultipartFile("file", "test.jpg", MediaType.IMAGE_JPEG_VALUE,
                        "otherChunk".getBytes());
                us.getUploadedRanges().add(new BytesRange(0L, 9L));
                us.setUploadedSize(10L);

                when(repository.findById(any())).thenReturn(Optional.of(us));
                when(repository.save(any())).thenAnswer(invocation -> invocation.<UploadSession>getArgument(0));
                when(fileService.writeChunk(any(), anyLong(), any(Path.class))).thenReturn(true);

                UploadSession result = service.writeChunk(us.getId(), mockFile, 10L, 19L, user);

                verify(repository).findById(eq(us.getId()));
                verify(fileService).writeChunk(eq(mockFile), eq(10L), eq(Path.of(us.getTempFilePath())));

                verify(repository).save(result);
                assertThat(result.getUploadedRanges()).containsExactlyInAnyOrder(
                        new BytesRange(0L, 9L),
                        new BytesRange(10L, 19L));
                assertThat(result.getUploadedSize()).isEqualTo(20L);
            }

            @Test
            void shouldThrowInvalidInput_whenChunkSizeDoesNotMatchRange() {
                MockMultipartFile mockFile = new MockMultipartFile("file", "test.jpg", MediaType.IMAGE_JPEG_VALUE,
                        "test-chunk".getBytes()); // 10 bytes

                assertThatThrownBy(() -> service.writeChunk(us.getId(), mockFile, 0L, 20L, user))
                        .isInstanceOf(InvalidInputException.class);

                verify(repository, never()).findById(any());
                verify(repository, never()).save(any());
                verify(fileService, never()).writeChunk(any(), anyLong(), any());
            }
        }
    }
}

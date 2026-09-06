package com.github.sbonjour.my_cloud.service.uploadSession;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
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
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import com.github.sbonjour.my_cloud.entity.UploadSession;
import com.github.sbonjour.my_cloud.entity.StoredFile.FileType;
import com.github.sbonjour.my_cloud.entity.UploadSession.BytesRange;
import com.github.sbonjour.my_cloud.entity.UploadSession.UploadSessionStatus;
import com.github.sbonjour.my_cloud.exception.ConflictException;
import com.github.sbonjour.my_cloud.exception.InvalidInputException;
import com.github.sbonjour.my_cloud.exception.NotFoundException;
import com.github.sbonjour.my_cloud.service.UploadSessionService.WriteChunkResult;

public class UploadSessionServiceWriteChunkTest extends UploadSessionServiceTest {
    @Nested
    class Image {

        UploadSession us;

        @BeforeEach
        void setUp() {
            lenient().when(fileService.getFileType(any(MultipartFile.class)))
                    .thenReturn(FileType.IMAGE);
            ReflectionTestUtils.setField(service, "imageChunkSize", 10L);

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
            MockMultipartFile mockFile = new MockMultipartFile("file", "test.jpg",
                    MediaType.IMAGE_JPEG_VALUE,
                    "test-chunk".getBytes());
            when(repository.findById(any())).thenReturn(Optional.of(us));
            when(repository.save(any()))
                    .thenAnswer(invocation -> invocation.<UploadSession>getArgument(0));
            when(fileService.writeChunk(any(), anyLong(), any(Path.class))).thenReturn(true);

            WriteChunkResult result = service.writeChunk(us.getId(), mockFile, 0L, 9L, user);

            verify(fileService).writeChunk(any(), anyLong(), any());
            verify(repository).findById(eq(us.getId()));

            assertThat(result).isNotNull();
            assertThat(result.uploadSession()).isNotNull();
            assertThat(result.uploadSession().getId()).isEqualTo(us.getId());
            assertThat(result.uploadSession().getUploadedRanges()).containsExactlyInAnyOrder(new BytesRange(0, 9));
            assertThat(result.uploadSession().getStatus()).isEqualTo(UploadSessionStatus.UPLOADING);
            assertThat(result.uploadSession().getUploadedSize()).isEqualTo(mockFile.getSize());

            assertThat(result.mediaAsset()).isNull();
        }

        @Test
        void shouldWriteChunk_whenRangeIsInTheMiddle() {
            MockMultipartFile mockFile = new MockMultipartFile("file", "test.jpg",
                    MediaType.IMAGE_JPEG_VALUE,
                    "otherChunk".getBytes());

            when(repository.findById(any())).thenReturn(Optional.of(us));
            when(repository.save(any()))
                    .thenAnswer(invocation -> invocation.<UploadSession>getArgument(0));
            when(fileService.writeChunk(any(), anyLong(), any(Path.class))).thenReturn(true);

            WriteChunkResult result = service.writeChunk(us.getId(), mockFile, 20L, 29L, user);

            verify(fileService).writeChunk(eq(mockFile), eq(20L),
                    eq(Path.of(us.getTempFilePath())));
            verify(repository).findById(eq(us.getId()));

            assertThat(result.uploadSession()).isNotNull();
            assertThat(result.uploadSession().getId()).isEqualTo(us.getId());
            assertThat(result.uploadSession().getUploadedRanges())
                    .containsExactlyInAnyOrder(new BytesRange(20L, 29L));
            assertThat(result.uploadSession().getStatus()).isEqualTo(UploadSessionStatus.UPLOADING);
            assertThat(result.uploadSession().getUploadedSize()).isEqualTo(mockFile.getSize());

            assertThat(result.mediaAsset()).isNull();
        }

        @Test
        void shouldWriteChunk_whenRangeEndsAtTotalSize() {
            MockMultipartFile mockFile = new MockMultipartFile("file", "test.jpg",
                    MediaType.IMAGE_JPEG_VALUE,
                    "lastChunk".getBytes());
            when(repository.findById(any())).thenReturn(Optional.of(us));
            when(repository.save(any()))
                    .thenAnswer(invocation -> invocation.<UploadSession>getArgument(0));
            when(fileService.writeChunk(any(), anyLong(), any(Path.class))).thenReturn(true);

            WriteChunkResult result = service.writeChunk(us.getId(), mockFile, 90L, 98L, user);

            verify(fileService).writeChunk(eq(mockFile), eq(90L),
                    eq(Path.of(us.getTempFilePath())));
            verify(repository).findById(eq(us.getId()));

            assertThat(result).isNotNull();
            assertThat(result.uploadSession().getId()).isEqualTo(us.getId());
            assertThat(result.uploadSession().getUploadedRanges())
                    .containsExactlyInAnyOrder(new BytesRange(90L, 98L));
            assertThat(result.uploadSession().getStatus()).isEqualTo(UploadSessionStatus.UPLOADING);
            assertThat(result.uploadSession().getUploadedSize()).isEqualTo(9L);
            
            assertThat(result.mediaAsset()).isNull();
        }

        @Test
        void shouldNotWriteChunk_whenRangeOverlapsExistingRange() {
            MockMultipartFile mockFile = new MockMultipartFile("file", "test.jpg",
                    MediaType.IMAGE_JPEG_VALUE,
                    "otherChunk".getBytes());
            us.addRange(0L, 9L);

            when(repository.findById(any())).thenReturn(Optional.of(us));
            when(repository.save(any()))
                    .thenAnswer(invocation -> invocation.<UploadSession>getArgument(0));

            assertThatThrownBy(() -> service.writeChunk(us.getId(), mockFile, 5L, 14L, user))
                    .isInstanceOf(ConflictException.class);

            verify(fileService, never()).writeChunk(any(), anyLong(), any());
            verify(repository).findById(eq(us.getId()));

            ArgumentCaptor<UploadSession> captor = ArgumentCaptor.forClass(UploadSession.class);
            verify(repository).save(captor.capture());
            assertThat(captor.getValue().getStatus()).isEqualTo(UploadSessionStatus.PAUSED);
            assertThat(captor.getValue().getUploadedRanges())
                    .containsExactly(new BytesRange(0L, 9L));
        }

        @Test
        void shouldNotWriteChunk_whenRangeExceedsChunkSize() {
            when(fileService.getFileType(any(MultipartFile.class))).thenReturn(FileType.IMAGE);
            MockMultipartFile mockFile = new MockMultipartFile("file", "test.jpg",
                    MediaType.IMAGE_JPEG_VALUE,
                    "otherChunkTooLong".getBytes());

            when(repository.findById(any())).thenReturn(Optional.of(us));
            when(repository.save(any()))
                    .thenAnswer(invocation -> invocation.<UploadSession>getArgument(0));

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
            MockMultipartFile mockFile = new MockMultipartFile("file", "test.jpg",
                    MediaType.IMAGE_JPEG_VALUE,
                    "otherChunk".getBytes());

            when(repository.findById(any())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.writeChunk(us.getId(), mockFile, 10L, 19L, user))
                    .isInstanceOf(NotFoundException.class);

            verify(repository).findById(eq(us.getId()));
        }

        @Test
        void shouldWriteChunk_whenSomeChunksAlreadyUploaded() {
            MockMultipartFile mockFile = new MockMultipartFile("file", "test.jpg",
                    MediaType.IMAGE_JPEG_VALUE,
                    "otherChunk".getBytes());
            us.getUploadedRanges().add(new BytesRange(0L, 9L));
            us.setUploadedSize(10L);

            when(repository.findById(any())).thenReturn(Optional.of(us));
            when(repository.save(any()))
                    .thenAnswer(invocation -> invocation.<UploadSession>getArgument(0));
            when(fileService.writeChunk(any(), anyLong(), any(Path.class))).thenReturn(true);

            WriteChunkResult result = service.writeChunk(us.getId(), mockFile, 10L, 19L, user);

            verify(repository).findById(eq(us.getId()));
            verify(fileService).writeChunk(eq(mockFile), eq(10L),
                    eq(Path.of(us.getTempFilePath())));

            verify(repository).save(result.uploadSession());
            assertThat(result.uploadSession().getUploadedRanges()).containsExactlyInAnyOrder(
                    new BytesRange(0L, 9L),
                    new BytesRange(10L, 19L));
            assertThat(result.uploadSession().getUploadedSize()).isEqualTo(20L);
            assertThat(result.mediaAsset()).isNull();
        }

        @Test
        void shouldThrowInvalidInput_whenChunkSizeDoesNotMatchRange() {
            MockMultipartFile mockFile = new MockMultipartFile("file", "test.jpg",
                    MediaType.IMAGE_JPEG_VALUE,
                    "test-chunk".getBytes()); // 10 bytes

            assertThatThrownBy(() -> service.writeChunk(us.getId(), mockFile, 0L, 20L, user))
                    .isInstanceOf(InvalidInputException.class);

            verify(repository, never()).findById(any());
            verify(repository, never()).save(any());
            verify(fileService, never()).writeChunk(any(), anyLong(), any());
        }
    }

}

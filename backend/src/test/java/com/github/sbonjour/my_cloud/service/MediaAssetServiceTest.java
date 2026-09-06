package com.github.sbonjour.my_cloud.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import com.github.sbonjour.my_cloud.config.RabbitMQConfig;
import com.github.sbonjour.my_cloud.entity.MediaAsset;
import com.github.sbonjour.my_cloud.entity.StoredFile;
import com.github.sbonjour.my_cloud.entity.User;
import com.github.sbonjour.my_cloud.entity.StoredFile.FileType;
import com.github.sbonjour.my_cloud.exception.ConflictException;
import com.github.sbonjour.my_cloud.exception.InternalServerErrorException;
import com.github.sbonjour.my_cloud.exception.NotFoundException;
import com.github.sbonjour.my_cloud.repository.MediaAssetRepository;

@ExtendWith(MockitoExtension.class)
public class MediaAssetServiceTest {
    @InjectMocks
    private MediaAssetService service;

    @Mock
    private MediaAssetRepository mediaAssetRepository;
    @Mock
    private StoredFileService storedFileService;
    @Mock
    private RabbitTemplate rabbitTemplate;

    private String uploadPath = "../images";
    @Mock
    private FileService fileService;

    @Nested
    class Upload {

        User owner;
        StoredFile sf;

        @BeforeEach
        void setUp() {
            ReflectionTestUtils.setField(service, "uploadPath", uploadPath);
            owner = User.builder()
                    .displayName("test")
                    .email("test@example.com")
                    .id(UUID.randomUUID())
                    .build();
            sf = StoredFile.builder()
                    .checksum("checksum")
                    .storagePath(uploadPath + "/checksum")
                    .hasThumbnail(true)
                    .fileType(FileType.IMAGE)
                    .mediaType("image/jpeg")
                    .sizeBytes(100)
                    .id(UUID.randomUUID())
                    .build();
        }

        @Test
        void shouldUpload_whenFileAlreadyExists() throws Exception {

            MultipartFile file = new MockMultipartFile("test.jpg", "test.jpg", "image/jpeg", new byte[100]);

            when(storedFileService.findByChecksum(any())).thenReturn(sf);
            when(mediaAssetRepository.findByOwnerAndFilenameIgnoringCase(any(), any())).thenReturn(Optional.empty());

            when(mediaAssetRepository.findByOwnerAndStoredFile(owner, sf)).thenReturn(Optional.empty());
            when(mediaAssetRepository.save(any())).thenAnswer(invocation -> invocation.<MediaAsset>getArgument(0));

            MediaAsset result = service.uploadFile(file, owner);

            verify(mediaAssetRepository).save(any(MediaAsset.class));

            assertThat(result.getFilename()).isEqualTo(file.getOriginalFilename());
            assertThat(result.getOwner()).isEqualTo(owner);

            assertThat(result.getStoredFile().getChecksum()).isEqualTo(sf.getChecksum());
            assertThat(result.getStoredFile().getSizeBytes()).isEqualTo(sf.getSizeBytes());
            assertThat(result.getStoredFile().getStoragePath()).isEqualTo(sf.getStoragePath());
            assertThat(result.getStoredFile().getFileType()).isEqualTo(sf.getFileType());
            assertThat(result.getStoredFile().getMediaType()).isEqualTo(sf.getMediaType());
            assertThat(result.getStoredFile().isHasThumbnail()).isEqualTo(sf.isHasThumbnail());

        }

        @Test
        void shouldUpload_whenFileIsNew() throws Exception {
            MultipartFile file = new MockMultipartFile("test.jpg", "test.jpg", "image/jpeg", new byte[100]);

            sf.setHasThumbnail(false); // file is new so no thumbnail

            when(storedFileService.findByChecksum(sf.getChecksum())).thenReturn(null);
            when(storedFileService.save(any(StoredFile.class)))
                    .thenAnswer(invocation -> invocation.<StoredFile>getArgument(0));

            when(mediaAssetRepository.findByOwnerAndFilenameIgnoringCase(any(), any())).thenReturn(Optional.empty());

            when(mediaAssetRepository.save(any())).thenAnswer(invocation -> {
                MediaAsset ma = invocation.<MediaAsset>getArgument(0);
                ma.setId(UUID.randomUUID());
                return ma;
            });

            when(fileService.getFileType(any(MultipartFile.class))).thenReturn(FileType.IMAGE);
            when(fileService.write(any(), any(), any(), any())).thenAnswer(invocation -> {
                MultipartFile f = invocation.getArgument(0);
                String storagePath = invocation.getArgument(1);
                String checksum = invocation.getArgument(2);
                FileType fileType = invocation.getArgument(3);

                return StoredFile.builder()
                        .checksum(checksum)
                        .storagePath(storagePath)
                        .mediaType(f.getContentType())
                        .sizeBytes(f.getSize())
                        .fileType(fileType)
                        .hasThumbnail(false)
                        .build();
            });

            when(fileService.calculateChecksum(file)).thenReturn(sf.getChecksum());

            MediaAsset result = service.uploadFile(file, owner);

            verify(fileService).write(file, uploadPath + "/" + sf.getChecksum(), sf.getChecksum(),
                    sf.getFileType());
            verify(mediaAssetRepository).save(any(MediaAsset.class));
            verify(storedFileService).save(any(StoredFile.class));
            verify(rabbitTemplate).convertAndSend(eq(RabbitMQConfig.THUMBNAIL_QUEUE), any(Map.class));

            assertThat(result.getFilename()).isEqualTo(file.getOriginalFilename());
            assertThat(result.getOwner()).isEqualTo(owner);

            assertThat(result.getStoredFile().getChecksum()).isEqualTo(sf.getChecksum());
            assertThat(result.getStoredFile().getSizeBytes()).isEqualTo(sf.getSizeBytes());
            assertThat(result.getStoredFile().getStoragePath()).isEqualTo(sf.getStoragePath());
            assertThat(result.getStoredFile().getMediaType()).isEqualTo(sf.getMediaType());
            assertThat(result.getStoredFile().getMediaType()).isEqualTo(sf.getMediaType());
            assertThat(result.getStoredFile().isHasThumbnail()).isEqualTo(sf.isHasThumbnail());
        }

        @Test
        void shouldNotUpload_whenHasSameMediaAsset() {
            MultipartFile file = new MockMultipartFile("test.jpg", "test.jpg", "image/jpeg", new byte[10]);

            MediaAsset existing = MediaAsset.builder()
                    .owner(owner)
                    .filename(file.getOriginalFilename())
                    .storedFile(sf)
                    .id(UUID.randomUUID())
                    .build();

            when(mediaAssetRepository.findByOwnerAndFilenameIgnoringCase(owner, file.getOriginalFilename()))
                    .thenReturn(Optional.of(existing));

            assertThatThrownBy(() -> service.uploadFile(file, owner))
                    .isInstanceOf(ConflictException.class)
                    .hasMessage("A media asset with the same name already exists for this user");
        }

        @Test
        void shouldNotUpload_whenHasSameStoredFile() {
            MultipartFile file = new MockMultipartFile("test.jpg", "test.jpg", "image/jpeg", new byte[10]);

            MediaAsset existing = MediaAsset.builder()
                    .owner(owner)
                    .filename(file.getOriginalFilename())
                    .storedFile(sf)
                    .id(UUID.randomUUID())
                    .build();

            when(mediaAssetRepository.findByOwnerAndFilenameIgnoringCase(owner, file.getOriginalFilename()))
                    .thenReturn(Optional.empty());
            when(mediaAssetRepository.findByOwnerAndStoredFile(owner, sf)).thenReturn(Optional.of(existing));

            when(fileService.calculateChecksum(any(MultipartFile.class))).thenReturn(sf.getChecksum());
            when(storedFileService.findByChecksum(anyString())).thenReturn(sf);

            assertThatThrownBy(() -> service.uploadFile(file, owner))
                    .isInstanceOf(ConflictException.class)
                    .hasMessage("A media asset with the same file already exists for this user");
        }

        @Test
        void shouldNotUpload_whenErrorSavingFile() throws Exception {
            MultipartFile file = new MockMultipartFile("test.jpg", "test.jpg", "image/jpeg", new byte[10]);

            when(mediaAssetRepository.findByOwnerAndFilenameIgnoringCase(owner, file.getOriginalFilename()))
                    .thenReturn(Optional.empty());

            when(fileService.calculateChecksum(any(MultipartFile.class))).thenReturn(sf.getChecksum());
            when(storedFileService.findByChecksum(anyString())).thenReturn(null);

            when(fileService.write(any(), any(), any(), any())).thenThrow(new IOException("disk full"));

            assertThatThrownBy(() -> service.uploadFile(file, owner))
                    .isInstanceOf(InternalServerErrorException.class);
        }
    }

    @Nested
    class DeleteMediaAsset {

        User owner;
        StoredFile sf;
        MediaAsset mediaAsset;

        @BeforeEach
        void setUp() {
            owner = User.builder()
                    .displayName("test")
                    .email("test@example.com")
                    .id(UUID.randomUUID())
                    .build();

            sf = StoredFile.builder()
                    .checksum("checksum")
                    .storagePath("../images/checksum")
                    .hasThumbnail(true)
                    .fileType(FileType.IMAGE)
                    .mediaType("image/jpeg")
                    .sizeBytes(100)
                    .id(UUID.randomUUID())
                    .build();

            mediaAsset = MediaAsset.builder()
                    .owner(owner)
                    .filename("test.jpg")
                    .storedFile(sf)
                    .id(UUID.randomUUID())
                    .build();
        }

        @Test
        void shouldThrow_whenMediaAssetNotFound() {
            when(mediaAssetRepository.findById(mediaAsset.getId())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.deleteMediaAsset(mediaAsset.getId(), owner))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Media asset not found");

            verify(mediaAssetRepository, never()).delete(any());
        }

        @Test
        void shouldThrow_whenUserIsNotOwner() {
            User otherUser = User.builder()
                    .displayName("other")
                    .email("other@example.com")
                    .id(UUID.randomUUID())
                    .build();

            when(mediaAssetRepository.findById(mediaAsset.getId())).thenReturn(Optional.of(mediaAsset));

            assertThatThrownBy(() -> service.deleteMediaAsset(mediaAsset.getId(), otherUser))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessage("You don't have access to this media asset");

            verify(mediaAssetRepository, never()).delete(any());
        }

        @Test
        void shouldDeleteMediaAssetAndStoredFile_whenNoOtherReferencesExist() {
            when(mediaAssetRepository.findById(mediaAsset.getId())).thenReturn(Optional.of(mediaAsset));
            when(mediaAssetRepository.findByStoredFile(sf)).thenReturn(List.of());

            try (MockedStatic<Files> filesMock = mockStatic(Files.class)) {
                filesMock.when(() -> Files.deleteIfExists(Path.of(sf.getStoragePath()))).thenReturn(true);

                service.deleteMediaAsset(mediaAsset.getId(), owner);

                verify(mediaAssetRepository).delete(mediaAsset);
                verify(storedFileService).delete(sf);
                filesMock.verify(() -> Files.deleteIfExists(Path.of(sf.getStoragePath())));
            }
        }

        @Test
        void shouldDeleteMediaAssetOnly_whenOtherReferencesExist() {
            MediaAsset otherMediaAsset = MediaAsset.builder()
                    .owner(owner)
                    .filename("other.jpg")
                    .storedFile(sf)
                    .id(UUID.randomUUID())
                    .build();

            when(mediaAssetRepository.findById(mediaAsset.getId())).thenReturn(Optional.of(mediaAsset));
            when(mediaAssetRepository.findByStoredFile(sf)).thenReturn(List.of(otherMediaAsset));

            service.deleteMediaAsset(mediaAsset.getId(), owner);

            verify(mediaAssetRepository).delete(mediaAsset);
            verify(storedFileService, never()).delete(any());
        }

        @Test
        void shouldThrow_whenFileDeletionFails() {
            when(mediaAssetRepository.findById(mediaAsset.getId())).thenReturn(Optional.of(mediaAsset));
            when(mediaAssetRepository.findByStoredFile(sf)).thenReturn(List.of());

            try (MockedStatic<Files> filesMock = mockStatic(Files.class)) {
                filesMock.when(() -> Files.deleteIfExists(Path.of(sf.getStoragePath())))
                        .thenThrow(new IOException("disk error"));

                assertThatThrownBy(() -> service.deleteMediaAsset(mediaAsset.getId(), owner))
                        .isInstanceOf(InternalServerErrorException.class)
                        .hasMessage("Error while deleting the file");

                verify(storedFileService, never()).delete(any());
            }
        }
    }

}

package com.github.sbonjour.my_cloud.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import com.github.sbonjour.my_cloud.config.RabbitMQConfig;
import com.github.sbonjour.my_cloud.entity.MediaAsset;
import com.github.sbonjour.my_cloud.entity.StoredFile;
import com.github.sbonjour.my_cloud.entity.User;
import com.github.sbonjour.my_cloud.entity.StoredFile.MediaType;
import com.github.sbonjour.my_cloud.repository.MediaAssetRepository;
import com.github.sbonjour.my_cloud.repository.StoredFileRepository;

@ExtendWith(MockitoExtension.class)
public class MediaAssetServiceTest {
    @InjectMocks
    private MediaAssetService service;

    @Mock
    private MediaAssetRepository mediaAssetRepository;
    @Mock
    private StoredFileRepository storedFileRepository;
    @Mock
    private RabbitTemplate rabbitTemplate;

    private String uploadPath = "../images";
    @Mock
    private FileStoreService fileStoreService;

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
                    .mediaType(MediaType.IMAGE)
                    .mimeType("image/jpeg")
                    .sizeBytes(100)
                    .id(UUID.randomUUID())
                    .build();
        }

        @Test
        void shouldUpload_whenFileAlreadyExists() throws Exception {

            MultipartFile file = new MockMultipartFile("test.jpg", "test.jpg", "image/jpeg", new byte[100]);

            when(storedFileRepository.findByChecksum(any())).thenReturn(Optional.of(sf));
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
            assertThat(result.getStoredFile().getMediaType()).isEqualTo(sf.getMediaType());
            assertThat(result.getStoredFile().getMimeType()).isEqualTo(sf.getMimeType());
            assertThat(result.getStoredFile().isHasThumbnail()).isEqualTo(sf.isHasThumbnail());

        }

        @Test
        void shouldUpload_whenFileIsNew() throws Exception {
            MultipartFile file = new MockMultipartFile("test.jpg", "test.jpg", "image/jpeg", new byte[100]);

            sf.setHasThumbnail(false); // file is new so no thumbnail

            when(storedFileRepository.findByChecksum(sf.getChecksum())).thenReturn(Optional.empty());
            when(storedFileRepository.save(any(StoredFile.class))).thenAnswer(invocation -> invocation.<StoredFile>getArgument(0));

            when(mediaAssetRepository.findByOwnerAndFilenameIgnoringCase(any(), any())).thenReturn(Optional.empty());

            when(mediaAssetRepository.save(any())).thenAnswer(invocation -> {
                MediaAsset ma = invocation.<MediaAsset>getArgument(0);
                ma.setId(UUID.randomUUID());
                return ma;
            });

            when(fileStoreService.write(any(), any(), any(), any())).thenAnswer(invocation -> {
                MultipartFile f = invocation.getArgument(0);
                String storagePath = invocation.getArgument(1);
                String checksum = invocation.getArgument(2);
                MediaType mediaType = invocation.getArgument(3);

                return StoredFile.builder()
                        .checksum(checksum)
                        .storagePath(storagePath)
                        .mimeType(f.getContentType())
                        .sizeBytes(f.getSize())
                        .mediaType(mediaType)
                        .hasThumbnail(false)
                        .build();
            });

            when(fileStoreService.calculateChecksum(file.getBytes())).thenReturn(sf.getChecksum());

            MediaAsset result = service.uploadFile(file, owner);


            verify(fileStoreService).write(file, uploadPath + "/" + sf.getChecksum(), sf.getChecksum(), sf.getMediaType());
            verify(mediaAssetRepository).save(any(MediaAsset.class));
            verify(storedFileRepository).save(any(StoredFile.class));
            verify(rabbitTemplate).convertAndSend(eq(RabbitMQConfig.THUMBNAIL_QUEUE), any(Map.class));

            assertThat(result.getFilename()).isEqualTo(file.getOriginalFilename());
            assertThat(result.getOwner()).isEqualTo(owner);

            assertThat(result.getStoredFile().getChecksum()).isEqualTo(sf.getChecksum());
            assertThat(result.getStoredFile().getSizeBytes()).isEqualTo(sf.getSizeBytes());
            assertThat(result.getStoredFile().getStoragePath()).isEqualTo(sf.getStoragePath());
            assertThat(result.getStoredFile().getMediaType()).isEqualTo(sf.getMediaType());
            assertThat(result.getStoredFile().getMimeType()).isEqualTo(sf.getMimeType());
            assertThat(result.getStoredFile().isHasThumbnail()).isEqualTo(sf.isHasThumbnail());

        }
    }

}

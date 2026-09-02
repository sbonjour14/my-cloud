package com.github.sbonjour.my_cloud.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.http.MediaType;

import com.github.sbonjour.my_cloud.TestDataFactory;
import com.github.sbonjour.my_cloud.entity.UploadSession;
import com.github.sbonjour.my_cloud.entity.StoredFile.FileType;

@DataJpaTest
public class UploadSessionRepositoryTest extends AbstractPostgresContainerTest {
    @Autowired
    UploadSessionRepository repository;

    @Autowired
    TestEntityManager entityManager;

    UploadSession uploadSession;

    @BeforeEach
    void setUp() {
        UploadSession us = UploadSession.builder()
                .filename("test.jpg")
                .fileType(FileType.IMAGE)
                .mediaType(MediaType.IMAGE_JPEG_VALUE)
                .tempFilePath("/uploads/tmp/checksum")
                .checksum("checksum")
                .build();

        uploadSession = TestDataFactory.persistUploadSession(entityManager, us);
    }

    @Nested 
    class findByChecksum {

        @Test
        void shouldFindByChecksum () {
            Optional<UploadSession> found = repository.findByChecksum("checksum");

            assertThat(found).isPresent();
            assertThat(found.get().getId()).isEqualTo(uploadSession.getId());
        }

        @Test
        void shouldNotFindByChecksum_whenCaseDiffers() {
            Optional<UploadSession> found = repository.findByChecksum("checkSum");
            assertThat(found).isEmpty();
        }

        @Test
        void shouldNotFindByChecksum_whenChecksumDiffers() {
            Optional<UploadSession> found = repository.findByChecksum("wrong-checksum");
            assertThat(found).isEmpty();
        }

        @Test
        void shouldNotFindByChecksum_whenChecksumisEmpty() {
            Optional<UploadSession> found = repository.findByChecksum("");
            assertThat(found).isEmpty();
        }
    }

    
}

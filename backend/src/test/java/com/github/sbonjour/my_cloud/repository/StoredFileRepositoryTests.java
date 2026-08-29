package com.github.sbonjour.my_cloud.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import com.github.sbonjour.my_cloud.TestDataFactory;
import com.github.sbonjour.my_cloud.entity.StoredFile;
import com.github.sbonjour.my_cloud.entity.StoredFile.MediaType;

@DataJpaTest
public class StoredFileRepositoryTests extends AbstractPostgresContainerTest {

    @Autowired
    StoredFileRepository repository;

    @Autowired
    TestEntityManager entityManager;

    @Nested
    class FindByChecksum {
        final String file1Checksum = "file1-checksum";
        StoredFile file1;

        @BeforeEach
        void setUp() {
            file1 = TestDataFactory.persistStoredFile(entityManager, file1Checksum, MediaType.IMAGE, "file1MimeType", 10,
                    "/uploads/" + file1Checksum);
        }

        @Test
        void shouldFindByChecksum() {
            Optional<StoredFile> found = repository.findByChecksum(file1Checksum);

            assertThat(found).isPresent();
            assertThat(found.get().getId()).isEqualTo(file1.getId());
        }

        @Test
        void shouldNotFindByChecksum_whenCaseDiffers() {
            Optional<StoredFile> found = repository.findByChecksum("FiLe1-checkSUM");

            assertThat(found).isEmpty();
        }
        @Test
        void shouldNotFindByChecksum_whenChecksumDiffers() {
            Optional<StoredFile> found = repository.findByChecksum("wrong-checksum");

            assertThat(found).isEmpty();
        }
        @Test
        void shouldNotFindByChecksum_whenChecksumisEmpty() {
            Optional<StoredFile> found = repository.findByChecksum("");

            assertThat(found).isEmpty();
        }
    }

    @Nested
    class ExistsByChecksum {
        final String file1Checksum = "file1-checksum";
        StoredFile file1;

        @BeforeEach
        void setUp() {
            file1 = TestDataFactory.persistStoredFile(entityManager, file1Checksum, MediaType.IMAGE, "file1MimeType", 10,
                    "/uploads/" + file1Checksum);
        }

        @Test
        void shouldExistsByChecksum() {
            boolean exists = repository.existsByChecksum(file1Checksum);

            assertThat(exists).isEqualTo(true);
        }
        @Test
        void shouldNotExistsByChecksum() {
            boolean exists = repository.existsByChecksum("wrong-checksum");

            assertThat(exists).isEqualTo(false);
        }
    }

    @Nested
    class FindByMediaType {

        @Test
        void shouldFindByMediaType_whenMediaTypeIsImage() {
            StoredFile image1 = TestDataFactory.persistStoredFile(entityManager, "image1Checksum", MediaType.IMAGE, "image1MimType", 10, "/uploads/image1Checksum");
            StoredFile image2 = TestDataFactory.persistStoredFile(entityManager, "image2Checksum", MediaType.IMAGE, "image2MimType", 10, "/uploads/image2Checksum");
            TestDataFactory.persistStoredFile(entityManager, "video1Checksum", MediaType.VIDEO, "video1MimType", 10, "/uploads/video1Checksum");

            List<StoredFile> found = repository.findByMediaType(MediaType.IMAGE);

            assertThat(found).containsExactlyInAnyOrder(image1, image2);
        }
        
        @Test
        void shouldFindByMediaType_whenMediaTypeIsVideo() {
            TestDataFactory.persistStoredFile(entityManager, "image1Checksum", MediaType.IMAGE, "image1MimType", 10, "/uploads/image1Checksum");
            TestDataFactory.persistStoredFile(entityManager, "image2Checksum", MediaType.IMAGE, "image2MimType", 10, "/uploads/image2Checksum");
            StoredFile video1 = TestDataFactory.persistStoredFile(entityManager, "video1Checksum", MediaType.VIDEO, "video1MimType", 10, "/uploads/video1Checksum");
            StoredFile video2 = TestDataFactory.persistStoredFile(entityManager, "video2Checksum", MediaType.VIDEO, "video2MimType", 10, "/uploads/video2Checksum");

            List<StoredFile> found = repository.findByMediaType(MediaType.VIDEO);

            assertThat(found).containsExactlyInAnyOrder(video1, video2);
        }
        
        @Test
        void shouldNotFindByMediaType_whenMediaTypeIsImage() {
            TestDataFactory.persistStoredFile(entityManager, "video1Checksum", MediaType.VIDEO, "video1MimType", 10, "/uploads/video1Checksum");
            TestDataFactory.persistStoredFile(entityManager, "video2Checksum", MediaType.VIDEO, "video2MimType", 10, "/uploads/video2Checksum");

            List<StoredFile> found = repository.findByMediaType(MediaType.IMAGE);

            assertThat(found).isEmpty();
        }
        
        @Test
        void shouldNotFindByMediaType_whenMediaTypeIsVideo() {
            TestDataFactory.persistStoredFile(entityManager, "image1Checksum", MediaType.IMAGE, "image1MimType", 10, "/uploads/image1Checksum");
            TestDataFactory.persistStoredFile(entityManager, "image2Checksum", MediaType.IMAGE, "image2MimType", 10, "/uploads/image2Checksum");

            List<StoredFile> found = repository.findByMediaType(MediaType.VIDEO);

            assertThat(found).isEmpty();
        }
    }
}

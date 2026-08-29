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
import com.github.sbonjour.my_cloud.entity.User;

@DataJpaTest
public class StoredFileRepositoryTest extends AbstractPostgresContainerTest {

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
            file1 = TestDataFactory.persistStoredFile(entityManager, file1Checksum, MediaType.IMAGE, "file1MimeType",
                    10,
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
            file1 = TestDataFactory.persistStoredFile(entityManager, file1Checksum, MediaType.IMAGE, "file1MimeType",
                    10,
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
            StoredFile image1 = TestDataFactory.persistStoredFile(entityManager, "image1Checksum", MediaType.IMAGE,
                    "image1MimType", 10, "/uploads/image1Checksum");
            StoredFile image2 = TestDataFactory.persistStoredFile(entityManager, "image2Checksum", MediaType.IMAGE,
                    "image2MimType", 10, "/uploads/image2Checksum");
            TestDataFactory.persistStoredFile(entityManager, "video1Checksum", MediaType.VIDEO, "video1MimType", 10,
                    "/uploads/video1Checksum");

            List<StoredFile> found = repository.findByMediaType(MediaType.IMAGE);

            assertThat(found).containsExactlyInAnyOrder(image1, image2);
        }

        @Test
        void shouldFindByMediaType_whenMediaTypeIsVideo() {
            TestDataFactory.persistStoredFile(entityManager, "image1Checksum", MediaType.IMAGE, "image1MimType", 10,
                    "/uploads/image1Checksum");
            TestDataFactory.persistStoredFile(entityManager, "image2Checksum", MediaType.IMAGE, "image2MimType", 10,
                    "/uploads/image2Checksum");
            StoredFile video1 = TestDataFactory.persistStoredFile(entityManager, "video1Checksum", MediaType.VIDEO,
                    "video1MimType", 10, "/uploads/video1Checksum");
            StoredFile video2 = TestDataFactory.persistStoredFile(entityManager, "video2Checksum", MediaType.VIDEO,
                    "video2MimType", 10, "/uploads/video2Checksum");

            List<StoredFile> found = repository.findByMediaType(MediaType.VIDEO);

            assertThat(found).containsExactlyInAnyOrder(video1, video2);
        }

        @Test
        void shouldNotFindByMediaType_whenMediaTypeIsImage() {
            TestDataFactory.persistStoredFile(entityManager, "video1Checksum", MediaType.VIDEO, "video1MimType", 10,
                    "/uploads/video1Checksum");
            TestDataFactory.persistStoredFile(entityManager, "video2Checksum", MediaType.VIDEO, "video2MimType", 10,
                    "/uploads/video2Checksum");

            List<StoredFile> found = repository.findByMediaType(MediaType.IMAGE);

            assertThat(found).isEmpty();
        }

        @Test
        void shouldNotFindByMediaType_whenMediaTypeIsVideo() {
            TestDataFactory.persistStoredFile(entityManager, "image1Checksum", MediaType.IMAGE, "image1MimType", 10,
                    "/uploads/image1Checksum");
            TestDataFactory.persistStoredFile(entityManager, "image2Checksum", MediaType.IMAGE, "image2MimType", 10,
                    "/uploads/image2Checksum");

            List<StoredFile> found = repository.findByMediaType(MediaType.VIDEO);

            assertThat(found).isEmpty();
        }
    }

    @Nested
    class GetTotalStorageUsed {
        @Test
        void shouldGetTotalStorageUsed_whenNoFile() {
            long found = repository.getTotalStorageUsed();

            assertThat(found).isEqualTo(0);
        }

        @Test
        void shouldGetTotalStorageUsed_whenOneFile() {
            long image1Size = 1032;
            TestDataFactory.persistStoredFile(entityManager, "image1Checksum", MediaType.IMAGE, "image1MimType",
                    image1Size, "/uploads/image1Checksum");
            long found = repository.getTotalStorageUsed();

            assertThat(found).isEqualTo(image1Size);
        }

        @Test
        void shouldGetTotalStorageUsed_whenMultipleFiles() {
            long image1Size = 1032032L;
            TestDataFactory.persistStoredFile(entityManager, "image1Checksum", MediaType.IMAGE, "image1MimType",
                    image1Size, "/uploads/image1Checksum");
            long image2Size = 320302L;
            TestDataFactory.persistStoredFile(entityManager, "image2Checksum", MediaType.IMAGE, "image2MimType",
                    image2Size, "/uploads/image2Checksum");
            long video1Size = 3230302012L;
            TestDataFactory.persistStoredFile(entityManager, "video1Checksum", MediaType.VIDEO, "video1MimType",
                    video1Size, "/uploads/video1Checksum");
            long video2Size = 80302012L;
            TestDataFactory.persistStoredFile(entityManager, "video2Checksum", MediaType.VIDEO, "video2MimType",
                    video2Size, "/uploads/video2Checksum");
            long found = repository.getTotalStorageUsed();

            assertThat(found).isEqualTo(image1Size + image2Size + video1Size + video2Size);
        }
    }

    @Nested
    class FindOrphaned {

        @Test
        void shouldFindOrphaned_whenStoredFileHasNoMediaAsset() {
            StoredFile orphan = TestDataFactory.persistStoredFile(entityManager, "orphan-checksum",
                    MediaType.IMAGE, "image/jpeg", 10, "/uploads/orphan-checksum");

            List<StoredFile> found = repository.findOrphaned();

            assertThat(found).containsExactly(orphan);
        }

        @Test
        void shouldNotFindOrphaned_whenStoredFileHasMediaAsset() {
            User owner = TestDataFactory.persistUser(entityManager, "owner@test.com", "owner", "hashedPassword");
            StoredFile file = TestDataFactory.persistStoredFile(entityManager, "file-checksum",
                    MediaType.IMAGE, "image/jpeg", 10, "/uploads/file-checksum");
            TestDataFactory.persistMediaAsset(entityManager, owner, file, "file.jpg");

            List<StoredFile> found = repository.findOrphaned();

            assertThat(found).isEmpty();
        }

        @Test
        void shouldFindOnlyOrphaned_whenMixOfOrphanedAndUsedFiles() {
            User owner = TestDataFactory.persistUser(entityManager, "owner@test.com", "owner", "hashedPassword");

            StoredFile used = TestDataFactory.persistStoredFile(entityManager, "used-checksum",
                    MediaType.IMAGE, "image/jpeg", 10, "/uploads/used-checksum");
            TestDataFactory.persistMediaAsset(entityManager, owner, used, "used.jpg");

            StoredFile orphan1 = TestDataFactory.persistStoredFile(entityManager, "orphan1-checksum",
                    MediaType.IMAGE, "image/jpeg", 10, "/uploads/orphan1-checksum");
            StoredFile orphan2 = TestDataFactory.persistStoredFile(entityManager, "orphan2-checksum",
                    MediaType.VIDEO, "video/mp4", 100, "/uploads/orphan2-checksum");

            List<StoredFile> found = repository.findOrphaned();

            assertThat(found).containsExactlyInAnyOrder(orphan1, orphan2);
        }

        @Test
        void shouldNotFindOrphaned_whenStoredFileWasOrphanedThenGotMediaAsset() {
            User owner = TestDataFactory.persistUser(entityManager, "owner@test.com", "owner", "hashedPassword");
            StoredFile file = TestDataFactory.persistStoredFile(entityManager, "file-checksum",
                    MediaType.IMAGE, "image/jpeg", 10, "/uploads/file-checksum");

            TestDataFactory.persistMediaAsset(entityManager, owner, file, "file.jpg");

            List<StoredFile> found = repository.findOrphaned();

            assertThat(found).doesNotContain(file);
        }

        @Test
        void shouldFindOrphaned_whenNoStoredFileAtAll() {
            List<StoredFile> found = repository.findOrphaned();

            assertThat(found).isEmpty();
        }
    }
}

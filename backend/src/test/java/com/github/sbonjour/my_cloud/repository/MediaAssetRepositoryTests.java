package com.github.sbonjour.my_cloud.repository;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.sbonjour.my_cloud.TestDataFactory;
import com.github.sbonjour.my_cloud.entity.MediaAsset;
import com.github.sbonjour.my_cloud.entity.StoredFile;
import com.github.sbonjour.my_cloud.entity.User;
import com.github.sbonjour.my_cloud.entity.StoredFile.MediaType;

@DataJpaTest
class MediaAssetRepositoryTests extends AbstractPostgresContainerTest {

        @Autowired
        MediaAssetRepository repository;

        @Autowired
        TestEntityManager entityManager;

        @Nested
        class FindByOwner {
                User owner;
                User otherOwner;
                StoredFile file;

                @BeforeEach
                void setUp() {
                        owner = TestDataFactory.persistUser(entityManager, "test@example.com", "test", "password1");
                        otherOwner = TestDataFactory.persistUser(entityManager, "other@example.com", "other",
                                        "password2");
                        file = TestDataFactory.persistStoredFile(entityManager, "file-checksum",
                                        MediaType.IMAGE,
                                        "file-mime-type", 10, "file.jpg");

                }

                @Test
                void shouldFindByOwner() {

                        MediaAsset asset = TestDataFactory.persistMediaAsset(entityManager, owner, file,
                                        "test-filename");

                        List<MediaAsset> found = repository.findByOwner(owner);

                        assertThat(found).containsExactly(asset);
                }

                @Test
                void shouldNotFindByOwner_whenOwnerHasNoAssets() {

                        TestDataFactory.persistMediaAsset(entityManager, owner, file, "test-filename");

                        List<MediaAsset> found = repository.findByOwner(otherOwner);

                        assertThat(found).isEmpty();
                }
        }

        @Nested
        class FindByOwnerAndFileNameIgnoringCase {
                User owner;
                User otherOwner;
                StoredFile file;
                StoredFile otherFile;

                @BeforeEach
                void setUp() {
                        owner = TestDataFactory.persistUser(entityManager, "test@example.com", "test",
                                        "password1");
                        otherOwner = TestDataFactory.persistUser(entityManager, "other@example.com", "other",
                                        "password2");

                        file = TestDataFactory.persistStoredFile(entityManager, "file-checksum",
                                        MediaType.IMAGE,
                                        "file-mime-type", 10, "file.jpg");
                        otherFile = TestDataFactory.persistStoredFile(entityManager, "other-file-checksum",
                                        MediaType.IMAGE,
                                        "other-file-mime-type", 10, "other-file.jpg");
                }

                @Test
                void shouldFindByOwnerAndFileNameIgnoringCase() {

                        TestDataFactory.persistMediaAsset(entityManager, owner, file, "file1");
                        MediaAsset asset = TestDataFactory.persistMediaAsset(entityManager, owner, otherFile, "file2");
                        TestDataFactory.persistMediaAsset(entityManager, otherOwner, otherFile, "file2");

                        Optional<MediaAsset> found = repository.findByOwnerAndFilenameIgnoringCase(owner, "FiLe2");

                        assertThat(found).isPresent();
                        assertThat(found.get().getId()).isEqualTo(asset.getId());
                        assertThat(found.get().getOwner().getId()).isEqualTo(owner.getId());

                }

                @Test
                void shouldNotFindByOwnerAndFileNameIgnoringCase_whenFilenameDoesntMatche() {

                        TestDataFactory.persistMediaAsset(entityManager, owner, file, "file1");
                        TestDataFactory.persistMediaAsset(entityManager, owner, otherFile, "file2");
                        TestDataFactory.persistMediaAsset(entityManager, otherOwner, otherFile, "file2");

                        Optional<MediaAsset> foundWithWrongName = repository.findByOwnerAndFilenameIgnoringCase(owner,
                                        "FiLe3");

                        assertThat(foundWithWrongName).isEmpty();

                }

                @Test
                void shouldNotFindByOwnerAndFileNameIgnoringCase_whenOwnerDoesntMatche() {

                        TestDataFactory.persistMediaAsset(entityManager, owner, file, "file1");
                        TestDataFactory.persistMediaAsset(entityManager, owner, otherFile, "file2");
                        TestDataFactory.persistMediaAsset(entityManager, otherOwner, otherFile, "file2");

                        Optional<MediaAsset> foundWithWrongOwner = repository.findByOwnerAndFilenameIgnoringCase(
                                        otherOwner,
                                        "FiLe1");

                        assertThat(foundWithWrongOwner).isEmpty();

                }
        }

        @Nested
        class FindByOwnerAndStoredFile {
                User owner;
                StoredFile file1;

                @BeforeEach
                void setUp() {
                        owner = TestDataFactory.persistUser(entityManager, "owner@test.com", "owner",
                                        "hashedPassword");

                        file1 = TestDataFactory.persistStoredFile(entityManager, "file1Checksum",
                                        MediaType.IMAGE,
                                        "file1MimeType", 10, "/uploads/file1Checksum");

                }

                @Test
                void shouldFindByOwnerAndStoredFile_whenOwnerHasMultipleMediaAssets() {
                        StoredFile file2 = TestDataFactory.persistStoredFile(entityManager, "file2Checksum",
                                        MediaType.IMAGE,
                                        "file2MimeType", 100, "/uploads/file2Checksum");

                        MediaAsset mediaAsset1 = TestDataFactory.persistMediaAsset(entityManager, owner, file1,
                                        "file1.jpg");
                        TestDataFactory.persistMediaAsset(entityManager, owner, file2, "file2.jpg");

                        Optional<MediaAsset> found = repository.findByOwnerAndStoredFile(owner, file1);

                        assertThat(found).isPresent();
                        assertThat(found.get().getId()).isEqualTo(mediaAsset1.getId());
                        assertThat(found.get().getOwner().getId()).isEqualTo(owner.getId());
                        assertThat(found.get().getStoredFile().getId()).isEqualTo(file1.getId());
                }

                @Test
                void shouldFindByOwnerAndStoredFile_whenOwnersShareSameStoredFile() {
                        User otherOwner = TestDataFactory.persistUser(entityManager, "otherOwner@test.com",
                                        "otherOwner",
                                        "otherHashedPassword");

                        MediaAsset mediaAsset1 = TestDataFactory.persistMediaAsset(entityManager, owner, file1,
                                        "file1.jpg");
                        TestDataFactory.persistMediaAsset(entityManager, otherOwner, file1, "otherFile1.jpg");

                        Optional<MediaAsset> found = repository.findByOwnerAndStoredFile(owner, file1);

                        assertThat(found).isPresent();
                        assertThat(found.get().getId()).isEqualTo(mediaAsset1.getId());
                        assertThat(found.get().getOwner().getId()).isEqualTo(owner.getId());
                        assertThat(found.get().getStoredFile().getId()).isEqualTo(file1.getId());
                }

                @Test
                void shouldNotFindByOwnerAndStoredFile_WhenOwnerDoesntMatch() {
                        User otherOwner = TestDataFactory.persistUser(entityManager, "otherOwner@test.com",
                                        "otherOwner",
                                        "otherHashedPassword");

                        StoredFile file2 = TestDataFactory.persistStoredFile(entityManager, "file2Checksum",
                                        MediaType.IMAGE,
                                        "file2MimeType", 10, "/uploads/file2Checksum");

                        TestDataFactory.persistMediaAsset(entityManager, otherOwner, file1, "file1.jpg");
                        TestDataFactory.persistMediaAsset(entityManager, owner, file2, "file2.jpg");

                        Optional<MediaAsset> found = repository.findByOwnerAndStoredFile(owner, file1);

                        assertThat(found).isEmpty();
                }

                @Test
                void shouldNotFindByOwnerAndStoredFile_WhenStoredFileDoesntMatch() {
                        StoredFile file2 = TestDataFactory.persistStoredFile(entityManager, "file2Checksum",
                                        MediaType.IMAGE,
                                        "file2MimeType", 10, "/uploads/file2Checksum");

                        TestDataFactory.persistMediaAsset(entityManager, owner, file1, "file1.jpg");

                        Optional<MediaAsset> found = repository.findByOwnerAndStoredFile(owner, file2);

                        assertThat(found).isEmpty();
                }
        }
        
        @Nested
        class FindByStoredFile {
                User owner;
                StoredFile file1;

                @BeforeEach
                void setUp() {
                        owner = TestDataFactory.persistUser(entityManager, "owner@test.com", "owner",
                                        "hashedPassword");

                        file1 = TestDataFactory.persistStoredFile(entityManager, "file1Checksum",
                                        MediaType.IMAGE,
                                        "file1MimeType", 10, "/uploads/file1Checksum");
                }

                @Test
                void shouldFindByStoredFile_WhenUsedByOneUser() {
                        StoredFile file2 = TestDataFactory.persistStoredFile(entityManager, "file2Checksum",
                                        MediaType.IMAGE,
                                        "file2MimeType", 10, "/uploads/file2Checksum");

                        MediaAsset mediaAsset1 = TestDataFactory.persistMediaAsset(entityManager, owner, file1,
                                        "file1.jpg");
                        TestDataFactory.persistMediaAsset(entityManager, owner, file2, "file2.jpg");

                        List<MediaAsset> found = repository.findByStoredFile(file1);

                        assertThat(found).hasSize(1).containsExactly(mediaAsset1);
                }

                @Test
                void shouldFindByStoredFile_WhenUsedByMultipleUsers() {
                        User otherOwner = TestDataFactory.persistUser(entityManager, "otherOwner@test.com",
                                        "otherOwner",
                                        "hashedPassword");

                        StoredFile file2 = TestDataFactory.persistStoredFile(entityManager, "file2Checksum",
                                        MediaType.IMAGE,
                                        "file2MimeType", 10, "/uploads/file2Checksum");

                        MediaAsset mediaAsset1 = TestDataFactory.persistMediaAsset(entityManager, owner, file1,
                                        "file1.jpg");
                        MediaAsset mediaAsset2 = TestDataFactory.persistMediaAsset(entityManager, otherOwner, file1,
                                        "otherFile1.jpg");
                        TestDataFactory.persistMediaAsset(entityManager, owner, file2, "file2.jpg");

                        List<MediaAsset> found = repository.findByStoredFile(file1);

                        assertThat(found).hasSize(2).containsExactlyInAnyOrder(mediaAsset1, mediaAsset2);
                }

                @Test
                void shouldNotFindByStoredFile() {
                        StoredFile file2 = TestDataFactory.persistStoredFile(entityManager, "file2Checksum",
                                        MediaType.IMAGE,
                                        "file2MimeType", 10, "/uploads/file2Checksum");

                        TestDataFactory.persistMediaAsset(entityManager, owner, file1, "file1.jpg");

                        List<MediaAsset> found = repository.findByStoredFile(file2);

                        assertThat(found).hasSize(0);
                }

        }

        @Nested
        class GetTotalUserStorageUsed {
                User owner;
                long storedFileSize = 10;
                StoredFile file1;

                @BeforeEach
                void setUp() {
                        owner = TestDataFactory.persistUser(entityManager, "owner@test.com", "owner",
                                        "hashedPassword");

                        storedFileSize = 10;
                        file1 = TestDataFactory.persistStoredFile(entityManager, "file1Checksum",
                                        MediaType.IMAGE,
                                        "file1MimeType", storedFileSize, "/uploads/file1Checksum");

                }

                @Test
                void shouldGetTotalUserStorageUsed_whenUserHasOneMediaAsset() {

                        TestDataFactory.persistMediaAsset(entityManager, owner, file1, "file1.jpg");

                        long found = repository.getUserTotalStorageUsed(owner);

                        assertThat(found).isEqualTo(storedFileSize);
                }

                @Test
                void shouldGetTotalUserStorageUsed_whenUserHasNoMediaAsset() {

                        long found = repository.getUserTotalStorageUsed(owner);

                        assertThat(found).isEqualTo(0);
                }
                
                @Test
                void shouldGetTotalUserStorageUsed_whenUserHasMultipleMediaAssets() {
                        long storedFile2Size = 234032;
                        long storedFile3Size = 32032;

                        StoredFile file2 = TestDataFactory.persistStoredFile(entityManager, "file2-checksum", MediaType.VIDEO, "file2MimeType", storedFile2Size, "/uploads/file2Checksum");
                        StoredFile file3 = TestDataFactory.persistStoredFile(entityManager, "file3-checksum", MediaType.VIDEO, "file3MimeType", storedFile3Size, "/uploads/file3Checksum");

                        TestDataFactory.persistMediaAsset(entityManager, owner, file1, "file1.jpg");
                        TestDataFactory.persistMediaAsset(entityManager, owner, file2, "file2.jpg");
                        TestDataFactory.persistMediaAsset(entityManager, owner, file3, "file3.jpg");

                        long found = repository.getUserTotalStorageUsed(owner);

                        assertThat(found).isEqualTo(storedFileSize + storedFile2Size + storedFile3Size);
                }
        }
}
package com.github.sbonjour.my_cloud.repository;

import java.util.List;
import java.util.Optional;

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
class MediaAssetsRepositoryTests extends AbstractPostgresContainerTest {

    @Autowired
    MediaAssetRepository repository;

    @Autowired
    TestEntityManager entityManager;

    @Test
    void shouldFindByOwner() {
        User owner = TestDataFactory.persistUser(entityManager, "test@example.com", "test", "password1");
        User otherOwner = TestDataFactory.persistUser(entityManager, "other@example.com", "other", "password2");

        StoredFile file = TestDataFactory.persistStoredFile(entityManager, "file-checksum", MediaType.IMAGE, "file-mime-type", 10, "file.jpg");
        StoredFile otherFile = TestDataFactory.persistStoredFile(entityManager, "other-file-checksum", MediaType.IMAGE, "other-file-mime-type", 10, "other-file.jpg");

        MediaAsset asset = TestDataFactory.persistMediaAsset(entityManager, owner, file, "test-filename");
        TestDataFactory.persistMediaAsset(entityManager, otherOwner, otherFile, "other-filename");

        List<MediaAsset> found = repository.findByOwner(owner);

        assertThat(found).hasSize(1);
        assertThat(found.get(0).getId()).isEqualTo(asset.getId());
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getOwner().getId()).isEqualTo(owner.getId());
        assertThat(found.get(0).getFilename()).isEqualTo("test-filename");
    }


    @Test
    void shouldNotFindByOwner() {
        User owner = TestDataFactory.persistUser(entityManager, "test@gmail.com", "owner", "hashedPassword");
        User otherOwner = TestDataFactory.persistUser(entityManager, "othertest@gmail.com", "other", "hashedPassword");

        StoredFile file = TestDataFactory.persistStoredFile(entityManager, "file-checksum", MediaType.IMAGE, "file-mime-type", 10, "file.jpg");

        TestDataFactory.persistMediaAsset(entityManager, owner, file, "test-filename");

        List<MediaAsset> found = repository.findByOwner(otherOwner);

        assertThat(found.isEmpty());
    }

    @Test
    void shouldFindByOwnerAndFileNameIgnoringCase(){
        User owner = TestDataFactory.persistUser(entityManager, "test@example.com", "test", "password1");
        User otherOwner = TestDataFactory.persistUser(entityManager, "other@example.com", "other", "password2");

        StoredFile file = TestDataFactory.persistStoredFile(entityManager, "file-checksum", MediaType.IMAGE, "file-mime-type", 10, "file.jpg");
        StoredFile otherFile = TestDataFactory.persistStoredFile(entityManager, "other-file-checksum", MediaType.IMAGE, "other-file-mime-type", 10, "other-file.jpg");

        TestDataFactory.persistMediaAsset(entityManager, owner, file, "file1");
        MediaAsset asset = TestDataFactory.persistMediaAsset(entityManager, owner, otherFile, "file2");
        TestDataFactory.persistMediaAsset(entityManager, otherOwner, otherFile, "file2");
        
        Optional<MediaAsset> found = repository.findByOwnerAndFilenameIgnoringCase(owner, "FiLe2");

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(asset.getId());
        assertThat(found.get().getOwner().getId()).isEqualTo(owner.getId());

    }
    
    @Test
    void shouldNotFindByOwnerAndFileNameIgnoringCase(){
        User owner = TestDataFactory.persistUser(entityManager, "test@example.com", "test", "password1");
        User otherOwner = TestDataFactory.persistUser(entityManager, "other@example.com", "other", "password2");

        StoredFile file = TestDataFactory.persistStoredFile(entityManager, "file-checksum", MediaType.IMAGE, "file-mime-type", 10, "file.jpg");
        StoredFile otherFile = TestDataFactory.persistStoredFile(entityManager, "other-file-checksum", MediaType.IMAGE, "other-file-mime-type", 10, "other-file.jpg");

        TestDataFactory.persistMediaAsset(entityManager, owner, file, "file1");
        TestDataFactory.persistMediaAsset(entityManager, owner, otherFile, "file2");
        TestDataFactory.persistMediaAsset(entityManager, otherOwner, otherFile, "file2");
        
        Optional<MediaAsset> foundWithWrongName = repository.findByOwnerAndFilenameIgnoringCase(owner, "FiLe3");
        Optional<MediaAsset> foundWithWrongOwner = repository.findByOwnerAndFilenameIgnoringCase(otherOwner, "FiLe1");

        assertThat(foundWithWrongName).isEmpty();
        assertThat(foundWithWrongOwner).isEmpty();

    }


    @Test
    void shouldFindByOwnerAndStoredFile_whenOwnerHasMultipleMediaAssets() {
        User owner = TestDataFactory.persistUser(entityManager, "owner@test.com", "owner", "hashedPassword");

        StoredFile file1 = TestDataFactory.persistStoredFile(entityManager, "file1Checksum", MediaType.IMAGE, "file1MimeType", 10, "/uploads/file1Checksum");
        StoredFile file2 = TestDataFactory.persistStoredFile(entityManager, "file2Checksum", MediaType.IMAGE, "file2MimeType", 100, "/uploads/file2Checksum");

        MediaAsset mediaAsset1 = TestDataFactory.persistMediaAsset(entityManager, owner, file1, "file1.jpg");
        TestDataFactory.persistMediaAsset(entityManager, owner, file2, "file2.jpg");

        Optional<MediaAsset> found = repository.findByOwnerAndStoredFile(owner, file1);

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(mediaAsset1.getId());
        assertThat(found.get().getOwner().getId()).isEqualTo(owner.getId());
        assertThat(found.get().getStoredFile().getId()).isEqualTo(file1.getId());
    }

    @Test
    void shouldFindByOwnerAndStoredFile_whenOwnersShareSameStoredFile() {
        User owner = TestDataFactory.persistUser(entityManager, "owner@test.com", "owner", "hashedPassword");
        User otherOwner = TestDataFactory.persistUser(entityManager, "otherOwner@test.com", "otherOwner", "otherHashedPassword");

        StoredFile file1 = TestDataFactory.persistStoredFile(entityManager, "file1Checksum", MediaType.IMAGE, "file1MimeType", 10, "/uploads/file1Checksum");

        MediaAsset mediaAsset1 = TestDataFactory.persistMediaAsset(entityManager, owner, file1, "file1.jpg");
        TestDataFactory.persistMediaAsset(entityManager, otherOwner, file1, "otherFile1.jpg");

        Optional<MediaAsset> found = repository.findByOwnerAndStoredFile(owner, file1);

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(mediaAsset1.getId());
        assertThat(found.get().getOwner().getId()).isEqualTo(owner.getId());
        assertThat(found.get().getStoredFile().getId()).isEqualTo(file1.getId());
    }
}
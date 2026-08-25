package com.github.sbonjour.my_cloud.repository;

import java.util.List;

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

}
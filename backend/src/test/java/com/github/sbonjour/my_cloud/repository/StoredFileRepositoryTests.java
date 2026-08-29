package com.github.sbonjour.my_cloud.repository;

import static org.assertj.core.api.Assertions.assertThat;

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

}

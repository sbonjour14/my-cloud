package com.github.sbonjour.my_cloud.repository;

import com.github.sbonjour.my_cloud.entity.StoredFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StoredFileRepository extends JpaRepository<StoredFile, UUID> {

    Optional<StoredFile> findByChecksum(String checksum);

    boolean existsByChecksum(String checksum);

    List<StoredFile> findByFileType(StoredFile.FileType mediaType);

    @Query("SELECT COALESCE(SUM(s.sizeBytes), 0) FROM StoredFile s")
    Long getTotalStorageUsed();

    @Query("""
            SELECT sf FROM StoredFile sf
            WHERE NOT EXISTS (
                SELECT 1 FROM MediaAsset ma WHERE ma.storedFile = sf
            )
            """)
    List<StoredFile> findOrphaned();
}
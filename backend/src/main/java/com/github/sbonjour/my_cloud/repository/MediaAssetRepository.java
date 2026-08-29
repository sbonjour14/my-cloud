package com.github.sbonjour.my_cloud.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.github.sbonjour.my_cloud.entity.MediaAsset;
import com.github.sbonjour.my_cloud.entity.StoredFile;
import com.github.sbonjour.my_cloud.entity.User;

public interface MediaAssetRepository extends JpaRepository<MediaAsset, UUID> {
    List<MediaAsset> findByOwner(User owner);

    Optional<MediaAsset> findByOwnerAndFilenameIgnoringCase(User owner, String fileName);

    Optional<MediaAsset> findByOwnerAndStoredFile(User owner, StoredFile storedFile);

    List<MediaAsset> findByStoredFile(StoredFile storedFile);

    @Query(
        """
        SELECT COALESCE(SUM(s.sizeBytes), 0) FROM StoredFile s
        WHERE EXISTS (
        SELECT 1 FROM MediaAsset ma WHERE ma.owner = :user AND ma.storedFile = s
        )
        """
    )
    Long getUserTotalStorageUsed(@Param("user") User user);
}

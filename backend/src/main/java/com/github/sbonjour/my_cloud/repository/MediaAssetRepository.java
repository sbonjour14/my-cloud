package com.github.sbonjour.my_cloud.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Window;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.github.sbonjour.my_cloud.FileType;
import com.github.sbonjour.my_cloud.entity.MediaAsset;
import com.github.sbonjour.my_cloud.entity.StoredFile;
import com.github.sbonjour.my_cloud.entity.User;

public interface MediaAssetRepository extends JpaRepository<MediaAsset, UUID> {
    List<MediaAsset> findByOwner(User owner);

    List<MediaAsset> findByOwner(User owner, Pageable pageable);

    List<MediaAsset> findByOwnerAndFileType(User owner, FileType type, Pageable pageable);

    Optional<MediaAsset> findByOwnerAndFilenameIgnoringCase(User owner, String fileName);

    Optional<MediaAsset> findByOwnerAndStoredFile(User owner, StoredFile storedFile);

    List<MediaAsset> findByStoredFile(StoredFile storedFile);

    @Query("""
            SELECT COALESCE(SUM(s.sizeBytes), 0) FROM StoredFile s
            WHERE EXISTS (
            SELECT 1 FROM MediaAsset ma WHERE ma.owner = :user AND ma.storedFile = s
            )
            """)
    Long getUserTotalStorageUsed(@Param("user") User user);

    long countByOwner(User owner);

    @EntityGraph(attributePaths = "storedFile")
    Window<MediaAsset> findByOwner(User owner, ScrollPosition position, Limit limit, Sort sort);

    @EntityGraph(attributePaths = "storedFile")
    Window<MediaAsset> findByOwnerAndFileType(User owner, FileType fileType,
            ScrollPosition position, Limit limit, Sort sort);
}

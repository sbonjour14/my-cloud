package com.github.sbonjour.my_cloud.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.github.sbonjour.my_cloud.entity.MediaAsset;
import com.github.sbonjour.my_cloud.entity.StoredFile;
import com.github.sbonjour.my_cloud.entity.User;

public interface MediaAssetRepository extends JpaRepository<MediaAsset, UUID> {
    List<MediaAsset> findByOwner(User owner);

    List<MediaAsset> findByOwnerOrderByUploadedAtAsc(User owner);

    List<MediaAsset> findByOwnerOrderByUploadedAtDesc(User owner);

    Optional<MediaAsset> findByOwnerAndFileNameIgnoringCase(User owner, String fileName);

    Optional<MediaAsset> findByOwnerAndStoredFile(User owner, StoredFile storedFile);

    List<MediaAsset> findByStoredFile(StoredFile storedFile);
}

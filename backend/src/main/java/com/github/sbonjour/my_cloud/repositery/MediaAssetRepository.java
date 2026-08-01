package com.github.sbonjour.my_cloud.repositery;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.github.sbonjour.my_cloud.entity.MediaAsset;
import com.github.sbonjour.my_cloud.entity.User;

public interface MediaAssetRepository extends JpaRepository<MediaAsset, UUID> {
    List<MediaAsset> findByOwner(User owner);

    List<MediaAsset> findByOwnerOrderByUploadedAtAsc(User owner);

    List<MediaAsset> findByOwnerOrderByUploadedAtDesc(User owner);

    Optional<MediaAsset> findByFileName(String fileName);
}

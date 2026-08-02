package com.github.sbonjour.my_cloud.repository;

import com.github.sbonjour.my_cloud.entity.Album;
import com.github.sbonjour.my_cloud.entity.AlbumItem;
import com.github.sbonjour.my_cloud.entity.MediaAsset;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AlbumItemRepository extends JpaRepository<AlbumItem, UUID> {

    List<AlbumItem> findByAlbum(Album album);

    List<AlbumItem> findByAlbumOrderByAddedAtDesc(Album album);

    List<AlbumItem> findByMediaAsset(MediaAsset mediaAsset);

    boolean existsByAlbumAndMediaAsset(Album album, MediaAsset mediaAsset);

    void deleteByAlbumAndMediaAsset(Album album, MediaAsset mediaAsset);

    long countByAlbum(Album album);
}
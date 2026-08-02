package com.github.sbonjour.my_cloud.repository;

import com.github.sbonjour.my_cloud.entity.Album;
import com.github.sbonjour.my_cloud.entity.AlbumShare;
import com.github.sbonjour.my_cloud.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AlbumShareRepository extends JpaRepository<AlbumShare, UUID> {

    List<AlbumShare> findByAlbum(Album album);

    List<AlbumShare> findBySharedWithUser(User user);

    Optional<AlbumShare> findByAlbumAndSharedWithUser(Album album, User user);

    boolean existsByAlbumAndSharedWithUser(Album album, User user);

    void deleteByAlbumAndSharedWithUser(Album album, User user);
}
package com.github.sbonjour.my_cloud.repositery;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.github.sbonjour.my_cloud.entity.Album;
import com.github.sbonjour.my_cloud.entity.User;

public interface AlbumRepository extends JpaRepository<Album, UUID> {
    List<Album> findByOwner(User owner);
    List<Album> findByOwnerOrderByCreatedAtDesc(User owner);
    List<Album> findByOwnerOrderByCreatedAtAsc(User owner);
    
}

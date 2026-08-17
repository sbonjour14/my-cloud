package com.github.sbonjour.my_cloud.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.github.sbonjour.my_cloud.entity.Album;

public interface AlbumRepository extends JpaRepository<Album, UUID> {
    
}

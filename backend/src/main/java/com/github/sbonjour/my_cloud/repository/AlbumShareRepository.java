package com.github.sbonjour.my_cloud.repository;

import com.github.sbonjour.my_cloud.entity.AlbumShare;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AlbumShareRepository extends JpaRepository<AlbumShare, UUID> {

}
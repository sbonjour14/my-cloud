package com.github.sbonjour.my_cloud.repository;

import com.github.sbonjour.my_cloud.entity.AlbumItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AlbumItemRepository extends JpaRepository<AlbumItem, UUID> {

}
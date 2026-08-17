package com.github.sbonjour.my_cloud.repository;

import com.github.sbonjour.my_cloud.entity.Friendship;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FriendshipRepository extends JpaRepository<Friendship, UUID> {
}
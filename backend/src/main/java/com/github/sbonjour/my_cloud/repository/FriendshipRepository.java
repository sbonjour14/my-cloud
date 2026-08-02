package com.github.sbonjour.my_cloud.repository;

import com.github.sbonjour.my_cloud.entity.Friendship;
import com.github.sbonjour.my_cloud.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FriendshipRepository extends JpaRepository<Friendship, UUID> {

    @Query("SELECT f FROM Friendship f WHERE " +
           "((f.requester = :user1 AND f.requested = :user2) OR " +
           " (f.requester = :user2 AND f.requested = :user1)) " +
           "AND f.status = 'ACCEPTED'")
    Optional<Friendship> findAcceptedFriendship(User user1, User user2);

    @Query("SELECT f FROM Friendship f WHERE " +
           "(f.requester = :user1 AND f.requested = :user2) OR " +
           "(f.requester = :user2 AND f.requested = :user1)")
    Optional<Friendship> findFriendshipBetween(User user1, User user2);

    List<Friendship> findByRequestedAndStatus(User requested, Friendship.FriendshipStatus status);

    List<Friendship> findByRequesterAndStatus(User requester, Friendship.FriendshipStatus status);
}
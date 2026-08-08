package com.github.sbonjour.my_cloud.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Represents a share of an album with another user.
 *
 * Each AlbumShare links an Album to a User, allowing the owner of
 * the album to share access with other users. An AlbumShare is
 * unique per combination of album and shared-with user.
 */

@Entity
@Table(name = "album_shares", uniqueConstraints = @UniqueConstraint(columnNames = { "album_id",
        "shared_with_user_id" }))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class AlbumShare {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "album_id", nullable = false)
    private Album album;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shared_with_user_id", nullable = false)
    private User sharedWithUser;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private Instant sharedAt = Instant.now();
}
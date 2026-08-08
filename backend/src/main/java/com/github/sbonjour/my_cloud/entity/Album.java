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
 * Represents a photo/video album owned by a single user.
 *
 * An album's name must be unique per owner (a user cannot have two
 * albums with the same name, but different users can reuse names).
 * Albums can be shared with other users via AlbumShare, and contain
 * media items via AlbumItem.
 */

@Entity
@Table(
    name = "albums",
    uniqueConstraints = @UniqueConstraint(columnNames = {"owner_id", "name"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")


public class Album {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(nullable = false)
    private String name;

    @Column
    private String description;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
    
}

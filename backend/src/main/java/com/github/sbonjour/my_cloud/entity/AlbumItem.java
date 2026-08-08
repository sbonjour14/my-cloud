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
 * Represents a media item (photo or video) that belongs to an album.
 *
 * Each AlbumItem links a MediaAsset to an Album, allowing users to
 * organize their media into albums. An AlbumItem is unique per
 * combination of album and media asset.
 */

@Entity
@Table(name = "album_items", uniqueConstraints = @UniqueConstraint(columnNames = { "album_id", "media_asset_id" }))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class AlbumItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "album_id", nullable = false)
    private Album album;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "media_asset_id", nullable = false)
    private MediaAsset mediaAsset;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private Instant addedAt = Instant.now();
}
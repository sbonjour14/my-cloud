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
 * Represents a media asset (photo or video) uploaded by a user.
 *
 * Each MediaAsset is associated with a StoredFile that contains the
 * actual file data, and is owned by a User. The combination of owner
 * and file name must be unique to prevent duplicate file names for
 * the same user.
 */

@Entity
@Table(name = "media_assets", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "owner_id", "filename" })
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class MediaAsset {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false, foreignKey = @ForeignKey(foreignKeyDefinition = "FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE"))
    private User owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stored_file_id", nullable = false, foreignKey = @ForeignKey(foreignKeyDefinition = "FOREIGN KEY (stored_file_id) REFERENCES stored_files(id) ON DELETE CASCADE"))
    private StoredFile storedFile;

    @Column(nullable = false)
    private String filename;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}
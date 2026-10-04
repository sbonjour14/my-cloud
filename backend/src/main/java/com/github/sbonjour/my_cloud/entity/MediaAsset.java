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

import com.github.sbonjour.my_cloud.service.FileType;

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
}, indexes = {
        @Index(name = "idx_media_assets_owner_taken_at", columnList = "owner_id, taken_at DESC, id DESC"),
        @Index(name = "idx_media_assets_owner_type_taken_at", columnList = "owner_id, file_type, taken_at DESC, id DESC")
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
    
    @Column(nullable = false, updatable = false)
    @Builder.Default
    private Instant takenAt = Instant.now();

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private Boolean takenAtKnown = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FileType fileType;

    public String getUrl() {
        return "/mediaAssets/" + id;
    }
}
package com.github.sbonjour.my_cloud.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


/**
 * Represents a file stored in the system, which can be an image or video.
 *
 * Each StoredFile has a unique checksum to prevent duplicate storage,
 * and contains metadata such as storage path, MIME type, size, and media type.
 */

@Entity
@Table(name = "stored_files")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class StoredFile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String checksum;

    @Column(nullable = false)
    private String storagePath;

    @Column
    private Boolean hasThumbnail;

    @Column(nullable = false)
    private String mimeType;

    @Column(nullable = false)
    private long sizeBytes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MediaType mediaType;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    public enum MediaType {
        IMAGE, VIDEO
    }

    public String getThumbnailPath() {
        int lastSlash = storagePath.lastIndexOf('/');
        String checksum = storagePath.substring(lastSlash + 1);
        return storagePath.substring(0, lastSlash) + "/thumbnail/" + checksum;
    }
}
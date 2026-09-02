package com.github.sbonjour.my_cloud.entity;

import java.util.Set;
import java.util.UUID;

import com.github.sbonjour.my_cloud.entity.StoredFile.FileType;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "upload_sessions")
@Getter
@Setter
@Builder
@AllArgsConstructor
@EqualsAndHashCode (of = "id")
@NoArgsConstructor
public class UploadSession {
    @Id
    @GeneratedValue (strategy = GenerationType.UUID)
    UUID id;

    @Column(nullable = false)
    String filename;

    @Column(nullable = false)
    String tempFilePath;

    @Column(nullable = false)
    String mediaType;

    @Column(nullable = false)
    FileType fileType;


    @Column(nullable = false)
    long totalSize;
    
    @Column(nullable = false)
    long uploadedSize;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    UploadSessionStatus status = UploadSessionStatus.UPLOADING;

    @Column(nullable = false)
    int totalChunks;


    @ElementCollection
    Set<Integer> uploadedChunks;

    public enum UploadSessionStatus {
        COMPLETE, UPLOADING, PAUSED
    }
}

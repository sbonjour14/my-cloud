package com.github.sbonjour.my_cloud.scheduler;

import com.github.sbonjour.my_cloud.entity.StoredFile;
import com.github.sbonjour.my_cloud.repository.StoredFileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Scheduled job for cleaning up orphaned stored files.
 *
 * This job runs daily at 3 AM and identifies stored files that are not associated
 * with any media assets. It deletes the physical files from the storage and removes
 * their records from the database.
 */


@Component
@RequiredArgsConstructor
@Slf4j
public class OrphanedFileCleanupJob {

    private final StoredFileRepository storedFileRepository;

    // Tous les jours à 3h du matin
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanupOrphanedFiles() {
        List<StoredFile> orphaned = storedFileRepository.findOrphaned();

        if (orphaned.isEmpty()) {
            log.info("No orphaned stored files found.");
            return;
        }

        log.info("Found {} orphaned stored file(s), cleaning up...", orphaned.size());

        for (StoredFile storedFile : orphaned) {
            try {
                Files.deleteIfExists(Path.of(storedFile.getStoragePath()));
                storedFileRepository.delete(storedFile);
                log.info("Deleted orphaned file: {} ({})", storedFile.getId(), storedFile.getStoragePath());
            } catch (IOException e) {
                log.error("Failed to delete physical file for {}: {}", storedFile.getId(), e.getMessage());
            }
        }
    }
}
package com.github.sbonjour.my_cloud.service;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.mov.QuickTimeDirectory;
import com.drew.metadata.mp4.Mp4Directory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class MetadataService {

    private static final Logger log = LoggerFactory.getLogger(MetadataService.class);
    private static final DateTimeFormatter EXIF_FORMAT = DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss");

    // Les vidéos sans date renvoient souvent 1904 ou 1970 : on les ignore
    private static final Instant MIN_VALID = Instant.parse("1971-01-01T00:00:00Z");

    // 2023-08-14, 20230814, 20230814_102231, 2023-08-14 10.22.31,
    // PXL_20230814_102231123...
    private static final Pattern DATE_IN_NAME = Pattern.compile(
            "(?<!\\d)((?:19|20)\\d{2})[-_.]?(0[1-9]|1[0-2])[-_.]?(0[1-9]|[12]\\d|3[01])"
                    + "(?:[-_. T]?([01]\\d|2[0-3])[-_.:]?([0-5]\\d)[-_.:]?([0-5]\\d))?");

    /**
     * Renvoie la date de prise de vue, ou null si elle est introuvable.
     * Ordre : métadonnées du fichier, puis nom d'origine du fichier.
     * Ne lance jamais d'exception.
     */
    public Instant extractTakenAt(Path file, String mimeType, String originalFilename) {
        Instant takenAt = null;

        if (mimeType != null && mimeType.startsWith("image/")) {
            takenAt = extractPhoto(file);
        } else if (mimeType != null && mimeType.startsWith("video/")) {
            takenAt = extractVideo(file);
        }

        if (takenAt == null) {
            takenAt = parseFromFilename(originalFilename);
        }
        return takenAt;
    }

    private Instant extractPhoto(Path file) {
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(file.toFile());
            ExifSubIFDDirectory exif = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
            if (exif == null)
                return null;

            String raw = exif.getString(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
            if (raw == null)
                raw = exif.getString(ExifSubIFDDirectory.TAG_DATETIME_DIGITIZED);
            if (raw == null)
                return null;

            return LocalDateTime.parse(raw.trim(), EXIF_FORMAT).toInstant(ZoneOffset.UTC);
        } catch (Exception e) {
            log.warn("Lecture EXIF impossible pour {}", file, e);
            return null;
        }
    }

    private Instant extractVideo(Path file) {
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(file.toFile());
            showDetails(metadata);

            Date date = null;
            Mp4Directory mp4 = metadata.getFirstDirectoryOfType(Mp4Directory.class);
            if (mp4 != null) {
                date = mp4.getDate(Mp4Directory.TAG_CREATION_TIME);
            }
            if (date == null) {
                QuickTimeDirectory mov = metadata.getFirstDirectoryOfType(QuickTimeDirectory.class);
                if (mov != null) {
                    date = mov.getDate(QuickTimeDirectory.TAG_CREATION_TIME);
                }
            }
            if (date == null)
                return null;

            Instant instant = date.toInstant();
            return instant.isBefore(MIN_VALID) ? null : instant;
        } catch (Exception e) {
            log.warn("Lecture des métadonnées vidéo impossible pour {}", file, e);
            return null;
        }
    }

    private Instant parseFromFilename(String filename) {
        if (filename == null)
            return null;

        Matcher m = DATE_IN_NAME.matcher(filename);
        if (!m.find())
            return null;

        try {
            int hour = m.group(4) != null ? Integer.parseInt(m.group(4)) : 0;
            int minute = m.group(5) != null ? Integer.parseInt(m.group(5)) : 0;
            int second = m.group(6) != null ? Integer.parseInt(m.group(6)) : 0;

            return LocalDateTime.of(
                    Integer.parseInt(m.group(1)),
                    Integer.parseInt(m.group(2)),
                    Integer.parseInt(m.group(3)),
                    hour, minute, second)
                    .toInstant(ZoneOffset.UTC);
        } catch (DateTimeException e) {
            return null; // ex. 20230231
        }
    }

    public static void showDetails(Metadata metadata) {
        System.out.println();
        System.out.println();
        for (Directory directory : metadata.getDirectories()) {
            System.out.println("===== " + directory.getName() + " =====");

            for (Tag tag : directory.getTags()) {
                System.out.println(
                        tag.getTagName() + " : " + tag.getDescription());
            }

            if (directory.hasErrors()) {
                for (String error : directory.getErrors()) {
                    System.out.println("ERROR : " + error);
                }
            }
        }
        System.out.println();
        System.out.println();
    }
}
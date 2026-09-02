package com.github.sbonjour.my_cloud;

import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import com.github.sbonjour.my_cloud.entity.MediaAsset;
import com.github.sbonjour.my_cloud.entity.StoredFile;
import com.github.sbonjour.my_cloud.entity.UploadSession;
import com.github.sbonjour.my_cloud.entity.User;
import com.github.sbonjour.my_cloud.entity.StoredFile.FileType;

public class TestDataFactory {
    

    public static User persistUser(TestEntityManager em, String email, String displayName, String hashedPassword) {
        User user = User.builder()
        .email(email)
        .password(hashedPassword)
        .displayName(displayName)
        .build();
        
        return em.persistAndFlush(user);
    }


    public static UploadSession persistUploadSession(TestEntityManager em, UploadSession us){
        return  em.persistAndFlush(us);
    }

    public static StoredFile persistStoredFile(TestEntityManager em, String checksum, FileType fileType, String mediaType, long size, String storagePath) {
        StoredFile file = StoredFile.builder()
        .checksum(checksum)
        .fileType(fileType)
        .mediaType(mediaType)
        .sizeBytes(size)
        .storagePath(storagePath)
        .build();

        return em.persistAndFlush(file);
    }


    public static MediaAsset persistMediaAsset(TestEntityManager em, User owner, StoredFile file, String filename) {
        MediaAsset ma = MediaAsset.builder()
        .filename(filename)
        .owner(owner)
        .storedFile(file)
        .build();

        return em.persistAndFlush(ma);
    }
}

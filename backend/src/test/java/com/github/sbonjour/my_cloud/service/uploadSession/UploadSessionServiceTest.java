package com.github.sbonjour.my_cloud.service.uploadSession;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.github.sbonjour.my_cloud.entity.User;
import com.github.sbonjour.my_cloud.repository.UploadSessionRepository;
import com.github.sbonjour.my_cloud.service.FileService;
import com.github.sbonjour.my_cloud.service.MediaAssetService;
import com.github.sbonjour.my_cloud.service.StoredFileService;
import com.github.sbonjour.my_cloud.service.UploadSessionService;

@ExtendWith(MockitoExtension.class)
public abstract class UploadSessionServiceTest {
        @InjectMocks
        protected UploadSessionService service;

        @Mock
        protected UploadSessionRepository repository;
        @Mock
        protected MediaAssetService mediaAssetService;
        @Mock
        protected StoredFileService storedFileService;
        @Mock
        protected FileService fileService;

        protected String uploadPath = "/uploads";
        User user;

        @BeforeEach
        void setUp() {
                ReflectionTestUtils.setField(service, "uploadPath", uploadPath);
                user = User.builder()
                                .displayName("test")
                                .email("test@example.com")
                                .id(UUID.randomUUID())
                                .password("hashedPassword")
                                .build();
        }
}

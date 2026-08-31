package com.github.sbonjour.my_cloud.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import jakarta.servlet.http.Cookie;

import com.github.sbonjour.my_cloud.entity.MediaAsset;
import com.github.sbonjour.my_cloud.entity.StoredFile;
import com.github.sbonjour.my_cloud.entity.User;
import com.github.sbonjour.my_cloud.repository.UserRepository;
import com.github.sbonjour.my_cloud.security.JwtService;
import com.github.sbonjour.my_cloud.service.MediaAssetService;

@WebMvcTest(MediaAssetController.class)
public class MediaAssetControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    MediaAssetService mediaAssetService;

    @MockitoBean
    UserRepository userRepository; // for JwtFilter

    @MockitoBean
    JwtService jwtService;

    private final String cookieName = "my-cloud-token";

    @Nested
    class UploadFile {

        @Test
        void shouldReturnSuccess() throws Exception {

            MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", new byte[50]);
            StoredFile sf = StoredFile.builder()
                    .id(UUID.randomUUID())
                    .checksum("checksum")
                    .storagePath("/uploads/checksum")
                    .hasThumbnail(false)
                    .mediaType(StoredFile.MediaType.IMAGE)
                    .mimeType("image/jpeg")
                    .sizeBytes(50)
                    .build();

            User user = User.builder()
                    .displayName("test")
                    .email("test@example.com")
                    .id(UUID.randomUUID())
                    .build();

            MediaAsset created = MediaAsset.builder()
                    .id(UUID.randomUUID())
                    .filename("test.jpg")
                    .owner(user)
                    .storedFile(sf)
                    .build();

            when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));

            when(mediaAssetService.uploadFile(any(), any())).thenReturn(created);

            when(jwtService.extractUserId(anyString())).thenReturn(user.getId());
            when(jwtService.isTokenValid(anyString())).thenReturn(true);

            mockMvc.perform(multipart("/mediaAssets")
                    .cookie(new Cookie(cookieName, "good-cookie"))
                    .file(file))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.filename").value("test.jpg"));
        }

    }

}

package com.github.sbonjour.my_cloud.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import jakarta.servlet.http.Cookie;

import com.github.sbonjour.my_cloud.entity.MediaAsset;
import com.github.sbonjour.my_cloud.entity.StoredFile;
import com.github.sbonjour.my_cloud.entity.User;
import com.github.sbonjour.my_cloud.exception.ConflictException;
import com.github.sbonjour.my_cloud.exception.InternalServerErrorException;
import com.github.sbonjour.my_cloud.service.MediaAssetService;

@WebMvcTest(MediaAssetController.class)
public class MediaAssetControllerTest extends AbstractControllerTest {

    @MockitoBean
    MediaAssetService mediaAssetService;

    private final String cookieName = "my-cloud-token";

    User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .displayName("test")
                .email("test@example.com")
                .id(UUID.randomUUID())
                .build();
    }

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

            MediaAsset created = MediaAsset.builder()
                    .id(UUID.randomUUID())
                    .filename("test.jpg")
                    .owner(user)
                    .storedFile(sf)
                    .build();

            when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

            when(mediaAssetService.uploadFile(any(), any())).thenReturn(created);

            when(jwtService.extractUserId(anyString())).thenReturn(user.getId());
            when(jwtService.isTokenValid(anyString())).thenReturn(true);

            var result = mockMvc.perform(multipart("/mediaAssets")
                    .file(file)
                    .cookie(new Cookie(cookieName, "good-cookie")))
                    .andReturn();

            verify(jwtService).isTokenValid("good-cookie");
            verify(jwtService).extractUserId("good-cookie");
            verify(userRepository).findById(user.getId());

            assertThat(result.getResponse().getStatus()).isEqualTo(201);
        }

        @Test
        void shouldReturn409_whenConflictExceptionThrown() throws Exception {
            MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", new byte[50]);

            when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
            when(jwtService.extractUserId(anyString())).thenReturn(user.getId());
            when(jwtService.isTokenValid(anyString())).thenReturn(true);

            when(mediaAssetService.uploadFile(any(), any()))
                    .thenThrow(new ConflictException("A media asset with the same name already exists for this user"));

            mockMvc.perform(multipart("/mediaAssets")
                    .cookie(new Cookie(cookieName, "good-cookie"))
                    .file(file))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status").value(409))
                    .andExpect(jsonPath("$.error").value("Conflict"))
                    .andExpect(jsonPath("$.message")
                            .value("A media asset with the same name already exists for this user"));
        }

        @Test
        void shouldReturn500_whenInternalServerErrorThrown() throws Exception {
            MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", new byte[50]);

            when(userRepository.findById(any())).thenReturn(Optional.of(user));
            when(jwtService.extractUserId(anyString())).thenReturn(user.getId());
            when(jwtService.isTokenValid(anyString())).thenReturn(true);

            when(mediaAssetService.uploadFile(any(), any()))
                    .thenThrow(new InternalServerErrorException("Error while saving the file"));

            mockMvc.perform(multipart("/mediaAssets")
                    .cookie(new Cookie(cookieName, "good-cookie"))
                    .file(file))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.status").value(500))
                    .andExpect(jsonPath("$.message").value("Error while saving the file"));
        }

        @Test
        void shouldReturn401_whenNoCookieProvided() throws Exception {
            MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", new byte[50]);

            mockMvc.perform(multipart("/mediaAssets")
                    .file(file))
                    .andExpect(status().isUnauthorized());

        }

        @Test
        void shouldReturn400_whenNoFileProvided() throws Exception {

            when(userRepository.findById(any())).thenReturn(Optional.of(user));
            when(jwtService.extractUserId(anyString())).thenReturn(user.getId());
            when(jwtService.isTokenValid(anyString())).thenReturn(true);

            mockMvc.perform(multipart("/mediaAssets")
                    .cookie(new Cookie(cookieName, "good-cookie")))
                    .andExpect(status().isBadRequest());
        }
    }
}

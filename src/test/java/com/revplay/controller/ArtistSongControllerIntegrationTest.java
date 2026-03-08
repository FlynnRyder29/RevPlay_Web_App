package com.revplay.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.revplay.dto.SongCreateRequest;
import com.revplay.dto.SongDTO;
import com.revplay.dto.SongUpdateRequest;
import com.revplay.exception.RevPlayAccessDeniedHandler;
import com.revplay.exception.RevPlayAuthenticationEntryPoint;
import com.revplay.model.Song;
import com.revplay.repository.UserRepository;
import com.revplay.service.CustomUserDetailsService;
import com.revplay.service.FileStorageService;
import com.revplay.service.SongService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static com.revplay.util.TestConstants.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.revplay.config.SecurityConfig;
import org.springframework.context.annotation.Import;

@WebMvcTest(ArtistSongController.class)
@Import(SecurityConfig.class)
@DisplayName("ArtistSongController Integration Tests")
class ArtistSongControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private SongService songService;
    @MockitoBean private FileStorageService fileStorageService;

    @MockitoBean private CustomUserDetailsService customUserDetailsService;
    @MockitoBean private RevPlayAuthenticationEntryPoint authEntryPoint;
    @MockitoBean private RevPlayAccessDeniedHandler accessDeniedHandler;
    @MockitoBean private UserRepository userRepository;

    private SongDTO testSongDTO;

    @BeforeEach
    void setUp() throws Exception {
        doAnswer(inv -> {
            jakarta.servlet.http.HttpServletResponse resp =
                    inv.getArgument(1, jakarta.servlet.http.HttpServletResponse.class);
            resp.setStatus(jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().flush();
            return null;
        }).when(authEntryPoint).commence(any(), any(), any());

        doAnswer(inv -> {
            jakarta.servlet.http.HttpServletResponse resp =
                    inv.getArgument(1, jakarta.servlet.http.HttpServletResponse.class);
            resp.setStatus(jakarta.servlet.http.HttpServletResponse.SC_FORBIDDEN);
            resp.getWriter().flush();
            return null;
        }).when(accessDeniedHandler).handle(any(), any(), any());

        testSongDTO = SongDTO.builder()
                .id(TEST_SONG_ID).title(TEST_SONG_TITLE)
                .genre(TEST_SONG_GENRE).duration(TEST_SONG_DURATION)
                .artistId(TEST_ARTIST_ID).artistName(TEST_ARTIST_NAME)
                .audioUrl(TEST_SONG_AUDIO_URL)
                .releaseDate(LocalDate.now())
                .build();
    }

    // ── POST /api/artists/songs (multipart) ───────────────────────

    @Test
    @DisplayName("POST /api/artists/songs (multipart) - artist - returns 201 with song")
    @WithMockUser(username = TEST_USER_EMAIL, roles = "ARTIST")
    void uploadSong_artist_returns201() throws Exception {
        MockMultipartFile audioFile = new MockMultipartFile(
                "audio", "song.mp3", "audio/mpeg", "audiodata".getBytes());

        when(fileStorageService.storeFile(any(), eq("audio"))).thenReturn("audio/song.mp3");
        when(songService.createSong(any(SongCreateRequest.class))).thenReturn(testSongDTO);

        mockMvc.perform(multipart("/api/artists/songs")
                        .file(audioFile)
                        .param("title", TEST_SONG_TITLE)
                        .param("genre", TEST_SONG_GENRE)
                        .param("duration", "210")
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value(TEST_SONG_TITLE));
    }


    @Test
    @DisplayName("POST /api/artists/songs (multipart) - unauthenticated - returns 401")
    void uploadSong_unauthenticated_returns401() throws Exception {
        MockMultipartFile audioFile = new MockMultipartFile(
                "audio", "song.mp3", "audio/mpeg", "audiodata".getBytes());

        mockMvc.perform(multipart("/api/artists/songs")
                        .file(audioFile)
                        .param("title", TEST_SONG_TITLE)
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/artists/songs (multipart) - with cover file - stores both files")
    @WithMockUser(username = TEST_USER_EMAIL, roles = "ARTIST")
    void uploadSong_withCoverFile_storesBothFiles() throws Exception {
        MockMultipartFile audioFile = new MockMultipartFile(
                "audio", "song.mp3", "audio/mpeg", "audiodata".getBytes());
        MockMultipartFile coverFile = new MockMultipartFile(
                "cover", "cover.jpg", "image/jpeg", "imgdata".getBytes());

        when(fileStorageService.storeFile(any(), eq("audio"))).thenReturn("audio/song.mp3");
        when(fileStorageService.storeFile(any(), eq("covers"))).thenReturn("covers/cover.jpg");
        when(songService.createSong(any())).thenReturn(testSongDTO);

        mockMvc.perform(multipart("/api/artists/songs")
                        .file(audioFile).file(coverFile)
                        .param("title", TEST_SONG_TITLE)
                        .with(csrf()))
                .andExpect(status().isCreated());

        verify(fileStorageService).storeFile(any(), eq("audio"));
        verify(fileStorageService).storeFile(any(), eq("covers"));
    }

    // ── POST /api/artists/songs (JSON) ────────────────────────────

    @Test
    @DisplayName("POST /api/artists/songs (JSON) - artist - returns 201")
    @WithMockUser(username = TEST_USER_EMAIL, roles = "ARTIST")
    void createSongJson_artist_returns201() throws Exception {
        when(songService.createSong(any(SongCreateRequest.class))).thenReturn(testSongDTO);

        SongCreateRequest req = new SongCreateRequest();
        req.setTitle(TEST_SONG_TITLE);
        req.setGenre(TEST_SONG_GENRE);
        req.setDuration(210);
        req.setAudioUrl("/audio/test.mp3");
        req.setVisibility(Song.Visibility.PUBLIC);

        mockMvc.perform(post("/api/artists/songs").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value(TEST_SONG_TITLE));
    }

    // ── PUT /api/artists/songs/{id} ───────────────────────────────

    @Test
    @DisplayName("PUT /api/artists/songs/{id} - artist - returns 200 with updated song")
    @WithMockUser(username = TEST_USER_EMAIL, roles = "ARTIST")
    void updateSong_artist_returns200() throws Exception {
        when(songService.updateSong(eq(TEST_SONG_ID), any(SongUpdateRequest.class))).thenReturn(testSongDTO);

        mockMvc.perform(put("/api/artists/songs/{id}", TEST_SONG_ID).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Updated Title\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(TEST_SONG_ID));
    }

    // ── DELETE /api/artists/songs/{id} ────────────────────────────

    @Test
    @DisplayName("DELETE /api/artists/songs/{id} - artist - returns 204")
    @WithMockUser(username = TEST_USER_EMAIL, roles = "ARTIST")
    void deleteSong_artist_returns204() throws Exception {
        doNothing().when(songService).deleteSong(TEST_SONG_ID);

        mockMvc.perform(delete("/api/artists/songs/{id}", TEST_SONG_ID).with(csrf()))
                .andExpect(status().isNoContent());

        verify(songService).deleteSong(TEST_SONG_ID);
    }


    // ── PUT /api/artists/songs/{id}/visibility ────────────────────

    @Test
    @DisplayName("PUT /api/artists/songs/{id}/visibility - artist - returns 200")
    @WithMockUser(username = TEST_USER_EMAIL, roles = "ARTIST")
    void updateVisibility_artist_returns200() throws Exception {
        when(songService.updateVisibility(eq(TEST_SONG_ID), eq("PUBLIC"))).thenReturn(testSongDTO);

        mockMvc.perform(put("/api/artists/songs/{id}/visibility", TEST_SONG_ID).with(csrf())
                        .param("visibility", "PUBLIC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(TEST_SONG_ID));
    }

    @Test
    @DisplayName("PUT /api/artists/songs/{id}/visibility - UNLISTED - returns 200")
    @WithMockUser(username = TEST_USER_EMAIL, roles = "ARTIST")
    void updateVisibility_unlisted_returns200() throws Exception {
        when(songService.updateVisibility(eq(TEST_SONG_ID), eq("UNLISTED"))).thenReturn(testSongDTO);

        mockMvc.perform(put("/api/artists/songs/{id}/visibility", TEST_SONG_ID).with(csrf())
                        .param("visibility", "UNLISTED"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/artists/songs (JSON) - artist - calls songService.createSong")
    @WithMockUser(username = TEST_USER_EMAIL, roles = "ARTIST")
    void createSongJson_artist_callsService() throws Exception {
        when(songService.createSong(any(SongCreateRequest.class))).thenReturn(testSongDTO);

        SongCreateRequest req = new SongCreateRequest();
        req.setTitle(TEST_SONG_TITLE);
        req.setGenre(TEST_SONG_GENRE);
        req.setDuration(210);
        req.setAudioUrl("/audio/test.mp3");
        req.setVisibility(Song.Visibility.PUBLIC);

        mockMvc.perform(post("/api/artists/songs").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());

        verify(songService).createSong(any(SongCreateRequest.class));
    }

    @Test
    @DisplayName("PUT /api/artists/songs/{id} - artist - calls songService.updateSong")
    @WithMockUser(username = TEST_USER_EMAIL, roles = "ARTIST")
    void updateSong_artist_callsService() throws Exception {
        when(songService.updateSong(eq(TEST_SONG_ID), any(SongUpdateRequest.class))).thenReturn(testSongDTO);

        mockMvc.perform(put("/api/artists/songs/{id}", TEST_SONG_ID).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"New Title\",\"genre\":\"Rock\"}"))
                .andExpect(status().isOk());

        verify(songService).updateSong(eq(TEST_SONG_ID), any(SongUpdateRequest.class));
    }

    @Test
    @DisplayName("DELETE /api/artists/songs/{id} - artist - calls songService.deleteSong")
    @WithMockUser(username = TEST_USER_EMAIL, roles = "ARTIST")
    void deleteSong_artist_callsService() throws Exception {
        doNothing().when(songService).deleteSong(TEST_SONG_ID);

        mockMvc.perform(delete("/api/artists/songs/{id}", TEST_SONG_ID).with(csrf()))
                .andExpect(status().isNoContent());

        verify(songService).deleteSong(TEST_SONG_ID);
    }

    @Test
    @DisplayName("POST /api/artists/songs (multipart) - no albumId - albumId is null in request")
    @WithMockUser(username = TEST_USER_EMAIL, roles = "ARTIST")
    void uploadSong_noAlbumId_albumIdIsNull() throws Exception {
        MockMultipartFile audioFile = new MockMultipartFile(
                "audio", "song.mp3", "audio/mpeg", "audiodata".getBytes());

        when(fileStorageService.storeFile(any(), eq("audio"))).thenReturn("audio/song.mp3");
        when(songService.createSong(any(SongCreateRequest.class))).thenReturn(testSongDTO);

        mockMvc.perform(multipart("/api/artists/songs")
                        .file(audioFile)
                        .param("title", TEST_SONG_TITLE)
                        .param("albumId", "")
                        .with(csrf()))
                .andExpect(status().isCreated());

        verify(songService).createSong(argThat(r -> r.getAlbumId() == null));
    }
}
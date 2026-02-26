package com.revplay.revplay_p2.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.revplay.revplay_p2.model.Playlist;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public class PlaylistControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testCreatePlaylist() throws Exception {
        Playlist playlist = new Playlist();
        playlist.setName("Test Playlist");
        playlist.setDescription("Test Description");
        playlist.setPublic(true);
        playlist.setUserId(1L);

        String json = objectMapper.writeValueAsString(playlist);

        MvcResult result = mockMvc.perform(post("/api/playlists")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Test Playlist"))
                .andReturn();

        Playlist created = objectMapper.readValue(result.getResponse().getContentAsString(), Playlist.class);
        assertNotNull(created.getId());
    }

    @Test
    public void testGetMyPlaylists() throws Exception {
        mockMvc.perform(get("/api/playlists/me")
                        .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    public void testGetPlaylistById() throws Exception {
        Playlist playlist = new Playlist();
        playlist.setName("GetById Test");
        playlist.setPublic(true);
        playlist.setUserId(1L);
        String json = objectMapper.writeValueAsString(playlist);

        MvcResult createResult = mockMvc.perform(post("/api/playlists")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andReturn();
        Playlist created = objectMapper.readValue(createResult.getResponse().getContentAsString(), Playlist.class);
        Long id = created.getId();

        mockMvc.perform(get("/api/playlists/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value("GetById Test"));
    }

    @Test
    public void testUpdatePlaylist() throws Exception {
        Playlist playlist = new Playlist();
        playlist.setName("Original Name");
        playlist.setPublic(true);
        playlist.setUserId(1L);
        String json = objectMapper.writeValueAsString(playlist);
        MvcResult createResult = mockMvc.perform(post("/api/playlists")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andReturn();
        Playlist created = objectMapper.readValue(createResult.getResponse().getContentAsString(), Playlist.class);
        Long id = created.getId();

        created.setName("Updated Name");
        created.setDescription("Updated description");
        String updateJson = objectMapper.writeValueAsString(created);

        mockMvc.perform(put("/api/playlists/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.description").value("Updated description"));
    }

    @Test
    public void testDeletePlaylist() throws Exception {
        Playlist playlist = new Playlist();
        playlist.setName("To Be Deleted");
        playlist.setPublic(true);
        playlist.setUserId(1L);
        String json = objectMapper.writeValueAsString(playlist);
        MvcResult createResult = mockMvc.perform(post("/api/playlists")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andReturn();
        Playlist created = objectMapper.readValue(createResult.getResponse().getContentAsString(), Playlist.class);
        Long id = created.getId();

        mockMvc.perform(delete("/api/playlists/{id}", id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/playlists/{id}", id))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void testGetPublicPlaylists() throws Exception {
        mockMvc.perform(get("/api/playlists/public"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    public void testAddSongToPlaylist() throws Exception {
        Playlist playlist = new Playlist();
        playlist.setName("Add Song Test");
        playlist.setPublic(true);
        playlist.setUserId(1L);
        String json = objectMapper.writeValueAsString(playlist);
        MvcResult createResult = mockMvc.perform(post("/api/playlists")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andReturn();
        Playlist created = objectMapper.readValue(createResult.getResponse().getContentAsString(), Playlist.class);
        Long id = created.getId();

        mockMvc.perform(post("/api/playlists/{id}/songs/{songId}", id, 101))
                .andExpect(status().isOk());
    }

    @Test
    public void testRemoveSongFromPlaylist() throws Exception {
        Playlist playlist = new Playlist();
        playlist.setName("Remove Song Test");
        playlist.setPublic(true);
        playlist.setUserId(1L);
        String json = objectMapper.writeValueAsString(playlist);
        MvcResult createResult = mockMvc.perform(post("/api/playlists")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andReturn();
        Playlist created = objectMapper.readValue(createResult.getResponse().getContentAsString(), Playlist.class);
        Long id = created.getId();

        mockMvc.perform(post("/api/playlists/{id}/songs/{songId}", id, 101))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/playlists/{id}/songs/{songId}", id, 101))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testReorderSongs() throws Exception {
        Playlist playlist = new Playlist();
        playlist.setName("Reorder Test");
        playlist.setPublic(true);
        playlist.setUserId(1L);
        String json = objectMapper.writeValueAsString(playlist);
        MvcResult createResult = mockMvc.perform(post("/api/playlists")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andReturn();
        Playlist created = objectMapper.readValue(createResult.getResponse().getContentAsString(), Playlist.class);
        Long id = created.getId();

        mockMvc.perform(post("/api/playlists/{id}/songs/{songId}", id, 101));
        mockMvc.perform(post("/api/playlists/{id}/songs/{songId}", id, 102));

        List<Long> newOrder = List.of(102L, 101L);
        String orderJson = objectMapper.writeValueAsString(newOrder);

        mockMvc.perform(put("/api/playlists/{id}/reorder", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson))
                .andExpect(status().isOk());
    }

    @Test
    public void testFollowPlaylist() throws Exception {
        Playlist playlist = new Playlist();
        playlist.setName("Follow Test");
        playlist.setPublic(true);
        playlist.setUserId(2L);
        String json = objectMapper.writeValueAsString(playlist);
        MvcResult createResult = mockMvc.perform(post("/api/playlists")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andReturn();
        Playlist created = objectMapper.readValue(createResult.getResponse().getContentAsString(), Playlist.class);
        Long id = created.getId();

        mockMvc.perform(post("/api/playlists/{id}/follow", id)
                        .param("userId", "1"))
                .andExpect(status().isOk());
    }

    @Test
    public void testUnfollowPlaylist() throws Exception {
        Playlist playlist = new Playlist();
        playlist.setName("Unfollow Test");
        playlist.setPublic(true);
        playlist.setUserId(2L);
        String json = objectMapper.writeValueAsString(playlist);
        MvcResult createResult = mockMvc.perform(post("/api/playlists")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andReturn();
        Playlist created = objectMapper.readValue(createResult.getResponse().getContentAsString(), Playlist.class);
        Long id = created.getId();

        mockMvc.perform(post("/api/playlists/{id}/follow", id).param("userId", "1"));

        mockMvc.perform(delete("/api/playlists/{id}/follow", id)
                        .param("userId", "1"))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testGetFollowers() throws Exception {
        Playlist playlist = new Playlist();
        playlist.setName("Followers Test");
        playlist.setPublic(true);
        playlist.setUserId(2L);
        String json = objectMapper.writeValueAsString(playlist);
        MvcResult createResult = mockMvc.perform(post("/api/playlists")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andReturn();
        Playlist created = objectMapper.readValue(createResult.getResponse().getContentAsString(), Playlist.class);
        Long id = created.getId();

        mockMvc.perform(post("/api/playlists/{id}/follow", id).param("userId", "1"));

        mockMvc.perform(get("/api/playlists/{id}/followers", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
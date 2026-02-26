package com.revplay.revplay_p2.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public class FavoriteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testAddFavorite() throws Exception {
        mockMvc.perform(post("/api/favorites/{songId}", 101)
                        .param("userId", "1"))
                .andExpect(status().isOk());
    }

    @Test
    public void testRemoveFavorite() throws Exception {
        mockMvc.perform(post("/api/favorites/{songId}", 101).param("userId", "1"));
        mockMvc.perform(delete("/api/favorites/{songId}", 101)
                        .param("userId", "1"))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testGetMyFavorites() throws Exception {
        mockMvc.perform(post("/api/favorites/{songId}", 101).param("userId", "1"));
        mockMvc.perform(get("/api/favorites")
                        .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
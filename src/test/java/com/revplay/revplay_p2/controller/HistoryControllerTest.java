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
public class HistoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testRecordPlay() throws Exception {
        mockMvc.perform(post("/api/history")
                        .param("userId", "1")
                        .param("songId", "101"))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetHistory() throws Exception {
        mockMvc.perform(post("/api/history").param("userId", "1").param("songId", "101"));
        mockMvc.perform(get("/api/history")
                        .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    public void testGetAllHistory() throws Exception {
        mockMvc.perform(post("/api/history").param("userId", "1").param("songId", "101"));
        mockMvc.perform(post("/api/history").param("userId", "1").param("songId", "102"));
        mockMvc.perform(get("/api/history/all")
                        .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    public void testClearHistory() throws Exception {
        mockMvc.perform(post("/api/history").param("userId", "1").param("songId", "101"));
        mockMvc.perform(delete("/api/history")
                        .param("userId", "1"))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/history/all")
                        .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
package com.revplay.controller;

import com.revplay.model.ListeningHistory;
import com.revplay.service.HistoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/history")
public class HistoryController {

    private static final Logger log =
            LoggerFactory.getLogger(HistoryController.class);

    private final HistoryService historyService;

    public HistoryController(HistoryService historyService) {
        this.historyService = historyService;
    }

    // -------------------------
    // ADD TO HISTORY
    // -------------------------
    @PostMapping
    public ResponseEntity<Void> addToHistory(@RequestBody HistoryRequest request) {
        historyService.addToHistory(request.getSongId());
        return ResponseEntity.ok().build();
    }

    // -------------------------
    // GET MY HISTORY
    // -------------------------
    @GetMapping
    public Page<ListeningHistory> getMyHistory(int limit) {
        if (limit <= 0) limit = 50;
        if (limit > 200) limit = 200;

        // -------------------------
    // CLEAR HISTORY
    // -------------------------
    @DeleteMapping
    public ResponseEntity<Void> clearHistory() {

        log.info("DELETE /api/history");

        historyService.clearHistory();

        return ResponseEntity.noContent().build();
    }
}
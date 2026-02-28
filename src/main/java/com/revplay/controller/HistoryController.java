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
    @PostMapping("/{songId}")
    public ResponseEntity<Void> addToHistory(@PathVariable Long songId) {

        log.info("POST /api/history/{}", songId);

        historyService.addToHistory(songId);

        return ResponseEntity.ok().build();
    }

    // -------------------------
    // GET MY HISTORY
    // -------------------------
    @GetMapping
    public ResponseEntity<Page<ListeningHistory>> getMyHistory(
            @RequestParam(defaultValue = "50") int limit) {

        log.info("GET /api/history?limit={}", limit);

        return ResponseEntity.ok(
                historyService.getMyHistory(limit)
        );
    }

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
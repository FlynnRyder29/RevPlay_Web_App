package com.revplay.controller;

import com.revplay.dto.HistoryDTO;
import com.revplay.dto.HistoryRequest;
import com.revplay.service.HistoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

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
    public ResponseEntity<Void> addToHistory(
            @Valid @RequestBody HistoryRequest request) {

        log.info("POST /api/history songId={}", request.getSongId());

        historyService.addToHistory(request.getSongId());

        return ResponseEntity.ok().build();
    }

    // -------------------------
    // GET MY HISTORY (RECENT / LIMITED)
    // GET /api/history?limit=50  ← original path preserved (backward-compatible)
    // -------------------------

    @GetMapping
    public ResponseEntity<Page<HistoryDTO>> getMyHistory(
            @RequestParam(defaultValue = "50") int limit) {

        log.info("GET /api/history?limit={}", limit);

        return ResponseEntity.ok(
                historyService.getMyHistory(limit)
        );
    }

    // -------------------------
    // GET ALL HISTORY (complete, unpaginated)
    // GET /api/history/all  ← new endpoint added per project plan
    // -------------------------

    @GetMapping("/all")
    public ResponseEntity<List<HistoryDTO>> getAllHistory() {

        log.info("GET /api/history/all");

        return ResponseEntity.ok(
                historyService.getAllHistory()
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
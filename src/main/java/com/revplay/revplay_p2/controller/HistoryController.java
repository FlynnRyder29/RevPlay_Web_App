package com.revplay.revplay_p2.controller;

import com.revplay.revplay_p2.model.ListeningHistory;
import com.revplay.revplay_p2.service.HistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/history")
public class HistoryController {

    @Autowired
    private HistoryService historyService;

    @PostMapping
    public ResponseEntity<ListeningHistory> record(@RequestParam Long userId, @RequestParam Long songId) {
        return ResponseEntity.ok(historyService.recordPlay(userId, songId));
    }

    @GetMapping
    public ResponseEntity<List<ListeningHistory>> getHistory(@RequestParam Long userId,
                                                             @RequestParam(required = false) Integer limit) {
        return ResponseEntity.ok(historyService.getHistory(userId, limit));
    }

    @GetMapping("/all")
    public ResponseEntity<List<ListeningHistory>> getAll(@RequestParam Long userId) {
        return ResponseEntity.ok(historyService.getAllHistory(userId));
    }

    @DeleteMapping
    public ResponseEntity<Void> clear(@RequestParam Long userId) {
        historyService.clearHistory(userId);
        return ResponseEntity.noContent().build();
    }
}
package com.revplay.revplay_p2.service;

import com.revplay.revplay_p2.model.ListeningHistory;
import com.revplay.revplay_p2.repository.HistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class HistoryService {

    @Autowired
    private HistoryRepository historyRepository;

    public ListeningHistory recordPlay(Long userId, Long songId) {
        ListeningHistory history = new ListeningHistory();
        history.setUserId(userId);
        history.setSongId(songId);
        history.setPlayedAt(LocalDateTime.now());
        return historyRepository.save(history);
    }

    public List<ListeningHistory> getHistory(Long userId, Integer limit) {
        List<ListeningHistory> all = historyRepository.findByUserIdOrderByPlayedAtDesc(userId);
        if (limit != null && limit > 0 && limit < all.size()) {
            return all.subList(0, limit);
        }
        return all;
    }

    public List<ListeningHistory> getAllHistory(Long userId) {
        return historyRepository.findByUserIdOrderByPlayedAtDesc(userId);
    }

    public void clearHistory(Long userId) {
        historyRepository.deleteByUserId(userId);
    }
}
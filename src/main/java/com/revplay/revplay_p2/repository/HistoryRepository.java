package com.revplay.revplay_p2.repository;

import com.revplay.revplay_p2.model.ListeningHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface HistoryRepository extends JpaRepository<ListeningHistory, Long> {
    List<ListeningHistory> findByUserIdOrderByPlayedAtDesc(Long userId);
    void deleteByUserId(Long userId);
}
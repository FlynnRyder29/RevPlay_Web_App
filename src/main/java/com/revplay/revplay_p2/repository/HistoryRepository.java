package com.revplay.revplay_p2.repository;

import com.revplay.revplay_p2.model.ListeningHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface HistoryRepository extends JpaRepository<ListeningHistory, Long> {
    List<ListeningHistory> findByUserIdOrderByPlayedAtDesc(Long userId);
    void deleteByUserId(Long userId);
}
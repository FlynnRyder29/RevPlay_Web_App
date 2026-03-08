package com.revplay.repository;

import com.revplay.model.ListeningHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoryRepository extends JpaRepository<ListeningHistory, Long> {

    // Recent history — paginated (used by getMyHistory)
    Page<ListeningHistory> findByUser_IdOrderByPlayedAtDesc(
            Long userId,
            Pageable pageable
    );

    // Full history — non-paginated (used by getAllHistory)
    List<ListeningHistory> findAllByUser_IdOrderByPlayedAtDesc(
            Long userId
    );

    // Delete all history for a user (used by clearHistory)
    void deleteByUser_Id(Long userId);
}
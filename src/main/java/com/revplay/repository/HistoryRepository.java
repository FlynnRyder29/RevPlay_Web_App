package com.revplay.repository;

import com.revplay.model.ListeningHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoryRepository extends JpaRepository<ListeningHistory, Long> {

    // Recent history (paginated)
    Page<ListeningHistory> findByUser_IdOrderByPlayedAtDesc(
            Long userId,
            Pageable pageable
    );

    // Full history (non-paginated)
    List<ListeningHistory> findByUser_IdOrderByPlayedAtDesc(
            Long userId
    );

    // Clear user history
    void deleteByUser_Id(Long userId);
}
package com.revplay.repository;

import com.revplay.model.ListeningHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface HistoryRepository extends JpaRepository<ListeningHistory, Long> {

    // Get listening history with pagination
    Page<ListeningHistory> findByUser_IdOrderByPlayedAtDesc(
            Long userId,
            Pageable pageable
    );

    // Delete all history of a user
    @Transactional
    void deleteByUser_Id(Long userId);
}
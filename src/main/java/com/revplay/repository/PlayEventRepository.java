package com.revplay.repository;

import com.revplay.model.PlayEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlayEventRepository extends JpaRepository<PlayEvent, Long> {

    List<PlayEvent> findBySongId(Long songId);

        List<PlayEvent> findByUserId(Long userId);

    long countBySongId(Long songId);
}
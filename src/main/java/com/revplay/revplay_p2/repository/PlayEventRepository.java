package com.revplay.revplay_p2.repository;

import com.revplay.revplay_p2.model.PlayEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlayEventRepository extends JpaRepository<PlayEvent, Long> {
}
package com.revplay.repository;

import com.revplay.model.PlayEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlayEventRepository extends JpaRepository<PlayEvent, Long> {
}
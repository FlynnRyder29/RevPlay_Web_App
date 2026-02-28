package com.revplay.repository;

import com.revplay.model.PlayEvent;
import com.revplay.model.Song;
import com.revplay.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlayEventRepository extends JpaRepository<PlayEvent, Long> {

    List<PlayEvent> findBySong(Song song);

    List<PlayEvent> findByUser(User user);

    long countBySong(Song song);
}
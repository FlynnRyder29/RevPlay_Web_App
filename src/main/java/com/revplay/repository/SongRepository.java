package com.revplay.repository;

import com.revplay.model.Song;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public interface SongRepository extends JpaRepository<Song, Long> {

    // needed for GET /api/songs (paginated)
    Page<Song> findAll(Pageable pageable);
}

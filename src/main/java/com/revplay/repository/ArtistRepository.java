package com.revplay.repository;

import com.revplay.model.Artist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ArtistRepository extends JpaRepository<Artist, Long> {
    // findAll() from JpaRepository is enough for GET /api/artists today
}

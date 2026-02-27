package com.revplay.repository;

import com.revplay.model.Album;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlbumRepository extends JpaRepository<Album, Long> {
    // findAll() from JpaRepository is enough for GET /api/albums today
}

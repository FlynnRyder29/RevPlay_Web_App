package com.revplay.repository;

import com.revplay.model.Genre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GenreRepository extends JpaRepository<Genre, Long> {
    // findAll() from JpaRepository is enough for GET /api/genres today
}

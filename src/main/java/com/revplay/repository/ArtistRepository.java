package com.revplay.repository;

import com.revplay.model.Artist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ArtistRepository extends JpaRepository<Artist, Long> {

    // Existing
    Optional<Artist> findByUserId(Long userId);
    boolean existsByUserId(Long userId);

    // ═══ NEW: Search artists by name or genre ═══
    @Query("SELECT a FROM Artist a WHERE " +
            "LOWER(a.artistName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(a.genre) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Artist> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
}
package com.revplay.repository;

import com.revplay.model.Playlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlaylistRepository extends JpaRepository<Playlist, Long> {

    // All playlists created by a specific user
    List<Playlist> findByUser_Id(Long userId);

    // All public playlists (unsorted — kept for backward compatibility)
    List<Playlist> findByIsPublicTrue();

    // All public playlists — newest first (used by browse endpoint)
    List<Playlist> findByIsPublicTrueOrderByCreatedAtDesc();

    // Public playlists filtered by name keyword — newest first (used by search)
    @Query("SELECT p FROM Playlist p WHERE p.isPublic = true " +
            "AND LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "ORDER BY p.createdAt DESC")
    List<Playlist> searchPublicByName(@Param("keyword") String keyword);

    // Public playlists of a specific user
    List<Playlist> findByUser_IdAndIsPublicTrue(Long userId);

    // Count playlists for a user (Day 8 — profile stats)
    long countByUser_Id(Long userId);
}
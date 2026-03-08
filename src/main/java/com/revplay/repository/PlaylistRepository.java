package com.revplay.repository;

import com.revplay.model.Playlist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlaylistRepository extends JpaRepository<Playlist, Long> {

    // Existing
    List<Playlist> findByUser_Id(Long userId);
    List<Playlist> findByPublicPlaylistTrue();
    List<Playlist> findByPublicPlaylistTrueOrderByCreatedAtDesc();

    @Query("SELECT p FROM Playlist p WHERE p.publicPlaylist = true " +
            "AND LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "ORDER BY p.createdAt DESC")
    List<Playlist> searchPublicByName(@Param("keyword") String keyword);

    List<Playlist> findByUser_IdAndPublicPlaylistTrue(Long userId);
    long countByUser_Id(Long userId);

    // ═══ NEW: Search public playlists (paginated) ═══
    @Query("SELECT p FROM Playlist p WHERE p.publicPlaylist = true AND " +
            "LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Playlist> searchPublicByKeyword(@Param("keyword") String keyword, Pageable pageable);
}
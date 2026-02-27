package com.revplay.repository;

import com.revplay.model.PlaylistSong;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PlaylistSongRepository extends JpaRepository<PlaylistSong, Long> {
    List<PlaylistSong> findByPlaylistIdOrderByPosition(Long playlistId);
    void deleteByPlaylistIdAndSongId(Long playlistId, Long songId);
    boolean existsByPlaylistIdAndSongId(Long playlistId, Long songId);
}
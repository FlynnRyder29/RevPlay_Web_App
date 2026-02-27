package com.revplay.revplay_p2.repository;

import com.revplay.revplay_p2.model.PlaylistSong;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PlaylistSongRepository extends JpaRepository<PlaylistSong, Long> {
    List<PlaylistSong> findByPlaylistIdOrderByPosition(Long playlistId);
    void deleteByPlaylistId(Long playlistId);
    void deleteByPlaylistIdAndSongId(Long playlistId, Long songId);
    int countByPlaylistId(Long playlistId);
}
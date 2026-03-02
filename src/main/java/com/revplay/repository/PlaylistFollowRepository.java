package com.revplay.repository;

import com.revplay.model.PlaylistFollow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlaylistFollowRepository
        extends JpaRepository<PlaylistFollow, Long> {

    List<PlaylistFollow> findByUser_Id(Long userId);

    boolean existsByUser_IdAndPlaylist_Id(Long userId, Long playlistId);

    void deleteByUser_IdAndPlaylist_Id(Long userId, Long playlistId);
}
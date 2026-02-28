package com.revplay.repository;

import com.revplay.model.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    // All favorites of a user
    List<Favorite> findByUser_Id(Long userId);

    // Find specific favorite
    Optional<Favorite> findByUser_IdAndSong_Id(Long userId, Long songId);

    // Check if already favorited
    boolean existsByUser_IdAndSong_Id(Long userId, Long songId);

    // Remove favorite
    void deleteByUser_IdAndSong_Id(Long userId, Long songId);
}
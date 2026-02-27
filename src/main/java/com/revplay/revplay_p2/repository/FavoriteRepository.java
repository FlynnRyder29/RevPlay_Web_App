package com.revplay.revplay_p2.repository;

import com.revplay.revplay_p2.model.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
<<<<<<< HEAD
import java.util.List;
import java.util.Optional;

=======
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
>>>>>>> 92cfd1a2bdda491fa75c04f993b7b58af38736c6
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    List<Favorite> findByUserId(Long userId);
    Optional<Favorite> findByUserIdAndSongId(Long userId, Long songId);
}
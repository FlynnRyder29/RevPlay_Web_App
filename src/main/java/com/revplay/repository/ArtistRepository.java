package com.revplay.repository;

import com.revplay.model.Artist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ArtistRepository extends JpaRepository<Artist, Long> {

    // Efficient lookup using foreign key
    Optional<Artist> findByUserId(Long userId);

    // Optional: existence check
    boolean existsByUserId(Long userId);
}
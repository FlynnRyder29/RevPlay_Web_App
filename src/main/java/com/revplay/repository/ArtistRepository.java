package com.revplay.repository;

import com.revplay.model.Artist;
import com.revplay.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ArtistRepository extends JpaRepository<Artist, Long> {

    Optional<Artist> findByUser(User user);

    boolean existsByUser(User user);
}
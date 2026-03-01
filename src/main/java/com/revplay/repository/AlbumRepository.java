package com.revplay.repository;

import com.revplay.model.Album;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AlbumRepository extends JpaRepository<Album, Long> {

    @Query("SELECT a FROM Album a LEFT JOIN FETCH a.artist WHERE a.artist.id = :artistId")
    List<Album> findAllByArtistId(@Param("artistId") Long artistId);

    @Query("SELECT a FROM Album a WHERE a.artist.id = :artistId")
    Page<Album> findByArtistId(@Param("artistId") Long artistId, Pageable pageable);

    Page<Album> findAll(Pageable pageable);
}

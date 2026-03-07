package com.revplay.repository;

import com.revplay.model.Song;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SongRepository extends JpaRepository<Song, Long>, JpaSpecificationExecutor<Song> {

    Page<Song> findAll(Pageable pageable);

    @Query("SELECT s FROM Song s WHERE s.artist.id = :artistId")
    Page<Song> findByArtistId(@Param("artistId") Long artistId, Pageable pageable);

    @Query("SELECT s FROM Song s LEFT JOIN FETCH s.album WHERE s.artist.id = :artistId")
    List<Song> findAllByArtistId(@Param("artistId") Long artistId);

    @Query("SELECT s FROM Song s WHERE s.album.id = :albumId")
    Page<Song> findByAlbumId(@Param("albumId") Long albumId, Pageable pageable);

    Page<Song> findByVisibility(Song.Visibility visibility, Pageable pageable);

    @Query("SELECT s FROM Song s WHERE " +
            "LOWER(s.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(s.genre) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(s.artist.artistName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Song> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT s FROM Song s WHERE s.visibility = :visibility AND (" +
            "LOWER(s.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(s.genre) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(s.artist.artistName) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Song> searchByKeywordAndVisibility(@Param("keyword") String keyword,
                                            @Param("visibility") Song.Visibility visibility,
                                            Pageable pageable);

    Page<Song> findByGenreIgnoreCase(String genre, Pageable pageable);

    @Query("SELECT COUNT(s) FROM Song s WHERE s.artist.id = :artistId")
    long countByArtistId(@Param("artistId") Long artistId);

    boolean existsByIdAndArtistId(Long id, Long artistId);

    // ═══ FIX: Atomic play count increment ═══
    @Modifying
    @Query("UPDATE Song s SET s.playCount = s.playCount + 1 WHERE s.id = :songId")
    void incrementPlayCount(@Param("songId") Long songId);
}
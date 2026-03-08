package com.revplay.specification;

import com.revplay.model.Song;
import org.springframework.data.jpa.domain.Specification;

public class SongSpecification {

    // ── FIX: Visibility filter — must be included in all public browse endpoints ──
    // Only returns PUBLIC songs. UNLISTED songs are accessible via direct link (getSongById)
    // but should NOT appear in browse/search/filter results.
    // PRIVATE songs are only visible to the owning artist.
    public static Specification<Song> isPublic() {
        return (root, query, cb) ->
                cb.equal(root.get("visibility"), Song.Visibility.PUBLIC);
    }

    public static Specification<Song> hasGenre(String genre) {
        return (root, query, cb) ->
                (genre == null || genre.isBlank()) ? null
                        : cb.like(cb.lower(root.get("genre")), "%" + genre.toLowerCase() + "%");
    }

    public static Specification<Song> hasArtistName(String artistName) {
        return (root, query, cb) ->
                (artistName == null || artistName.isBlank()) ? null
                        : cb.like(cb.lower(root.get("artist").get("artistName")),
                        "%" + artistName.toLowerCase() + "%");
    }

    public static Specification<Song> hasAlbumName(String albumName) {
        return (root, query, cb) ->
                (albumName == null || albumName.isBlank()) ? null
                        : cb.like(cb.lower(root.get("album").get("name")),
                        "%" + albumName.toLowerCase() + "%");
    }

    public static Specification<Song> hasYear(Integer year) {
        return (root, query, cb) ->
                year == null ? null
                        : cb.equal(cb.function("YEAR", Integer.class,
                        root.get("releaseDate")), year);
    }
}
package com.revplay.util;

import com.revplay.model.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Fluent test data builder for all RevPlay JPA entities.
 *
 * Returns real model classes so builders work directly with
 * repositories, services, and MockMvc tests without any casting.
 *
 * Usage:
 *   User     user   = TestDataBuilder.aUser().build();
 *   Artist   artist = TestDataBuilder.anArtist().withUserId(user.getId()).build();
 *   Album    album  = TestDataBuilder.anAlbum().withArtist(artist).build();
 *   Song     song   = TestDataBuilder.aSong().withArtist(artist).build();
 *   Playlist pl     = TestDataBuilder.aPlaylist().asPrivate().build();
 *   Favorite fav    = TestDataBuilder.aFavorite().withUser(user).withSong(song).build();
 *
 * ── IMPORTANT: @CreationTimestamp fields ────────────────────────
 * Fields like createdAt / updatedAt are managed by Hibernate when
 * entities are saved via a repository. In integration tests, the
 * manually set values here will be IGNORED by Hibernate on persist.
 * These are useful only for unit tests with detached/mocked objects.
 *
 * ── ROLE USAGE ──────────────────────────────────────────────────
 * Use TEST_ROLE_* enum constants for entity-level builders (here).
 * Use TEST_SECURITY_ROLE_* String constants for MockMvc .roles() calls.
 */
public class TestDataBuilder {

    // ── User Builder ──────────────────────────────────────────────

    public static UserBuilder aUser() { return new UserBuilder(); }

    public static class UserBuilder {
        private Long          id           = TestConstants.TEST_USER_ID;
        private String        email        = TestConstants.TEST_USER_EMAIL;
        private String        username     = TestConstants.TEST_USER_USERNAME;
        private String        passwordHash = "$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36Q5V2xJHpXHhkEtWb2VVXK";
        private String        displayName  = TestConstants.TEST_USER_DISPLAY_NAME;
        private String        bio          = "Test user bio.";
        private Role          role         = TestConstants.TEST_ROLE_LISTENER;
        private LocalDateTime createdAt    = LocalDateTime.now();
        private LocalDateTime updatedAt    = LocalDateTime.now();

        public UserBuilder withId(Long id)              { this.id = id;                        return this; }
        public UserBuilder withEmail(String email)      { this.email = email;                  return this; }
        public UserBuilder withUsername(String u)       { this.username = u;                   return this; }
        public UserBuilder withDisplayName(String name) { this.displayName = name;             return this; }
        public UserBuilder withBio(String bio)          { this.bio = bio;                      return this; }
        public UserBuilder withRole(Role role)          { this.role = role;                    return this; }
        public UserBuilder asArtist()                   { this.role = TestConstants.TEST_ROLE_ARTIST; return this; }
        public UserBuilder asAdmin()                    { this.role = TestConstants.TEST_ROLE_ADMIN;  return this; }

        public User build() {
            User user = new User();
            user.setId(id);
            user.setEmail(email);
            user.setUsername(username);
            user.setPasswordHash(passwordHash);
            user.setDisplayName(displayName);
            user.setBio(bio);
            user.setRole(role);
            user.setCreatedAt(createdAt);
            user.setUpdatedAt(updatedAt);
            return user;
        }
    }

    // ── Artist Builder ────────────────────────────────────────────
    // Artist stores userId as a plain Long — matches Artist.java (no @ManyToOne to User)

    public static ArtistBuilder anArtist() { return new ArtistBuilder(); }

    public static class ArtistBuilder {
        private Long          id                = TestConstants.TEST_ARTIST_ID;
        private Long          userId            = TestConstants.TEST_ARTIST_USER_ID;
        private String        artistName        = TestConstants.TEST_ARTIST_NAME;
        private String        bio               = TestConstants.TEST_ARTIST_BIO;
        private String        genre             = TestConstants.TEST_ARTIST_GENRE;
        private String        profilePictureUrl = "/uploads/artists/test_pfp.jpg";
        private String        bannerImageUrl    = "/uploads/artists/test_banner.jpg";
        private String        instagram         = null;
        private String        twitter           = null;
        private String        youtube           = null;
        private String        spotify           = null;
        private String        website           = null;
        private LocalDateTime createdAt         = LocalDateTime.now();

        public ArtistBuilder withId(Long id)                { this.id = id;                return this; }
        public ArtistBuilder withUserId(Long userId)        { this.userId = userId;        return this; }
        public ArtistBuilder withArtistName(String name)    { this.artistName = name;      return this; }
        public ArtistBuilder withBio(String bio)            { this.bio = bio;              return this; }
        public ArtistBuilder withGenre(String genre)        { this.genre = genre;          return this; }
        public ArtistBuilder withInstagram(String url)      { this.instagram = url;        return this; }
        public ArtistBuilder withTwitter(String url)        { this.twitter = url;          return this; }
        public ArtistBuilder withYoutube(String url)        { this.youtube = url;          return this; }
        public ArtistBuilder withSpotify(String url)        { this.spotify = url;          return this; }
        public ArtistBuilder withWebsite(String url)        { this.website = url;          return this; }
        public ArtistBuilder withProfilePicture(String url) { this.profilePictureUrl = url; return this; }
        public ArtistBuilder withBannerImage(String url)    { this.bannerImageUrl = url;   return this; }

        public Artist build() {
            Artist artist = new Artist();
            artist.setId(id);
            artist.setUserId(userId);
            artist.setArtistName(artistName);
            artist.setBio(bio);
            artist.setGenre(genre);
            artist.setProfilePictureUrl(profilePictureUrl);
            artist.setBannerImageUrl(bannerImageUrl);
            artist.setInstagram(instagram);
            artist.setTwitter(twitter);
            artist.setYoutube(youtube);
            artist.setSpotify(spotify);
            artist.setWebsite(website);
            artist.setCreatedAt(createdAt);
            return artist;
        }
    }

    // ── Album Builder ─────────────────────────────────────────────

    public static AlbumBuilder anAlbum() { return new AlbumBuilder(); }

    public static class AlbumBuilder {
        private Long          id            = TestConstants.TEST_ALBUM_ID;
        private String        name          = TestConstants.TEST_ALBUM_NAME;
        private String        description   = TestConstants.TEST_ALBUM_DESCRIPTION;
        private String        coverImageUrl = "/uploads/albums/test_cover.jpg";
        private LocalDate     releaseDate   = LocalDate.of(2024, 1, 1);
        private Artist        artist        = anArtist().build();
        private LocalDateTime createdAt     = LocalDateTime.now();

        public AlbumBuilder withId(Long id)                 { this.id = id;             return this; }
        public AlbumBuilder withName(String name)           { this.name = name;         return this; }
        public AlbumBuilder withDescription(String desc)    { this.description = desc;  return this; }
        public AlbumBuilder withArtist(Artist artist)       { this.artist = artist;     return this; }
        public AlbumBuilder withReleaseDate(LocalDate date) { this.releaseDate = date;  return this; }
        public AlbumBuilder withCoverImage(String url)      { this.coverImageUrl = url; return this; }

        public Album build() {
            Album album = new Album();
            album.setId(id);
            album.setName(name);
            album.setDescription(description);
            album.setCoverImageUrl(coverImageUrl);
            album.setReleaseDate(releaseDate);
            album.setArtist(artist);
            album.setCreatedAt(createdAt);
            return album;
        }
    }

    // ── Song Builder ──────────────────────────────────────────────
    // genre is a plain String on Song entity — NOT a FK to Genre table

    public static SongBuilder aSong() { return new SongBuilder(); }

    public static class SongBuilder {
        private Long            id            = TestConstants.TEST_SONG_ID;
        private String          title         = TestConstants.TEST_SONG_TITLE;
        private String          genre         = TestConstants.TEST_SONG_GENRE;
        private Integer         duration      = TestConstants.TEST_SONG_DURATION;
        private String          audioUrl      = TestConstants.TEST_SONG_AUDIO_URL;
        private String          coverImageUrl = "/uploads/songs/test_cover.jpg";
        private LocalDate       releaseDate   = LocalDate.of(2024, 1, 1);
        private Long            playCount     = 0L;
        private Song.Visibility visibility    = Song.Visibility.PUBLIC;
        private Artist          artist        = anArtist().build();
        private Album           album         = null;
        private LocalDateTime   createdAt     = LocalDateTime.now();

        public SongBuilder withId(Long id)                   { this.id = id;                                  return this; }
        public SongBuilder withTitle(String title)           { this.title = title;                            return this; }
        public SongBuilder withGenre(String genre)           { this.genre = genre;                            return this; }
        public SongBuilder withDuration(int duration)        { this.duration = duration;                      return this; }
        public SongBuilder withArtist(Artist artist)         { this.artist = artist;                          return this; }
        public SongBuilder withAlbum(Album album)            { this.album = album;                            return this; }
        public SongBuilder withPlayCount(Long count)         { this.playCount = count;                        return this; }
        public SongBuilder withReleaseDate(LocalDate date)   { this.releaseDate = date;                       return this; }
        public SongBuilder withVisibility(Song.Visibility v) { this.visibility = v;                           return this; }
        public SongBuilder withVisibility(String v)          { this.visibility = Song.Visibility.valueOf(v);  return this; }
        public SongBuilder asUnlisted()                      { this.visibility = Song.Visibility.UNLISTED;    return this; }
        public SongBuilder asPrivate()                       { this.visibility = Song.Visibility.PRIVATE;     return this; }

        public Song build() {
            Song song = new Song();
            song.setId(id);
            song.setTitle(title);
            song.setGenre(genre);
            song.setDuration(duration);
            song.setAudioUrl(audioUrl);
            song.setCoverImageUrl(coverImageUrl);
            song.setReleaseDate(releaseDate);
            song.setPlayCount(playCount);
            song.setVisibility(visibility);
            song.setArtist(artist);
            song.setAlbum(album);
            song.setCreatedAt(createdAt);
            return song;
        }
    }

    // ── Playlist Builder ──────────────────────────────────────────
    // NOTE: Lombok generates setPublicPlaylist() (not setIsPublic()) for a boolean field named isPublic.
    // Verify Playlist entity uses @Getter/@Setter (not @Data) and the field is named isPublic.

    public static PlaylistBuilder aPlaylist() { return new PlaylistBuilder(); }

    public static class PlaylistBuilder {
        private Long          id          = TestConstants.TEST_PLAYLIST_ID;
        private String        name        = TestConstants.TEST_PLAYLIST_NAME;
        private String        description = TestConstants.TEST_PLAYLIST_DESC;
        private boolean       isPublic    = TestConstants.TEST_PLAYLIST_PUBLIC;
        private User          user        = aUser().build();
        private LocalDateTime createdAt   = LocalDateTime.now();
        private LocalDateTime updatedAt   = LocalDateTime.now();

        public PlaylistBuilder withId(Long id)              { this.id = id;            return this; }
        public PlaylistBuilder withName(String name)        { this.name = name;        return this; }
        public PlaylistBuilder withDescription(String desc) { this.description = desc; return this; }
        public PlaylistBuilder withUser(User user)          { this.user = user;        return this; }
        public PlaylistBuilder asPrivate()                  { this.isPublic = false;   return this; }
        public PlaylistBuilder asPublic()                   { this.isPublic = true;    return this; }

        public Playlist build() {
            Playlist playlist = new Playlist();
            playlist.setId(id);
            playlist.setName(name);
            playlist.setDescription(description);
            playlist.setPublicPlaylist(isPublic);
            playlist.setUser(user);
            playlist.setCreatedAt(createdAt);
            playlist.setUpdatedAt(updatedAt);
            return playlist;
        }
    }

    // ── Favorite Builder ──────────────────────────────────────────

    public static FavoriteBuilder aFavorite() { return new FavoriteBuilder(); }

    public static class FavoriteBuilder {
        private Long          id        = 1L;
        private User          user      = aUser().build();
        private Song          song      = aSong().build();
        private LocalDateTime createdAt = LocalDateTime.now();

        public FavoriteBuilder withId(Long id)     { this.id = id;     return this; }
        public FavoriteBuilder withUser(User user) { this.user = user; return this; }
        public FavoriteBuilder withSong(Song song) { this.song = song; return this; }

        public Favorite build() {
            Favorite favorite = new Favorite();
            favorite.setId(id);
            favorite.setUser(user);
            favorite.setSong(song);
            favorite.setCreatedAt(createdAt);
            return favorite;
        }
    }

    // ── PlayEvent Builder ─────────────────────────────────────────

    public static PlayEventBuilder aPlayEvent() { return new PlayEventBuilder(); }

    public static class PlayEventBuilder {
        private Long          id        = 1L;
        private Song          song      = aSong().build();
        private User          user      = null; // nullable — anonymous plays allowed
        private LocalDateTime playedAt  = LocalDateTime.now();

        public PlayEventBuilder withId(Long id)     { this.id = id;       return this; }
        public PlayEventBuilder withSong(Song song) { this.song = song;   return this; }
        public PlayEventBuilder withUser(User user) { this.user = user;   return this; }
        public PlayEventBuilder asAnonymous()       { this.user = null;   return this; }
        public PlayEventBuilder withPlayedAt(LocalDateTime t) { this.playedAt = t; return this; }

        public PlayEvent build() {
            PlayEvent event = new PlayEvent();
            event.setId(id);
            event.setSong(song);
            event.setUser(user);
            event.setPlayedAt(playedAt);
            return event;
        }
    }
}
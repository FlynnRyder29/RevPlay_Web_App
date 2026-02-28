package com.revplay.util;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Fluent test data builder for all RevPlay entities.
 *
 * COMPILE SAFETY: Each builder returns a plain Java object using
 * reflection-free setters. Once Member 2 finalises the model classes,
 * replace each build() method's new Object() stub with the real entity.
 *
 * HOW TO USE (any member):
 *
 *   // Default objects
 *   TestDataBuilder.UserData    user    = TestDataBuilder.aUser().build();
 *   TestDataBuilder.SongData    song    = TestDataBuilder.aSong().build();
 *   TestDataBuilder.ArtistData  artist  = TestDataBuilder.anArtist().build();
 *   TestDataBuilder.AlbumData   album   = TestDataBuilder.anAlbum().build();
 *   TestDataBuilder.PlaylistData pl     = TestDataBuilder.aPlaylist().build();
 *
 *   // Override specific fields
 *   TestDataBuilder.UserData user = TestDataBuilder.aUser()
 *           .withEmail("custom@mail.com")
 *           .withRole("ARTIST")
 *           .build();
 *
 *   // Chain to simulate relations
 *   TestDataBuilder.ArtistData artist = TestDataBuilder.anArtist().withId(10L).build();
 *   TestDataBuilder.SongData   song   = TestDataBuilder.aSong().withArtistId(10L).build();
 *
 * WHEN MODELS ARE READY (Member 2 finishes entities):
 *   Replace each XxxData inner class with the real model import and
 *   update build() to return the real entity. No other test code changes.
 */
public class TestDataBuilder {

    // ── Portable data holders (used until real models are ready) ──

    public static class UserData {
        public Long          id;
        public String        email;
        public String        username;
        public String        passwordHash;
        public String        displayName;
        public String        bio;
        public String        role;
        public LocalDateTime createdAt;
        public LocalDateTime updatedAt;
    }

    public static class ArtistData {
        public Long          id;
        public Long          userId;
        public String        artistName;
        public String        bio;
        public String        genre;
        public String        profilePictureUrl;
        public String        bannerImageUrl;
        public String        instagram;
        public String        twitter;
        public String        youtube;
        public String        spotify;
        public String        website;
        public LocalDateTime createdAt;
    }

    public static class AlbumData {
        public Long          id;
        public String        name;
        public String        description;
        public String        coverImageUrl;
        public LocalDate     releaseDate;
        public Long          artistId;
        public LocalDateTime createdAt;
    }

    public static class SongData {
        public Long          id;
        public String        title;
        public String        genre;
        public int           duration;
        public String        audioUrl;
        public String        coverImageUrl;
        public LocalDate     releaseDate;
        public long          playCount;
        public String        visibility;
        public Long          artistId;
        public Long          albumId;
        public LocalDateTime createdAt;
    }

    public static class PlaylistData {
        public Long          id;
        public String        name;
        public String        description;
        public boolean       isPublic;
        public Long          userId;
        public LocalDateTime createdAt;
        public LocalDateTime updatedAt;
    }

    public static class FavoriteData {
        public Long          id;
        public Long          userId;
        public Long          songId;
        public LocalDateTime createdAt;
    }

    // ── User Builder ──────────────────────────────────────────────

    public static UserBuilder aUser() { return new UserBuilder(); }

    public static class UserBuilder {
        private Long          id          = TestConstants.TEST_USER_ID;
        private String        email       = TestConstants.TEST_USER_EMAIL;
        private String        username    = TestConstants.TEST_USER_USERNAME;
        private String        passwordHash = "$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36Q5V2xJHpXHhkEtWb2VVXK";
        private String        displayName = TestConstants.TEST_USER_DISPLAY_NAME;
        private String        bio         = "Test user bio.";
        private String        role        = TestConstants.TEST_USER_ROLE_LISTENER;
        private LocalDateTime createdAt   = LocalDateTime.now();
        private LocalDateTime updatedAt   = LocalDateTime.now();

        public UserBuilder withId(Long id)               { this.id = id;           return this; }
        public UserBuilder withEmail(String email)       { this.email = email;     return this; }
        public UserBuilder withUsername(String u)        { this.username = u;      return this; }
        public UserBuilder withDisplayName(String n)     { this.displayName = n;   return this; }
        public UserBuilder withBio(String bio)           { this.bio = bio;         return this; }
        public UserBuilder withRole(String role)         { this.role = role;       return this; }
        public UserBuilder asArtist()                    { this.role = TestConstants.TEST_USER_ROLE_ARTIST; return this; }
        public UserBuilder asAdmin()                     { this.role = TestConstants.TEST_USER_ROLE_ADMIN;  return this; }

        public UserData build() {
            UserData d       = new UserData();
            d.id             = id;
            d.email          = email;
            d.username       = username;
            d.passwordHash   = passwordHash;
            d.displayName    = displayName;
            d.bio            = bio;
            d.role           = role;
            d.createdAt      = createdAt;
            d.updatedAt      = updatedAt;
            return d;
        }
    }

    // ── Artist Builder ────────────────────────────────────────────

    public static ArtistBuilder anArtist() { return new ArtistBuilder(); }

    public static class ArtistBuilder {
        private Long          id               = TestConstants.TEST_ARTIST_ID;
        private Long          userId           = TestConstants.TEST_ARTIST_USER_ID;
        private String        artistName       = TestConstants.TEST_ARTIST_NAME;
        private String        bio              = TestConstants.TEST_ARTIST_BIO;
        private String        genre            = TestConstants.TEST_ARTIST_GENRE;
        private String        profilePictureUrl = "/uploads/artists/test_pfp.jpg";
        private String        bannerImageUrl   = "/uploads/artists/test_banner.jpg";
        private String        instagram        = null;
        private String        twitter          = null;
        private String        youtube          = null;
        private String        spotify          = null;
        private String        website          = null;
        private LocalDateTime createdAt        = LocalDateTime.now();

        public ArtistBuilder withId(Long id)                { this.id = id;               return this; }
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

        public ArtistData build() {
            ArtistData d        = new ArtistData();
            d.id                = id;
            d.userId            = userId;
            d.artistName        = artistName;
            d.bio               = bio;
            d.genre             = genre;
            d.profilePictureUrl = profilePictureUrl;
            d.bannerImageUrl    = bannerImageUrl;
            d.instagram         = instagram;
            d.twitter           = twitter;
            d.youtube           = youtube;
            d.spotify           = spotify;
            d.website           = website;
            d.createdAt         = createdAt;
            return d;
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
        private Long          artistId      = TestConstants.TEST_ARTIST_ID;
        private LocalDateTime createdAt     = LocalDateTime.now();

        public AlbumBuilder withId(Long id)                  { this.id = id;             return this; }
        public AlbumBuilder withName(String name)            { this.name = name;         return this; }
        public AlbumBuilder withDescription(String desc)     { this.description = desc;  return this; }
        public AlbumBuilder withArtistId(Long artistId)      { this.artistId = artistId; return this; }
        public AlbumBuilder withReleaseDate(LocalDate date)  { this.releaseDate = date;  return this; }
        public AlbumBuilder withCoverImage(String url)       { this.coverImageUrl = url; return this; }

        public AlbumData build() {
            AlbumData d      = new AlbumData();
            d.id             = id;
            d.name           = name;
            d.description    = description;
            d.coverImageUrl  = coverImageUrl;
            d.releaseDate    = releaseDate;
            d.artistId       = artistId;
            d.createdAt      = createdAt;
            return d;
        }
    }

    // ── Song Builder ──────────────────────────────────────────────

    public static SongBuilder aSong() { return new SongBuilder(); }

    public static class SongBuilder {
        private Long          id            = TestConstants.TEST_SONG_ID;
        private String        title         = TestConstants.TEST_SONG_TITLE;
        private String        genre         = TestConstants.TEST_SONG_GENRE;
        private int           duration      = TestConstants.TEST_SONG_DURATION;
        private String        audioUrl      = TestConstants.TEST_SONG_AUDIO_URL;
        private String        coverImageUrl = "/uploads/songs/test_cover.jpg";
        private LocalDate     releaseDate   = LocalDate.of(2024, 1, 1);
        private long          playCount     = 0L;
        private String        visibility    = TestConstants.TEST_SONG_VISIBILITY_PUBLIC;
        private Long          artistId      = TestConstants.TEST_ARTIST_ID;
        private Long          albumId       = null;
        private LocalDateTime createdAt     = LocalDateTime.now();

        public SongBuilder withId(Long id)                   { this.id = id;             return this; }
        public SongBuilder withTitle(String title)           { this.title = title;       return this; }
        public SongBuilder withGenre(String genre)           { this.genre = genre;       return this; }
        public SongBuilder withDuration(int duration)        { this.duration = duration; return this; }
        public SongBuilder withArtistId(Long artistId)       { this.artistId = artistId; return this; }
        public SongBuilder withAlbumId(Long albumId)         { this.albumId = albumId;   return this; }
        public SongBuilder withPlayCount(long count)         { this.playCount = count;   return this; }
        public SongBuilder asUnlisted()                      { this.visibility = TestConstants.TEST_SONG_VISIBILITY_UNLISTED; return this; }
        public SongBuilder withVisibility(String v)          { this.visibility = v;      return this; }
        public SongBuilder withReleaseDate(LocalDate date)   { this.releaseDate = date;  return this; }

        public SongData build() {
            SongData d      = new SongData();
            d.id            = id;
            d.title         = title;
            d.genre         = genre;
            d.duration      = duration;
            d.audioUrl      = audioUrl;
            d.coverImageUrl = coverImageUrl;
            d.releaseDate   = releaseDate;
            d.playCount     = playCount;
            d.visibility    = visibility;
            d.artistId      = artistId;
            d.albumId       = albumId;
            d.createdAt     = createdAt;
            return d;
        }
    }

    // ── Playlist Builder ──────────────────────────────────────────

    public static PlaylistBuilder aPlaylist() { return new PlaylistBuilder(); }

    public static class PlaylistBuilder {
        private Long          id          = TestConstants.TEST_PLAYLIST_ID;
        private String        name        = TestConstants.TEST_PLAYLIST_NAME;
        private String        description = TestConstants.TEST_PLAYLIST_DESC;
        private boolean       isPublic    = TestConstants.TEST_PLAYLIST_PUBLIC;
        private Long          userId      = TestConstants.TEST_USER_ID;
        private LocalDateTime createdAt   = LocalDateTime.now();
        private LocalDateTime updatedAt   = LocalDateTime.now();

        public PlaylistBuilder withId(Long id)               { this.id = id;             return this; }
        public PlaylistBuilder withName(String name)         { this.name = name;         return this; }
        public PlaylistBuilder withDescription(String desc)  { this.description = desc;  return this; }
        public PlaylistBuilder withUserId(Long userId)       { this.userId = userId;     return this; }
        public PlaylistBuilder asPrivate()                   { this.isPublic = false;    return this; }
        public PlaylistBuilder asPublic()                    { this.isPublic = true;     return this; }

        public PlaylistData build() {
            PlaylistData d  = new PlaylistData();
            d.id            = id;
            d.name          = name;
            d.description   = description;
            d.isPublic      = isPublic;
            d.userId        = userId;
            d.createdAt     = createdAt;
            d.updatedAt     = updatedAt;
            return d;
        }
    }

    // ── Favorite Builder ──────────────────────────────────────────

    public static FavoriteBuilder aFavorite() { return new FavoriteBuilder(); }

    public static class FavoriteBuilder {
        private Long          id        = 1L;
        private Long          userId    = TestConstants.TEST_USER_ID;
        private Long          songId    = TestConstants.TEST_SONG_ID;
        private LocalDateTime createdAt = LocalDateTime.now();

        public FavoriteBuilder withId(Long id)       { this.id = id;         return this; }
        public FavoriteBuilder withUserId(Long uid)  { this.userId = uid;    return this; }
        public FavoriteBuilder withSongId(Long sid)  { this.songId = sid;    return this; }

        public FavoriteData build() {
            FavoriteData d  = new FavoriteData();
            d.id            = id;
            d.userId        = userId;
            d.songId        = songId;
            d.createdAt     = createdAt;
            return d;
        }
    }
}
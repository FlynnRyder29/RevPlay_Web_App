-- ============================================================
-- RevPlay Music Streaming Application
-- Flyway Migration: V1__initial_schema.sql
-- Author: Member 6 — Database, Testing & DevOps
-- ============================================================

-- ============================================================
-- MEMBER 1 — AUTH
-- ============================================================

CREATE TABLE users (
    id            BIGINT          NOT NULL AUTO_INCREMENT,
    email         VARCHAR(255)    NOT NULL,
    username      VARCHAR(100)    NOT NULL,
    password_hash VARCHAR(255)    NOT NULL,
    display_name  VARCHAR(150),
    bio           TEXT,
    profile_picture_url VARCHAR(500),
    role          ENUM('LISTENER', 'ARTIST', 'ADMIN') NOT NULL DEFAULT 'LISTENER',
    created_at    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    CONSTRAINT uq_users_email    UNIQUE (email),
    CONSTRAINT uq_users_username UNIQUE (username)
);

-- ============================================================
-- MEMBER 2 — MUSIC CATALOG (Song / Album / Artist / Genre)
-- ============================================================

CREATE TABLE genres (
    id   BIGINT       NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,

    PRIMARY KEY (id),
    CONSTRAINT uq_genres_name UNIQUE (name)
);

CREATE TABLE artists (
    id                  BIGINT       NOT NULL AUTO_INCREMENT,
    user_id             BIGINT       NOT NULL,
    artist_name         VARCHAR(150) NOT NULL,
    bio                 TEXT,
    genre               VARCHAR(100),
    profile_picture_url VARCHAR(500),
    banner_image_url    VARCHAR(500),
    instagram           VARCHAR(255),
    twitter             VARCHAR(255),
    youtube             VARCHAR(255),
    spotify             VARCHAR(255),
    website             VARCHAR(255),
    created_at          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    CONSTRAINT uq_artists_user_id UNIQUE (user_id),
    CONSTRAINT fk_artists_user_id FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE albums (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    name            VARCHAR(255) NOT NULL,
    description     TEXT,
    cover_image_url VARCHAR(500),
    release_date    DATE,
    artist_id       BIGINT       NOT NULL,
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    CONSTRAINT fk_albums_artist_id FOREIGN KEY (artist_id) REFERENCES artists (id) ON DELETE CASCADE
);

CREATE TABLE songs (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    title           VARCHAR(255) NOT NULL,
    genre           VARCHAR(100),
    duration        INT          NOT NULL COMMENT 'Duration in seconds',
    audio_url       VARCHAR(500) NOT NULL,
    cover_image_url VARCHAR(500),
    release_date    DATE,
    play_count      BIGINT       NOT NULL DEFAULT 0,
    visibility      ENUM('PUBLIC', 'UNLISTED') NOT NULL DEFAULT 'PUBLIC',
    artist_id       BIGINT       NOT NULL,
    album_id        BIGINT,
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    CONSTRAINT fk_songs_artist_id FOREIGN KEY (artist_id) REFERENCES artists (id) ON DELETE CASCADE,
    CONSTRAINT fk_songs_album_id  FOREIGN KEY (album_id)  REFERENCES albums  (id) ON DELETE SET NULL
);

-- ============================================================
-- MEMBER 3 — PLAYLISTS / FAVORITES / HISTORY / FOLLOWS
-- ============================================================

CREATE TABLE playlists (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    name        VARCHAR(255) NOT NULL,
    description TEXT,
    is_public   BOOLEAN      NOT NULL DEFAULT TRUE,
    user_id     BIGINT       NOT NULL,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    CONSTRAINT fk_playlists_user_id FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE playlist_songs (
    id          BIGINT   NOT NULL AUTO_INCREMENT,
    playlist_id BIGINT   NOT NULL,
    song_id     BIGINT   NOT NULL,
    position    INT      NOT NULL DEFAULT 0 COMMENT 'Order of song within the playlist',
    added_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    CONSTRAINT uq_playlist_songs UNIQUE (playlist_id, song_id),
    CONSTRAINT fk_ps_playlist_id FOREIGN KEY (playlist_id) REFERENCES playlists (id) ON DELETE CASCADE,
    CONSTRAINT fk_ps_song_id     FOREIGN KEY (song_id)     REFERENCES songs     (id) ON DELETE CASCADE
);

CREATE TABLE favorites (
    id         BIGINT   NOT NULL AUTO_INCREMENT,
    user_id    BIGINT   NOT NULL,
    song_id    BIGINT   NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    CONSTRAINT uq_favorites UNIQUE (user_id, song_id),
    CONSTRAINT fk_fav_user_id FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_fav_song_id FOREIGN KEY (song_id) REFERENCES songs (id) ON DELETE CASCADE
);

CREATE TABLE listening_history (
    id        BIGINT   NOT NULL AUTO_INCREMENT,
    user_id   BIGINT   NOT NULL,
    song_id   BIGINT   NOT NULL,
    played_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    CONSTRAINT fk_lh_user_id FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_lh_song_id FOREIGN KEY (song_id) REFERENCES songs (id) ON DELETE CASCADE
);

CREATE TABLE playlist_follows (
    id          BIGINT   NOT NULL AUTO_INCREMENT,
    user_id     BIGINT   NOT NULL,
    playlist_id BIGINT   NOT NULL,
    followed_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    CONSTRAINT uq_playlist_follows UNIQUE (user_id, playlist_id),
    CONSTRAINT fk_pf_user_id     FOREIGN KEY (user_id)     REFERENCES users     (id) ON DELETE CASCADE,
    CONSTRAINT fk_pf_playlist_id FOREIGN KEY (playlist_id) REFERENCES playlists (id) ON DELETE CASCADE
);

-- ============================================================
-- MEMBER 4 — ANALYTICS
-- ============================================================

CREATE TABLE play_events (
    id        BIGINT   NOT NULL AUTO_INCREMENT,
    song_id   BIGINT   NOT NULL,
    user_id   BIGINT,
    played_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    CONSTRAINT fk_pe_song_id FOREIGN KEY (song_id) REFERENCES songs (id) ON DELETE CASCADE,
    CONSTRAINT fk_pe_user_id FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE SET NULL
);

-- ============================================================
-- INDEXES — search, filter, and FK lookups
-- ============================================================

-- users
CREATE INDEX idx_users_email    ON users (email);
CREATE INDEX idx_users_username ON users (username);
CREATE INDEX idx_users_role     ON users (role);

-- artists
CREATE INDEX idx_artists_artist_name ON artists (artist_name);

-- albums
CREATE INDEX idx_albums_artist_id    ON albums (artist_id);
CREATE INDEX idx_albums_release_date ON albums (release_date);

-- songs
CREATE INDEX idx_songs_artist_id    ON songs (artist_id);
CREATE INDEX idx_songs_album_id     ON songs (album_id);
CREATE INDEX idx_songs_title        ON songs (title);
CREATE INDEX idx_songs_genre        ON songs (genre);
CREATE INDEX idx_songs_visibility   ON songs (visibility);
CREATE INDEX idx_songs_play_count   ON songs (play_count DESC);
CREATE INDEX idx_songs_release_date ON songs (release_date);

-- playlists
CREATE INDEX idx_playlists_user_id   ON playlists (user_id);
CREATE INDEX idx_playlists_is_public ON playlists (is_public);

-- playlist_songs
CREATE INDEX idx_ps_playlist_id ON playlist_songs (playlist_id);
CREATE INDEX idx_ps_song_id     ON playlist_songs (song_id);

-- favorites
CREATE INDEX idx_fav_user_id ON favorites (user_id);
CREATE INDEX idx_fav_song_id ON favorites (song_id);

-- listening_history
CREATE INDEX idx_lh_user_id   ON listening_history (user_id);
CREATE INDEX idx_lh_song_id   ON listening_history (song_id);
CREATE INDEX idx_lh_played_at ON listening_history (played_at DESC);

-- playlist_follows
CREATE INDEX idx_pf_user_id     ON playlist_follows (user_id);
CREATE INDEX idx_pf_playlist_id ON playlist_follows (playlist_id);

-- play_events
CREATE INDEX idx_pe_song_id   ON play_events (song_id);
CREATE INDEX idx_pe_user_id   ON play_events (user_id);
CREATE INDEX idx_pe_played_at ON play_events (played_at DESC);
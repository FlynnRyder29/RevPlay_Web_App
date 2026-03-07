-- ============================================================
-- RevPlay Music Streaming Application
-- Flyway Migration: V99__seed_data.sql
-- Author: Member 6 — Database, Testing & DevOps
-- Description: Demo seed data for development and presentation
--
-- PASSWORDS (plain text for login):
--   admin@revplay.com  → Password@123
--   alice@mail.com     → AlicePass@1
--   bob@mail.com       → BobPass@123
--   aria@mail.com      → AriaPass@1
--   djnova@mail.com    → NovaPass@1
--   marco@mail.com     → MarcoPass@1
--
-- Audio  : SoundHelix (royalty-free, stable hosted URLs)
-- Images : picsum.photos (stable seeded images, always same result)
-- ============================================================

SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================
-- USERS
-- password column stores BCrypt hash of the plain passwords above
-- Users log in with the plain password — Spring Security hashes
-- and compares automatically. Never store or type the hash manually.
-- ============================================================

INSERT INTO users (id, email, username, password_hash, display_name, bio, role) VALUES
(1, 'admin@revplay.com', 'admin',       '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36Q5V2xJHpXHhkEtWb2VVXK', 'RevPlay Admin', 'Platform administrator.',             'ADMIN'),
(2, 'alice@mail.com',    'alice_music', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Alice',         'Music lover. Playlist curator.',      'LISTENER'),
(3, 'bob@mail.com',      'bob_beats',  '$2a$10$GDvJEJVGNaAVFQdFPlYS4OhBZ9MjNoNrRtlA28g8EGqFGPXP6WDSC', 'Bob',           'Hip-hop and chill vibes only.',       'LISTENER'),
(4, 'aria@mail.com',     'aria_artist','$2a$10$TKh8H1.PfQx37YgCzwiKb.KjNyWgaHb9cbcoQgdIVFlYg7B9tpZ/e', 'Aria',          'Singer-songwriter. Indie pop artist.','ARTIST'),
(5, 'djnova@mail.com',   'dj_nova',    '$2a$10$jemhoQ3ksTqqkktHHG6ohOGQ3Bu6K24jKDVc2WcHKEZqCPJi9PBTS', 'DJ Nova',       'Electronic and house music producer.','ARTIST'),
(6, 'marco@mail.com',    'marco_jazz', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Marco Jazz',    'Jazz guitarist. Bringing classics back.','ARTIST');

-- ============================================================
-- GENRES
-- ============================================================

INSERT INTO genres (id, name) VALUES
(1, 'Pop'),
(2, 'Hip-Hop'),
(3, 'Electronic'),
(4, 'Jazz'),
(5, 'Rock'),
(6, 'R&B'),
(7, 'Classical'),
(8, 'Indie');

-- ============================================================
-- ARTISTS
-- Images: picsum.photos/seed/{keyword}/WxH — same keyword always
--         returns the same image, making data consistent across resets
-- ============================================================

INSERT INTO artists (id, user_id, artist_name, bio, genre, profile_picture_url, banner_image_url, instagram, twitter, youtube, spotify, website) VALUES
(1, 4, 'Aria',
    'Indie pop singer-songwriter based in Austin, TX. Known for dreamy melodies and honest lyrics.',
    'Indie',
    'https://picsum.photos/seed/aria-pfp/300/300',
    'https://picsum.photos/seed/aria-banner/1200/400',
    'https://instagram.com/aria_music',
    'https://twitter.com/aria_music',
    NULL, NULL, NULL),

(2, 5, 'DJ Nova',
    'Electronic music producer and live performer. Specializes in deep house and ambient techno.',
    'Electronic',
    'https://picsum.photos/seed/nova-pfp/300/300',
    'https://picsum.photos/seed/nova-banner/1200/400',
    NULL,
    'https://twitter.com/djnova',
    'https://youtube.com/@djnova',
    NULL, NULL),

(3, 6, 'Marco Jazz',
    'Jazz guitarist with 15 years of studio and live performance experience.',
    'Jazz',
    'https://picsum.photos/seed/marco-pfp/300/300',
    NULL,
    NULL, NULL, NULL, NULL,
    'https://marcojazz.com');

-- ============================================================
-- ALBUMS
-- ============================================================

INSERT INTO albums (id, name, description, cover_image_url, release_date, artist_id) VALUES
(1, 'Daydream',
    'Debut album by Aria. Soft indie pop tracks about love and growth.',
    'https://picsum.photos/seed/daydream/400/400',
    '2023-03-15', 1),

(2, 'Pulse',
    'High energy electronic album by DJ Nova. Floor-ready from start to finish.',
    'https://picsum.photos/seed/pulse-album/400/400',
    '2023-07-01', 2),

(3, 'Blue Hour',
    'Mellow jazz sessions recorded live in one evening.',
    'https://picsum.photos/seed/bluehour/400/400',
    '2022-11-20', 3),

(4, 'Neon Nights EP',
    'Four-track EP with dark synth and driving bass lines.',
    'https://picsum.photos/seed/neonnights/400/400',
    '2024-01-10', 2);

-- ============================================================
-- SONGS
-- Audio: SoundHelix royalty-free MP3s (17 available, all stable)
-- https://www.soundhelix.com/examples/mp3/SoundHelix-Song-N.mp3
-- Assigned by vibe: slow/indie → lower numbers, electronic → mid,
-- jazz → higher numbers
-- ============================================================

INSERT INTO songs (id, title, genre, duration, audio_url, cover_image_url, release_date, play_count, visibility, artist_id, album_id) VALUES

-- ── Aria — Daydream ──────────────────────────────────────────
(1,  'Golden Hour',
     'Indie', 214,
     'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3',
     'https://picsum.photos/seed/daydream/400/400',
     '2023-03-15', 18420, 'PUBLIC', 1, 1),

(2,  'Paper Walls',
     'Indie', 198,
     'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3',
     'https://picsum.photos/seed/daydream/400/400',
     '2023-03-15', 9310, 'PUBLIC', 1, 1),

(3,  'Gravity',
     'Pop', 223,
     'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3',
     'https://picsum.photos/seed/daydream/400/400',
     '2023-03-15', 7650, 'PUBLIC', 1, 1),

(4,  'Wildfire',
     'Indie', 187,
     'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3',
     'https://picsum.photos/seed/daydream/400/400',
     '2023-03-15', 5200, 'PUBLIC', 1, 1),

-- ── Aria — standalone single ─────────────────────────────────
(5,  'Haze',
     'Indie', 205,
     'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-5.mp3',
     'https://picsum.photos/seed/haze-cover/400/400',
     '2024-02-01', 3100, 'PUBLIC', 1, NULL),

-- ── DJ Nova — Pulse ──────────────────────────────────────────
(6,  'Overdrive',
     'Electronic', 382,
     'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-6.mp3',
     'https://picsum.photos/seed/pulse-album/400/400',
     '2023-07-01', 31200, 'PUBLIC', 2, 2),

(7,  'Resonance',
     'Electronic', 354,
     'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-7.mp3',
     'https://picsum.photos/seed/pulse-album/400/400',
     '2023-07-01', 22800, 'PUBLIC', 2, 2),

(8,  'Midnight Grid',
     'Electronic', 410,
     'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-8.mp3',
     'https://picsum.photos/seed/pulse-album/400/400',
     '2023-07-01', 17500, 'PUBLIC', 2, 2),

-- ── DJ Nova — Neon Nights EP ─────────────────────────────────
(9,  'Neon Drift',
     'Electronic', 298,
     'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-9.mp3',
     'https://picsum.photos/seed/neonnights/400/400',
     '2024-01-10', 12400, 'PUBLIC', 2, 4),

(10, 'Static Pulse',
     'Electronic', 315,
     'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-10.mp3',
     'https://picsum.photos/seed/neonnights/400/400',
     '2024-01-10', 8900, 'PUBLIC', 2, 4),

-- ── DJ Nova — unlisted WIP ───────────────────────────────────
(11, 'Untitled WIP',
     'Electronic', 180,
     'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-11.mp3',
     NULL,
     '2024-06-01', 0, 'UNLISTED', 2, NULL),

-- ── Marco Jazz — Blue Hour ───────────────────────────────────
(12, 'Blue in Green',
     'Jazz', 328,
     'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-12.mp3',
     'https://picsum.photos/seed/bluehour/400/400',
     '2022-11-20', 14300, 'PUBLIC', 3, 3),

(13, 'Autumn Leaves',
     'Jazz', 295,
     'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-13.mp3',
     'https://picsum.photos/seed/bluehour/400/400',
     '2022-11-20', 11600, 'PUBLIC', 3, 3),

(14, 'Misty',
     'Jazz', 310,
     'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-14.mp3',
     'https://picsum.photos/seed/bluehour/400/400',
     '2022-11-20', 9800, 'PUBLIC', 3, 3),

(15, 'So What',
     'Jazz', 342,
     'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-15.mp3',
     'https://picsum.photos/seed/bluehour/400/400',
     '2022-11-20', 8200, 'PUBLIC', 3, 3),

(16, 'Feel It',
     'R&B', 180,
     'https://audio.jukehost.co.uk/M9BTPYkJiNkqMmeLPHjSWjlz4TtQLEin',
     'https://i.pinimg.com/736x/c4/0b/12/c40b12e8d09daed4b78f963c62ce2717.jpg',
     '2023-01-01', 0, 'PUBLIC', 1, NULL);

-- ============================================================
-- PLAYLISTS
-- ============================================================

INSERT INTO playlists (id, name, description, is_public, user_id) VALUES
(1, 'Morning Vibes',    'Chill songs to start the day right.',      TRUE,  2),
(2, 'Late Night Drive', 'Electronic bangers for the road.',         TRUE,  2),
(3, 'My Private Mix',   'Personal collection — not for sharing.',   FALSE, 2),
(4, 'Jazz Corner',      'Classic jazz and new recordings.',         TRUE,  3),
(5, 'Workout Mix',      'High BPM tracks for gym sessions.',        TRUE,  3);

-- ============================================================
-- PLAYLIST SONGS
-- ============================================================

INSERT INTO playlist_songs (playlist_id, song_id, position) VALUES
-- Morning Vibes
(1, 1, 1), (1, 2, 2), (1, 5, 3), (1, 12, 4), (1, 13, 5),
-- Late Night Drive
(2, 6, 1), (2, 7, 2), (2, 8, 3), (2, 9, 4),  (2, 10, 5),
-- My Private Mix
(3, 1, 1), (3, 3, 2), (3, 6, 3),
-- Jazz Corner
(4, 12, 1), (4, 13, 2), (4, 14, 3), (4, 15, 4),
-- Workout Mix
(5, 6, 1), (5, 8, 2), (5, 9, 3), (5, 7, 4);

-- ============================================================
-- FAVORITES
-- ============================================================

INSERT INTO favorites (user_id, song_id) VALUES
-- Alice
(2, 1), (2, 2), (2, 6), (2, 12), (2, 13),
-- Bob
(3, 6), (3, 7), (3, 8), (3, 12), (3, 1);

-- ============================================================
-- PLAYLIST FOLLOWS
-- ============================================================

INSERT INTO playlist_follows (user_id, playlist_id, followed_at) VALUES
(3, 1, NOW() - INTERVAL 5 DAY),  -- Bob follows Alice's Morning Vibes
(3, 2, NOW() - INTERVAL 3 DAY),  -- Bob follows Alice's Late Night Drive
(2, 4, NOW() - INTERVAL 4 DAY),  -- Alice follows Bob's Jazz Corner
(2, 5, NOW() - INTERVAL 2 DAY);  -- Alice follows Bob's Workout Mix

-- ============================================================
-- LISTENING HISTORY
-- ============================================================

INSERT INTO listening_history (user_id, song_id, played_at) VALUES
-- Alice
(2, 1,  NOW() - INTERVAL 10 MINUTE),
(2, 2,  NOW() - INTERVAL 25 MINUTE),
(2, 3,  NOW() - INTERVAL 40 MINUTE),
(2, 6,  NOW() - INTERVAL 1  HOUR),
(2, 12, NOW() - INTERVAL 2  HOUR),
(2, 13, NOW() - INTERVAL 3  HOUR),
(2, 7,  NOW() - INTERVAL 1  DAY),
(2, 8,  NOW() - INTERVAL 1  DAY),
(2, 5,  NOW() - INTERVAL 2  DAY),
(2, 14, NOW() - INTERVAL 3  DAY),
-- Bob
(3, 6,  NOW() - INTERVAL 15 MINUTE),
(3, 7,  NOW() - INTERVAL 30 MINUTE),
(3, 8,  NOW() - INTERVAL 50 MINUTE),
(3, 12, NOW() - INTERVAL 2  HOUR),
(3, 9,  NOW() - INTERVAL 4  HOUR),
(3, 1,  NOW() - INTERVAL 1  DAY),
(3, 13, NOW() - INTERVAL 2  DAY);

-- ============================================================
-- PLAY EVENTS (analytics)
-- ============================================================

INSERT INTO play_events (song_id, user_id, played_at) VALUES
(1,  2,    NOW() - INTERVAL 10 MINUTE),
(1,  3,    NOW() - INTERVAL 1  DAY),
(1,  NULL, NOW() - INTERVAL 2  DAY),
(1,  2,    NOW() - INTERVAL 3  DAY),
(1,  3,    NOW() - INTERVAL 5  DAY),
(1,  NULL, NOW() - INTERVAL 7  DAY),
(6,  2,    NOW() - INTERVAL 1  HOUR),
(6,  3,    NOW() - INTERVAL 15 MINUTE),
(6,  NULL, NOW() - INTERVAL 2  DAY),
(6,  2,    NOW() - INTERVAL 4  DAY),
(6,  3,    NOW() - INTERVAL 6  DAY),
(6,  NULL, NOW() - INTERVAL 8  DAY),
(6,  2,    NOW() - INTERVAL 10 DAY),
(12, 2,    NOW() - INTERVAL 2  HOUR),
(12, 3,    NOW() - INTERVAL 2  HOUR),
(12, NULL, NOW() - INTERVAL 3  DAY),
(12, 2,    NOW() - INTERVAL 7  DAY),
(7,  3,    NOW() - INTERVAL 30 MINUTE),
(7,  NULL, NOW() - INTERVAL 3  DAY),
(7,  2,    NOW() - INTERVAL 6  DAY),
(13, 2,    NOW() - INTERVAL 3  HOUR),
(13, 3,    NOW() - INTERVAL 2  DAY),
(13, NULL, NOW() - INTERVAL 9  DAY);

-- ============================================================
-- D4vd (user_id=8 already in users above, artist added below)
-- ============================================================

INSERT INTO users (id, email, username, password_hash, display_name, bio, role) VALUES
(8, 'd4vd@mail.com', 'd4vd', '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'D4vd', 'Indie R&B artist from Houston, TX.', 'ARTIST');

INSERT INTO artists (id, user_id, artist_name, bio, genre, profile_picture_url, banner_image_url, instagram, twitter, youtube, spotify, website) VALUES
(4, 8, 'D4vd',
    'Indie R&B and dream pop artist from Houston, TX. Known for Feel It and Here With Me.',
    'R&B',
    'https://picsum.photos/seed/d4vd-pfp/300/300',
    'https://picsum.photos/seed/d4vd-banner/1200/400',
    'https://instagram.com/d4vd',
    'https://twitter.com/d4vd_',
    'https://youtube.com/@d4vd',
    NULL, NULL);

-- ============================================================
-- Mac DeMarco (user_id=11, artist_id=5)
-- ============================================================

INSERT INTO users (id, email, username, password_hash, display_name, bio, role) VALUES
(11, 'macdemarco@mail.com', 'mac_demarco', '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'Mac DeMarco', 'Canadian indie rock and jangle pop musician.', 'ARTIST');

INSERT INTO artists (id, user_id, artist_name, bio, genre, profile_picture_url, banner_image_url, instagram, twitter, youtube, spotify, website) VALUES
(5, 11, 'Mac DeMarco',
    'Canadian singer-songwriter known for his lo-fi indie rock and jangle pop style.',
    'Indie',
    'https://picsum.photos/seed/macdemarco-pfp/300/300',
    'https://picsum.photos/seed/macdemarco-banner/1200/400',
    'https://instagram.com/macdemarco',
    'https://twitter.com/macdemarco',
    'https://youtube.com/@macdemarcofficial',
    NULL, NULL);

-- ============================================================
-- USERS (IDs 12–42)
-- ============================================================

INSERT INTO users (id, email, username, password_hash, display_name, bio, role) VALUES
(12, 'billieeilish@mail.com',   'billie_eilish',    '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'Billie Eilish',                'Pop and alternative artist from Los Angeles.',            'ARTIST'),
(13, 'boa@mail.com',            'boa_official',     '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'Boa',                          'British singer known for Duvet.',                         'ARTIST'),
(14, 'djo@mail.com',            'djo_music',        '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'Djo',                          'Indie pop artist and actor Joe Keery.',                   'ARTIST'),
(15, 'richymitch@mail.com',     'richy_mitch',      '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'Richy Mitch & The Coal Miners','Folk and indie rock band.',                               'ARTIST'),
(16, 'radiohead@mail.com',      'radiohead',        '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'Radiohead',                    'British rock band from Abingdon.',                        'ARTIST'),
(17, 'surfcurse@mail.com',      'surf_curse',       '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'Surf Curse',                   'Indie rock duo from Reno, Nevada.',                       'ARTIST'),
(18, 'laufey@mail.com',         'laufey',           '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'Laufey',                       'Icelandic jazz-pop singer-songwriter.',                   'ARTIST'),
(19, 'sukiwaterhouse@mail.com', 'suki_waterhouse',  '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'Suki Waterhouse',              'British singer-songwriter and actress.',                  'ARTIST'),
(20, 'imogenheap@mail.com',     'imogen_heap',      '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'Imogen Heap',                  'British singer, songwriter and record producer.',         'ARTIST'),
(21, 'thesmiths@mail.com',      'the_smiths',       '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'The Smiths',                   'British rock band from Manchester.',                      'ARTIST'),
(22, 'walters@mail.com',        'walters_music',    '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'Walters',                      'Indie pop artist.',                                       'ARTIST'),
(23, 'tameimpala@mail.com',     'tame_impala',      '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'Tame Impala',                  'Psychedelic rock project by Kevin Parker.',               'ARTIST'),
(24, 'tylerthecreator@mail.com','tyler_the_creator','$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'Tyler the Creator',            'Rapper and producer from Los Angeles.',                   'ARTIST'),
(25, 'mgmt@mail.com',           'mgmt_official',    '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'MGMT',                         'Indie pop and psychedelic rock duo.',                     'ARTIST'),
(26, 'tvgirl@mail.com',         'tv_girl',          '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'TV Girl',                      'Indie pop band from San Diego.',                          'ARTIST'),
(27, 'ankittiwari@mail.com',    'ankit_tiwari',     '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'Ankit Tiwari',                 'Indian playback singer and composer.',                    'ARTIST'),
(28, 'faeemashraf@mail.com',    'faheem_ashraf',    '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'Faheem Ashraf',                'Pakistani singer-songwriter.',                            'ARTIST'),
(29, 'mananbhardwaj@mail.com',  'manan_bhardwaj',   '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'Manan Bhardwaj',               'Indian playback singer.',                                 'ARTIST'),
(30, 'sukhwindersingh@mail.com','sukhwinder_singh',  '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'Sukhwinder Singh',             'Indian playback singer known for Jai Ho.',                'ARTIST'),
(31, 'arijitsingh@mail.com',    'arijit_singh',     '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'Arijit Singh',                 'Indian playback singer and music composer.',              'ARTIST'),
(32, 'bpraak@mail.com',         'b_praak',          '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'B Praak',                      'Indian singer and music producer.',                       'ARTIST'),
(33, 'roopkumar@mail.com',      'roop_kumar',       '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'Roop Kumar',                   'Indian playback singer.',                                 'ARTIST'),
(34, 'sonunigam@mail.com',      'sonu_nigam',       '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'Sonu Nigam',                   'Indian playback singer and composer.',                    'ARTIST'),
(35, 'palakmucchal@mail.com',   'palak_mucchal',    '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'Palak Mucchal',                'Indian playback singer.',                                 'ARTIST'),
(36, 'edsheeran@mail.com',      'ed_sheeran',       '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'Ed Sheeran',                   'British singer-songwriter.',                              'ARTIST'),
(37, 'justinbieber@mail.com',   'justin_bieber',    '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'Justin Bieber',                'Canadian pop singer.',                                    'ARTIST'),
(38, 'akon@mail.com',           'akon_official',    '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'Akon',                         'Senegalese-American R&B and pop singer.',                 'ARTIST'),
(39, 'dollyparton@mail.com',    'dolly_parton',     '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'Dolly Parton',                 'American country music singer-songwriter.',               'ARTIST'),
(40, 'enriqueiglesias@mail.com','enrique_iglesias',  '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'Enrique Iglesias',             'Spanish singer known as the King of Latin Pop.',          'ARTIST'),
(41, 'selenagomez@mail.com',    'selena_gomez',     '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'Selena Gomez',                 'American singer, actress and producer.',                  'ARTIST'),
(42, 'linkinpark@mail.com',     'linkin_park',      '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'Linkin Park',                  'American rock band from Agoura Hills, California.',       'ARTIST');

-- ============================================================
-- ARTISTS (IDs 6–36)
-- ============================================================

INSERT INTO artists (id, user_id, artist_name, bio, genre, profile_picture_url, banner_image_url, instagram, twitter, youtube, spotify, website) VALUES
(6,  12, 'Billie Eilish',                'Pop and alternative artist from Los Angeles, CA.',                    'Pop',        'https://picsum.photos/seed/billieeilish-pfp/300/300',  'https://picsum.photos/seed/billieeilish-banner/1200/400',  'https://instagram.com/billieeilish',  'https://twitter.com/billieeilish',   'https://youtube.com/@billieeilish',  NULL, NULL),
(7,  13, 'Boa',                          'British singer known for the cult classic Duvet.',                    'Indie',      'https://picsum.photos/seed/boa-pfp/300/300',           'https://picsum.photos/seed/boa-banner/1200/400',           NULL, NULL, NULL, NULL, NULL),
(8,  14, 'Djo',                          'Indie pop project of actor Joe Keery.',                               'Indie',      'https://picsum.photos/seed/djo-pfp/300/300',           'https://picsum.photos/seed/djo-banner/1200/400',           'https://instagram.com/djo', NULL, NULL, NULL, NULL),
(9,  15, 'Richy Mitch & The Coal Miners','Folk and indie rock band.',                                           'Folk',       'https://picsum.photos/seed/richymitch-pfp/300/300',    'https://picsum.photos/seed/richymitch-banner/1200/400',    NULL, NULL, NULL, NULL, NULL),
(10, 16, 'Radiohead',                    'Pioneering British rock band from Abingdon, Oxfordshire.',            'Rock',       'https://picsum.photos/seed/radiohead-pfp/300/300',     'https://picsum.photos/seed/radiohead-banner/1200/400',     NULL, 'https://twitter.com/radiohead', 'https://youtube.com/@radiohead', NULL, 'https://radiohead.com'),
(11, 17, 'Surf Curse',                   'Indie rock duo from Reno, Nevada.',                                   'Indie',      'https://picsum.photos/seed/surfcurse-pfp/300/300',     'https://picsum.photos/seed/surfcurse-banner/1200/400',     'https://instagram.com/surfcurse', NULL, NULL, NULL, NULL),
(12, 18, 'Laufey',                       'Icelandic jazz-pop singer-songwriter.',                               'Jazz',       'https://picsum.photos/seed/laufey-pfp/300/300',        'https://picsum.photos/seed/laufey-banner/1200/400',        'https://instagram.com/laufeymusic', 'https://twitter.com/laufeymusic', 'https://youtube.com/@laufey', NULL, NULL),
(13, 19, 'Suki Waterhouse',              'British singer-songwriter and actress.',                              'Indie',      'https://picsum.photos/seed/sukiwaterhouse-pfp/300/300','https://picsum.photos/seed/sukiwaterhouse-banner/1200/400','https://instagram.com/sukiwaterhouse', NULL, NULL, NULL, NULL),
(14, 20, 'Imogen Heap',                  'British singer, songwriter and record producer.',                     'Electronic', 'https://picsum.photos/seed/imogenheap-pfp/300/300',    'https://picsum.photos/seed/imogenheap-banner/1200/400',    'https://instagram.com/imogenheap', 'https://twitter.com/imogenheap', 'https://youtube.com/@imogenheap', NULL, NULL),
(15, 21, 'The Smiths',                   'Iconic British rock band from Manchester.',                           'Rock',       'https://picsum.photos/seed/thesmiths-pfp/300/300',     'https://picsum.photos/seed/thesmiths-banner/1200/400',     NULL, NULL, NULL, NULL, NULL),
(16, 22, 'Walters',                      'Indie pop artist.',                                                   'Indie',      'https://picsum.photos/seed/walters-pfp/300/300',       'https://picsum.photos/seed/walters-banner/1200/400',       NULL, NULL, NULL, NULL, NULL),
(17, 23, 'Tame Impala',                  'Psychedelic rock project by Kevin Parker from Perth, Australia.',    'Electronic', 'https://picsum.photos/seed/tameimpala-pfp/300/300',    'https://picsum.photos/seed/tameimpala-banner/1200/400',    'https://instagram.com/tameimpala', 'https://twitter.com/tameimpala', 'https://youtube.com/@tameimpala', NULL, NULL),
(18, 24, 'Tyler the Creator',            'Rapper and producer from Los Angeles.',                               'Hip-Hop',    'https://picsum.photos/seed/tylerthecreator-pfp/300/300','https://picsum.photos/seed/tylerthecreator-banner/1200/400','https://instagram.com/feliciathegoat', NULL, 'https://youtube.com/@tylerthecreator', NULL, NULL),
(19, 25, 'MGMT',                         'Indie pop and psychedelic rock duo.',                                 'Indie',      'https://picsum.photos/seed/mgmt-pfp/300/300',          'https://picsum.photos/seed/mgmt-banner/1200/400',          NULL, 'https://twitter.com/mgmt', NULL, NULL, NULL),
(20, 26, 'TV Girl',                      'Indie pop band from San Diego.',                                      'Indie',      'https://picsum.photos/seed/tvgirl-pfp/300/300',        'https://picsum.photos/seed/tvgirl-banner/1200/400',        'https://instagram.com/tvgirl', NULL, NULL, NULL, NULL),
(21, 27, 'Ankit Tiwari',                 'Indian playback singer and composer.',                                'Pop',        'https://picsum.photos/seed/ankittiwari-pfp/300/300',   'https://picsum.photos/seed/ankittiwari-banner/1200/400',   'https://instagram.com/ankittiwari', 'https://twitter.com/ankittiwari', NULL, NULL, NULL),
(22, 28, 'Faheem Ashraf',                'Pakistani singer-songwriter.',                                        'Pop',        'https://picsum.photos/seed/faeemashraf-pfp/300/300',   'https://picsum.photos/seed/faeemashraf-banner/1200/400',   NULL, NULL, NULL, NULL, NULL),
(23, 29, 'Manan Bhardwaj',               'Indian playback singer.',                                             'Pop',        'https://picsum.photos/seed/mananbhardwaj-pfp/300/300', 'https://picsum.photos/seed/mananbhardwaj-banner/1200/400', NULL, NULL, NULL, NULL, NULL),
(24, 30, 'Sukhwinder Singh',             'Indian playback singer known for Jai Ho.',                            'Pop',        'https://picsum.photos/seed/sukhwindersingh-pfp/300/300','https://picsum.photos/seed/sukhwindersingh-banner/1200/400', NULL, NULL, NULL, NULL, NULL),
(25, 31, 'Arijit Singh',                 'Indian playback singer and music composer.',                          'Pop',        'https://picsum.photos/seed/arijitsingh-pfp/300/300',   'https://picsum.photos/seed/arijitsingh-banner/1200/400',   'https://instagram.com/arijitsingh', NULL, NULL, NULL, NULL),
(26, 32, 'B Praak',                      'Indian singer and music producer.',                                   'Pop',        'https://picsum.photos/seed/bpraak-pfp/300/300',        'https://picsum.photos/seed/bpraak-banner/1200/400',        'https://instagram.com/bpraak', NULL, NULL, NULL, NULL),
(27, 33, 'Roop Kumar',                   'Indian playback singer.',                                             'Pop',        'https://picsum.photos/seed/roopkumar-pfp/300/300',     'https://picsum.photos/seed/roopkumar-banner/1200/400',     NULL, NULL, NULL, NULL, NULL),
(28, 34, 'Sonu Nigam',                   'Indian playback singer and composer.',                                'Pop',        'https://picsum.photos/seed/sonunigam-pfp/300/300',     'https://picsum.photos/seed/sonunigam-banner/1200/400',     'https://instagram.com/sonunigamofficial', NULL, NULL, NULL, NULL),
(29, 35, 'Palak Mucchal',                'Indian playback singer.',                                             'Pop',        'https://picsum.photos/seed/palakmucchal-pfp/300/300',  'https://picsum.photos/seed/palakmucchal-banner/1200/400',  'https://instagram.com/palakmucchal', NULL, NULL, NULL, NULL),
(30, 36, 'Ed Sheeran',                   'British singer-songwriter.',                                          'Pop',        'https://picsum.photos/seed/edsheeran-pfp/300/300',     'https://picsum.photos/seed/edsheeran-banner/1200/400',     'https://instagram.com/teddysphotos', 'https://twitter.com/edsheeran', 'https://youtube.com/@edsheeran', NULL, NULL),
(31, 37, 'Justin Bieber',                'Canadian pop singer.',                                                'Pop',        'https://picsum.photos/seed/justinbieber-pfp/300/300',  'https://picsum.photos/seed/justinbieber-banner/1200/400',  'https://instagram.com/justinbieber', 'https://twitter.com/justinbieber', 'https://youtube.com/@justinbieber', NULL, NULL),
(32, 38, 'Akon',                         'Senegalese-American R&B and pop singer.',                             'R&B',        'https://picsum.photos/seed/akon-pfp/300/300',          'https://picsum.photos/seed/akon-banner/1200/400',          NULL, 'https://twitter.com/akon', NULL, NULL, NULL),
(33, 39, 'Dolly Parton',                 'American country music singer-songwriter.',                           'Pop',        'https://picsum.photos/seed/dollyparton-pfp/300/300',   'https://picsum.photos/seed/dollyparton-banner/1200/400',   'https://instagram.com/dollyparton', 'https://twitter.com/dollyparton', NULL, NULL, 'https://dollyparton.com'),
(34, 40, 'Enrique Iglesias',             'Spanish singer known as the King of Latin Pop.',                      'Pop',        'https://picsum.photos/seed/enriqueiglesias-pfp/300/300','https://picsum.photos/seed/enriqueiglesias-banner/1200/400','https://instagram.com/enriqueiglesias', NULL, 'https://youtube.com/@enriqueiglesias', NULL, NULL),
(35, 41, 'Selena Gomez',                 'American singer, actress and producer.',                              'Pop',        'https://picsum.photos/seed/selenagomez-pfp/300/300',   'https://picsum.photos/seed/selenagomez-banner/1200/400',   'https://instagram.com/selenagomez', 'https://twitter.com/selenagomez', 'https://youtube.com/@selenagomez', NULL, NULL),
(36, 42, 'Linkin Park',                  'American rock band from Agoura Hills, California.',                   'Rock',       'https://picsum.photos/seed/linkinpark-pfp/300/300',    'https://picsum.photos/seed/linkinpark-banner/1200/400',    'https://instagram.com/linkinpark', 'https://twitter.com/linkinpark', 'https://youtube.com/@linkinpark', NULL, 'https://linkinpark.com');

-- ============================================================
-- USERS (IDs 43–67) — Anime / Misc batch
-- ============================================================

INSERT INTO users (id, email, username, password_hash, display_name, bio, role) VALUES
(43, 'fictionjunction@mail.com',      'fiction_junction',        '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'Fiction Junction',              'Japanese music unit by Yuki Kajiura.',                          'ARTIST'),
(44, 'lisa@mail.com',                 'lisa_official',           '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'LiSA',                          'Japanese rock singer known for Demon Slayer themes.',           'ARTIST'),
(45, 'arkpetrol@mail.com',            'ark_petrol',              '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'Ark Petrol',                    'Indie artist.',                                                 'ARTIST'),
(46, 'bonesuk@mail.com',              'bones_uk',                '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'Bones UK',                      'British rock band.',                                            'ARTIST'),
(47, 'manwithamission@mail.com',      'man_with_a_mission',      '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'Man With a Mission and Milet',  'Japanese rock band collaboration with singer Milet.',           'ARTIST'),
(48, 'poormanspois@mail.com',         'poor_mans_poison',        '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'Poor Mans Poison',              'Dark folk and fantasy music artist.',                           'ARTIST'),
(49, 'sergenova@mail.com',            'serge_nova',              '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'Serge Nova',                    'Indie pop artist.',                                             'ARTIST'),
(50, 'againstthecurrent@mail.com',    'against_the_current',     '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'Against the Current',           'American pop rock band.',                                       'ARTIST'),
(51, 'thefatrat@mail.com',            'thefatrat_maisykay',      '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'TheFatRat and Maisy Kay',       'Electronic music producer TheFatRat ft. vocalist Maisy Kay.',   'ARTIST'),
(52, 'yuikooohara@mail.com',          'yuiko_ohara',             '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'Yuiko Ohara',                   'Japanese singer known for Mushoku Tensei themes.',              'ARTIST'),
(53, 'konomisuzuki@mail.com',         'konomi_suzuki',           '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'Konomi Suzuki',                 'Japanese singer known for No Game No Life OP.',                 'ARTIST'),
(54, 'yoasobi@mail.com',              'yoasobi',                 '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'YOASOBI',                       'Japanese music duo known for Racing into the Night.',           'ARTIST'),
(55, 'gawrgura@mail.com',             'gawr_gura',               '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'Gawr Gura',                     'VTuber and singer from Hololive.',                              'ARTIST'),
(56, 'kinggnu@mail.com',              'king_gnu',                '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'King Gnu',                      'Japanese art rock band known for Specialz.',                    'ARTIST'),
(57, 'goosehouse@mail.com',           'goose_house',             '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'Goose House',                   'Japanese music group known for Hikaru Nara.',                   'ARTIST'),
(58, 'gloriagaynor@mail.com',         'gloria_gaynor',           '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'Gloria Gaynor',                 'American disco and R&B singer.',                                'ARTIST'),
(59, 'fieldofview@mail.com',          'field_of_view',           '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'Field of View',                 'Japanese band known for Dragon Ball GT OP.',                    'ARTIST'),
(60, 'fictionjunction2@mail.com',     'fictionjunction',         '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'FictionJunction',               'Japanese music project by Yuki Kajiura for anime themes.',      'ARTIST'),
(61, 'lennycodefiction@mail.com',     'lenny_code_fiction',      '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'Lenny Code Fiction',            'Japanese band known for My Hero Academia OP.',                  'ARTIST'),
(62, 'fujiikaze@mail.com',            'fujii_kaze',              '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'Fujii Kaze',                    'Japanese singer-songwriter and pianist.',                       'ARTIST'),
(63, 'hiroyukisawano@mail.com',       'hiroyuki_sawano',         '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'Hiroyuki Sawano',               'Japanese composer known for Attack on Titan OST.',              'ARTIST'),
(64, 'lisafelixstraykids@mail.com',   'lisa_felix_straykids',    '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'Lisa feat. Felix of Stray Kids','Collaboration between LiSA and Felix from Stray Kids.',         'ARTIST'),
(65, 'ikimonogakari@mail.com',        'ikimonogakari',           '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'Ikimonogakari',                 'Japanese pop band known for Blue Bird (Naruto OP).',            'ARTIST'),
(66, 'sim@mail.com',                  'sim_official',            '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'SiM',                           'Japanese punk reggae band known for The Rumbling.',             'ARTIST'),
(67, 'tk@mail.com',                   'tk_official',             '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'TK',                            'Japanese musician known for Unravel (Tokyo Ghoul OP).',         'ARTIST');

-- ============================================================
-- ARTISTS (IDs 37–61)
-- ============================================================

INSERT INTO artists (id, user_id, artist_name, bio, genre, profile_picture_url, banner_image_url, instagram, twitter, youtube, spotify, website) VALUES
(37, 43, 'Fiction Junction',              'Japanese music unit formed by composer Yuki Kajiura.',                        'Anime',      'https://upload.wikimedia.org/wikipedia/commons/d/df/LiSA_by_Gage_Skidmore.jpg',                       'https://picsum.photos/seed/fictionjunction-banner/1200/400',       NULL, NULL, NULL, NULL, NULL),
(38, 44, 'LiSA',                          'Japanese rock singer known for Demon Slayer and SAO themes.',                 'Anime',      'https://upload.wikimedia.org/wikipedia/commons/d/df/LiSA_by_Gage_Skidmore.jpg',                       'https://picsum.photos/seed/lisa-banner/1200/400',                  'https://instagram.com/lisaofficial', 'https://twitter.com/LiSA_OLiVE', NULL, NULL, NULL),
(39, 45, 'Ark Petrol',                    'Indie artist.',                                                               'Indie',      'https://f4.bcbits.com/img/a0206482820_10.jpg',                                                        'https://picsum.photos/seed/arkpetrol-banner/1200/400',             NULL, NULL, NULL, NULL, NULL),
(40, 46, 'Bones UK',                      'British rock band.',                                                          'Rock',       'https://upload.wikimedia.org/wikipedia/commons/0/0c/Bones_UK_10.21.2019_-_50425293592.jpg',            'https://picsum.photos/seed/bonesuk-banner/1200/400',               'https://instagram.com/bonesuk', NULL, NULL, NULL, NULL),
(41, 47, 'Man With a Mission and Milet',  'Japanese rock band collaboration with pop singer Milet.',                     'Anime',      'https://static.wikia.nocookie.net/kimetsu-no-yaiba/images/d/d1/Kizuna_no_Kiseki_Regular_Edition_Cover.png/revision/latest?cb=20230412015040', 'https://picsum.photos/seed/manwithamission-banner/1200/400', NULL, NULL, NULL, NULL, NULL),
(42, 48, 'Poor Mans Poison',              'Dark folk and fantasy music artist.',                                         'Folk',       'https://is1-ssl.mzstatic.com/image/thumb/Music116/v4/17/69/09/1769094c-9042-9210-d15f-c48e270d13d1/859740564316_cover.jpg/592x592bb.webp', 'https://picsum.photos/seed/poormanspois-banner/1200/400', NULL, NULL, NULL, NULL, NULL),
(43, 49, 'Serge Nova',                    'Indie pop artist.',                                                           'Indie',      'https://c.saavncdn.com/716/I-Feel-Alone-English-2021-20210402121040-500x500.jpg',                     'https://picsum.photos/seed/sergenova-banner/1200/400',             NULL, NULL, NULL, NULL, NULL),
(44, 50, 'Against the Current',           'American pop rock band.',                                                     'Rock',       'https://cdn-images.dzcdn.net/images/artist/39621dce16e1afbbb3bd7198392ffd20/500x500-000000-80-0-0.jpg','https://picsum.photos/seed/againstthecurrent-banner/1200/400',    'https://instagram.com/atcofficial', 'https://twitter.com/atcofficial', NULL, NULL, NULL),
(45, 51, 'TheFatRat and Maisy Kay',       'Electronic music producer TheFatRat featuring vocalist Maisy Kay.',           'Electronic', 'https://pbs.twimg.com/media/EQqv3LRW4AIVKAT?format=jpg&name=900x900',                               'https://picsum.photos/seed/thefatrat-banner/1200/400',             NULL, NULL, NULL, NULL, NULL),
(46, 52, 'Yuiko Ohara',                   'Japanese singer known for Mushoku Tensei ED themes.',                         'Anime',      'https://www.lyrical-nonsense.com/wp-content/uploads/2021/01/Yuiko-Ohara-Tabibito-no-Uta.jpg',         'https://picsum.photos/seed/yuikooohara-banner/1200/400',           NULL, NULL, NULL, NULL, NULL),
(47, 53, 'Konomi Suzuki',                 'Japanese singer known for the No Game No Life opening.',                      'Anime',      'https://m.media-amazon.com/images/I/718-9oa7ryL._AC_UF894,1000_QL80_.jpg',                           'https://picsum.photos/seed/konomisuzuki-banner/1200/400',          NULL, NULL, NULL, NULL, NULL),
(48, 54, 'YOASOBI',                       'Japanese music duo known for Racing into the Night and Idol.',                'Anime',      'https://upload.wikimedia.org/wikipedia/en/thumb/9/93/Yoru_ni_Kakeru_cover_art.jpg/250px-Yoru_ni_Kakeru_cover_art.jpg', 'https://picsum.photos/seed/yoasobi-banner/1200/400', 'https://instagram.com/yoasobi_music', NULL, NULL, NULL, NULL),
(49, 55, 'Gawr Gura',                     'VTuber and singer from Hololive.',                                            'Pop',        'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTo3ovYLXSCl51d43WlbrwI-kqP8oPrUUWbKw&s',     'https://picsum.photos/seed/gawrgura-banner/1200/400',              NULL, NULL, NULL, NULL, NULL),
(50, 56, 'King Gnu',                      'Japanese art rock band known for Specialz and Ichizu.',                       'Rock',       'https://audio.com/s3w/audio.com.static/audio/image/45/44/1851989779574445-1851989882255901.jpeg',      'https://picsum.photos/seed/kinggnu-banner/1200/400',               'https://instagram.com/kinggnu_official', NULL, NULL, NULL, NULL),
(51, 57, 'Goose House',                   'Japanese music group known for Hikaru Nara from Your Lie in April.',          'Anime',      'https://i1.sndcdn.com/artworks-000114147280-sajh2v-t500x500.jpg',                                    'https://picsum.photos/seed/goosehouse-banner/1200/400',            NULL, NULL, NULL, NULL, NULL),
(52, 58, 'Gloria Gaynor',                 'American disco and R&B singer.',                                              'R&B',        'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRtQi2h0mmJNBnaFO2ngEccKtl_pY7j1Z890g&s',     'https://picsum.photos/seed/gloriagaynor-banner/1200/400',          NULL, NULL, NULL, NULL, NULL),
(53, 59, 'Field of View',                 'Japanese band known for Dragon Ball GT opening Dan Dan Kokoro Hikareteku.',   'Anime',      'https://m.media-amazon.com/images/M/MV5BMWQyMzQwNzQtZTgxYy00OGY0LTg0MmItZjNkNTk3NjgyYmJjXkEyXkFqcGc@._V1_FMjpg_UX1000_.jpg', 'https://picsum.photos/seed/fieldofview-banner/1200/400', NULL, NULL, NULL, NULL, NULL),
(54, 60, 'FictionJunction',               'Japanese music project by Yuki Kajiura for anime soundtracks.',               'Anime',      'https://www.sittingonclouds.net/_image?href=https%3A%2F%2Fsittingonclouds.s3web.calibour.net%2Fprod%2Fimg%2Falbum%2F2089.png&w=500&h=500&q=low&f=webp', 'https://picsum.photos/seed/fictionjunction2-banner/1200/400', NULL, NULL, NULL, NULL, NULL),
(55, 61, 'Lenny Code Fiction',            'Japanese band known for Make My Story from My Hero Academia.',                'Anime',      'https://static.bandainamcoent.eu/high/my-hero-academia/my-hero-alls-justice/00-page-product/MHAJ-header-mobile.jpg', 'https://picsum.photos/seed/lennycodefiction-banner/1200/400', NULL, NULL, NULL, NULL, NULL),
(56, 62, 'Fujii Kaze',                    'Japanese singer-songwriter and pianist.',                                      'Pop',        'https://upload.wikimedia.org/wikipedia/commons/4/49/Fujii_Kaze_performing_during_Best_Of_Fujii_Kaze_2020-2024_Asia_Tour_in_Axiata_Arena_Kuala_Lumpur_%28cropped%29.jpg', 'https://picsum.photos/seed/fujiikaze-banner/1200/400', 'https://instagram.com/fujiikaze', NULL, NULL, NULL, NULL),
(57, 63, 'Hiroyuki Sawano',               'Japanese composer known for Attack on Titan OST.',                            'Anime',      'https://image-cdn-ak.spotifycdn.com/image/ab67706c0000da84956c55753bca75c958888364',                  'https://picsum.photos/seed/hiroyukisawano-banner/1200/400',        NULL, NULL, NULL, NULL, NULL),
(58, 64, 'Lisa feat. Felix of Stray Kids','Collaboration between LiSA and Felix from Stray Kids for Solo Leveling.',     'Anime',      'https://static.wikia.nocookie.net/hiroyuki-sawano/images/4/41/ReawakeR_Limited_Edition.jpg/revision/latest/scale-to-width/360?cb=20250322095719', 'https://picsum.photos/seed/lisafelix-banner/1200/400', NULL, NULL, NULL, NULL, NULL),
(59, 65, 'Ikimonogakari',                 'Japanese pop band known for Blue Bird the Naruto opening.',                   'Anime',      'https://cdn-images.dzcdn.net/images/cover/7ad86630de524f2edc1988d715f02fce/0x1900-000000-80-0-0.jpg', 'https://picsum.photos/seed/ikimonogakari-banner/1200/400',        NULL, NULL, NULL, NULL, NULL),
(60, 66, 'SiM',                           'Japanese punk reggae band known for The Rumbling from Attack on Titan.',      'Rock',       'https://i1.sndcdn.com/artworks-I5yygyDTMzx7a030-hW3UiQ-t500x500.jpg',                               'https://picsum.photos/seed/sim-banner/1200/400',                   'https://instagram.com/sim_official', NULL, NULL, NULL, NULL),
(61, 67, 'TK',                            'Japanese musician known for Unravel the Tokyo Ghoul opening.',                'Anime',      'https://i.pinimg.com/736x/0e/5c/23/0e5c23103e94fe2ccf4f89f517784725.jpg',                           'https://picsum.photos/seed/tk-banner/1200/400',                    NULL, NULL, NULL, NULL, NULL);

-- ============================================================
-- NEW ALBUMS (IDs 5–14)
-- ============================================================

INSERT INTO albums (id, name, description, cover_image_url, release_date, artist_id) VALUES
(5,  'Mac DeMarco Collection',
     'A selection of Mac DeMarco''s most iconic lo-fi indie tracks.',
     'https://picsum.photos/seed/macdemarco-album/400/400',
     '2014-04-01', 5),

(6,  'Arijit Singh Hits',
     'A collection of Arijit Singh''s most beloved Bollywood tracks.',
     'https://picsum.photos/seed/arijitsingh-album/400/400',
     '2022-07-17', 25),

(7,  'Ankit Tiwari Hits',
     'Soulful compositions by Ankit Tiwari from popular Bollywood films.',
     'https://picsum.photos/seed/ankittiwari-album/400/400',
     '2013-04-26', 21),

(8,  'Enrique Iglesias Greatest Hits',
     'The greatest Latin pop anthems by the King of Latin Pop.',
     'https://picsum.photos/seed/enrique-album/400/400',
     '1999-07-13', 34),

(9,  'Purpose',
     'Justin Bieber''s chart-topping pop album.',
     'https://picsum.photos/seed/justinbieber-album/400/400',
     '2015-11-13', 31),

(10, 'Selena Gomez Hits',
     'A collection of Selena Gomez''s most popular pop tracks.',
     'https://picsum.photos/seed/selenagomez-album/400/400',
     '2015-06-22', 35),

(11, 'Trouble',
     'Akon''s debut album featuring his most iconic R&B and pop tracks.',
     'https://picsum.photos/seed/akon-album/400/400',
     '2004-06-08', 32),

(12, 'Linkin Park Hits',
     'Essential tracks from Linkin Park''s rock discography.',
     'https://picsum.photos/seed/linkinpark-album/400/400',
     '2007-04-02', 36),

(13, 'Demon Slayer OST',
     'Iconic opening and insert songs from the Demon Slayer anime series.',
     'https://static.wikia.nocookie.net/kimetsu-no-yaiba/images/d/d1/Kizuna_no_Kiseki_Regular_Edition_Cover.png/revision/latest?cb=20230412015040',
     '2023-04-09', 38),

(14, 'Attack on Titan OST',
     'Powerful tracks from the Attack on Titan anime series.',
     'https://image-cdn-ak.spotifycdn.com/image/ab67706c0000da84956c55753bca75c958888364',
     '2013-06-26', 57);

-- ============================================================
-- SONGS (IDs 16–89)
-- Songs 1–15 already inserted above in the base V101 block
-- ============================================================

INSERT INTO songs (id, title, genre, duration, audio_url, cover_image_url, release_date, play_count, visibility, artist_id, album_id) VALUES

-- ── D4vd ──────────────────────────────────────────────────────
(16, 'Feel It',
     'R&B', 180,
     'https://audio.jukehost.co.uk/M9BTPYkJiNkqMmeLPHjSWjlz4TtQLEin',
     'https://i.pinimg.com/736x/c4/0b/12/c40b12e8d09daed4b78f963c62ce2717.jpg',
     '2022-08-05', 0, 'PUBLIC', 4, NULL),

-- ── Mac DeMarco Collection (album_id=5) ───────────────────────
(17, 'Chamber of Reflection',
     'Indie', 240,
     'https://audio.jukehost.co.uk/GDWQZobDzUgyTJzy8YfkHRNcRTRCeXHZ',
     'https://i.pinimg.com/736x/07/e3/85/07e385839c0772b6f7cbd281924f8239.jpg',
     '2014-04-01', 0, 'PUBLIC', 5, 5),

-- ── Billie Eilish ─────────────────────────────────────────────
(18, 'CHIHIRO',
     'Pop', 252,
     'https://audio.jukehost.co.uk/GClKqu7ZECNAD4Mrr9c1bVWO4IhBEmI3',
     'https://i.pinimg.com/736x/79/90/40/799040ef1cb7df0b5d77ce612d452f42.jpg',
     '2024-05-17', 0, 'PUBLIC', 6, NULL),

-- ── Boa ──────────────────────────────────────────────────────
(19, 'Duvet',
     'Indie', 290,
     'https://audio.jukehost.co.uk/euk6snIFCx3seFv8cdFvdkjmWp9MdWck',
     'https://i.pinimg.com/736x/f9/f9/68/f9f96822e73980062462488b119038e3.jpg',
     '1998-01-01', 0, 'PUBLIC', 7, NULL),

-- ── Djo ──────────────────────────────────────────────────────
(20, 'End of Beginning',
     'Indie', 214,
     'https://audio.jukehost.co.uk/SMCvRHZWFqFJRY5YnG1qi0mOkuLWphyN',
     'https://i.pinimg.com/736x/65/14/11/6514119580651d4b950c6a7dac788560.jpg',
     '2022-10-21', 0, 'PUBLIC', 8, NULL),

-- ── Richy Mitch & The Coal Miners ────────────────────────────
(21, 'Evergreen',
     'Folk', 240,
     'https://audio.jukehost.co.uk/xixnNFhjXHUdsaObdWXxKLLSJmii2PS9',
     'https://i.pinimg.com/736x/9a/e2/ad/9ae2ad72f695e4e7297e476e373f790d.jpg',
     '2021-01-01', 0, 'PUBLIC', 9, NULL),

-- ── Radiohead ─────────────────────────────────────────────────
(22, 'Exit Music (For A Film)',
     'Rock', 244,
     'https://audio.jukehost.co.uk/qcMpDjZFNdJLfOnWQDXoa1iez3dKx5CO',
     'https://i.pinimg.com/1200x/a9/11/3e/a9113e0dea1aa406f997203b6451fd9c.jpg',
     '1997-05-21', 0, 'PUBLIC', 10, NULL),

-- ── Mac DeMarco Collection (album_id=5) ───────────────────────
(23, 'Freaking Out the Neighborhood',
     'Indie', 183,
     'https://audio.jukehost.co.uk/gX2sbyw5cPmBidZskDjFuTIP4rhEuZ77',
     'https://i1.sndcdn.com/artworks-000029723179-knqqe8-t1080x1080.jpg',
     '2012-10-01', 0, 'PUBLIC', 5, 5),

-- ── Surf Curse ───────────────────────────────────────────────
(24, 'Freaks',
     'Indie', 148,
     'https://audio.jukehost.co.uk/dgYufRVh6ukvd9l06Lbu0I9UOVwVYxXk',
     'https://i.pinimg.com/1200x/10/18/88/101888a76e353defade24a8d3c2df7f4.jpg',
     '2013-01-01', 0, 'PUBLIC', 11, NULL),

-- ── Laufey ───────────────────────────────────────────────────
(25, 'From The Start',
     'Jazz', 198,
     'https://audio.jukehost.co.uk/DFYBqFFrJ3cHFKEhvsMFEabGQUpEDk2c',
     'https://i.pinimg.com/736x/c8/84/e0/c884e0284e003cf36da86731020431c2.jpg',
     '2023-02-23', 0, 'PUBLIC', 12, NULL),

-- ── Suki Waterhouse ──────────────────────────────────────────
(26, 'Good Looking',
     'Indie', 193,
     'https://audio.jukehost.co.uk/1dMLvmU5bbR3KbhL8QirGyh15SsyJ4wM',
     'https://i.pinimg.com/736x/e4/e5/fb/e4e5fba45be231b23dc40ff8f3ba4863.jpg',
     '2023-02-17', 0, 'PUBLIC', 13, NULL),

-- ── Imogen Heap ──────────────────────────────────────────────
(27, 'Headlock',
     'Electronic', 268,
     'https://audio.jukehost.co.uk/94nOtGIZB7sDSllwaRorSZI3WFE7soGy',
     'https://i.pinimg.com/736x/5f/83/e4/5f83e4541db528d2d637ef817cbc305a.jpg',
     '2005-09-26', 0, 'PUBLIC', 14, NULL),

-- ── Mac DeMarco Collection (album_id=5) ───────────────────────
(28, 'Heart to Heart',
     'Indie', 219,
     'https://audio.jukehost.co.uk/TkZHw28Rlbdd00sjiIN7h71GD0aTLvaH',
     'https://i.pinimg.com/736x/3a/dd/f8/3addf83ba27cdb1be7f25a14b570e8ff.jpg',
     '2019-05-10', 0, 'PUBLIC', 5, 5),

-- ── The Smiths ───────────────────────────────────────────────
(29, 'Heaven Knows I''m Miserable Now',
     'Rock', 209,
     'https://audio.jukehost.co.uk/Lg6MRFgZj4GcKsS7DWiSjwuYK58ylDcB',
     'https://i.scdn.co/image/ab67616d0000b273786b44c75ebf915866523f5b',
     '1984-05-01', 0, 'PUBLIC', 15, NULL),

-- ── Walters ──────────────────────────────────────────────────
(30, 'I Love You So',
     'Indie', 176,
     'https://audio.jukehost.co.uk/rBWPAENIp2kfunuX4gYcgB0f0BDJGI7K',
     'https://i.pinimg.com/1200x/26/ea/51/26ea5121409f71f3dae0d44944abdeed.jpg',
     '2016-01-01', 0, 'PUBLIC', 16, NULL),

-- ── Tame Impala ──────────────────────────────────────────────
(31, 'Let It Happen',
     'Electronic', 467,
     'https://audio.jukehost.co.uk/1YmXcwgdOeHCtpuEj0g8W0t73nKtH2mU',
     'https://i.pinimg.com/736x/4a/21/c7/4a21c70d53bbefbcec478f659ce96181.jpg',
     '2015-05-15', 0, 'PUBLIC', 17, NULL),

-- ── Tyler the Creator ─────────────────────────────────────────
(32, 'Like Him',
     'Hip-Hop', 233,
     'https://audio.jukehost.co.uk/RyaFRWY3esl8VKZYAWqd8Hzk5WucJrEH',
     'https://i.pinimg.com/736x/84/06/ad/8406adb3abff889203f4734ba517696b.jpg',
     '2024-06-07', 0, 'PUBLIC', 18, NULL),

-- ── MGMT ─────────────────────────────────────────────────────
(33, 'Little Dark Age',
     'Indie', 213,
     'https://audio.jukehost.co.uk/fd6qCCAfx0wOeII8sW2XJENohpK9PaYx',
     'https://i.pinimg.com/736x/32/43/a2/3243a28492d668c4338b1c52ff17e476.jpg',
     '2018-02-09', 0, 'PUBLIC', 19, NULL),

-- ── TV Girl ──────────────────────────────────────────────────
(34, 'Lovers Rock',
     'Indie', 207,
     'https://audio.jukehost.co.uk/Q53axKuZHF0uskhbcp9lE1ZjWQ4oVNld',
     'https://i.pinimg.com/736x/0c/d0/01/0cd001b4e7c9c9ac8fff56a6e1aee9cb.jpg',
     '2014-01-01', 0, 'PUBLIC', 20, NULL),

-- ── Ankit Tiwari Hits (album_id=7) ───────────────────────────
(35, 'Tum Hi Ho',
     'Pop', 268,
     'https://audio.jukehost.co.uk/lCV9Tub62pUhfzEhXpPFkRMr384O5vDL',
     'https://upload.wikimedia.org/wikipedia/en/f/f3/Aashiqui_2_%28Poster%29.jpg',
     '2013-04-26', 0, 'PUBLIC', 21, 7),

-- ── Faheem Ashraf ─────────────────────────────────────────────
(36, 'Saiyyara',
     'Pop', 240,
     'https://audio.jukehost.co.uk/Smg3K5byxGBF1D1PyPUB9usfhfPN2r11',
     'https://upload.wikimedia.org/wikipedia/en/d/db/Saiyaara_film_poster.jpg',
     '2025-07-18', 0, 'PUBLIC', 22, NULL),

-- ── Manan Bhardwaj ───────────────────────────────────────────
(37, 'Shiddat',
     'Pop', 235,
     'https://audio.jukehost.co.uk/jUMrCg3zvZTkfYpBjsH6K9fU0i5hwgV4',
     'https://upload.wikimedia.org/wikipedia/en/5/5e/Shiddat_poster.jpg',
     '2021-10-01', 0, 'PUBLIC', 23, NULL),

-- ── Ankit Tiwari Hits (album_id=7) ───────────────────────────
(38, 'Sanam Teri Kasam',
     'Pop', 298,
     'https://audio.jukehost.co.uk/PJyJPSrPHeKj6YBtEP1hsV6Jvuk3EHoK',
     'https://upload.wikimedia.org/wikipedia/en/7/72/Sanam_Teri_Kasam_2016.jpeg',
     '2016-02-05', 0, 'PUBLIC', 21, 7),

-- ── Sukhwinder Singh ─────────────────────────────────────────
(39, 'Dil Se',
     'Pop', 320,
     'https://audio.jukehost.co.uk/5exycALVStbHcup1rkZpAgWTXCf5Ryw8',
     'https://upload.wikimedia.org/wikipedia/en/7/7a/Dil_Se_poster.jpg',
     '1998-08-21', 0, 'PUBLIC', 24, NULL),

-- ── Arijit Singh Hits (album_id=6) ───────────────────────────
(40, 'Kesariya',
     'Pop', 278,
     'https://audio.jukehost.co.uk/ka5Pm6IzaPCzlYNHwFHsFYzlAUjHHkjg',
     'https://upload.wikimedia.org/wikipedia/en/3/3c/Kesariya_song_cover.jpg',
     '2022-07-17', 0, 'PUBLIC', 25, 6),

(41, 'Sanam Re',
     'Pop', 265,
     'https://audio.jukehost.co.uk/uv93tt3O4PHd68l6wlOwfnGWl9ys7qpb',
     'https://upload.wikimedia.org/wikipedia/en/9/9f/Sanam_Re_movie_poster.jpg',
     '2016-01-29', 0, 'PUBLIC', 25, 6),

(42, 'Chaleya',
     'Pop', 248,
     'https://audio.jukehost.co.uk/LsMikK7XcMVgehyStIZluSd8ZeOKH27h',
     'https://upload.wikimedia.org/wikipedia/en/3/39/Jawan_film_poster.jpg',
     '2023-09-07', 0, 'PUBLIC', 25, 6),

(43, 'Zaalima',
     'Pop', 255,
     'https://audio.jukehost.co.uk/36w33XcvOPLTxCAYu5bH0t9v3uL61swP',
     'https://upload.wikimedia.org/wikipedia/en/9/9b/Zaalima_Raees_Cover.jpg',
     '2017-01-11', 0, 'PUBLIC', 25, 6),

(44, 'Khairiyat',
     'Pop', 262,
     'https://audio.jukehost.co.uk/rIrsLgKrglDw9ZdkremYIY0J3zLJWK0b',
     'https://upload.wikimedia.org/wikipedia/en/3/3d/Chhichhore_Poster.jpg',
     '2019-08-30', 0, 'PUBLIC', 25, 6),

-- ── B Praak ──────────────────────────────────────────────────
(45, 'Ranjha',
     'Pop', 245,
     'https://audio.jukehost.co.uk/d921w1NlI3oNt6KjCKcVwVmj0oGN0g2C',
     'https://upload.wikimedia.org/wikipedia/en/3/32/Shershaah_soundtrack.jpg',
     '2021-07-02', 0, 'PUBLIC', 26, NULL),

-- ── Roop Kumar ───────────────────────────────────────────────
(46, 'Tuj Me Rab Dikhta Hai',
     'Pop', 310,
     'https://audio.jukehost.co.uk/oBEVQzBWm0D97QRUlKHYOdKt1pSqbAPH',
     'https://upload.wikimedia.org/wikipedia/en/a/ab/Rab_Ne_Bana_Di_Jodi.jpg',
     '2008-10-12', 0, 'PUBLIC', 27, NULL),

-- ── Sonu Nigam ───────────────────────────────────────────────
(47, 'Mai Agar Kahoon',
     'Pop', 285,
     'https://audio.jukehost.co.uk/k3glXifESWaSeAuX41YyUhgrLjr0TTBO',
     'https://upload.wikimedia.org/wikipedia/en/4/41/Om_Shanti_Om.jpg',
     '2007-11-09', 0, 'PUBLIC', 28, NULL),

-- ── Palak Mucchal ────────────────────────────────────────────
(48, 'Kaun Tuje Yun Pyaar Karega',
     'Pop', 272,
     'https://audio.jukehost.co.uk/csg5To2hj4MWplzGj9gEiw7iKejTIBoU',
     'https://upload.wikimedia.org/wikipedia/en/3/33/M.S._Dhoni_-_The_Untold_Story_poster.jpg',
     '2016-09-30', 0, 'PUBLIC', 29, NULL),

-- ── Ed Sheeran ───────────────────────────────────────────────
(49, 'Shape of You',
     'Pop', 234,
     'https://audio.jukehost.co.uk/17rIxtgIdR2d8AaQH0qg3X0ZbwCBJBNJ',
     'https://upload.wikimedia.org/wikipedia/en/b/b4/Shape_Of_You_%28Official_Single_Cover%29_by_Ed_Sheeran.png',
     '2017-01-06', 0, 'PUBLIC', 30, NULL),

-- ── Justin Bieber — Purpose (album_id=9) ─────────────────────
(50, 'Sorry',
     'Pop', 200,
     'https://audio.jukehost.co.uk/4QbRTm7QPuRhehe5PFI6faks0cuIWfiO',
     'https://upload.wikimedia.org/wikipedia/en/d/dc/Justin_Bieber_-_Sorry_%28Official_Single_Cover%29.png',
     '2015-10-23', 0, 'PUBLIC', 31, 9),

(51, 'What Do You Mean?',
     'Pop', 198,
     'https://audio.jukehost.co.uk/ixGwlJqnQHEgH6eIBcPk871up4AsRtDS',
     'https://upload.wikimedia.org/wikipedia/en/9/9e/JustinBieberWhatDoYouMeanCover.png',
     '2015-08-28', 0, 'PUBLIC', 31, 9),

-- ── Akon — Trouble (album_id=11) ─────────────────────────────
(52, 'Ghetto',
     'R&B', 238,
     'https://audio.jukehost.co.uk/wy6uWR8stRtwfRTBpZCw49nh62q5FOZk',
     'https://upload.wikimedia.org/wikipedia/en/4/4b/Akon_-_Ghetto.jpg',
     '2004-01-01', 0, 'PUBLIC', 32, 11),

(53, 'Beautiful',
     'R&B', 244,
     'https://audio.jukehost.co.uk/D91BAx5QxkIFIWLP0VDcsn6l1FzuPxin',
     'https://upload.wikimedia.org/wikipedia/en/0/02/Beautiful_%28Akon_song%29.jpg',
     '2004-01-01', 0, 'PUBLIC', 32, 11),

-- ── Dolly Parton ─────────────────────────────────────────────
(54, 'Joshua',
     'Pop', 195,
     'https://audio.jukehost.co.uk/kBx71ieGm50tBqaWuMmQWpiFgihK0qmK',
     'https://upload.wikimedia.org/wikipedia/en/2/2b/Joshua_Parton.jpg',
     '1971-01-01', 0, 'PUBLIC', 33, NULL),

-- ── Enrique Iglesias Greatest Hits (album_id=8) ──────────────
(55, 'Bailamos',
     'Pop', 237,
     'https://audio.jukehost.co.uk/o4XzpRyC72Iw3OktzBOFj1NRjXeD4GUI',
     'https://upload.wikimedia.org/wikipedia/en/b/bd/Bailamos_1.jpg',
     '1999-07-13', 0, 'PUBLIC', 34, 8),

(56, 'Bailando',
     'Pop', 270,
     'https://audio.jukehost.co.uk/xh1vD5wejy8mcuGVT3s0SOEFOQ1R2rTF',
     'https://upload.wikimedia.org/wikipedia/en/c/c0/Enriquebailandocover.jpg',
     '2014-04-11', 0, 'PUBLIC', 34, 8),

(57, 'Heartbeat',
     'Pop', 222,
     'https://audio.jukehost.co.uk/Zli6Qfq1rXH2p6cJQ3QD3IZj1vf3GRZq',
     'https://upload.wikimedia.org/wikipedia/en/1/17/Enrique_Iglesias_%26_Nicole_Scherzinger_-_Heartbeat.jpg',
     '2010-09-13', 0, 'PUBLIC', 34, 8),

(58, 'Rhythm Divine',
     'Pop', 248,
     'https://audio.jukehost.co.uk/mBYjz1jD06sxkC4e6MwasCjkkUGcMHEr',
     'https://upload.wikimedia.org/wikipedia/en/c/c8/Rhythm_Divine_1.jpg',
     '1999-01-01', 0, 'PUBLIC', 34, 8),

(59, 'Takin Back My Love',
     'Pop', 215,
     'https://audio.jukehost.co.uk/zW7xxuPQxfWADuzvzmz5QgSGrwsvkRZJ',
     'https://upload.wikimedia.org/wikipedia/en/5/56/Takinbackmylove_.JPEG',
     '2009-01-13', 0, 'PUBLIC', 34, 8),

-- ── Selena Gomez Hits (album_id=10) ──────────────────────────
(60, 'Good For You',
     'Pop', 193,
     'https://audio.jukehost.co.uk/UsWWLeh1gAvBZ3v0o72kJTBVTAbDNc04',
     'https://upload.wikimedia.org/wikipedia/en/1/1e/Selena_Gomez_-_Good_For_You_%28Official_Single_Cover%29.png',
     '2015-06-22', 0, 'PUBLIC', 35, 10),

(61, 'Hands to Myself',
     'Pop', 194,
     'https://audio.jukehost.co.uk/6PDNLHnFmnnGuD3BI5OEbEG2zlAfj3Po',
     'https://upload.wikimedia.org/wikipedia/en/2/2c/Selena_Gomez_-_Hands_to_Myself_single_cover.png',
     '2015-11-23', 0, 'PUBLIC', 35, 10),

(62, 'A Year Without Rain',
     'Pop', 196,
     'https://audio.jukehost.co.uk/tXiXVTPVDXhG3HHhu9cGFJBZShzXIfIY',
     'https://upload.wikimedia.org/wikipedia/en/1/12/Selena_Gomez_%26_the_Scene_-_A_Year_Without_Rain_%28single_cover%29.jpg',
     '2010-09-17', 0, 'PUBLIC', 35, 10),

-- ── Linkin Park Hits (album_id=12) ───────────────────────────
(63, 'The Catalyst',
     'Rock', 265,
     'https://audio.jukehost.co.uk/aZS5ucAKGKsSv03ptpGjjgNlyc0pOz4o',
     'https://upload.wikimedia.org/wikipedia/en/c/cd/The-catalyst-single-cover-500x500.png',
     '2010-08-02', 0, 'PUBLIC', 36, 12),

(64, 'What I''ve Done',
     'Rock', 226,
     'https://audio.jukehost.co.uk/ssXEfsyY4tMwR7CgjCQqYSQDfMMUWO4c',
     'https://upload.wikimedia.org/wikipedia/en/9/94/WhatI%27veDoneCover.jpg',
     '2007-04-02', 0, 'PUBLIC', 36, 12),

-- ── Demon Slayer OST (album_id=13) ───────────────────────────
(65, 'Age of Darkness (Demon Slayer)',
     'Anime', 240,
     'https://audio.jukehost.co.uk/WfD4VJ6QPnuIiOEDsvu4sFEaqVxBdn7f',
     'https://upload.wikimedia.org/wikipedia/commons/d/df/LiSA_by_Gage_Skidmore.jpg',
     '2023-01-01', 0, 'PUBLIC', 37, 13),

(66, 'Akeboshi (Demon Slayer)',
     'Anime', 220,
     'https://audio.jukehost.co.uk/qXuM8BxoQh7XOvMGtMQTXBgenpLLPoJv',
     'https://upload.wikimedia.org/wikipedia/commons/d/df/LiSA_by_Gage_Skidmore.jpg',
     '2021-10-10', 0, 'PUBLIC', 38, 13),

-- ── Ark Petrol ────────────────────────────────────────────────
(67, 'Let Go',
     'Indie', 210,
     'https://audio.jukehost.co.uk/JBf1oYsBAf2uSIMUe15VkM6mmjQRBjOt',
     'https://f4.bcbits.com/img/a0206482820_10.jpg',
     '2020-01-01', 0, 'PUBLIC', 39, NULL),

-- ── Bones UK ──────────────────────────────────────────────────
(68, 'Beautiful is Boring',
     'Rock', 195,
     'https://audio.jukehost.co.uk/EbFPRuyMrCFi6RUqqxwsnc84CJb7BOqA',
     'https://upload.wikimedia.org/wikipedia/commons/0/0c/Bones_UK_10.21.2019_-_50425293592.jpg',
     '2019-01-01', 0, 'PUBLIC', 40, NULL),

-- ── Demon Slayer OST (album_id=13) ───────────────────────────
(69, 'Kizuna no Kiseki (Demon Slayer)',
     'Anime', 255,
     'https://audio.jukehost.co.uk/0AJsVRGFsZNMKrMbmdVQkawKMhjyWHvt',
     'https://static.wikia.nocookie.net/kimetsu-no-yaiba/images/d/d1/Kizuna_no_Kiseki_Regular_Edition_Cover.png/revision/latest?cb=20230412015040',
     '2023-04-09', 0, 'PUBLIC', 41, 13),

-- ── Poor Mans Poison ──────────────────────────────────────────
(70, 'Feed The Machine',
     'Folk', 248,
     'https://audio.jukehost.co.uk/PQrT0QNLFGV4uZKbagJ1TZwAZd1yZjHz',
     'https://is1-ssl.mzstatic.com/image/thumb/Music116/v4/17/69/09/1769094c-9042-9210-d15f-c48e270d13d1/859740564316_cover.jpg/592x592bb.webp',
     '2019-01-01', 0, 'PUBLIC', 42, NULL),

-- ── Serge Nova ────────────────────────────────────────────────
(71, 'I Feel Alone',
     'Indie', 185,
     'https://audio.jukehost.co.uk/0eigLzyG7wPaTFuOHHKsjnGx1yLCzZgU',
     'https://c.saavncdn.com/716/I-Feel-Alone-English-2021-20210402121040-500x500.jpg',
     '2021-04-02', 0, 'PUBLIC', 43, NULL),

-- ── Against the Current ───────────────────────────────────────
(72, 'Legends Never Die',
     'Rock', 213,
     'https://audio.jukehost.co.uk/6uEnA1qaKu7Iczxl5LHFUWqDbgUVlMT1',
     'https://cdn-images.dzcdn.net/images/artist/39621dce16e1afbbb3bd7198392ffd20/500x500-000000-80-0-0.jpg',
     '2017-01-01', 0, 'PUBLIC', 44, NULL),

-- ── TheFatRat and Maisy Kay ───────────────────────────────────
(73, 'The Storm',
     'Electronic', 230,
     'https://audio.jukehost.co.uk/tnI9bZaX5nQLk2ulopnJDiazeUfddrH0',
     'https://pbs.twimg.com/media/EQqv3LRW4AIVKAT?format=jpg&name=900x900',
     '2020-01-01', 0, 'PUBLIC', 45, NULL),

-- ── Yuiko Ohara ───────────────────────────────────────────────
(74, 'Tabibito no Uta (Mushoku Tensei)',
     'Anime', 218,
     'https://audio.jukehost.co.uk/rzXZOXHbgjOQrIvW5tqDNuVRMd0LSEvS',
     'https://www.lyrical-nonsense.com/wp-content/uploads/2021/01/Yuiko-Ohara-Tabibito-no-Uta.jpg',
     '2021-01-10', 0, 'PUBLIC', 46, NULL),

-- ── Konomi Suzuki ─────────────────────────────────────────────
(75, 'This Game (No Game No Life)',
     'Anime', 245,
     'https://audio.jukehost.co.uk/TKKu8G078En1S4VXhjZnFAXxKZMHlQWw',
     'https://m.media-amazon.com/images/I/718-9oa7ryL._AC_UF894,1000_QL80_.jpg',
     '2014-06-25', 0, 'PUBLIC', 47, NULL),

-- ── YOASOBI ───────────────────────────────────────────────────
(76, 'Racing into the Night',
     'Anime', 254,
     'https://audio.jukehost.co.uk/ywglCLwAqIYDBGYb8UgFqYtDIkiDZ1EC',
     'https://upload.wikimedia.org/wikipedia/en/thumb/9/93/Yoru_ni_Kakeru_cover_art.jpg/250px-Yoru_ni_Kakeru_cover_art.jpg',
     '2019-11-01', 0, 'PUBLIC', 48, NULL),

-- ── Gawr Gura ─────────────────────────────────────────────────
(77, 'Reflect',
     'Pop', 198,
     'https://audio.jukehost.co.uk/l8C8ifyE0yrVNNlOYuJRzwcWBMGyJ5IO',
     'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTo3ovYLXSCl51d43WlbrwI-kqP8oPrUUWbKw&s',
     '2021-09-08', 0, 'PUBLIC', 49, NULL),

-- ── King Gnu ──────────────────────────────────────────────────
(78, 'Specialz (Jujutsu Kaisen)',
     'Anime', 240,
     'https://audio.jukehost.co.uk/9PkWR8yEz8iSCd5Ei6hMt7cfuCwxVaTj',
     'https://audio.com/s3w/audio.com.static/audio/image/45/44/1851989779574445-1851989882255901.jpeg',
     '2023-10-01', 0, 'PUBLIC', 50, NULL),

-- ── Goose House ───────────────────────────────────────────────
(79, 'Hikaru Nara (Your Lie in April)',
     'Anime', 262,
     'https://audio.jukehost.co.uk/qwsK5W15bIQhZ8M7dybcgOTHEoS38nyo',
     'https://i1.sndcdn.com/artworks-000114147280-sajh2v-t500x500.jpg',
     '2014-10-10', 0, 'PUBLIC', 51, NULL),

-- ── Gloria Gaynor ─────────────────────────────────────────────
(80, 'I Will Survive',
     'R&B', 195,
     'https://audio.jukehost.co.uk/fsMwqnYAGQxYBPoJSbRqCCYsf9OVxRf4',
     'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRtQi2h0mmJNBnaFO2ngEccKtl_pY7j1Z890g&s',
     '1978-10-01', 0, 'PUBLIC', 52, NULL),

-- ── Field of View ─────────────────────────────────────────────
(81, 'Dan Dan Kokoro Hikareteku (Dragon Ball GT)',
     'Anime', 272,
     'https://audio.jukehost.co.uk/xoSGmk8IfrJgTwRElrLFLd4dGsXGamTO',
     'https://m.media-amazon.com/images/M/MV5BMWQyMzQwNzQtZTgxYy00OGY0LTg0MmItZjNkNTk3NjgyYmJjXkEyXkFqcGc@._V1_FMjpg_UX1000_.jpg',
     '1996-11-20', 0, 'PUBLIC', 53, NULL),

-- ── FictionJunction ───────────────────────────────────────────
(82, 'Sokyuu no Fafner (Sword Art Online)',
     'Anime', 258,
     'https://audio.jukehost.co.uk/d8dAf5PRXnjOJIk5XNVehaYaI9HpXG2q',
     'https://www.sittingonclouds.net/_image?href=https%3A%2F%2Fsittingonclouds.s3web.calibour.net%2Fprod%2Fimg%2Falbum%2F2089.png&w=500&h=500&q=low&f=webp',
     '2014-01-01', 0, 'PUBLIC', 54, NULL),

-- ── Lenny Code Fiction ────────────────────────────────────────
(83, 'Make My Story (My Hero Academia)',
     'Anime', 235,
     'https://audio.jukehost.co.uk/XIQ0THpWpPYHUWQ47diRw9ocd3OOMQum',
     'https://static.bandainamcoent.eu/high/my-hero-academia/my-hero-alls-justice/00-page-product/MHAJ-header-mobile.jpg',
     '2018-10-06', 0, 'PUBLIC', 55, NULL),

-- ── Fujii Kaze ────────────────────────────────────────────────
(84, 'Shinunoga E Wa',
     'Pop', 248,
     'https://audio.jukehost.co.uk/BY2oPFRwyjGID8ZBCHF6GJddcyU0UVNy',
     'https://upload.wikimedia.org/wikipedia/commons/4/49/Fujii_Kaze_performing_during_Best_Of_Fujii_Kaze_2020-2024_Asia_Tour_in_Axiata_Arena_Kuala_Lumpur_%28cropped%29.jpg',
     '2020-06-19', 0, 'PUBLIC', 56, NULL),

-- ── Attack on Titan OST (album_id=14) ────────────────────────
(85, 'Vogel im Kafig (Attack on Titan)',
     'Anime', 268,
     'https://audio.jukehost.co.uk/dGNKHDqhZScPzlUh84rq9qpy9iYrkL6A',
     'https://image-cdn-ak.spotifycdn.com/image/ab67706c0000da84956c55753bca75c958888364',
     '2013-06-26', 0, 'PUBLIC', 57, 14),

-- ── Lisa feat. Felix of Stray Kids ────────────────────────────
(86, 'Reawaker (Solo Leveling)',
     'Anime', 252,
     'https://audio.jukehost.co.uk/pW9QoBneW5hz1ZwRPzRuQVAgdI1vg5EI',
     'https://static.wikia.nocookie.net/hiroyuki-sawano/images/4/41/ReawakeR_Limited_Edition.jpg/revision/latest/scale-to-width/360?cb=20250322095719',
     '2025-01-11', 0, 'PUBLIC', 58, NULL),

-- ── Ikimonogakari ─────────────────────────────────────────────
(87, 'Blue Bird (Naruto)',
     'Anime', 228,
     'https://audio.jukehost.co.uk/Nhgw4isD8h2ZIU318l8cozvDqa1euCOt',
     'https://cdn-images.dzcdn.net/images/cover/7ad86630de524f2edc1988d715f02fce/0x1900-000000-80-0-0.jpg',
     '2008-03-05', 0, 'PUBLIC', 59, NULL),

-- ── Attack on Titan OST (album_id=14) ────────────────────────
(88, 'The Rumbling (Attack on Titan)',
     'Rock', 243,
     'https://audio.jukehost.co.uk/EsGPmB9mLCKPRJdGEOIyo1LrkNhVYgeZ',
     'https://i1.sndcdn.com/artworks-I5yygyDTMzx7a030-hW3UiQ-t500x500.jpg',
     '2022-01-09', 0, 'PUBLIC', 60, 14),

-- ── TK ────────────────────────────────────────────────────────
(89, 'Unravel',
     'Anime', 230,
     'https://audio.jukehost.co.uk/Uh6X7ojDs8Ywt80uxLaxxr05Lr5qXYU9',
     'https://i.pinimg.com/736x/0e/5c/23/0e5c23103e94fe2ccf4f89f517784725.jpg',
     '2014-01-04', 0, 'PUBLIC', 61, NULL);

SET FOREIGN_KEY_CHECKS = 1;
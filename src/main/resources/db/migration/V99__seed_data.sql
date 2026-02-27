-- ============================================================
-- RevPlay Music Streaming Application
-- Flyway Migration: V99__seed_data.sql
-- Author: Member 6 — Database, Testing & DevOps
-- Description: Demo seed data for development and presentation
-- Passwords are BCrypt hash of "Password@123"
-- ============================================================

SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================
-- USERS
-- 2 listeners, 3 artists (also users), 1 admin
-- ============================================================

INSERT INTO users (id, email, username, password_hash, display_name, bio, role) VALUES
(1,  'admin@revplay.com',   'admin',      '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36Q5V2xJHpXHhkEtWb2VVXK', 'RevPlay Admin',   'Platform administrator.',                            'ADMIN'),
(2,  'alice@mail.com',      'alice_music','$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36Q5V2xJHpXHhkEtWb2VVXK', 'Alice',           'Music lover. Playlist curator.',                     'LISTENER'),
(3,  'bob@mail.com',        'bob_beats',  '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36Q5V2xJHpXHhkEtWb2VVXK', 'Bob',             'Hip-hop and chill vibes only.',                      'LISTENER'),
(4,  'aria@mail.com',       'aria_artist','$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36Q5V2xJHpXHhkEtWb2VVXK', 'Aria',            'Singer-songwriter. Indie pop artist.',                'ARTIST'),
(5,  'djnova@mail.com',     'dj_nova',    '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36Q5V2xJHpXHhkEtWb2VVXK', 'DJ Nova',         'Electronic and house music producer.',               'ARTIST'),
(6,  'marco@mail.com',      'marco_jazz', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36Q5V2xJHpXHhkEtWb2VVXK', 'Marco Jazz',      'Jazz guitarist. Bringing classics back.',            'ARTIST');

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
-- ============================================================

INSERT INTO artists (id, user_id, artist_name, bio, genre, profile_picture_url, banner_image_url, instagram, twitter, youtube, spotify, website) VALUES
(1, 4, 'Aria',      'Indie pop singer-songwriter based in Austin, TX. Known for dreamy melodies and honest lyrics.',
    'Indie', '/uploads/artists/aria_pfp.jpg', '/uploads/artists/aria_banner.jpg',
    'https://instagram.com/aria_music', 'https://twitter.com/aria_music', NULL, NULL, NULL),
(2, 5, 'DJ Nova',   'Electronic music producer and live performer. Specializes in deep house and ambient techno.',
    'Electronic', '/uploads/artists/nova_pfp.jpg', '/uploads/artists/nova_banner.jpg',
    NULL, 'https://twitter.com/djnova', 'https://youtube.com/djnova', NULL, NULL),
(3, 6, 'Marco Jazz','Jazz guitarist with 15 years of studio and live performance experience.',
    'Jazz', '/uploads/artists/marco_pfp.jpg', NULL,
    NULL, NULL, NULL, NULL, 'https://marcojazz.com');

-- ============================================================
-- ALBUMS
-- ============================================================

INSERT INTO albums (id, name, description, cover_image_url, release_date, artist_id) VALUES
(1, 'Daydream',         'Debut album by Aria. Soft indie pop tracks about love and growth.',              '/uploads/albums/daydream.jpg',     '2023-03-15', 1),
(2, 'Pulse',            'High energy electronic album by DJ Nova. Floor-ready from start to finish.',    '/uploads/albums/pulse.jpg',        '2023-07-01', 2),
(3, 'Blue Hour',        'Mellow jazz sessions recorded live in one evening.',                             '/uploads/albums/blue_hour.jpg',    '2022-11-20', 3),
(4, 'Neon Nights EP',   'Four-track EP with dark synth and driving bass lines.',                          '/uploads/albums/neon_nights.jpg',  '2024-01-10', 2);

-- ============================================================
-- SONGS
-- ============================================================

INSERT INTO songs (id, title, genre, duration, audio_url, cover_image_url, release_date, play_count, visibility, artist_id, album_id) VALUES
-- Aria — Daydream
(1,  'Golden Hour',     'Indie',        214, '/audio/aria/golden_hour.mp3',      '/uploads/albums/daydream.jpg',    '2023-03-15', 18420, 'PUBLIC', 1, 1),
(2,  'Paper Walls',     'Indie',        198, '/audio/aria/paper_walls.mp3',      '/uploads/albums/daydream.jpg',    '2023-03-15',  9310, 'PUBLIC', 1, 1),
(3,  'Gravity',         'Pop',          223, '/audio/aria/gravity.mp3',          '/uploads/albums/daydream.jpg',    '2023-03-15',  7650, 'PUBLIC', 1, 1),
(4,  'Wildfire',        'Indie',        187, '/audio/aria/wildfire.mp3',         '/uploads/albums/daydream.jpg',    '2023-03-15',  5200, 'PUBLIC', 1, 1),
-- Aria — standalone single
(5,  'Haze',            'Indie',        205, '/audio/aria/haze.mp3',             '/uploads/songs/haze.jpg',         '2024-02-01',  3100, 'PUBLIC', 1, NULL),
-- DJ Nova — Pulse
(6,  'Overdrive',       'Electronic',   382, '/audio/nova/overdrive.mp3',        '/uploads/albums/pulse.jpg',       '2023-07-01', 31200, 'PUBLIC', 2, 2),
(7,  'Resonance',       'Electronic',   354, '/audio/nova/resonance.mp3',        '/uploads/albums/pulse.jpg',       '2023-07-01', 22800, 'PUBLIC', 2, 2),
(8,  'Midnight Grid',   'Electronic',   410, '/audio/nova/midnight_grid.mp3',    '/uploads/albums/pulse.jpg',       '2023-07-01', 17500, 'PUBLIC', 2, 2),
-- DJ Nova — Neon Nights EP
(9,  'Neon Drift',      'Electronic',   298, '/audio/nova/neon_drift.mp3',       '/uploads/albums/neon_nights.jpg', '2024-01-10', 12400, 'PUBLIC', 2, 4),
(10, 'Static Pulse',    'Electronic',   315, '/audio/nova/static_pulse.mp3',     '/uploads/albums/neon_nights.jpg', '2024-01-10',  8900, 'PUBLIC', 2, 4),
-- DJ Nova — unlisted WIP
(11, 'Untitled WIP',    'Electronic',   180, '/audio/nova/wip.mp3',              NULL,                              '2024-06-01',     0, 'UNLISTED', 2, NULL),
-- Marco Jazz — Blue Hour
(12, 'Blue in Green',   'Jazz',         328, '/audio/marco/blue_in_green.mp3',   '/uploads/albums/blue_hour.jpg',   '2022-11-20', 14300, 'PUBLIC', 3, 3),
(13, 'Autumn Leaves',   'Jazz',         295, '/audio/marco/autumn_leaves.mp3',   '/uploads/albums/blue_hour.jpg',   '2022-11-20', 11600, 'PUBLIC', 3, 3),
(14, 'Misty',           'Jazz',         310, '/audio/marco/misty.mp3',           '/uploads/albums/blue_hour.jpg',   '2022-11-20',  9800, 'PUBLIC', 3, 3),
(15, 'So What',         'Jazz',         342, '/audio/marco/so_what.mp3',         '/uploads/albums/blue_hour.jpg',   '2022-11-20',  8200, 'PUBLIC', 3, 3);

-- ============================================================
-- PLAYLISTS
-- ============================================================

INSERT INTO playlists (id, name, description, is_public, user_id) VALUES
(1, 'Morning Vibes',    'Chill songs to start the day right.',          TRUE,  2),
(2, 'Late Night Drive', 'Electronic bangers for the road.',             TRUE,  2),
(3, 'My Private Mix',   'Personal collection — not for sharing.',       FALSE, 2),
(4, 'Jazz Corner',      'Classic jazz and new recordings.',             TRUE,  3),
(5, 'Workout Mix',      'High BPM tracks for gym sessions.',            TRUE,  3);

-- ============================================================
-- PLAYLIST SONGS
-- ============================================================

INSERT INTO playlist_songs (playlist_id, song_id, position) VALUES
-- Morning Vibes
(1, 1,  1), (1, 2,  2), (1, 5,  3), (1, 12, 4), (1, 13, 5),
-- Late Night Drive
(2, 6,  1), (2, 7,  2), (2, 8,  3), (2, 9,  4), (2, 10, 5),
-- My Private Mix
(3, 1,  1), (3, 3,  2), (3, 6,  3),
-- Jazz Corner
(4, 12, 1), (4, 13, 2), (4, 14, 3), (4, 15, 4),
-- Workout Mix
(5, 6,  1), (5, 8,  2), (5, 9,  3), (5, 7,  4);

-- ============================================================
-- FAVORITES
-- ============================================================

INSERT INTO favorites (user_id, song_id) VALUES
-- Alice's favorites
(2, 1), (2, 2), (2, 6), (2, 12), (2, 13),
-- Bob's favorites
(3, 6), (3, 7), (3, 8), (3, 12), (3, 1);

-- ============================================================
-- PLAYLIST FOLLOWS
-- ============================================================

INSERT INTO playlist_follows (user_id, playlist_id) VALUES
(3, 1),  -- Bob follows Alice's Morning Vibes
(3, 2),  -- Bob follows Alice's Late Night Drive
(2, 4),  -- Alice follows Bob's Jazz Corner
(2, 5);  -- Alice follows Bob's Workout Mix

-- ============================================================
-- LISTENING HISTORY
-- ============================================================

INSERT INTO listening_history (user_id, song_id, played_at) VALUES
-- Alice recent history
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
-- Bob recent history
(3, 6,  NOW() - INTERVAL 15 MINUTE),
(3, 7,  NOW() - INTERVAL 30 MINUTE),
(3, 8,  NOW() - INTERVAL 50 MINUTE),
(3, 12, NOW() - INTERVAL 2  HOUR),
(3, 9,  NOW() - INTERVAL 4  HOUR),
(3, 1,  NOW() - INTERVAL 1  DAY),
(3, 13, NOW() - INTERVAL 2  DAY);

-- ============================================================
-- PLAY EVENTS (analytics — granular per-play events)
-- ============================================================

INSERT INTO play_events (song_id, user_id, played_at) VALUES
-- Golden Hour plays (song 1) — spread across days
(1, 2,   NOW() - INTERVAL 10  MINUTE),
(1, 3,   NOW() - INTERVAL 1   DAY),
(1, NULL,NOW() - INTERVAL 2   DAY),
(1, 2,   NOW() - INTERVAL 3   DAY),
(1, 3,   NOW() - INTERVAL 5   DAY),
(1, NULL,NOW() - INTERVAL 7   DAY),
-- Overdrive plays (song 6) — most played
(6, 2,   NOW() - INTERVAL 1   HOUR),
(6, 3,   NOW() - INTERVAL 15  MINUTE),
(6, NULL,NOW() - INTERVAL 2   DAY),
(6, 2,   NOW() - INTERVAL 4   DAY),
(6, 3,   NOW() - INTERVAL 6   DAY),
(6, NULL,NOW() - INTERVAL 8   DAY),
(6, 2,   NOW() - INTERVAL 10  DAY),
-- Blue in Green plays (song 12)
(12, 2,  NOW() - INTERVAL 2   HOUR),
(12, 3,  NOW() - INTERVAL 2   HOUR),
(12, NULL,NOW() - INTERVAL 3  DAY),
(12, 2,  NOW() - INTERVAL 7   DAY),
-- Resonance plays (song 7)
(7, 3,   NOW() - INTERVAL 30  MINUTE),
(7, NULL,NOW() - INTERVAL 3   DAY),
(7, 2,   NOW() - INTERVAL 6   DAY),
-- Autumn Leaves (song 13)
(13, 2,  NOW() - INTERVAL 3   HOUR),
(13, 3,  NOW() - INTERVAL 2   DAY),
(13, NULL,NOW() - INTERVAL 9  DAY);

SET FOREIGN_KEY_CHECKS = 1;
-- ============================================================
-- RevPlay Music Streaming Application
-- Flyway Migration: V99__seed_data.sql
-- Author: Team — Database, Testing & DevOps
-- Description: Demo seed data for development and presentation
--
-- ┌─────────────────────────────────────────────────────────┐
-- │                   LOGIN CREDENTIALS                     │
-- ├──────────────────────┬──────────────────────────────────┤
-- │ Username             │ Password        │ Role           │
-- ├──────────────────────┼─────────────────┼────────────────┤
-- │ admin                │ Password@123    │ ADMIN          │
-- │ piyush_admin         │ Piyush@123      │ ADMIN          │
-- │ joshua_admin         │ Joshua@123      │ ADMIN          │
-- │ ibrahim_admin        │ Ibrahim@123     │ ADMIN          │
-- │ sanjeev_admin        │ Sanjeev@123     │ ADMIN          │
-- │ vijay_admin          │ Vijay@123       │ ADMIN          │
-- │ alice_music          │ AlicePass@1     │ LISTENER       │
-- │ bob_beats            │ BobPass@123     │ LISTENER       │
-- │ (listeners 68–92)    │ Listen@123      │ LISTENER       │
-- │ aria_artist          │ AriaPass@1      │ ARTIST         │
-- │ dj_nova              │ NovaPass@1      │ ARTIST         │
-- │ marco_jazz           │ MarcoPass@1     │ ARTIST         │
-- │ (artists 8,11,12–67) │ Password@123    │ ARTIST         │
-- │ (artists 68–78)      │ Password@123    │ ARTIST         │
-- └──────────────────────┴─────────────────┴────────────────┘
--
-- NOTE: All $2b$12$ hashes encode "Password@123"
--       Listener hash ($2a$10$..T3LjP..) encodes "Listen@123"
-- Audio  : JukeHost (user-uploaded, stable hosted URLs)
-- Images : picsum.photos / Wikipedia / pinimg (stable)
-- ============================================================

SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================
-- SCHEMA ADDITIONS
-- ============================================================

ALTER TABLE playlists
    ADD COLUMN cover_image_url VARCHAR(500) NULL;

-- ============================================================
-- USERS — original 6
-- ============================================================

INSERT INTO users (id, email, username, password_hash, display_name, bio, role) VALUES
(1, 'admin@revplay.com', 'admin',        '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36Q5V2xJHpXHhkEtWb2VVXK', 'RevPlay Admin', 'Platform administrator.',              'ADMIN'),
(2, 'alice@mail.com',    'alice_music',  '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Alice',         'Music lover. Playlist curator.',       'LISTENER'),
(3, 'bob@mail.com',      'bob_beats',   '$2a$10$GDvJEJVGNaAVFQdFPlYS4OhBZ9MjNoNrRtlA28g8EGqFGPXP6WDSC', 'Bob',           'Hip-hop and chill vibes only.',        'LISTENER'),
(4, 'aria@mail.com',     'aria_artist', '$2a$10$TKh8H1.PfQx37YgCzwiKb.KjNyWgaHb9cbcoQgdIVFlYg7B9tpZ/e', 'Aria',          'Singer-songwriter. Indie pop artist.', 'ARTIST'),
(5, 'djnova@mail.com',   'dj_nova',     '$2a$10$jemhoQ3ksTqqkktHHG6ohOGQ3Bu6K24jKDVc2WcHKEZqCPJi9PBTS', 'DJ Nova',       'Electronic and house music producer.', 'ARTIST'),
(6, 'marco@mail.com',    'marco_jazz',  '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi','Marco Jazz',   'Jazz guitarist. Bringing classics back.','ARTIST');

-- ============================================================
-- NEW ADMIN USERS — Piyush, Joshua, Ibrahim, Sanjeev, Vijay
-- All use $2b$12$ hash which encodes "Password@123"
-- Individual passwords documented in header above
-- ============================================================

INSERT INTO users (id, email, username, password_hash, display_name, bio, role) VALUES
(68, 'piyush@revplay.com',  'piyush_admin',  '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'Piyush',  'Platform admin. Music is life.',       'ADMIN'),
(69, 'joshua@revplay.com',  'joshua_admin',  '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'Joshua',  'Admin and avid playlist builder.',     'ADMIN'),
(70, 'ibrahim@revplay.com', 'ibrahim_admin', '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'Ibrahim', 'Backend admin. Loves lo-fi beats.',    'ADMIN'),
(71, 'sanjeev@revplay.com', 'sanjeev_admin', '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'Sanjeev', 'Admin. Bollywood and jazz fan.',       'ADMIN'),
(72, 'vijay@revplay.com',   'vijay_admin',   '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'Vijay',   'Admin. Rock and anime OST aficionado.','ADMIN');

-- ============================================================
-- LISTENER USERS (IDs 73–92) — 20 listeners
-- Password: Listen@123
-- Hash: $2a$10$T3LjPsoBL2BL2ZxBuiMRSuFfxLJpgXzq6YZ.OdE3TGZmqXr7Qn0Cy
-- ============================================================

INSERT INTO users (id, email, username, password_hash, display_name, bio, role) VALUES
(73,  'emma@mail.com',     'emma_tunes',    '$2a$10$T3LjPsoBL2BL2ZxBuiMRSuFfxLJpgXzq6YZ.OdE3TGZmqXr7Qn0Cy', 'Emma',     'Indie and folk music lover.',           'LISTENER'),
(74,  'liam@mail.com',     'liam_listens',  '$2a$10$T3LjPsoBL2BL2ZxBuiMRSuFfxLJpgXzq6YZ.OdE3TGZmqXr7Qn0Cy', 'Liam',     'Hip-hop head. Always on the go.',       'LISTENER'),
(75,  'sophia@mail.com',   'sophia_beats',  '$2a$10$T3LjPsoBL2BL2ZxBuiMRSuFfxLJpgXzq6YZ.OdE3TGZmqXr7Qn0Cy', 'Sophia',   'Pop and R&B is my vibe.',               'LISTENER'),
(76,  'noah@mail.com',     'noah_waves',    '$2a$10$T3LjPsoBL2BL2ZxBuiMRSuFfxLJpgXzq6YZ.OdE3TGZmqXr7Qn0Cy', 'Noah',     'Electronic and chillwave fan.',         'LISTENER'),
(77,  'olivia@mail.com',   'olivia_music',  '$2a$10$T3LjPsoBL2BL2ZxBuiMRSuFfxLJpgXzq6YZ.OdE3TGZmqXr7Qn0Cy', 'Olivia',   'Anime OST obsessed.',                   'LISTENER'),
(78,  'ethan@mail.com',    'ethan_hifi',    '$2a$10$T3LjPsoBL2BL2ZxBuiMRSuFfxLJpgXzq6YZ.OdE3TGZmqXr7Qn0Cy', 'Ethan',    'Rock and alternative music only.',      'LISTENER'),
(79,  'ava@mail.com',      'ava_playlist',  '$2a$10$T3LjPsoBL2BL2ZxBuiMRSuFfxLJpgXzq6YZ.OdE3TGZmqXr7Qn0Cy', 'Ava',      'Playlist curator extraordinaire.',      'LISTENER'),
(80,  'mason@mail.com',    'mason_jams',    '$2a$10$T3LjPsoBL2BL2ZxBuiMRSuFfxLJpgXzq6YZ.OdE3TGZmqXr7Qn0Cy', 'Mason',    'Jazz and blues aficionado.',            'LISTENER'),
(81,  'isabella@mail.com', 'isabella_sings','$2a$10$T3LjPsoBL2BL2ZxBuiMRSuFfxLJpgXzq6YZ.OdE3TGZmqXr7Qn0Cy', 'Isabella', 'K-pop and J-pop fan.',                  'LISTENER'),
(82,  'james@mail.com',    'james_groove',  '$2a$10$T3LjPsoBL2BL2ZxBuiMRSuFfxLJpgXzq6YZ.OdE3TGZmqXr7Qn0Cy', 'James',    'Bollywood and pop mix.',                'LISTENER'),
(83,  'mia@mail.com',      'mia_vibes',     '$2a$10$T3LjPsoBL2BL2ZxBuiMRSuFfxLJpgXzq6YZ.OdE3TGZmqXr7Qn0Cy', 'Mia',      'Chill and lofi for studying.',          'LISTENER'),
(84,  'lucas@mail.com',    'lucas_loud',    '$2a$10$T3LjPsoBL2BL2ZxBuiMRSuFfxLJpgXzq6YZ.OdE3TGZmqXr7Qn0Cy', 'Lucas',    'Metal and prog rock fan.',              'LISTENER'),
(85,  'charlotte@mail.com','charlotte_pop', '$2a$10$T3LjPsoBL2BL2ZxBuiMRSuFfxLJpgXzq6YZ.OdE3TGZmqXr7Qn0Cy', 'Charlotte','Pop princess. Knows every lyric.',     'LISTENER'),
(86,  'henry@mail.com',    'henry_hears',   '$2a$10$T3LjPsoBL2BL2ZxBuiMRSuFfxLJpgXzq6YZ.OdE3TGZmqXr7Qn0Cy', 'Henry',    'Classical and modern fusion.',          'LISTENER'),
(87,  'amelia@mail.com',   'amelia_notes',  '$2a$10$T3LjPsoBL2BL2ZxBuiMRSuFfxLJpgXzq6YZ.OdE3TGZmqXr7Qn0Cy', 'Amelia',   'Singer who loves discovering artists.', 'LISTENER'),
(88,  'jack@mail.com',     'jack_bass',     '$2a$10$T3LjPsoBL2BL2ZxBuiMRSuFfxLJpgXzq6YZ.OdE3TGZmqXr7Qn0Cy', 'Jack',     'Bass music and EDM all day.',           'LISTENER'),
(89,  'lily@mail.com',     'lily_lyrics',   '$2a$10$T3LjPsoBL2BL2ZxBuiMRSuFfxLJpgXzq6YZ.OdE3TGZmqXr7Qn0Cy', 'Lily',     'Knows every lyric to every song.',     'LISTENER'),
(90,  'ryan@mail.com',     'ryan_radio',    '$2a$10$T3LjPsoBL2BL2ZxBuiMRSuFfxLJpgXzq6YZ.OdE3TGZmqXr7Qn0Cy', 'Ryan',     'Radio enthusiast gone digital.',        'LISTENER'),
(91,  'zoe@mail.com',      'zoe_zen',       '$2a$10$T3LjPsoBL2BL2ZxBuiMRSuFfxLJpgXzq6YZ.OdE3TGZmqXr7Qn0Cy', 'Zoe',      'Meditation and ambient soundscapes.',   'LISTENER'),
(92,  'aiden@mail.com',    'aiden_alt',     '$2a$10$T3LjPsoBL2BL2ZxBuiMRSuFfxLJpgXzq6YZ.OdE3TGZmqXr7Qn0Cy', 'Aiden',    'Alt rock and post-punk.',               'LISTENER');

-- ============================================================
-- ARTIST USERS (IDs 8, 11, 12–78) — unchanged from original + new
-- ============================================================

INSERT INTO users (id, email, username, password_hash, display_name, bio, role) VALUES
    (8,  'd4vd@mail.com',            'd4vd',              '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'D4vd',              'Indie R&B artist from Houston, TX.',                            'ARTIST'),
    (11, 'macdemarco@mail.com',      'mac_demarco',       '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'Mac DeMarco',       'Canadian indie rock and jangle pop musician.',                  'ARTIST');

INSERT INTO users (id, email, username, password_hash, display_name, bio, role) VALUES
    (12, 'billieeilish@mail.com',    'billie_eilish',     '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'Billie Eilish',     'Pop and alternative artist from Los Angeles.',                  'ARTIST'),
    (13, 'boa@mail.com',             'boa_official',      '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'Boa',               'British singer known for Duvet.',                               'ARTIST'),
    (14, 'djo@mail.com',             'djo_music',         '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'Djo',               'Indie pop artist and actor Joe Keery.',                         'ARTIST'),
    (15, 'richymitch@mail.com',      'richy_mitch',       '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'Richy Mitch & The Coal Miners','Folk and indie rock band.',                               'ARTIST'),
    (16, 'radiohead@mail.com',       'radiohead',         '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'Radiohead',         'British rock band from Abingdon.',                              'ARTIST'),
    (17, 'surfcurse@mail.com',       'surf_curse',        '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'Surf Curse',        'Indie rock duo from Reno, Nevada.',                             'ARTIST'),
    (18, 'laufey@mail.com',          'laufey',            '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'Laufey',            'Icelandic jazz-pop singer-songwriter.',                          'ARTIST'),
    (19, 'sukiwaterhouse@mail.com',  'suki_waterhouse',   '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'Suki Waterhouse',   'British singer-songwriter and actress.',                        'ARTIST'),
    (20, 'imogenheap@mail.com',      'imogen_heap',       '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'Imogen Heap',       'British singer, songwriter and record producer.',               'ARTIST'),
    (21, 'thesmiths@mail.com',       'the_smiths',        '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'The Smiths',        'British rock band from Manchester.',                            'ARTIST'),
    (22, 'walters@mail.com',         'walters_music',     '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'Walters',           'Indie pop artist.',                                             'ARTIST'),
    (23, 'tameimpala@mail.com',      'tame_impala',       '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'Tame Impala',       'Psychedelic rock project by Kevin Parker.',                     'ARTIST'),
    (24, 'tylerthecreator@mail.com', 'tyler_the_creator', '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'Tyler the Creator', 'Rapper and producer from Los Angeles.',                         'ARTIST'),
    (25, 'mgmt@mail.com',            'mgmt_official',     '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'MGMT',              'Indie pop and psychedelic rock duo.',                           'ARTIST'),
    (26, 'tvgirl@mail.com',          'tv_girl',           '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'TV Girl',           'Indie pop band from San Diego.',                                'ARTIST'),
    (27, 'ankittiwari@mail.com',     'ankit_tiwari',      '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'Ankit Tiwari',      'Indian playback singer and composer.',                          'ARTIST'),
    (28, 'faeemashraf@mail.com',     'faheem_ashraf',     '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'Faheem Ashraf',     'Pakistani singer-songwriter.',                                  'ARTIST'),
    (29, 'mananbhardwaj@mail.com',   'manan_bhardwaj',    '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'Manan Bhardwaj',    'Indian playback singer.',                                       'ARTIST'),
    (30, 'sukhwindersingh@mail.com', 'sukhwinder_singh',  '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'Sukhwinder Singh',  'Indian playback singer known for Jai Ho.',                      'ARTIST'),
    (31, 'arijitsingh@mail.com',     'arijit_singh',      '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'Arijit Singh',      'Indian playback singer and music composer.',                    'ARTIST'),
    (32, 'bpraak@mail.com',          'b_praak',           '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'B Praak',           'Indian singer and music producer.',                             'ARTIST'),
    (33, 'roopkumar@mail.com',       'roop_kumar',        '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'Roop Kumar',        'Indian playback singer.',                                       'ARTIST'),
    (34, 'sonunigam@mail.com',       'sonu_nigam',        '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'Sonu Nigam',        'Indian playback singer and composer.',                          'ARTIST'),
    (35, 'palakmucchal@mail.com',    'palak_mucchal',     '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'Palak Mucchal',     'Indian playback singer.',                                       'ARTIST'),
    (36, 'edsheeran@mail.com',       'ed_sheeran',        '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'Ed Sheeran',        'British singer-songwriter.',                                    'ARTIST'),
    (37, 'justinbieber@mail.com',    'justin_bieber',     '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'Justin Bieber',     'Canadian pop singer.',                                          'ARTIST'),
    (38, 'akon@mail.com',            'akon_official',     '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'Akon',              'Senegalese-American R&B and pop singer.',                       'ARTIST'),
    (39, 'dollyparton@mail.com',     'dolly_parton',      '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'Dolly Parton',      'American country music singer-songwriter.',                     'ARTIST'),
    (40, 'enriqueiglesias@mail.com', 'enrique_iglesias',  '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'Enrique Iglesias',  'Spanish singer known as the King of Latin Pop.',                'ARTIST'),
    (41, 'selenagomez@mail.com',     'selena_gomez',      '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'Selena Gomez',      'American singer, actress and producer.',                        'ARTIST'),
    (42, 'linkinpark@mail.com',      'linkin_park',       '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'Linkin Park',       'American rock band from Agoura Hills, California.',             'ARTIST'),
    (43, 'fictionjunction@mail.com', 'fiction_junction',  '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'Fiction Junction',  'Japanese music unit by Yuki Kajiura.',                          'ARTIST'),
    (44, 'lisa@mail.com',            'lisa_official',     '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'LiSA',              'Japanese rock singer known for Demon Slayer themes.',           'ARTIST'),
    (45, 'arkpetrol@mail.com',       'ark_petrol',        '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'Ark Petrol',        'Indie artist.',                                                 'ARTIST'),
    (46, 'bonesuk@mail.com',         'bones_uk',          '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'Bones UK',          'British rock band.',                                            'ARTIST'),
    (47, 'manwithamission@mail.com', 'man_with_a_mission','$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'Man With a Mission and Milet','Japanese rock band collaboration with singer Milet.',     'ARTIST'),
    (48, 'poormanspois@mail.com',    'poor_mans_poison',  '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'Poor Mans Poison',  'Dark folk and fantasy music artist.',                           'ARTIST'),
    (49, 'sergenova@mail.com',       'serge_nova',        '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'Serge Nova',        'Indie pop artist.',                                             'ARTIST'),
    (50, 'againstthecurrent@mail.com','against_the_current','$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu','Against the Current','American pop rock band.',                                    'ARTIST'),
    (51, 'thefatrat@mail.com',       'thefatrat_maisykay','$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'TheFatRat and Maisy Kay','Electronic music producer TheFatRat ft. vocalist Maisy Kay.','ARTIST'),
    (52, 'yuikooohara@mail.com',     'yuiko_ohara',       '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'Yuiko Ohara',       'Japanese singer known for Mushoku Tensei themes.',              'ARTIST'),
    (53, 'konomisuzuki@mail.com',    'konomi_suzuki',     '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'Konomi Suzuki',     'Japanese singer known for No Game No Life OP.',                 'ARTIST'),
    (54, 'yoasobi@mail.com',         'yoasobi',           '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'YOASOBI',           'Japanese music duo known for Racing into the Night.',           'ARTIST'),
    (55, 'gawrgura@mail.com',        'gawr_gura',         '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'Gawr Gura',         'VTuber and singer from Hololive.',                              'ARTIST'),
    (56, 'kinggnu@mail.com',         'king_gnu',          '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'King Gnu',          'Japanese art rock band known for Specialz.',                    'ARTIST'),
    (57, 'goosehouse@mail.com',      'goose_house',       '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'Goose House',       'Japanese music group known for Hikaru Nara.',                   'ARTIST'),
    (58, 'gloriagaynor@mail.com',    'gloria_gaynor',     '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'Gloria Gaynor',     'American disco and R&B singer.',                                'ARTIST'),
    (59, 'fieldofview@mail.com',     'field_of_view',     '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'Field of View',     'Japanese band known for Dragon Ball GT OP.',                    'ARTIST'),
    (60, 'fictionjunction2@mail.com','fictionjunction',   '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'FictionJunction',   'Japanese music project by Yuki Kajiura for anime themes.',      'ARTIST'),
    (61, 'lennycodefiction@mail.com','lenny_code_fiction','$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'Lenny Code Fiction','Japanese band known for My Hero Academia OP.',                  'ARTIST'),
    (62, 'fujiikaze@mail.com',       'fujii_kaze',        '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'Fujii Kaze',        'Japanese singer-songwriter and pianist.',                       'ARTIST'),
    (63, 'hiroyukisawano@mail.com',  'hiroyuki_sawano',   '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'Hiroyuki Sawano',   'Japanese composer known for Attack on Titan OST.',              'ARTIST'),
    (64, 'lisafelixstraykids@mail.com','lisa_felix_straykids','$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu','Lisa feat. Felix of Stray Kids','Collaboration between LiSA and Felix from Stray Kids.','ARTIST'),
    (65, 'ikimonogakari@mail.com',   'ikimonogakari',     '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'Ikimonogakari',     'Japanese pop band known for Blue Bird (Naruto OP).',            'ARTIST'),
    (66, 'sim@mail.com',             'sim_official',      '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'SiM',               'Japanese punk reggae band known for The Rumbling.',             'ARTIST'),
    (67, 'tk@mail.com',              'tk_official',       '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'TK',                'Japanese musician known for Unravel (Tokyo Ghoul OP).',         'ARTIST'),
    (68, 'alanwalker@mail.com',      'alan_walker',       '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'Alan Walker',       'Norwegian DJ and music producer known for Alone and Faded.',   'ARTIST'),
    (69, 'imaginedragons@mail.com',  'imagine_dragons',   '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'Imagine Dragons',   'American pop rock band from Las Vegas.',                        'ARTIST'),
    (70, 'leagueoflegends@mail.com', 'league_of_legends', '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'League of Legends', 'Official music channel for League of Legends esports themes.',  'ARTIST'),
    (71, 'keane@mail.com',           'keane_official',    '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'Keane',             'British rock band known for Somewhere Only We Know.',          'ARTIST'),
    (72, 'hyde@mail.com',            'hyde_official',     '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'Hyde',              'Japanese rock vocalist known for Demon Slayer insert songs.',  'ARTIST'),
    (73, 'finalfantasyix@mail.com',  'final_fantasy_ix',  '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'Final Fantasy IX',  'Official music from the Final Fantasy IX game soundtrack.',     'ARTIST'),
    (74, 'mikimatsubara@mail.com',   'miki_matsubara',    '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'Miki Matsubara',    'Japanese city pop singer known for Stay With Me.',              'ARTIST'),
    (75, 'melaniemartinez@mail.com', 'melanie_martinez',  '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'Melanie Martinez',  'American singer-songwriter known for her dark pop aesthetic.',  'ARTIST'),
    (76, 'akarikito@mail.com',       'akari_kito',        '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'Akari Kito',        'Japanese voice actress and singer known for Hanako-kun themes.','ARTIST'),
    (77, 'akeboshi@mail.com',        'akeboshi_music',    '$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu', 'Akeboshi',          'Japanese singer-songwriter known for Wind from Naruto.',        'ARTIST'),
    (78, 'thousandfootkrutch@mail.com','thousand_foot_krutch','$2b$12$0cfRzzj9DathC2D5Z09KOei8iqM3ertj/wpSRyHVU5qqsoVR9QNPu','Thousand Foot Krutch','Canadian Christian rock band known for Courtesy Call.',     'ARTIST');

-- ============================================================
-- GENRES
-- ============================================================

INSERT INTO genres (id, name) VALUES
(1, 'Pop'), (2, 'Hip-Hop'), (3, 'Electronic'), (4, 'Jazz'),
(5, 'Rock'), (6, 'R&B'),    (7, 'Classical'),  (8, 'Indie'),
(9, 'Folk'), (10, 'Anime');

-- ============================================================
-- ARTISTS (IDs 1–3) — original
-- ============================================================

INSERT INTO artists (id, user_id, artist_name, bio, genre, profile_picture_url, banner_image_url, instagram, twitter, youtube, spotify, website) VALUES
(1, 4, 'Aria',       'Indie pop singer-songwriter based in Austin, TX. Known for dreamy melodies and honest lyrics.', 'Indie', 'https://picsum.photos/seed/aria-pfp/300/300',  'https://picsum.photos/seed/aria-banner/1200/400',  'https://instagram.com/aria_music', 'https://twitter.com/aria_music', NULL, NULL, NULL),
(2, 5, 'DJ Nova',    'Electronic music producer and live performer. Specializes in deep house and ambient techno.',   'Electronic', 'https://picsum.photos/seed/nova-pfp/300/300',  'https://picsum.photos/seed/nova-banner/1200/400',  NULL, 'https://twitter.com/djnova', 'https://youtube.com/@djnova', NULL, NULL),
(3, 6, 'Marco Jazz', 'Jazz guitarist with 15 years of studio and live performance experience.',                        'Jazz', 'https://picsum.photos/seed/marco-pfp/300/300', NULL, NULL, NULL, NULL, NULL, 'https://marcojazz.com');

-- ============================================================
-- ARTISTS (IDs 4–61) — unchanged from original
-- ============================================================

INSERT INTO artists (id, user_id, artist_name, bio, genre, profile_picture_url, banner_image_url, instagram, twitter, youtube, spotify, website) VALUES
(4,  8,  'D4vd',                      'Indie R&B and dream pop artist from Houston, TX. Known for Feel It and Here With Me.',        'R&B',        'https://picsum.photos/seed/d4vd-pfp/300/300',          'https://picsum.photos/seed/d4vd-banner/1200/400',          'https://instagram.com/d4vd', 'https://twitter.com/d4vd_', 'https://youtube.com/@d4vd', NULL, NULL),
(5,  11, 'Mac DeMarco',               'Canadian singer-songwriter known for his lo-fi indie rock and jangle pop style.',             'Indie',      'https://picsum.photos/seed/macdemarco-pfp/300/300',    'https://picsum.photos/seed/macdemarco-banner/1200/400',    'https://instagram.com/macdemarco', 'https://twitter.com/macdemarco', 'https://youtube.com/@macdemarcofficial', NULL, NULL),
(6,  12, 'Billie Eilish',             'Pop and alternative artist from Los Angeles, CA.',                                            'Pop',        'https://picsum.photos/seed/billieeilish-pfp/300/300',  'https://picsum.photos/seed/billieeilish-banner/1200/400',  'https://instagram.com/billieeilish', 'https://twitter.com/billieeilish', 'https://youtube.com/@billieeilish', NULL, NULL),
(7,  13, 'Boa',                       'British singer known for the cult classic Duvet.',                                            'Indie',      'https://picsum.photos/seed/boa-pfp/300/300',           'https://picsum.photos/seed/boa-banner/1200/400',           NULL, NULL, NULL, NULL, NULL),
(8,  14, 'Djo',                       'Indie pop project of actor Joe Keery.',                                                       'Indie',      'https://picsum.photos/seed/djo-pfp/300/300',           'https://picsum.photos/seed/djo-banner/1200/400',           'https://instagram.com/djo', NULL, NULL, NULL, NULL),
(9,  15, 'Richy Mitch & The Coal Miners','Folk and indie rock band.',                                                                'Folk',       'https://picsum.photos/seed/richymitch-pfp/300/300',    'https://picsum.photos/seed/richymitch-banner/1200/400',    NULL, NULL, NULL, NULL, NULL),
(10, 16, 'Radiohead',                 'Pioneering British rock band from Abingdon, Oxfordshire.',                                    'Rock',       'https://picsum.photos/seed/radiohead-pfp/300/300',     'https://picsum.photos/seed/radiohead-banner/1200/400',     NULL, 'https://twitter.com/radiohead', 'https://youtube.com/@radiohead', NULL, 'https://radiohead.com'),
(11, 17, 'Surf Curse',                'Indie rock duo from Reno, Nevada.',                                                           'Indie',      'https://picsum.photos/seed/surfcurse-pfp/300/300',     'https://picsum.photos/seed/surfcurse-banner/1200/400',     'https://instagram.com/surfcurse', NULL, NULL, NULL, NULL),
(12, 18, 'Laufey',                    'Icelandic jazz-pop singer-songwriter.',                                                       'Jazz',       'https://picsum.photos/seed/laufey-pfp/300/300',        'https://picsum.photos/seed/laufey-banner/1200/400',        'https://instagram.com/laufeymusic', 'https://twitter.com/laufeymusic', 'https://youtube.com/@laufey', NULL, NULL),
(13, 19, 'Suki Waterhouse',           'British singer-songwriter and actress.',                                                      'Indie',      'https://picsum.photos/seed/sukiwaterhouse-pfp/300/300','https://picsum.photos/seed/sukiwaterhouse-banner/1200/400','https://instagram.com/sukiwaterhouse', NULL, NULL, NULL, NULL),
(14, 20, 'Imogen Heap',               'British singer, songwriter and record producer.',                                             'Electronic', 'https://picsum.photos/seed/imogenheap-pfp/300/300',    'https://picsum.photos/seed/imogenheap-banner/1200/400',    'https://instagram.com/imogenheap', 'https://twitter.com/imogenheap', 'https://youtube.com/@imogenheap', NULL, NULL),
(15, 21, 'The Smiths',                'Iconic British rock band from Manchester.',                                                   'Rock',       'https://picsum.photos/seed/thesmiths-pfp/300/300',     'https://picsum.photos/seed/thesmiths-banner/1200/400',     NULL, NULL, NULL, NULL, NULL),
(16, 22, 'Walters',                   'Indie pop artist.',                                                                           'Indie',      'https://picsum.photos/seed/walters-pfp/300/300',       'https://picsum.photos/seed/walters-banner/1200/400',       NULL, NULL, NULL, NULL, NULL),
(17, 23, 'Tame Impala',               'Psychedelic rock project by Kevin Parker from Perth, Australia.',                             'Electronic', 'https://picsum.photos/seed/tameimpala-pfp/300/300',    'https://picsum.photos/seed/tameimpala-banner/1200/400',    'https://instagram.com/tameimpala', 'https://twitter.com/tameimpala', 'https://youtube.com/@tameimpala', NULL, NULL),
(18, 24, 'Tyler the Creator',         'Rapper and producer from Los Angeles.',                                                       'Hip-Hop',    'https://picsum.photos/seed/tylerthecreator-pfp/300/300','https://picsum.photos/seed/tylerthecreator-banner/1200/400','https://instagram.com/feliciathegoat', NULL, 'https://youtube.com/@tylerthecreator', NULL, NULL),
(19, 25, 'MGMT',                      'Indie pop and psychedelic rock duo.',                                                         'Indie',      'https://picsum.photos/seed/mgmt-pfp/300/300',          'https://picsum.photos/seed/mgmt-banner/1200/400',          NULL, 'https://twitter.com/mgmt', NULL, NULL, NULL),
(20, 26, 'TV Girl',                   'Indie pop band from San Diego.',                                                              'Indie',      'https://picsum.photos/seed/tvgirl-pfp/300/300',        'https://picsum.photos/seed/tvgirl-banner/1200/400',        'https://instagram.com/tvgirl', NULL, NULL, NULL, NULL),
(21, 27, 'Ankit Tiwari',              'Indian playback singer and composer.',                                                        'Pop',        'https://picsum.photos/seed/ankittiwari-pfp/300/300',   'https://picsum.photos/seed/ankittiwari-banner/1200/400',   'https://instagram.com/ankittiwari', 'https://twitter.com/ankittiwari', NULL, NULL, NULL),
(22, 28, 'Faheem Ashraf',             'Pakistani singer-songwriter.',                                                                'Pop',        'https://picsum.photos/seed/faeemashraf-pfp/300/300',   'https://picsum.photos/seed/faeemashraf-banner/1200/400',   NULL, NULL, NULL, NULL, NULL),
(23, 29, 'Manan Bhardwaj',            'Indian playback singer.',                                                                     'Pop',        'https://picsum.photos/seed/mananbhardwaj-pfp/300/300', 'https://picsum.photos/seed/mananbhardwaj-banner/1200/400', NULL, NULL, NULL, NULL, NULL),
(24, 30, 'Sukhwinder Singh',          'Indian playback singer known for Jai Ho.',                                                    'Pop',        'https://picsum.photos/seed/sukhwindersingh-pfp/300/300','https://picsum.photos/seed/sukhwindersingh-banner/1200/400', NULL, NULL, NULL, NULL, NULL),
(25, 31, 'Arijit Singh',              'Indian playback singer and music composer.',                                                  'Pop',        'https://picsum.photos/seed/arijitsingh-pfp/300/300',   'https://picsum.photos/seed/arijitsingh-banner/1200/400',   'https://instagram.com/arijitsingh', NULL, NULL, NULL, NULL),
(26, 32, 'B Praak',                   'Indian singer and music producer.',                                                           'Pop',        'https://picsum.photos/seed/bpraak-pfp/300/300',        'https://picsum.photos/seed/bpraak-banner/1200/400',        'https://instagram.com/bpraak', NULL, NULL, NULL, NULL),
(27, 33, 'Roop Kumar',                'Indian playback singer.',                                                                     'Pop',        'https://picsum.photos/seed/roopkumar-pfp/300/300',     'https://picsum.photos/seed/roopkumar-banner/1200/400',     NULL, NULL, NULL, NULL, NULL),
(28, 34, 'Sonu Nigam',                'Indian playback singer and composer.',                                                        'Pop',        'https://picsum.photos/seed/sonunigam-pfp/300/300',     'https://picsum.photos/seed/sonunigam-banner/1200/400',     'https://instagram.com/sonunigamofficial', NULL, NULL, NULL, NULL),
(29, 35, 'Palak Mucchal',             'Indian playback singer.',                                                                     'Pop',        'https://picsum.photos/seed/palakmucchal-pfp/300/300',  'https://picsum.photos/seed/palakmucchal-banner/1200/400',  'https://instagram.com/palakmucchal', NULL, NULL, NULL, NULL),
(30, 36, 'Ed Sheeran',                'British singer-songwriter.',                                                                  'Pop',        'https://picsum.photos/seed/edsheeran-pfp/300/300',     'https://picsum.photos/seed/edsheeran-banner/1200/400',     'https://instagram.com/teddysphotos', 'https://twitter.com/edsheeran', 'https://youtube.com/@edsheeran', NULL, NULL),
(31, 37, 'Justin Bieber',             'Canadian pop singer.',                                                                        'Pop',        'https://picsum.photos/seed/justinbieber-pfp/300/300',  'https://picsum.photos/seed/justinbieber-banner/1200/400',  'https://instagram.com/justinbieber', 'https://twitter.com/justinbieber', 'https://youtube.com/@justinbieber', NULL, NULL),
(32, 38, 'Akon',                      'Senegalese-American R&B and pop singer.',                                                     'R&B',        'https://picsum.photos/seed/akon-pfp/300/300',          'https://picsum.photos/seed/akon-banner/1200/400',          NULL, 'https://twitter.com/akon', NULL, NULL, NULL),
(33, 39, 'Dolly Parton',              'American country music singer-songwriter.',                                                   'Pop',        'https://picsum.photos/seed/dollyparton-pfp/300/300',   'https://picsum.photos/seed/dollyparton-banner/1200/400',   'https://instagram.com/dollyparton', 'https://twitter.com/dollyparton', NULL, NULL, 'https://dollyparton.com'),
(34, 40, 'Enrique Iglesias',          'Spanish singer known as the King of Latin Pop.',                                              'Pop',        'https://picsum.photos/seed/enriqueiglesias-pfp/300/300','https://picsum.photos/seed/enriqueiglesias-banner/1200/400','https://instagram.com/enriqueiglesias', NULL, 'https://youtube.com/@enriqueiglesias', NULL, NULL),
(35, 41, 'Selena Gomez',              'American singer, actress and producer.',                                                      'Pop',        'https://picsum.photos/seed/selenagomez-pfp/300/300',   'https://picsum.photos/seed/selenagomez-banner/1200/400',   'https://instagram.com/selenagomez', 'https://twitter.com/selenagomez', 'https://youtube.com/@selenagomez', NULL, NULL),
(36, 42, 'Linkin Park',               'American rock band from Agoura Hills, California.',                                           'Rock',       'https://picsum.photos/seed/linkinpark-pfp/300/300',    'https://picsum.photos/seed/linkinpark-banner/1200/400',    'https://instagram.com/linkinpark', 'https://twitter.com/linkinpark', 'https://youtube.com/@linkinpark', NULL, 'https://linkinpark.com'),
(37, 43, 'Fiction Junction',          'Japanese music unit formed by composer Yuki Kajiura.',                                        'Anime',      'https://upload.wikimedia.org/wikipedia/commons/d/df/LiSA_by_Gage_Skidmore.jpg', 'https://picsum.photos/seed/fictionjunction-banner/1200/400', NULL, NULL, NULL, NULL, NULL),
(38, 44, 'LiSA',                      'Japanese rock singer known for Demon Slayer and SAO themes.',                                 'Anime',      'https://upload.wikimedia.org/wikipedia/commons/d/df/LiSA_by_Gage_Skidmore.jpg', 'https://picsum.photos/seed/lisa-banner/1200/400', 'https://instagram.com/lisaofficial', 'https://twitter.com/LiSA_OLiVE', NULL, NULL, NULL),
(39, 45, 'Ark Petrol',                'Indie artist.',                                                                               'Indie',      'https://f4.bcbits.com/img/a0206482820_10.jpg',          'https://picsum.photos/seed/arkpetrol-banner/1200/400',     NULL, NULL, NULL, NULL, NULL),
(40, 46, 'Bones UK',                  'British rock band.',                                                                          'Rock',       'https://upload.wikimedia.org/wikipedia/commons/0/0c/Bones_UK_10.21.2019_-_50425293592.jpg', 'https://picsum.photos/seed/bonesuk-banner/1200/400', 'https://instagram.com/bonesuk', NULL, NULL, NULL, NULL),
(41, 47, 'Man With a Mission and Milet','Japanese rock band collaboration with pop singer Milet.',                                   'Anime',      'https://static.wikia.nocookie.net/kimetsu-no-yaiba/images/d/d1/Kizuna_no_Kiseki_Regular_Edition_Cover.png/revision/latest?cb=20230412015040', 'https://picsum.photos/seed/manwithamission-banner/1200/400', NULL, NULL, NULL, NULL, NULL),
(42, 48, 'Poor Mans Poison',          'Dark folk and fantasy music artist.',                                                         'Folk',       'https://is1-ssl.mzstatic.com/image/thumb/Music116/v4/17/69/09/1769094c-9042-9210-d15f-c48e270d13d1/859740564316_cover.jpg/592x592bb.webp', 'https://picsum.photos/seed/poormanspois-banner/1200/400', NULL, NULL, NULL, NULL, NULL),
(43, 49, 'Serge Nova',                'Indie pop artist.',                                                                           'Indie',      'https://c.saavncdn.com/716/I-Feel-Alone-English-2021-20210402121040-500x500.jpg', 'https://picsum.photos/seed/sergenova-banner/1200/400', NULL, NULL, NULL, NULL, NULL),
(44, 50, 'Against the Current',       'American pop rock band.',                                                                     'Rock',       'https://cdn-images.dzcdn.net/images/artist/39621dce16e1afbbb3bd7198392ffd20/500x500-000000-80-0-0.jpg', 'https://picsum.photos/seed/againstthecurrent-banner/1200/400', 'https://instagram.com/atcofficial', 'https://twitter.com/atcofficial', NULL, NULL, NULL),
(45, 51, 'TheFatRat and Maisy Kay',   'Electronic music producer TheFatRat featuring vocalist Maisy Kay.',                          'Electronic', 'https://pbs.twimg.com/media/EQqv3LRW4AIVKAT?format=jpg&name=900x900', 'https://picsum.photos/seed/thefatrat-banner/1200/400', NULL, NULL, NULL, NULL, NULL),
(46, 52, 'Yuiko Ohara',               'Japanese singer known for Mushoku Tensei ED themes.',                                        'Anime',      'https://www.lyrical-nonsense.com/wp-content/uploads/2021/01/Yuiko-Ohara-Tabibito-no-Uta.jpg', 'https://picsum.photos/seed/yuikooohara-banner/1200/400', NULL, NULL, NULL, NULL, NULL),
(47, 53, 'Konomi Suzuki',             'Japanese singer known for the No Game No Life opening.',                                     'Anime',      'https://m.media-amazon.com/images/I/718-9oa7ryL._AC_UF894,1000_QL80_.jpg', 'https://picsum.photos/seed/konomisuzuki-banner/1200/400', NULL, NULL, NULL, NULL, NULL),
(48, 54, 'YOASOBI',                   'Japanese music duo known for Racing into the Night and Idol.',                               'Anime',      'https://upload.wikimedia.org/wikipedia/en/thumb/9/93/Yoru_ni_Kakeru_cover_art.jpg/250px-Yoru_ni_Kakeru_cover_art.jpg', 'https://picsum.photos/seed/yoasobi-banner/1200/400', 'https://instagram.com/yoasobi_music', NULL, NULL, NULL, NULL),
(49, 55, 'Gawr Gura',                 'VTuber and singer from Hololive.',                                                           'Pop',        'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTo3ovYLXSCl51d43WlbrwI-kqP8oPrUUWbKw&s', 'https://picsum.photos/seed/gawrgura-banner/1200/400', NULL, NULL, NULL, NULL, NULL),
(50, 56, 'King Gnu',                  'Japanese art rock band known for Specialz and Ichizu.',                                      'Rock',       'https://audio.com/s3w/audio.com.static/audio/image/45/44/1851989779574445-1851989882255901.jpeg', 'https://picsum.photos/seed/kinggnu-banner/1200/400', 'https://instagram.com/kinggnu_official', NULL, NULL, NULL, NULL),
(51, 57, 'Goose House',               'Japanese music group known for Hikaru Nara from Your Lie in April.',                        'Anime',      'https://i1.sndcdn.com/artworks-000114147280-sajh2v-t500x500.jpg', 'https://picsum.photos/seed/goosehouse-banner/1200/400', NULL, NULL, NULL, NULL, NULL),
(52, 58, 'Gloria Gaynor',             'American disco and R&B singer.',                                                             'R&B',        'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRtQi2h0mmJNBnaFO2ngEccKtl_pY7j1Z890g&s', 'https://picsum.photos/seed/gloriagaynor-banner/1200/400', NULL, NULL, NULL, NULL, NULL),
(53, 59, 'Field of View',             'Japanese band known for Dragon Ball GT opening Dan Dan Kokoro Hikareteku.',                  'Anime',      'https://m.media-amazon.com/images/M/MV5BMWQyMzQwNzQtZTgxYy00OGY0LTg0MmItZjNkNTk3NjgyYmJjXkEyXkFqcGc@._V1_FMjpg_UX1000_.jpg', 'https://picsum.photos/seed/fieldofview-banner/1200/400', NULL, NULL, NULL, NULL, NULL),
(54, 60, 'FictionJunction',           'Japanese music project by Yuki Kajiura for anime soundtracks.',                             'Anime',      'https://www.sittingonclouds.net/_image?href=https%3A%2F%2Fsittingonclouds.s3web.calibour.net%2Fprod%2Fimg%2Falbum%2F2089.png&w=500&h=500&q=low&f=webp', 'https://picsum.photos/seed/fictionjunction2-banner/1200/400', NULL, NULL, NULL, NULL, NULL),
(55, 61, 'Lenny Code Fiction',        'Japanese band known for Make My Story from My Hero Academia.',                              'Anime',      'https://static.bandainamcoent.eu/high/my-hero-academia/my-hero-alls-justice/00-page-product/MHAJ-header-mobile.jpg', 'https://picsum.photos/seed/lennycodefiction-banner/1200/400', NULL, NULL, NULL, NULL, NULL),
(56, 62, 'Fujii Kaze',                'Japanese singer-songwriter and pianist.',                                                   'Pop',        'https://upload.wikimedia.org/wikipedia/commons/4/49/Fujii_Kaze_performing_during_Best_Of_Fujii_Kaze_2020-2024_Asia_Tour_in_Axiata_Arena_Kuala_Lumpur_%28cropped%29.jpg', 'https://picsum.photos/seed/fujiikaze-banner/1200/400', 'https://instagram.com/fujiikaze', NULL, NULL, NULL, NULL),
(57, 63, 'Hiroyuki Sawano',           'Japanese composer known for Attack on Titan OST.',                                          'Anime',      'https://image-cdn-ak.spotifycdn.com/image/ab67706c0000da84956c55753bca75c958888364', 'https://picsum.photos/seed/hiroyukisawano-banner/1200/400', NULL, NULL, NULL, NULL, NULL),
(58, 64, 'Lisa feat. Felix of Stray Kids','Collaboration between LiSA and Felix from Stray Kids for Solo Leveling.',              'Anime',      'https://static.wikia.nocookie.net/hiroyuki-sawano/images/4/41/ReawakeR_Limited_Edition.jpg/revision/latest/scale-to-width/360?cb=20250322095719', 'https://picsum.photos/seed/lisafelix-banner/1200/400', NULL, NULL, NULL, NULL, NULL),
(59, 65, 'Ikimonogakari',             'Japanese pop band known for Blue Bird the Naruto opening.',                                 'Anime',      'https://cdn-images.dzcdn.net/images/cover/7ad86630de524f2edc1988d715f02fce/0x1900-000000-80-0-0.jpg', 'https://picsum.photos/seed/ikimonogakari-banner/1200/400', NULL, NULL, NULL, NULL, NULL),
(60, 66, 'SiM',                       'Japanese punk reggae band known for The Rumbling from Attack on Titan.',                    'Rock',       'https://i1.sndcdn.com/artworks-I5yygyDTMzx7a030-hW3UiQ-t500x500.jpg', 'https://picsum.photos/seed/sim-banner/1200/400', 'https://instagram.com/sim_official', NULL, NULL, NULL, NULL),
(61, 67, 'TK',                        'Japanese musician known for Unravel the Tokyo Ghoul opening.',                             'Anime',      'https://i.pinimg.com/736x/0e/5c/23/0e5c23103e94fe2ccf4f89f517784725.jpg', 'https://picsum.photos/seed/tk-banner/1200/400', NULL, NULL, NULL, NULL, NULL),
(62, 68, 'Alan Walker',              'Norwegian DJ and record producer known for Alone, Faded, and On My Way.',                'Electronic', 'https://picsum.photos/seed/alanwalker-pfp/300/300',    'https://picsum.photos/seed/alanwalker-banner/1200/400',    'https://instagram.com/alanwalkermusic', 'https://twitter.com/alanwalker97', 'https://youtube.com/@alanwalker97', NULL, NULL),
(63, 69, 'Imagine Dragons',          'American pop rock band from Las Vegas known for Warriors and Demons.',                   'Rock',       'https://picsum.photos/seed/imaginedragons-pfp/300/300', 'https://picsum.photos/seed/imaginedragons-banner/1200/400', 'https://instagram.com/imaginedragons', 'https://twitter.com/imaginedragons', 'https://youtube.com/@imaginedragons', NULL, NULL),
(64, 70, 'League of Legends',        'Official artist channel for League of Legends esports and game music.',                 'Electronic', 'https://i.scdn.co/image/ab67616d0000b273304ae5169ad8e53e261b93f2', 'https://picsum.photos/seed/lol-banner/1200/400', NULL, NULL, 'https://youtube.com/@leagueoflegends', NULL, NULL),
(65, 71, 'Keane',                    'British rock band from Battle, East Sussex, known for Somewhere Only We Know.',         'Rock',       'https://picsum.photos/seed/keane-pfp/300/300',         'https://picsum.photos/seed/keane-banner/1200/400',         NULL, 'https://twitter.com/keaneofficial', NULL, NULL, NULL),
(66, 72, 'Hyde',                     'Japanese rock vocalist, member of L''Arc-en-Ciel, known for Demon Slayer insert songs.','Rock',       'https://picsum.photos/seed/hyde-pfp/300/300',          'https://picsum.photos/seed/hyde-banner/1200/400',          'https://instagram.com/hydeofficial', NULL, NULL, NULL, NULL),
(67, 73, 'Final Fantasy IX',         'Official music from the Final Fantasy IX game, composed by Nobuo Uematsu.',             'Electronic', 'https://i.scdn.co/image/ab67616d0000b27397bc434b73f6344af6a0d07b', 'https://picsum.photos/seed/ffix-banner/1200/400', NULL, NULL, NULL, NULL, NULL),
(68, 74, 'Miki Matsubara',           'Japanese city pop singer known for the timeless Stay With Me.',                         'Pop',        'https://cdn-images.dzcdn.net/images/cover/9167ba9c16a4e5f2c39691ee27a30029/0x1900-000000-80-0-0.jpg', 'https://picsum.photos/seed/mikimatsubara-banner/1200/400', NULL, NULL, NULL, NULL, NULL),
(69, 75, 'Melanie Martinez',         'American singer-songwriter known for her whimsical dark pop style.',                    'Pop',        'https://i1.sndcdn.com/artworks-O1lwpozUA9GDtxPs-kUyaQQ-t500x500.jpg', 'https://picsum.photos/seed/melaniemartinez-banner/1200/400', 'https://instagram.com/melaniemartinez', 'https://twitter.com/melaniemusic', NULL, NULL, NULL),
(70, 76, 'Akari Kito',               'Japanese voice actress and singer known for Hanako-kun and Kimetsu no Yaiba themes.',   'Anime',      'https://i1.sndcdn.com/artworks-zBYj2TbkUw8QVITt-6xl9jQ-t500x500.jpg', 'https://picsum.photos/seed/akarikito-banner/1200/400', 'https://instagram.com/akarikito_official', NULL, NULL, NULL, NULL),
(71, 77, 'Akeboshi',                 'Japanese singer-songwriter known for Wind, the second Naruto ending theme.',            'Anime',      'https://i.scdn.co/image/ab67616d0000b27319ccf9d983d109bbb710e3b8', 'https://picsum.photos/seed/akeboshi-banner/1200/400', NULL, NULL, NULL, NULL, NULL),
(72, 78, 'Thousand Foot Krutch',     'Canadian Christian rock band known for Courtesy Call used in Sword Art Online AMVs.',   'Rock',       'https://c.saavncdn.com/161/End-Is-Where-We-Begin-English-2019-20210112055134-500x500.jpg', 'https://picsum.photos/seed/thousandfootkrutch-banner/1200/400', 'https://instagram.com/thousandfootkrutch', NULL, NULL, NULL, NULL);

-- ============================================================
-- ALBUMS (IDs 1–14) — unchanged
-- ============================================================

INSERT INTO albums (id, name, description, cover_image_url, release_date, artist_id) VALUES
(1,  'Daydream',                    'Debut album by Aria. Soft indie pop tracks about love and growth.',                            'https://picsum.photos/seed/daydream/400/400',          '2023-03-15', 1),
(2,  'Pulse',                       'High energy electronic album by DJ Nova. Floor-ready from start to finish.',                   'https://picsum.photos/seed/pulse-album/400/400',       '2023-07-01', 2),
(3,  'Blue Hour',                   'Mellow jazz sessions recorded live in one evening.',                                           'https://picsum.photos/seed/bluehour/400/400',          '2022-11-20', 3),
(4,  'Neon Nights EP',              'Four-track EP with dark synth and driving bass lines.',                                        'https://picsum.photos/seed/neonnights/400/400',        '2024-01-10', 2),
(5,  'Mac DeMarco Collection',      'A selection of Mac DeMarco''s most iconic lo-fi indie tracks.',                               'https://picsum.photos/seed/macdemarco-album/400/400',  '2014-04-01', 5),
(6,  'Arijit Singh Hits',           'A collection of Arijit Singh''s most beloved Bollywood tracks.',                              'https://picsum.photos/seed/arijitsingh-album/400/400', '2022-07-17', 25),
(7,  'Ankit Tiwari Hits',           'Soulful compositions by Ankit Tiwari from popular Bollywood films.',                          'https://picsum.photos/seed/ankittiwari-album/400/400', '2013-04-26', 21),
(8,  'Enrique Iglesias Greatest Hits','The greatest Latin pop anthems by the King of Latin Pop.',                                  'https://picsum.photos/seed/enrique-album/400/400',     '1999-07-13', 34),
(9,  'Purpose',                     'Justin Bieber''s chart-topping pop album.',                                                   'https://picsum.photos/seed/justinbieber-album/400/400','2015-11-13', 31),
(10, 'Selena Gomez Hits',           'A collection of Selena Gomez''s most popular pop tracks.',                                    'https://picsum.photos/seed/selenagomez-album/400/400', '2015-06-22', 35),
(11, 'Trouble',                     'Akon''s debut album featuring his most iconic R&B and pop tracks.',                           'https://picsum.photos/seed/akon-album/400/400',        '2004-06-08', 32),
(12, 'Linkin Park Hits',            'Essential tracks from Linkin Park''s rock discography.',                                      'https://picsum.photos/seed/linkinpark-album/400/400',  '2007-04-02', 36),
(13, 'Demon Slayer OST',            'Iconic opening and insert songs from the Demon Slayer anime series.',                         'https://static.wikia.nocookie.net/kimetsu-no-yaiba/images/d/d1/Kizuna_no_Kiseki_Regular_Edition_Cover.png/revision/latest?cb=20230412015040', '2023-04-09', 38),
(14, 'Attack on Titan OST',         'Powerful tracks from the Attack on Titan anime series.',                                      'https://image-cdn-ak.spotifycdn.com/image/ab67706c0000da84956c55753bca75c958888364', '2013-06-26', 57),
(15, 'Alan Walker Collection',       'A collection of Alan Walker''s most iconic electronic anthems.',                               'https://linkstorage.linkfire.com/medialinks/images/3b67ba2c-9ab8-48ba-8088-9f93ddfd3b59/artwork-600x315.jpg', '2018-06-18', 62),
(16, 'Imagine Dragons Hits',         'The biggest anthems from Imagine Dragons'' rock discography.',                                  'https://upload.wikimedia.org/wikipedia/en/f/fb/Song_Cover_for_%22Warriors%22_by_Imagine_Dragons.jpg', '2012-09-04', 63);

-- ============================================================
-- SONGS (IDs 16–89) — all with realistic play_counts
-- Trending Now shows songs ordered by play_count DESC + recent release_date
-- High play_counts on popular/recent songs ensures they appear in trending
-- ============================================================

INSERT INTO songs (id, title, genre, duration, audio_url, cover_image_url, release_date, play_count, visibility, artist_id, album_id) VALUES

-- ── D4vd ──────────────────────────────────────────────────────
(16, 'Feel It',
 'R&B', 180,
 'https://audio.jukehost.co.uk/M9BTPYkJiNkqMmeLPHjSWjlz4TtQLEin',
 'https://i.pinimg.com/736x/c4/0b/12/c40b12e8d09daed4b78f963c62ce2717.jpg',
 '2022-08-05', 48200, 'PUBLIC', 4, NULL),

-- ── Mac DeMarco Collection (album_id=5) ───────────────────────
(17, 'Chamber of Reflection',
 'Indie', 240,
 'https://audio.jukehost.co.uk/GDWQZobDzUgyTJzy8YfkHRNcRTRCeXHZ',
 'https://i.pinimg.com/736x/07/e3/85/07e385839c0772b6f7cbd281924f8239.jpg',
 '2014-04-01', 32100, 'PUBLIC', 5, 5),

-- ── Billie Eilish ─────────────────────────────────────────────
(18, 'CHIHIRO',
 'Pop', 252,
 'https://audio.jukehost.co.uk/GClKqu7ZECNAD4Mrr9c1bVWO4IhBEmI3',
 'https://i.pinimg.com/736x/79/90/40/799040ef1cb7df0b5d77ce612d452f42.jpg',
 '2024-05-17', 61500, 'PUBLIC', 6, NULL),

-- ── Boa ──────────────────────────────────────────────────────
(19, 'Duvet',
 'Indie', 290,
 'https://audio.jukehost.co.uk/euk6snIFCx3seFv8cdFvdkjmWp9MdWck',
 'https://i.pinimg.com/736x/f9/f9/68/f9f96822e73980062462488b119038e3.jpg',
 '1998-01-01', 28900, 'PUBLIC', 7, NULL),

-- ── Djo ──────────────────────────────────────────────────────
(20, 'End of Beginning',
 'Indie', 214,
 'https://audio.jukehost.co.uk/SMCvRHZWFqFJRY5YnG1qi0mOkuLWphyN',
 'https://i.pinimg.com/736x/65/14/11/6514119580651d4b950c6a7dac788560.jpg',
 '2022-10-21', 72400, 'PUBLIC', 8, NULL),

-- ── Richy Mitch & The Coal Miners ────────────────────────────
(21, 'Evergreen',
 'Folk', 240,
 'https://audio.jukehost.co.uk/xixnNFhjXHUdsaObdWXxKLLSJmii2PS9',
 'https://i.pinimg.com/736x/9a/e2/ad/9ae2ad72f695e4e7297e476e373f790d.jpg',
 '2021-01-01', 19300, 'PUBLIC', 9, NULL),

-- ── Radiohead ─────────────────────────────────────────────────
(22, 'Exit Music (For A Film)',
 'Rock', 244,
 'https://audio.jukehost.co.uk/qcMpDjZFNdJLfOnWQDXoa1iez3dKx5CO',
 'https://i.pinimg.com/1200x/a9/11/3e/a9113e0dea1aa406f997203b6451fd9c.jpg',
 '1997-05-21', 45800, 'PUBLIC', 10, NULL),

-- ── Mac DeMarco Collection (album_id=5) ───────────────────────
(23, 'Freaking Out the Neighborhood',
 'Indie', 183,
 'https://audio.jukehost.co.uk/gX2sbyw5cPmBidZskDjFuTIP4rhEuZ77',
 'https://i1.sndcdn.com/artworks-000029723179-knqqe8-t1080x1080.jpg',
 '2012-10-01', 27600, 'PUBLIC', 5, 5),

-- ── Surf Curse ───────────────────────────────────────────────
(24, 'Freaks',
 'Indie', 148,
 'https://audio.jukehost.co.uk/dgYufRVh6ukvd9l06Lbu0I9UOVwVYxXk',
 'https://i.pinimg.com/1200x/10/18/88/101888a76e353defade24a8d3c2df7f4.jpg',
 '2013-01-01', 38700, 'PUBLIC', 11, NULL),

-- ── Laufey ───────────────────────────────────────────────────
(25, 'From The Start',
 'Jazz', 198,
 'https://audio.jukehost.co.uk/DFYBqFFrJ3cHFKEhvsMFEabGQUpEDk2c',
 'https://i.pinimg.com/736x/c8/84/e0/c884e0284e003cf36da86731020431c2.jpg',
 '2023-02-23', 54300, 'PUBLIC', 12, NULL),

-- ── Suki Waterhouse ──────────────────────────────────────────
(26, 'Good Looking',
 'Indie', 193,
 'https://audio.jukehost.co.uk/1dMLvmU5bbR3KbhL8QirGyh15SsyJ4wM',
 'https://i.pinimg.com/736x/e4/e5/fb/e4e5fba45be231b23dc40ff8f3ba4863.jpg',
 '2023-02-17', 31200, 'PUBLIC', 13, NULL),

-- ── Imogen Heap ──────────────────────────────────────────────
(27, 'Headlock',
 'Electronic', 268,
 'https://audio.jukehost.co.uk/94nOtGIZB7sDSllwaRorSZI3WFE7soGy',
 'https://i.pinimg.com/736x/5f/83/e4/5f83e4541db528d2d637ef817cbc305a.jpg',
 '2005-09-26', 23400, 'PUBLIC', 14, NULL),

-- ── Mac DeMarco Collection (album_id=5) ───────────────────────
(28, 'Heart to Heart',
 'Indie', 219,
 'https://audio.jukehost.co.uk/TkZHw28Rlbdd00sjiIN7h71GD0aTLvaH',
 'https://i.pinimg.com/736x/3a/dd/f8/3addf83ba27cdb1be7f25a14b570e8ff.jpg',
 '2019-05-10', 21800, 'PUBLIC', 5, 5),

-- ── The Smiths ───────────────────────────────────────────────
(29, 'Heaven Knows I''m Miserable Now',
 'Rock', 209,
 'https://audio.jukehost.co.uk/Lg6MRFgZj4GcKsS7DWiSjwuYK58ylDcB',
 'https://i.scdn.co/image/ab67616d0000b273786b44c75ebf915866523f5b',
 '1984-05-01', 34100, 'PUBLIC', 15, NULL),

-- ── Walters ──────────────────────────────────────────────────
(30, 'I Love You So',
 'Indie', 176,
 'https://audio.jukehost.co.uk/rBWPAENIp2kfunuX4gYcgB0f0BDJGI7K',
 'https://i.pinimg.com/1200x/26/ea/51/26ea5121409f71f3dae0d44944abdeed.jpg',
 '2016-01-01', 42600, 'PUBLIC', 16, NULL),

-- ── Tame Impala ──────────────────────────────────────────────
(31, 'Let It Happen',
 'Electronic', 467,
 'https://audio.jukehost.co.uk/1YmXcwgdOeHCtpuEj0g8W0t73nKtH2mU',
 'https://i.pinimg.com/736x/4a/21/c7/4a21c70d53bbefbcec478f659ce96181.jpg',
 '2015-05-15', 58900, 'PUBLIC', 17, NULL),

-- ── Tyler the Creator ─────────────────────────────────────────
(32, 'Like Him',
 'Hip-Hop', 233,
 'https://audio.jukehost.co.uk/RyaFRWY3esl8VKZYAWqd8Hzk5WucJrEH',
 'https://i.pinimg.com/736x/84/06/ad/8406adb3abff889203f4734ba517696b.jpg',
 '2024-06-07', 67800, 'PUBLIC', 18, NULL),

-- ── MGMT ─────────────────────────────────────────────────────
(33, 'Little Dark Age',
 'Indie', 213,
 'https://audio.jukehost.co.uk/fd6qCCAfx0wOeII8sW2XJENohpK9PaYx',
 'https://i.pinimg.com/736x/32/43/a2/3243a28492d668c4338b1c52ff17e476.jpg',
 '2018-02-09', 81200, 'PUBLIC', 19, NULL),

-- ── TV Girl ──────────────────────────────────────────────────
(34, 'Lovers Rock',
 'Indie', 207,
 'https://audio.jukehost.co.uk/Q53axKuZHF0uskhbcp9lE1ZjWQ4oVNld',
 'https://i.pinimg.com/736x/0c/d0/01/0cd001b4e7c9c9ac8fff56a6e1aee9cb.jpg',
 '2014-01-01', 39400, 'PUBLIC', 20, NULL),

-- ── Ankit Tiwari Hits (album_id=7) ───────────────────────────
(35, 'Tum Hi Ho',
 'Pop', 268,
 'https://audio.jukehost.co.uk/lCV9Tub62pUhfzEhXpPFkRMr384O5vDL',
 'https://upload.wikimedia.org/wikipedia/en/f/f3/Aashiqui_2_%28Poster%29.jpg',
 '2013-04-26', 93100, 'PUBLIC', 21, 7),

-- ── Faheem Ashraf ─────────────────────────────────────────────
(36, 'Saiyyara',
 'Pop', 240,
 'https://audio.jukehost.co.uk/Smg3K5byxGBF1D1PyPUB9usfhfPN2r11',
 'https://upload.wikimedia.org/wikipedia/en/d/db/Saiyaara_film_poster.jpg',
 '2025-07-18', 112000, 'PUBLIC', 22, NULL),

-- ── Manan Bhardwaj ───────────────────────────────────────────
(37, 'Shiddat',
 'Pop', 235,
 'https://audio.jukehost.co.uk/jUMrCg3zvZTkfYpBjsH6K9fU0i5hwgV4',
 'https://upload.wikimedia.org/wikipedia/en/5/5e/Shiddat_poster.jpg',
 '2021-10-01', 44700, 'PUBLIC', 23, NULL),

-- ── Ankit Tiwari Hits (album_id=7) ───────────────────────────
(38, 'Sanam Teri Kasam',
 'Pop', 298,
 'https://audio.jukehost.co.uk/PJyJPSrPHeKj6YBtEP1hsV6Jvuk3EHoK',
 'https://upload.wikimedia.org/wikipedia/en/7/72/Sanam_Teri_Kasam_2016.jpeg',
 '2016-02-05', 56300, 'PUBLIC', 21, 7),

-- ── Sukhwinder Singh ─────────────────────────────────────────
(39, 'Dil Se',
 'Pop', 320,
 'https://audio.jukehost.co.uk/5exycALVStbHcup1rkZpAgWTXCf5Ryw8',
 'https://upload.wikimedia.org/wikipedia/en/7/7a/Dil_Se_poster.jpg',
 '1998-08-21', 29800, 'PUBLIC', 24, NULL),

-- ── Arijit Singh Hits (album_id=6) ───────────────────────────
(40, 'Kesariya',
 'Pop', 278,
 'https://audio.jukehost.co.uk/ka5Pm6IzaPCzlYNHwFHsFYzlAUjHHkjg',
 'https://upload.wikimedia.org/wikipedia/en/3/3c/Kesariya_song_cover.jpg',
 '2022-07-17', 88400, 'PUBLIC', 25, 6),

(41, 'Sanam Re',
 'Pop', 265,
 'https://audio.jukehost.co.uk/uv93tt3O4PHd68l6wlOwfnGWl9ys7qpb',
 'https://upload.wikimedia.org/wikipedia/en/9/9f/Sanam_Re_movie_poster.jpg',
 '2016-01-29', 52100, 'PUBLIC', 25, 6),

(42, 'Chaleya',
 'Pop', 248,
 'https://audio.jukehost.co.uk/LsMikK7XcMVgehyStIZluSd8ZeOKH27h',
 'https://upload.wikimedia.org/wikipedia/en/3/39/Jawan_film_poster.jpg',
 '2023-09-07', 74600, 'PUBLIC', 25, 6),

(43, 'Zaalima',
 'Pop', 255,
 'https://audio.jukehost.co.uk/36w33XcvOPLTxCAYu5bH0t9v3uL61swP',
 'https://upload.wikimedia.org/wikipedia/en/9/9b/Zaalima_Raees_Cover.jpg',
 '2017-01-11', 61900, 'PUBLIC', 25, 6),

(44, 'Khairiyat',
 'Pop', 262,
 'https://audio.jukehost.co.uk/rIrsLgKrglDw9ZdkremYIY0J3zLJWK0b',
 'https://upload.wikimedia.org/wikipedia/en/3/3d/Chhichhore_Poster.jpg',
 '2019-08-30', 47300, 'PUBLIC', 25, 6),

-- ── B Praak ──────────────────────────────────────────────────
(45, 'Ranjha',
 'Pop', 245,
 'https://audio.jukehost.co.uk/d921w1NlI3oNt6KjCKcVwVmj0oGN0g2C',
 'https://upload.wikimedia.org/wikipedia/en/3/32/Shershaah_soundtrack.jpg',
 '2021-07-02', 69200, 'PUBLIC', 26, NULL),

-- ── Roop Kumar ───────────────────────────────────────────────
(46, 'Tuj Me Rab Dikhta Hai',
 'Pop', 310,
 'https://audio.jukehost.co.uk/oBEVQzBWm0D97QRUlKHYOdKt1pSqbAPH',
 'https://upload.wikimedia.org/wikipedia/en/a/ab/Rab_Ne_Bana_Di_Jodi.jpg',
 '2008-10-12', 38200, 'PUBLIC', 27, NULL),

-- ── Sonu Nigam ───────────────────────────────────────────────
(47, 'Mai Agar Kahoon',
 'Pop', 285,
 'https://audio.jukehost.co.uk/k3glXifESWaSeAuX41YyUhgrLjr0TTBO',
 'https://upload.wikimedia.org/wikipedia/en/4/41/Om_Shanti_Om.jpg',
 '2007-11-09', 33600, 'PUBLIC', 28, NULL),

-- ── Palak Mucchal ────────────────────────────────────────────
(48, 'Kaun Tuje Yun Pyaar Karega',
 'Pop', 272,
 'https://audio.jukehost.co.uk/csg5To2hj4MWplzGj9gEiw7iKejTIBoU',
 'https://upload.wikimedia.org/wikipedia/en/3/33/M.S._Dhoni_-_The_Untold_Story_poster.jpg',
 '2016-09-30', 41800, 'PUBLIC', 29, NULL),

-- ── Ed Sheeran ───────────────────────────────────────────────
(49, 'Shape of You',
 'Pop', 234,
 'https://audio.jukehost.co.uk/17rIxtgIdR2d8AaQH0qg3X0ZbwCBJBNJ',
 'https://upload.wikimedia.org/wikipedia/en/b/b4/Shape_Of_You_%28Official_Single_Cover%29_by_Ed_Sheeran.png',
 '2017-01-06', 95600, 'PUBLIC', 30, NULL),

-- ── Justin Bieber — Purpose (album_id=9) ─────────────────────
(50, 'Sorry',
 'Pop', 200,
 'https://audio.jukehost.co.uk/4QbRTm7QPuRhehe5PFI6faks0cuIWfiO',
 'https://upload.wikimedia.org/wikipedia/en/d/dc/Justin_Bieber_-_Sorry_%28Official_Single_Cover%29.png',
 '2015-10-23', 78300, 'PUBLIC', 31, 9),

(51, 'What Do You Mean?',
 'Pop', 198,
 'https://audio.jukehost.co.uk/ixGwlJqnQHEgH6eIBcPk871up4AsRtDS',
 'https://upload.wikimedia.org/wikipedia/en/9/9e/JustinBieberWhatDoYouMeanCover.png',
 '2015-08-28', 65400, 'PUBLIC', 31, 9),

-- ── Akon — Trouble (album_id=11) ─────────────────────────────
(52, 'Ghetto',
 'R&B', 238,
 'https://audio.jukehost.co.uk/wy6uWR8stRtwfRTBpZCw49nh62q5FOZk',
 'https://upload.wikimedia.org/wikipedia/en/4/4b/Akon_-_Ghetto.jpg',
 '2004-01-01', 44100, 'PUBLIC', 32, 11),

(53, 'Beautiful',
 'R&B', 244,
 'https://audio.jukehost.co.uk/D91BAx5QxkIFIWLP0VDcsn6l1FzuPxin',
 'https://upload.wikimedia.org/wikipedia/en/0/02/Beautiful_%28Akon_song%29.jpg',
 '2004-01-01', 51700, 'PUBLIC', 32, 11),

-- ── Dolly Parton ─────────────────────────────────────────────
(54, 'Joshua',
 'Pop', 195,
 'https://audio.jukehost.co.uk/kBx71ieGm50tBqaWuMmQWpiFgihK0qmK',
 'https://upload.wikimedia.org/wikipedia/en/2/2b/Joshua_Parton.jpg',
 '1971-01-01', 22500, 'PUBLIC', 33, NULL),

-- ── Enrique Iglesias Greatest Hits (album_id=8) ──────────────
(55, 'Bailamos',
 'Pop', 237,
 'https://audio.jukehost.co.uk/o4XzpRyC72Iw3OktzBOFj1NRjXeD4GUI',
 'https://upload.wikimedia.org/wikipedia/en/b/bd/Bailamos_1.jpg',
 '1999-07-13', 48900, 'PUBLIC', 34, 8),

(56, 'Bailando',
 'Pop', 270,
 'https://audio.jukehost.co.uk/xh1vD5wejy8mcuGVT3s0SOEFOQ1R2rTF',
 'https://upload.wikimedia.org/wikipedia/en/c/c0/Enriquebailandocover.jpg',
 '2014-04-11', 63200, 'PUBLIC', 34, 8),

(57, 'Heartbeat',
 'Pop', 222,
 'https://audio.jukehost.co.uk/Zli6Qfq1rXH2p6cJQ3QD3IZj1vf3GRZq',
 'https://upload.wikimedia.org/wikipedia/en/1/17/Enrique_Iglesias_%26_Nicole_Scherzinger_-_Heartbeat.jpg',
 '2010-09-13', 37800, 'PUBLIC', 34, 8),

(58, 'Rhythm Divine',
 'Pop', 248,
 'https://upload.wikimedia.org/wikipedia/en/c/c8/Rhythm_Divine_1.jpg',
 'https://audio.jukehost.co.uk/mBYjz1jD06sxkC4e6MwasCjkkUGcMHEr',
 '1999-01-01', 29400, 'PUBLIC', 34, 8),

(59, 'Takin Back My Love',
 'Pop', 215,
 'https://audio.jukehost.co.uk/zW7xxuPQxfWADuzvzmz5QgSGrwsvkRZJ',
 'https://upload.wikimedia.org/wikipedia/en/5/56/Takinbackmylove_.JPEG',
 '2009-01-13', 33100, 'PUBLIC', 34, 8),

-- ── Selena Gomez Hits (album_id=10) ──────────────────────────
(60, 'Good For You',
 'Pop', 193,
 'https://audio.jukehost.co.uk/UsWWLeh1gAvBZ3v0o72kJTBVTAbDNc04',
 'https://upload.wikimedia.org/wikipedia/en/1/1e/Selena_Gomez_-_Good_For_You_%28Official_Single_Cover%29.png',
 '2015-06-22', 57600, 'PUBLIC', 35, 10),

(61, 'Hands to Myself',
 'Pop', 194,
 'https://audio.jukehost.co.uk/6PDNLHnFmnnGuD3BI5OEbEG2zlAfj3Po',
 'https://upload.wikimedia.org/wikipedia/en/2/2c/Selena_Gomez_-_Hands_to_Myself_single_cover.png',
 '2015-11-23', 43200, 'PUBLIC', 35, 10),

(62, 'A Year Without Rain',
 'Pop', 196,
 'https://audio.jukehost.co.uk/tXiXVTPVDXhG3HHhu9cGFJBZShzXIfIY',
 'https://upload.wikimedia.org/wikipedia/en/1/12/Selena_Gomez_%26_the_Scene_-_A_Year_Without_Rain_%28single_cover%29.jpg',
 '2010-09-17', 35900, 'PUBLIC', 35, 10),

-- ── Linkin Park Hits (album_id=12) ───────────────────────────
(63, 'The Catalyst',
 'Rock', 265,
 'https://audio.jukehost.co.uk/aZS5ucAKGKsSv03ptpGjjgNlyc0pOz4o',
 'https://upload.wikimedia.org/wikipedia/en/c/cd/The-catalyst-single-cover-500x500.png',
 '2010-08-02', 54800, 'PUBLIC', 36, 12),

(64, 'What I''ve Done',
 'Rock', 226,
 'https://audio.jukehost.co.uk/ssXEfsyY4tMwR7CgjCQqYSQDfMMUWO4c',
 'https://upload.wikimedia.org/wikipedia/en/9/94/WhatI%27veDoneCover.jpg',
 '2007-04-02', 71300, 'PUBLIC', 36, 12),

-- ── Demon Slayer OST (album_id=13) ───────────────────────────
(65, 'Age of Darkness (Demon Slayer)',
 'Anime', 240,
 'https://audio.jukehost.co.uk/WfD4VJ6QPnuIiOEDsvu4sFEaqVxBdn7f',
 'https://upload.wikimedia.org/wikipedia/commons/d/df/LiSA_by_Gage_Skidmore.jpg',
 '2023-01-01', 38600, 'PUBLIC', 37, 13),

(66, 'Akeboshi (Demon Slayer)',
 'Anime', 220,
 'https://audio.jukehost.co.uk/qXuM8BxoQh7XOvMGtMQTXBgenpLLPoJv',
 'https://upload.wikimedia.org/wikipedia/commons/d/df/LiSA_by_Gage_Skidmore.jpg',
 '2021-10-10', 62400, 'PUBLIC', 38, 13),

-- ── Ark Petrol ────────────────────────────────────────────────
(67, 'Let Go',
 'Indie', 210,
 'https://audio.jukehost.co.uk/JBf1oYsBAf2uSIMUe15VkM6mmjQRBjOt',
 'https://f4.bcbits.com/img/a0206482820_10.jpg',
 '2020-01-01', 18700, 'PUBLIC', 39, NULL),

-- ── Bones UK ──────────────────────────────────────────────────
(68, 'Beautiful is Boring',
 'Rock', 195,
 'https://audio.jukehost.co.uk/EbFPRuyMrCFi6RUqqxwsnc84CJb7BOqA',
 'https://upload.wikimedia.org/wikipedia/commons/0/0c/Bones_UK_10.21.2019_-_50425293592.jpg',
 '2019-01-01', 26300, 'PUBLIC', 40, NULL),

-- ── Demon Slayer OST (album_id=13) ───────────────────────────
(69, 'Kizuna no Kiseki (Demon Slayer)',
 'Anime', 255,
 'https://audio.jukehost.co.uk/0AJsVRGFsZNMKrMbmdVQkawKMhjyWHvt',
 'https://static.wikia.nocookie.net/kimetsu-no-yaiba/images/d/d1/Kizuna_no_Kiseki_Regular_Edition_Cover.png/revision/latest?cb=20230412015040',
 '2023-04-09', 79200, 'PUBLIC', 41, 13),

-- ── Poor Mans Poison ──────────────────────────────────────────
(70, 'Feed The Machine',
 'Folk', 248,
 'https://audio.jukehost.co.uk/PQrT0QNLFGV4uZKbagJ1TZwAZd1yZjHz',
 'https://is1-ssl.mzstatic.com/image/thumb/Music116/v4/17/69/09/1769094c-9042-9210-d15f-c48e270d13d1/859740564316_cover.jpg/592x592bb.webp',
 '2019-01-01', 31500, 'PUBLIC', 42, NULL),

-- ── Serge Nova ────────────────────────────────────────────────
(71, 'I Feel Alone',
 'Indie', 185,
 'https://audio.jukehost.co.uk/0eigLzyG7wPaTFuOHHKsjnGx1yLCzZgU',
 'https://c.saavncdn.com/716/I-Feel-Alone-English-2021-20210402121040-500x500.jpg',
 '2021-04-02', 22900, 'PUBLIC', 43, NULL),

-- ── Against the Current ───────────────────────────────────────
(72, 'Legends Never Die',
 'Rock', 213,
 'https://audio.jukehost.co.uk/6uEnA1qaKu7Iczxl5LHFUWqDbgUVlMT1',
 'https://cdn-images.dzcdn.net/images/artist/39621dce16e1afbbb3bd7198392ffd20/500x500-000000-80-0-0.jpg',
 '2017-01-01', 49700, 'PUBLIC', 44, NULL),

-- ── TheFatRat and Maisy Kay ───────────────────────────────────
(73, 'The Storm',
 'Electronic', 230,
 'https://audio.jukehost.co.uk/tnI9bZaX5nQLk2ulopnJDiazeUfddrH0',
 'https://pbs.twimg.com/media/EQqv3LRW4AIVKAT?format=jpg&name=900x900',
 '2020-01-01', 35800, 'PUBLIC', 45, NULL),

-- ── Yuiko Ohara ───────────────────────────────────────────────
(74, 'Tabibito no Uta (Mushoku Tensei)',
 'Anime', 218,
 'https://audio.jukehost.co.uk/rzXZOXHbgjOQrIvW5tqDNuVRMd0LSEvS',
 'https://www.lyrical-nonsense.com/wp-content/uploads/2021/01/Yuiko-Ohara-Tabibito-no-Uta.jpg',
 '2021-01-10', 41200, 'PUBLIC', 46, NULL),

-- ── Konomi Suzuki ─────────────────────────────────────────────
(75, 'This Game (No Game No Life)',
 'Anime', 245,
 'https://audio.jukehost.co.uk/TKKu8G078En1S4VXhjZnFAXxKZMHlQWw',
 'https://m.media-amazon.com/images/I/718-9oa7ryL._AC_UF894,1000_QL80_.jpg',
 '2014-06-25', 53800, 'PUBLIC', 47, NULL),

-- ── YOASOBI ───────────────────────────────────────────────────
(76, 'Racing into the Night',
 'Anime', 254,
 'https://audio.jukehost.co.uk/ywglCLwAqIYDBGYb8UgFqYtDIkiDZ1EC',
 'https://upload.wikimedia.org/wikipedia/en/thumb/9/93/Yoru_ni_Kakeru_cover_art.jpg/250px-Yoru_ni_Kakeru_cover_art.jpg',
 '2019-11-01', 87600, 'PUBLIC', 48, NULL),

-- ── Gawr Gura ─────────────────────────────────────────────────
(77, 'Reflect',
 'Pop', 198,
 'https://audio.jukehost.co.uk/l8C8ifyE0yrVNNlOYuJRzwcWBMGyJ5IO',
 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTo3ovYLXSCl51d43WlbrwI-kqP8oPrUUWbKw&s',
 '2021-09-08', 28400, 'PUBLIC', 49, NULL),

-- ── King Gnu ──────────────────────────────────────────────────
(78, 'Specialz (Jujutsu Kaisen)',
 'Anime', 240,
 'https://audio.jukehost.co.uk/9PkWR8yEz8iSCd5Ei6hMt7cfuCwxVaTj',
 'https://audio.com/s3w/audio.com.static/audio/image/45/44/1851989779574445-1851989882255901.jpeg',
 '2023-10-01', 91400, 'PUBLIC', 50, NULL),

-- ── Goose House ───────────────────────────────────────────────
(79, 'Hikaru Nara (Your Lie in April)',
 'Anime', 262,
 'https://audio.jukehost.co.uk/qwsK5W15bIQhZ8M7dybcgOTHEoS38nyo',
 'https://i1.sndcdn.com/artworks-000114147280-sajh2v-t500x500.jpg',
 '2014-10-10', 64700, 'PUBLIC', 51, NULL),

-- ── Gloria Gaynor ─────────────────────────────────────────────
(80, 'I Will Survive',
 'R&B', 195,
 'https://audio.jukehost.co.uk/fsMwqnYAGQxYBPoJSbRqCCYsf9OVxRf4',
 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRtQi2h0mmJNBnaFO2ngEccKtl_pY7j1Z890g&s',
 '1978-10-01', 58300, 'PUBLIC', 52, NULL),

-- ── Field of View ─────────────────────────────────────────────
(81, 'Dan Dan Kokoro Hikareteku (Dragon Ball GT)',
 'Anime', 272,
 'https://audio.jukehost.co.uk/xoSGmk8IfrJgTwRElrLFLd4dGsXGamTO',
 'https://m.media-amazon.com/images/M/MV5BMWQyMzQwNzQtZTgxYy00OGY0LTg0MmItZjNkNTk3NjgyYmJjXkEyXkFqcGc@._V1_FMjpg_UX1000_.jpg',
 '1996-11-20', 47100, 'PUBLIC', 53, NULL),

-- ── FictionJunction ───────────────────────────────────────────
(82, 'Sokyuu no Fafner (Sword Art Online)',
 'Anime', 258,
 'https://audio.jukehost.co.uk/d8dAf5PRXnjOJIk5XNVehaYaI9HpXG2q',
 'https://www.sittingonclouds.net/_image?href=https%3A%2F%2Fsittingonclouds.s3web.calibour.net%2Fprod%2Fimg%2Falbum%2F2089.png&w=500&h=500&q=low&f=webp',
 '2014-01-01', 36200, 'PUBLIC', 54, NULL),

-- ── Lenny Code Fiction ────────────────────────────────────────
(83, 'Make My Story (My Hero Academia)',
 'Anime', 235,
 'https://audio.jukehost.co.uk/XIQ0THpWpPYHUWQ47diRw9ocd3OOMQum',
 'https://static.bandainamcoent.eu/high/my-hero-academia/my-hero-alls-justice/00-page-product/MHAJ-header-mobile.jpg',
 '2018-10-06', 52600, 'PUBLIC', 55, NULL),

-- ── Fujii Kaze ────────────────────────────────────────────────
(84, 'Shinunoga E Wa',
 'Pop', 248,
 'https://audio.jukehost.co.uk/BY2oPFRwyjGID8ZBCHF6GJddcyU0UVNy',
 'https://upload.wikimedia.org/wikipedia/commons/4/49/Fujii_Kaze_performing_during_Best_Of_Fujii_Kaze_2020-2024_Asia_Tour_in_Axiata_Arena_Kuala_Lumpur_%28cropped%29.jpg',
 '2020-06-19', 43900, 'PUBLIC', 56, NULL),

-- ── Attack on Titan OST (album_id=14) ────────────────────────
(85, 'Vogel im Kafig (Attack on Titan)',
 'Anime', 268,
 'https://audio.jukehost.co.uk/dGNKHDqhZScPzlUh84rq9qpy9iYrkL6A',
 'https://image-cdn-ak.spotifycdn.com/image/ab67706c0000da84956c55753bca75c958888364',
 '2013-06-26', 67800, 'PUBLIC', 57, 14),

-- ── Lisa feat. Felix of Stray Kids ────────────────────────────
(86, 'Reawaker (Solo Leveling)',
 'Anime', 252,
 'https://audio.jukehost.co.uk/pW9QoBneW5hz1ZwRPzRuQVAgdI1vg5EI',
 'https://static.wikia.nocookie.net/hiroyuki-sawano/images/4/41/ReawakeR_Limited_Edition.jpg/revision/latest/scale-to-width/360?cb=20250322095719',
 '2025-01-11', 104500, 'PUBLIC', 58, NULL),

-- ── Ikimonogakari ─────────────────────────────────────────────
(87, 'Blue Bird (Naruto)',
 'Anime', 228,
 'https://audio.jukehost.co.uk/Nhgw4isD8h2ZIU318l8cozvDqa1euCOt',
 'https://cdn-images.dzcdn.net/images/cover/7ad86630de524f2edc1988d715f02fce/0x1900-000000-80-0-0.jpg',
 '2008-03-05', 73400, 'PUBLIC', 59, NULL),

-- ── Attack on Titan OST (album_id=14) ────────────────────────
(88, 'The Rumbling (Attack on Titan)',
 'Rock', 243,
 'https://audio.jukehost.co.uk/EsGPmB9mLCKPRJdGEOIyo1LrkNhVYgeZ',
 'https://i1.sndcdn.com/artworks-I5yygyDTMzx7a030-hW3UiQ-t500x500.jpg',
 '2022-01-09', 98700, 'PUBLIC', 60, 14),

-- ── TK ────────────────────────────────────────────────────────
(89, 'Unravel',
 'Anime', 230,
 'https://audio.jukehost.co.uk/Uh6X7ojDs8Ywt80uxLaxxr05Lr5qXYU9',
 'https://i.pinimg.com/736x/0e/5c/23/0e5c23103e94fe2ccf4f89f517784725.jpg',
 '2014-01-04', 86100, 'PUBLIC', 61, NULL);

-- ── Alan Walker Collection (album_id=15) ─────────────────────
(90, 'Alone',
 'Electronic', 161,
 'https://audio.jukehost.co.uk/rn6ZMEU0eUvhHS1qUx6R5C5GuDB9kjN3',
 'https://linkstorage.linkfire.com/medialinks/images/3b67ba2c-9ab8-48ba-8088-9f93ddfd3b59/artwork-600x315.jpg',
 '2016-12-07', 94200, 'PUBLIC', 62, 15),

(91, 'On My Way',
 'Electronic', 195,
 'https://audio.jukehost.co.uk/xoVwioQlFMkZ71HTtatN6JzZUOnYVgZZ',
 'https://m.media-amazon.com/images/M/MV5BNGMzMWMyODEtN2QxYi00MzEyLWFlYzgtZWUyZTk2NjRhOTJkXkEyXkFqcGc@._V1_.jpg',
 '2019-02-13', 81600, 'PUBLIC', 62, 15),

(92, 'Darkside',
 'Electronic', 206,
 'https://audio.jukehost.co.uk/46JM74Qudb7WNeu32oh2ExxY3azsc7n9',
 'https://upload.wikimedia.org/wikipedia/en/b/ba/Darkside_by_Alan_Walker.png',
 '2018-06-18', 76300, 'PUBLIC', 62, 15),

-- ── Imagine Dragons Hits (album_id=16) ───────────────────────
(93, 'Warriors',
 'Rock', 170,
 'https://audio.jukehost.co.uk/hHmllurpN2wHsGTSI0McRgbkqXuHl47o',
 'https://upload.wikimedia.org/wikipedia/en/f/fb/Song_Cover_for_%22Warriors%22_by_Imagine_Dragons.jpg',
 '2014-09-18', 102400, 'PUBLIC', 63, 16),

(94, 'Demons',
 'Rock', 176,
 'https://audio.jukehost.co.uk/EUimTgifxK1cBt17H4z983doRWZrOdu3',
 'https://images.genius.com/5cce297c6ed3af397d6ae2a355d91eae.1000x1000x1.png',
 '2013-01-01', 88700, 'PUBLIC', 63, 16),

-- ── League of Legends ─────────────────────────────────────────
(95, 'Rise',
 'Electronic', 213,
 'https://audio.jukehost.co.uk/cbwJ1X2uZv8gHYWQlyCRKKqphvOMqXnP',
 'https://i.scdn.co/image/ab67616d0000b273304ae5169ad8e53e261b93f2',
 '2018-09-27', 79500, 'PUBLIC', 64, NULL),

-- ── Keane ─────────────────────────────────────────────────────
(96, 'Somewhere Only We Know',
 'Rock', 233,
 'https://audio.jukehost.co.uk/TgLXRB2bLKdJGZJKWq9YGqF63GtklqRG',
 'https://i1.sndcdn.com/artworks-wc2LkH1WnCUahg0H-0mjKvQ-t500x500.jpg',
 '2004-03-22', 65800, 'PUBLIC', 65, NULL),

-- ── Demon Slayer OST (album_id=13) ───────────────────────────
(97, 'Tokoshie (Demon Slayer)',
 'Anime', 247,
 'https://audio.jukehost.co.uk/DNW5RQxviEenfQZhl7tzs8bQwTsZ2v44',
 'https://is1-ssl.mzstatic.com/image/thumb/Music221/v4/26/d3/0a/26d30a76-f62d-3562-cbf8-6b38cc63e6c9/4547366684575.jpg/600x600cc.webp',
 '2023-09-10', 57300, 'PUBLIC', 66, 13),

-- ── Final Fantasy IX ──────────────────────────────────────────
(98, 'Melodies of Life',
 'Electronic', 278,
 'https://audio.jukehost.co.uk/vUHZqQeFUQYLxNmsnFy3Xr9azB6sT4kC',
 'https://i.scdn.co/image/ab67616d0000b27397bc434b73f6344af6a0d07b',
 '2000-07-07', 44600, 'PUBLIC', 67, NULL),

-- ── Miki Matsubara ────────────────────────────────────────────
(99, 'Stay With Me',
 'Pop', 268,
 'https://audio.jukehost.co.uk/KFbLBBlr3aofKz3eTgxnt5QkqBL1PuLd',
 'https://cdn-images.dzcdn.net/images/cover/9167ba9c16a4e5f2c39691ee27a30029/0x1900-000000-80-0-0.jpg',
 '1980-04-01', 53100, 'PUBLIC', 68, NULL),

-- ── Melanie Martinez ──────────────────────────────────────────
(100, 'Play Date',
 'Pop', 202,
 'https://audio.jukehost.co.uk/vMlTjkQ5hiL25gzM50jxl4m5eLtkx1vN',
 'https://i1.sndcdn.com/artworks-O1lwpozUA9GDtxPs-kUyaQQ-t500x500.jpg',
 '2015-08-14', 71200, 'PUBLIC', 69, NULL),

-- ── Akari Kito ────────────────────────────────────────────────
(101, 'Tiny Light (Hanako Kun)',
 'Anime', 218,
 'https://audio.jukehost.co.uk/wTSjVKcdZDTeTF4K7LJBdQFrDHyhvSJx',
 'https://i1.sndcdn.com/artworks-zBYj2TbkUw8QVITt-6xl9jQ-t500x500.jpg',
 '2020-01-09', 48900, 'PUBLIC', 70, NULL),

-- ── Man With a Mission (artist_id=41) ────────────────────────
(102, 'Merry Go Round (My Hero Academia)',
 'Anime', 215,
 'https://audio.jukehost.co.uk/PzGAHEfw1zjhzVRYQT8372c0Cx5URHxz',
 'https://static.wikia.nocookie.net/bokunoheroacademia/images/b/b9/Merry-Go-Round_CD_Cover.png/revision/latest?cb=20210825122144',
 '2021-10-02', 61800, 'PUBLIC', 41, NULL),

-- ── Akeboshi ─────────────────────────────────────────────────
(103, 'Wind (Naruto)',
 'Anime', 264,
 'https://audio.jukehost.co.uk/lp6wxQhQM2QWu2h0EHbwlL5ZkmiYpaVY',
 'https://i.scdn.co/image/ab67616d0000b27319ccf9d983d109bbb710e3b8',
 '2003-10-25', 55700, 'PUBLIC', 71, NULL),

-- ── Thousand Foot Krutch ──────────────────────────────────────
(104, 'Courtesy Call',
 'Rock', 207,
 'https://audio.jukehost.co.uk/kfHbEOZyWzcTr3G5U7TYZSaecICOyEUO',
 'https://c.saavncdn.com/161/End-Is-Where-We-Begin-English-2019-20210112055134-500x500.jpg',
 '2011-01-01', 42300, 'PUBLIC', 72, NULL);
-- All reference songs 16–104 (no songs 1–15)
-- ============================================================

INSERT INTO playlists (id, name, description, is_public, user_id, cover_image_url) VALUES
(1,  'Indie Feels',          'The best indie tracks for every mood.',                    TRUE,  2,   'https://i.pinimg.com/736x/65/14/11/6514119580651d4b950c6a7dac788560.jpg'),
(2,  'Late Night Drive',     'Electronic and chill for driving after dark.',             TRUE,  2,   'https://i.pinimg.com/736x/4a/21/c7/4a21c70d53bbefbcec478f659ce96181.jpg'),
(3,  'My Private Stash',     'Songs for my ears only.',                                  FALSE, 2,   'https://i.pinimg.com/736x/c8/84/e0/c884e0284e003cf36da86731020431c2.jpg'),
(4,  'Bollywood Hits',       'Classic and new Bollywood bangers.',                       TRUE,  3,   'https://upload.wikimedia.org/wikipedia/en/3/3c/Kesariya_song_cover.jpg'),
(5,  'Anime Anthems',        'Opening and ending themes from the best anime.',           TRUE,  3,   'https://i.pinimg.com/736x/0e/5c/23/0e5c23103e94fe2ccf4f89f517784725.jpg'),
(6,  'Rock & Alternative',   'Hard-hitting rock tracks and alt bangers.',                TRUE,  73,  'https://upload.wikimedia.org/wikipedia/en/c/cd/The-catalyst-single-cover-500x500.png'),
(7,  'Pop World',            'Global pop hits from every era.',                         TRUE,  74,  'https://upload.wikimedia.org/wikipedia/en/b/b4/Shape_Of_You_%28Official_Single_Cover%29_by_Ed_Sheeran.png'),
(8,  'Chill Study Mix',      'Lo-fi and soft tunes for focus.',                         TRUE,  75,  'https://i.pinimg.com/736x/07/e3/85/07e385839c0772b6f7cbd281924f8239.jpg'),
(9,  'Midnight Jazz',        'Late-night jazz and soul.',                               FALSE, 76,  'https://i.pinimg.com/736x/c8/84/e0/c884e0284e003cf36da86731020431c2.jpg'),
(10, 'Electronic Overload',  'Trance, house and synth — full volume.',                  TRUE,  77,  'https://pbs.twimg.com/media/EQqv3LRW4AIVKAT?format=jpg&name=900x900'),
(11, 'Folk & Acoustic',      'Stories told with acoustic guitars.',                     TRUE,  78,  'https://i.pinimg.com/736x/9a/e2/ad/9ae2ad72f695e4e7297e476e373f790d.jpg'),
(12, 'Top Charts',           'The most-played songs on RevPlay right now.',             TRUE,  79,  'https://i.pinimg.com/736x/32/43/a2/3243a28492d668c4338b1c52ff17e476.jpg'),
(13, 'Throwback Thursday',   'Classics that never get old.',                            TRUE,  80,  'https://i.pinimg.com/1200x/a9/11/3e/a9113e0dea1aa406f997203b6451fd9c.jpg'),
(14, 'Workout Beast Mode',   'High BPM tracks to push your limits.',                   TRUE,  81,  'https://i1.sndcdn.com/artworks-I5yygyDTMzx7a030-hW3UiQ-t500x500.jpg'),
(15, 'Weekend Vibes',        'Mixed genres for a perfect weekend soundtrack.',          TRUE,  82,  'https://upload.wikimedia.org/wikipedia/en/thumb/9/93/Yoru_ni_Kakeru_cover_art.jpg/250px-Yoru_ni_Kakeru_cover_art.jpg');

-- ============================================================
-- PLAYLIST SONGS — using songs 16–89 only
-- ============================================================

INSERT INTO playlist_songs (playlist_id, song_id, position) VALUES
-- Indie Feels (playlist 1)
(1, 20, 1), (1, 24, 2), (1, 19, 3), (1, 30, 4), (1, 34, 5), (1, 33, 6),
-- Late Night Drive (playlist 2)
(2, 31, 1), (2, 27, 2), (2, 73, 3), (2, 20, 4), (2, 18, 5),
-- My Private Stash (playlist 3)
(3, 25, 1), (3, 16, 2), (3, 89, 3), (3, 33, 4),
-- Bollywood Hits (playlist 4)
(4, 35, 1), (4, 40, 2), (4, 42, 3), (4, 45, 4), (4, 36, 5), (4, 38, 6), (4, 43, 7),
-- Anime Anthems (playlist 5)
(5, 89, 1), (5, 76, 2), (5, 78, 3), (5, 87, 4), (5, 69, 5), (5, 79, 6), (5, 88, 7),
-- Rock & Alternative (playlist 6)
(6, 22, 1), (6, 63, 2), (6, 64, 3), (6, 68, 4), (6, 72, 5), (6, 88, 6),
-- Pop World (playlist 7)
(7, 49, 1), (7, 50, 2), (7, 18, 3), (7, 56, 4), (7, 60, 5), (7, 51, 6),
-- Chill Study Mix (playlist 8)
(8, 17, 1), (8, 71, 2), (8, 26, 3), (8, 25, 4), (8, 21, 5),
-- Midnight Jazz (playlist 9)
(9, 25, 1), (9, 47, 2), (9, 27, 3), (9, 80, 4),
-- Electronic Overload (playlist 10)
(10, 31, 1), (10, 73, 2), (10, 27, 3), (10, 20, 4),
-- Folk & Acoustic (playlist 11)
(11, 21, 1), (11, 70, 2), (11, 67, 3), (11, 28, 4),
-- Top Charts (playlist 12)
(12, 36, 1), (12, 33, 2), (12, 78, 3), (12, 32, 4), (12, 35, 5), (12, 88, 6), (12, 86, 7),
-- Throwback Thursday (playlist 13)
(13, 22, 1), (13, 29, 2), (13, 54, 3), (13, 55, 4), (13, 80, 5), (13, 81, 6),
-- Workout Beast Mode (playlist 14)
(14, 32, 1), (14, 64, 2), (14, 63, 3), (14, 88, 4), (14, 72, 5), (14, 68, 6),
-- Weekend Vibes (playlist 15)
(15, 18, 1), (15, 20, 2), (15, 76, 3), (15, 49, 4), (15, 33, 5), (15, 87, 6), (15, 42, 7);

-- ============================================================
-- FAVORITES — listeners favoriting songs 16–89
-- ============================================================

INSERT INTO favorites (user_id, song_id) VALUES
-- Alice
(2, 33), (2, 20), (2, 25), (2, 30), (2, 76),
-- Bob
(3, 35), (3, 40), (3, 88), (3, 78), (3, 69),
-- Emma
(73, 20), (73, 24), (73, 19), (73, 30),
-- Liam
(74, 32), (74, 64), (74, 88), (74, 87),
-- Sophia
(75, 49), (75, 50), (75, 56), (75, 18),
-- Noah
(76, 31), (76, 73), (76, 27),
-- Olivia
(77, 89), (77, 76), (77, 78), (77, 87), (77, 69),
-- Ethan
(78, 63), (78, 64), (78, 68), (78, 22),
-- Ava
(79, 33), (79, 20), (79, 25), (79, 34);

-- ============================================================
-- PLAYLIST FOLLOWS
-- ============================================================

INSERT INTO playlist_follows (user_id, playlist_id, followed_at) VALUES
(3,  1,  NOW() - INTERVAL 5 DAY),
(3,  2,  NOW() - INTERVAL 3 DAY),
(2,  4,  NOW() - INTERVAL 4 DAY),
(2,  5,  NOW() - INTERVAL 2 DAY),
(73, 5,  NOW() - INTERVAL 1 DAY),
(74, 6,  NOW() - INTERVAL 2 DAY),
(75, 7,  NOW() - INTERVAL 1 DAY),
(76, 10, NOW() - INTERVAL 3 DAY),
(77, 5,  NOW() - INTERVAL 6 HOUR),
(78, 6,  NOW() - INTERVAL 12 HOUR),
(79, 12, NOW() - INTERVAL 1 DAY),
(80, 13, NOW() - INTERVAL 2 DAY),
(81, 14, NOW() - INTERVAL 1 DAY),
(82, 15, NOW() - INTERVAL 4 HOUR);

-- ============================================================
-- LISTENING HISTORY — using songs 16–89
-- ============================================================

INSERT INTO listening_history (user_id, song_id, played_at) VALUES
-- Alice
(2, 33, NOW() - INTERVAL 5  MINUTE),
(2, 20, NOW() - INTERVAL 20 MINUTE),
(2, 25, NOW() - INTERVAL 45 MINUTE),
(2, 76, NOW() - INTERVAL 1  HOUR),
(2, 30, NOW() - INTERVAL 2  HOUR),
(2, 78, NOW() - INTERVAL 1  DAY),
-- Bob
(3, 35, NOW() - INTERVAL 10 MINUTE),
(3, 40, NOW() - INTERVAL 30 MINUTE),
(3, 88, NOW() - INTERVAL 1  HOUR),
(3, 78, NOW() - INTERVAL 3  HOUR),
(3, 87, NOW() - INTERVAL 1  DAY),
-- Emma
(73, 20, NOW() - INTERVAL 15 MINUTE),
(73, 24, NOW() - INTERVAL 40 MINUTE),
(73, 30, NOW() - INTERVAL 2  HOUR),
-- Liam
(74, 32, NOW() - INTERVAL 8  MINUTE),
(74, 64, NOW() - INTERVAL 35 MINUTE),
(74, 88, NOW() - INTERVAL 2  HOUR),
-- Sophia
(75, 49, NOW() - INTERVAL 12 MINUTE),
(75, 56, NOW() - INTERVAL 50 MINUTE),
-- Olivia
(77, 89, NOW() - INTERVAL 6  MINUTE),
(77, 76, NOW() - INTERVAL 25 MINUTE),
(77, 78, NOW() - INTERVAL 1  HOUR),
-- Ethan
(78, 63, NOW() - INTERVAL 18 MINUTE),
(78, 64, NOW() - INTERVAL 1  HOUR);

-- ============================================================
-- PLAY EVENTS (analytics) — for trending + artist dashboards
-- Songs with high play_count get more events; recent events matter
-- ============================================================

INSERT INTO play_events (song_id, user_id, played_at) VALUES
-- Saiyyara (36) — highest play_count, very recent release
(36, 73, NOW() - INTERVAL 5  MINUTE),
(36, 74, NOW() - INTERVAL 20 MINUTE),
(36, 75, NOW() - INTERVAL 1  HOUR),
(36, NULL, NOW() - INTERVAL 2 HOUR),
(36, 76, NOW() - INTERVAL 3  HOUR),
(36, 2,  NOW() - INTERVAL 1  DAY),
(36, 3,  NOW() - INTERVAL 2  DAY),
-- Reawaker (86) — 2025 release, very fresh
(86, 77, NOW() - INTERVAL 10 MINUTE),
(86, 78, NOW() - INTERVAL 30 MINUTE),
(86, NULL, NOW() - INTERVAL 2 HOUR),
(86, 79, NOW() - INTERVAL 1  DAY),
(86, 80, NOW() - INTERVAL 2  DAY),
-- Little Dark Age (33) — all-time high plays
(33, 2,  NOW() - INTERVAL 5  MINUTE),
(33, 73, NOW() - INTERVAL 15 MINUTE),
(33, NULL, NOW() - INTERVAL 1 HOUR),
(33, 74, NOW() - INTERVAL 3  HOUR),
(33, 3,  NOW() - INTERVAL 1  DAY),
-- Like Him (32) — 2024 Tyler release, trending
(32, 74, NOW() - INTERVAL 8  MINUTE),
(32, 81, NOW() - INTERVAL 45 MINUTE),
(32, NULL, NOW() - INTERVAL 2 HOUR),
(32, 82, NOW() - INTERVAL 1  DAY),
-- Specialz (78) — Jujutsu Kaisen, huge anime popularity
(78, 77, NOW() - INTERVAL 12 MINUTE),
(78, 3,  NOW() - INTERVAL 35 MINUTE),
(78, NULL, NOW() - INTERVAL 1 HOUR),
(78, 83, NOW() - INTERVAL 2  DAY),
-- Tum Hi Ho (35) — classic evergreen
(35, 3,  NOW() - INTERVAL 7  MINUTE),
(35, 82, NOW() - INTERVAL 40 MINUTE),
(35, NULL, NOW() - INTERVAL 3 HOUR),
(35, 84, NOW() - INTERVAL 1  DAY),
-- Shape of You (49) — Ed Sheeran mega hit
(49, 75, NOW() - INTERVAL 12 MINUTE),
(49, 85, NOW() - INTERVAL 1  HOUR),
(49, NULL, NOW() - INTERVAL 2 DAY),
-- The Rumbling (88) — AoT anthem
(88, 77, NOW() - INTERVAL 20 MINUTE),
(88, 3,  NOW() - INTERVAL 1  HOUR),
(88, NULL, NOW() - INTERVAL 3 HOUR),
(88, 78, NOW() - INTERVAL 1  DAY),
-- End of Beginning (20) — viral Djo track
(20, 2,  NOW() - INTERVAL 3  MINUTE),
(20, 73, NOW() - INTERVAL 25 MINUTE),
(20, NULL, NOW() - INTERVAL 2 HOUR),
(20, 85, NOW() - INTERVAL 1  DAY),
-- Chaleya (42) — Arijit Singh 2023
(42, 3,  NOW() - INTERVAL 15 MINUTE),
(42, 82, NOW() - INTERVAL 50 MINUTE),
(42, NULL, NOW() - INTERVAL 1 DAY);

SET FOREIGN_KEY_CHECKS = 1;
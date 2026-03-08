# 🎵 RevPlay — Architecture & ERD Documentation

> Complete technical architecture of the RevPlay music streaming platform

---

## 1. 🗃️ Entity Relationship Diagram (ERD)

### 1.1 Full Database Schema

```mermaid
erDiagram
    USERS {
        bigint id PK "AUTO_INCREMENT"
        varchar email UK "NOT NULL"
        varchar username UK "NOT NULL"
        varchar password_hash "NOT NULL"
        varchar display_name "nullable"
        text bio "nullable"
        varchar profile_picture_url "nullable"
        enum role "LISTENER or ARTIST or ADMIN"
        datetime created_at "NOT NULL"
        datetime updated_at "NOT NULL"
    }

    ARTISTS {
        bigint id PK "AUTO_INCREMENT"
        bigint user_id FK "UNIQUE, NOT NULL, refs users"
        varchar artist_name "NOT NULL"
        text bio "nullable"
        varchar genre "nullable"
        varchar profile_picture_url "nullable"
        varchar banner_image_url "nullable"
        varchar instagram "nullable"
        varchar twitter "nullable"
        varchar youtube "nullable"
        varchar spotify "nullable"
        varchar website "nullable"
        datetime created_at "NOT NULL"
    }

    GENRES {
        bigint id PK "AUTO_INCREMENT"
        varchar name UK "NOT NULL"
    }

    ALBUMS {
        bigint id PK "AUTO_INCREMENT"
        varchar name "NOT NULL"
        text description "nullable"
        varchar cover_image_url "nullable"
        date release_date "nullable"
        bigint artist_id FK "NOT NULL, refs artists, CASCADE"
        datetime created_at "NOT NULL"
    }

    SONGS {
        bigint id PK "AUTO_INCREMENT"
        varchar title "NOT NULL"
        varchar genre "nullable"
        int duration "NOT NULL, in seconds"
        varchar audio_url "NOT NULL"
        varchar cover_image_url "nullable"
        date release_date "nullable"
        bigint play_count "DEFAULT 0"
        enum visibility "PUBLIC or UNLISTED or PRIVATE"
        bigint artist_id FK "NOT NULL, refs artists, CASCADE"
        bigint album_id FK "nullable, refs albums, SET NULL"
        datetime created_at "NOT NULL"
    }

    PLAYLISTS {
        bigint id PK "AUTO_INCREMENT"
        varchar name "NOT NULL"
        text description "nullable"
        boolean is_public "DEFAULT TRUE"
        varchar cover_image_url "nullable"
        bigint user_id FK "NOT NULL, refs users, CASCADE"
        datetime created_at "NOT NULL"
        datetime updated_at "NOT NULL"
    }

    PLAYLIST_SONGS {
        bigint id PK "AUTO_INCREMENT"
        bigint playlist_id FK "NOT NULL, refs playlists, CASCADE"
        bigint song_id FK "NOT NULL, refs songs, CASCADE"
        int position "Order in playlist"
        datetime added_at "NOT NULL"
    }

    FAVORITES {
        bigint id PK "AUTO_INCREMENT"
        bigint user_id FK "NOT NULL, refs users, CASCADE"
        bigint song_id FK "NOT NULL, refs songs, CASCADE"
        datetime created_at "NOT NULL"
    }

    LISTENING_HISTORY {
        bigint id PK "AUTO_INCREMENT"
        bigint user_id FK "NOT NULL, refs users, CASCADE"
        bigint song_id FK "NOT NULL, refs songs, CASCADE"
        datetime played_at "NOT NULL"
    }

    PLAYLIST_FOLLOWS {
        bigint id PK "AUTO_INCREMENT"
        bigint user_id FK "NOT NULL, refs users, CASCADE"
        bigint playlist_id FK "NOT NULL, refs playlists, CASCADE"
        datetime followed_at "NOT NULL"
    }

    PLAY_EVENTS {
        bigint id PK "AUTO_INCREMENT"
        bigint song_id FK "NOT NULL, refs songs, CASCADE"
        bigint user_id FK "nullable, refs users, SET NULL"
        datetime played_at "NOT NULL"
    }

    ARTIST_REQUESTS {
        bigint id PK "AUTO_INCREMENT"
        bigint user_id FK "NOT NULL, refs users"
        varchar artist_name "NOT NULL"
        varchar genre "nullable"
        text reason "nullable"
        enum status "PENDING or APPROVED or REJECTED"
        text admin_note "nullable"
        bigint reviewed_by FK "nullable, refs users"
        datetime reviewed_at "nullable"
        datetime created_at "NOT NULL"
    }

    %% Relationships
    USERS ||--o| ARTISTS : "can become"
    USERS ||--o{ PLAYLISTS : "creates"
    USERS ||--o{ FAVORITES : "marks"
    USERS ||--o{ LISTENING_HISTORY : "listens"
    USERS ||--o{ PLAYLIST_FOLLOWS : "follows"
    USERS ||--o{ PLAY_EVENTS : "generates"
    USERS ||--o{ ARTIST_REQUESTS : "submits"

    ARTISTS ||--o{ SONGS : "uploads"
    ARTISTS ||--o{ ALBUMS : "creates"

    ALBUMS ||--o{ SONGS : "contains"

    SONGS ||--o{ FAVORITES : "receives"
    SONGS ||--o{ LISTENING_HISTORY : "appears in"
    SONGS ||--o{ PLAYLIST_SONGS : "added to"
    SONGS ||--o{ PLAY_EVENTS : "tracked by"

    PLAYLISTS ||--o{ PLAYLIST_SONGS : "contains"
    PLAYLISTS ||--o{ PLAYLIST_FOLLOWS : "followed by"
```

### 1.2 Relationships Summary

```mermaid
graph LR
    subgraph Auth["🔐 Auth Domain"]
        U["👤 Users"]
        AR["📝 Artist Requests"]
    end

    subgraph Music["🎵 Music Domain"]
        A["🎤 Artists"]
        AL["💿 Albums"]
        S["🎵 Songs"]
        G["🏷️ Genres"]
    end

    subgraph Social["❤️ Social Domain"]
        P["📋 Playlists"]
        PS["🔗 Playlist Songs"]
        F["♥ Favorites"]
        PF["👥 Playlist Follows"]
    end

    subgraph Analytics["📊 Analytics Domain"]
        LH["📜 Listening History"]
        PE["📈 Play Events"]
    end

    U -->|"1:1"| A
    U -->|"1:N"| P
    U -->|"1:N"| F
    U -->|"1:N"| LH
    U -->|"1:N"| PF
    U -->|"1:N"| PE
    U -->|"1:N"| AR

    A -->|"1:N"| S
    A -->|"1:N"| AL
    AL -->|"1:N"| S

    S -->|"1:N"| PS
    S -->|"1:N"| F
    S -->|"1:N"| LH
    S -->|"1:N"| PE

    P -->|"1:N"| PS
    P -->|"1:N"| PF

    style Auth fill:#1a1a2e,stroke:#e94560,color:#edf2f4,stroke-width:2px
    style Music fill:#0f3460,stroke:#00d2ff,color:#edf2f4,stroke-width:2px
    style Social fill:#16213e,stroke:#a855f7,color:#edf2f4,stroke-width:2px
    style Analytics fill:#1b4332,stroke:#4ade80,color:#edf2f4,stroke-width:2px

    style U fill:#e94560,stroke:#fff,color:#fff
    style AR fill:#f97316,stroke:#fff,color:#fff
    style A fill:#00d2ff,stroke:#fff,color:#000
    style AL fill:#06b6d4,stroke:#fff,color:#000
    style S fill:#3b82f6,stroke:#fff,color:#fff
    style G fill:#8b5cf6,stroke:#fff,color:#fff
    style P fill:#a855f7,stroke:#fff,color:#fff
    style PS fill:#c084fc,stroke:#fff,color:#000
    style F fill:#ec4899,stroke:#fff,color:#fff
    style PF fill:#d946ef,stroke:#fff,color:#fff
    style LH fill:#4ade80,stroke:#fff,color:#000
    style PE fill:#22c55e,stroke:#fff,color:#000
```

### 1.3 Database Indexes

| Table | Index | Column(s) | Purpose |
|:------|:------|:----------|:--------|
| `users` | `idx_users_email` | `email` | Login lookup |
| `users` | `idx_users_username` | `username` | Login lookup |
| `users` | `idx_users_role` | `role` | Admin role filtering |
| `artists` | `idx_artists_artist_name` | `artist_name` | Search by name |
| `albums` | `idx_albums_artist_id` | `artist_id` | FK join optimization |
| `albums` | `idx_albums_release_date` | `release_date` | Sort by date |
| `songs` | `idx_songs_artist_id` | `artist_id` | FK join optimization |
| `songs` | `idx_songs_album_id` | `album_id` | FK join optimization |
| `songs` | `idx_songs_title` | `title` | Search by title |
| `songs` | `idx_songs_genre` | `genre` | Filter by genre |
| `songs` | `idx_songs_visibility` | `visibility` | Filter public/private |
| `songs` | `idx_songs_play_count` | `play_count DESC` | Trending sort |
| `songs` | `idx_songs_release_date` | `release_date` | Sort by date |
| `playlists` | `idx_playlists_user_id` | `user_id` | My playlists lookup |
| `playlists` | `idx_playlists_is_public` | `is_public` | Browse public playlists |
| `playlist_songs` | `idx_ps_playlist_id` | `playlist_id` | Playlist contents |
| `playlist_songs` | `idx_ps_song_id` | `song_id` | Song usage lookup |
| `favorites` | `idx_fav_user_id` | `user_id` | My favorites |
| `favorites` | `idx_fav_song_id` | `song_id` | Song popularity |
| `listening_history` | `idx_lh_user_id` | `user_id` | User history |
| `listening_history` | `idx_lh_played_at` | `played_at DESC` | Recent history |
| `play_events` | `idx_pe_song_id` | `song_id` | Analytics queries |
| `play_events` | `idx_pe_played_at` | `played_at DESC` | Trend analysis |

### 1.4 Unique Constraints

| Table | Constraint | Column(s) |
|:------|:----------|:----------|
| `users` | `uq_users_email` | `email` |
| `users` | `uq_users_username` | `username` |
| `genres` | `uq_genres_name` | `name` |
| `artists` | `uq_artists_user_id` | `user_id` |
| `playlist_songs` | `uq_playlist_songs` | `(playlist_id, song_id)` |
| `favorites` | `uq_favorites` | `(user_id, song_id)` |
| `playlist_follows` | `uq_playlist_follows` | `(user_id, playlist_id)` |

---

## 2. 🏗️ Application Architecture

### 2.1 Layered Architecture

```mermaid
graph TB
    subgraph Client["🖥️ Client Browser"]
        direction LR
        HTML["📄 Thymeleaf Pages<br/>21 pages"]
        FRAG["🧩 Fragments<br/>7 reusable"]
        CSS["🎨 CSS Modules<br/>19 files"]
        JSM["⚡ JavaScript<br/>7 modules"]
        AUD["🔊 HTML5 Audio"]
    end

    subgraph Security["🔒 Spring Security Layer"]
        direction LR
        SCONF["SecurityConfig<br/>URL protection + BCrypt"]
        UDS["CustomUserDetailsService<br/>DB auth provider"]
        AEP["AuthenticationEntryPoint<br/>Login redirect"]
        ADH["AccessDeniedHandler<br/>403 handling"]
    end

    subgraph Controllers["🌐 Controller Layer (17 controllers)"]
        direction TB
        subgraph MVC["📄 MVC Controllers (5)"]
            AC["AuthController"]
            PC["PageController"]
            UC["UserController"]
            ADC["AdminController"]
            LC["LibraryController"]
        end
        subgraph REST["📡 REST API Controllers (12)"]
            SC["SongController"]
            ALC["AlbumController"]
            ACC["ArtistCatalogController"]
            GC["GenreController"]
            PLC["PlaylistController"]
            PLFC["PlaylistFollowController"]
            FC["FavoriteController"]
            HC["HistoryController"]
            AMC["ArtistMgmtController"]
            ASC["ArtistSongController"]
            AALC["ArtistAlbumController"]
            ANC["AnalyticsController"]
        end
    end

    subgraph Services["⚙️ Service Layer (17 services)"]
        direction LR
        AS["AuthService"]
        US["UserService"]
        ADS["AdminService"]
        SS["SongService"]
        ALCS["AlbumCatalogService"]
        ARCS["ArtistCatalogService"]
        PS["PlaylistService"]
        PLFS["PlaylistFollowService"]
        FS["FavoriteService"]
        HS["HistoryService"]
        ASI["ArtistServiceImpl"]
        ALSI["AlbumServiceImpl"]
        ANS["AnalyticsService"]
        FSS["FileStorageService"]
    end

    subgraph Data["📦 Data Access Layer"]
        direction LR
        REPO["JPA Repositories<br/>12 repositories"]
        SPEC["SongSpecification<br/>Dynamic filtering"]
        MAP["SongMapper<br/>Entity ↔ DTO"]
        ENT["JPA Entities<br/>12 entities + 2 enums"]
    end

    subgraph CrossCut["🔧 Cross-Cutting Concerns"]
        direction LR
        GEH["GlobalExceptionHandler<br/>@ControllerAdvice"]
        GMA["GlobalModelAdvice<br/>Navbar data injection"]
        SU["SecurityUtils<br/>Current user helper"]
    end

    subgraph Storage["💾 Persistence"]
        direction LR
        DB[("🐬 MySQL 8<br/>revplay_db<br/>12 tables")]
        FW["🔄 Flyway<br/>5 migrations"]
        FS2[("📁 Filesystem<br/>/uploads/<br/>audio + images")]
    end

    Client -->|"HTTP / PJAX / AJAX"| Security
    Security --> Controllers
    Controllers --> Services
    Services --> Data
    Data --> Storage

    style Client fill:#1e1b4b,stroke:#818cf8,color:#e0e7ff,stroke-width:2px
    style Security fill:#7f1d1d,stroke:#f87171,color:#fef2f2,stroke-width:2px
    style Controllers fill:#1e3a5f,stroke:#60a5fa,color:#dbeafe,stroke-width:2px
    style MVC fill:#1e3a5f,stroke:#93c5fd,color:#dbeafe,stroke-width:1px
    style REST fill:#1e3a5f,stroke:#93c5fd,color:#dbeafe,stroke-width:1px
    style Services fill:#2d1b69,stroke:#a78bfa,color:#ede9fe,stroke-width:2px
    style Data fill:#064e3b,stroke:#34d399,color:#d1fae5,stroke-width:2px
    style CrossCut fill:#78350f,stroke:#fbbf24,color:#fef3c7,stroke-width:2px
    style Storage fill:#3b0764,stroke:#c084fc,color:#f5f3ff,stroke-width:2px
```

### 2.2 Request Flow

```mermaid
sequenceDiagram
    actor User as 🧑 User
    participant Browser as 🌐 Browser
    participant Security as 🔒 SecurityFilter
    participant Controller as 🎯 Controller
    participant Service as ⚙️ Service
    participant Repository as 📦 Repository
    participant MySQL as 🐬 MySQL

    User->>Browser: Click / Navigate
    Browser->>Security: HTTP Request

    alt 🚫 Not Authenticated + Protected
        Security-->>Browser: 302 → /login
    else ✅ Authenticated or Public
        Security->>Controller: Forward Request
        Controller->>Service: Business Logic Call
        Service->>Repository: JPA Query
        Repository->>MySQL: SQL
        MySQL-->>Repository: ResultSet
        Repository-->>Service: Entity / List
        Service-->>Controller: DTO / Model

        alt 📄 MVC Route
            Controller-->>Browser: Thymeleaf HTML Page
        else 📡 REST API
            Controller-->>Browser: JSON Response
        end
    end

    Browser-->>User: Rendered Page / Updated UI
```

---

## 3. 🎨 Frontend Architecture

### 3.1 Page & Component Map

```mermaid
graph TB
    subgraph Layout["📐 Layout System"]
        LY["layout.html<br/>(master template)"]
        NAV["🧭 navbar.html"]
        SIDE["📱 sidebar.html"]
        FOOT["📋 footer.html"]
        PBAR["🎵 player-bar.html"]
    end

    subgraph PublicPages["🌍 Public Pages"]
        IDX["🏠 index.html<br/>Home"]
        LOG["🔑 login.html"]
        REG["📝 register.html"]
        LIB["📚 library.html"]
        SRCH["🔍 search.html"]
        PLY["▶️ player.html"]
        AP["🎤 artist-profile.html"]
        ART["👥 artists.html"]
        ALB["💿 albums.html"]
        AD["💿 album-detail.html"]
        SD["🎵 song-detail.html"]
        ABT["ℹ️ about.html"]
    end

    subgraph AuthPages["🔐 Authenticated Pages"]
        PLL["📋 playlist.html"]
        FAV["❤️ favorites.html"]
        HIS["📜 history.html"]
        PRF["👤 profile.html"]
    end

    subgraph ArtistPages["🎤 Artist Pages"]
        ADASH["📊 artist-dashboard.html"]
        ASNG["🎵 artist-songs.html"]
        AALB["💿 artist-albums.html"]
    end

    subgraph AdminPages["👑 Admin Pages"]
        ADM["⚙️ admin.html<br/>(4-tab dashboard)"]
    end

    LY --> NAV & SIDE & FOOT & PBAR
    LY --> PublicPages & AuthPages & ArtistPages & AdminPages

    subgraph SharedFragments["🧩 Shared Fragments"]
        SC["🃏 song-card.html"]
        IC["🎯 icon.html"]
        PM["📋 playlist-modal.html"]
    end

    PublicPages --> SharedFragments
    AuthPages --> SharedFragments

    style Layout fill:#1e1b4b,stroke:#818cf8,color:#e0e7ff,stroke-width:2px
    style PublicPages fill:#064e3b,stroke:#34d399,color:#d1fae5,stroke-width:2px
    style AuthPages fill:#78350f,stroke:#fbbf24,color:#fef3c7,stroke-width:2px
    style ArtistPages fill:#1e3a5f,stroke:#60a5fa,color:#dbeafe,stroke-width:2px
    style AdminPages fill:#7f1d1d,stroke:#f87171,color:#fef2f2,stroke-width:2px
    style SharedFragments fill:#2d1b69,stroke:#a78bfa,color:#ede9fe,stroke-width:2px
```

### 3.2 JavaScript Module Architecture

```mermaid
graph LR
    subgraph Core["🧠 Core Modules"]
        PLAYER["🎵 player.js<br/>Audio engine, queue,<br/>keyboard shortcuts,<br/>shuffle, repeat"]
        NAV["🧭 navigation.js<br/>PJAX router,<br/>history API,<br/>cache management"]
        TOAST["🔔 confirm-toast.js<br/>Custom dialogs,<br/>toast notifications"]
    end

    subgraph Features["⭐ Feature Modules"]
        FAVS["❤️ favorites.js<br/>Heart toggle,<br/>optimistic UI,<br/>cross-page sync"]
        PLACT["📋 playlist-actions.js<br/>CRUD modals,<br/>add-to-playlist,<br/>PJAX-compatible"]
    end

    subgraph UI["🎨 UI Modules"]
        THEME["🌓 theme.js<br/>Light/dark toggle,<br/>localStorage"]
        SIDEBAR["📱 sidebar.js<br/>Mobile drawer,<br/>hamburger animation"]
    end

    NAV -->|"pjax:complete"| FAVS
    NAV -->|"pjax:complete"| PLACT
    NAV -->|"reinit"| PLAYER
    PLAYER -->|"song change"| FAVS
    FAVS -->|"cache invalidate"| NAV
    PLACT -->|"cache invalidate"| NAV
    SIDE2[sidebar events] -.-> NAV

    style Core fill:#1e1b4b,stroke:#818cf8,color:#e0e7ff,stroke-width:2px
    style Features fill:#064e3b,stroke:#34d399,color:#d1fae5,stroke-width:2px
    style UI fill:#78350f,stroke:#fbbf24,color:#fef3c7,stroke-width:2px
```

### 3.3 CSS Architecture

```mermaid
graph TB
    subgraph Foundation["🎨 Foundation (7 files)"]
        BASE["base.css<br/>Variables, resets"]
        LAYOUT["layout.css<br/>Grid, nav, sidebar, footer"]
        COMP["components.css<br/>Buttons, forms, modals, cards"]
        ICONS["icons.css<br/>SVG icon system"]
        PCSS["player.css<br/>Audio player, queue"]
        TCSS["theme.css<br/>Light/dark overrides"]
        RCSS["responsive.css<br/>3 breakpoints"]
    end

    subgraph Pages["📄 Page Styles (12 files)"]
        PA["about.css"]
        PD["admin.css"]
        PL["album.css"]
        PR["artist.css"]
        PU["auth.css"]
        PH["history.css"]
        PO["home.css"]
        PI["library.css"]
        PP["playlist.css"]
        PF["profile.css"]
        PS["search.css"]
        PSD["song-detail.css"]
    end

    BASE --> LAYOUT --> COMP --> ICONS --> PCSS --> TCSS --> RCSS
    RCSS --> Pages

    style Foundation fill:#1e1b4b,stroke:#818cf8,color:#e0e7ff,stroke-width:2px
    style Pages fill:#2d1b69,stroke:#a78bfa,color:#ede9fe,stroke-width:2px
```

---

## 4. 🔐 Role-Based Access Control

```mermaid
graph TD
    subgraph Roles["User Roles"]
        L["🎧 LISTENER<br/><i>Default role</i>"]
        A["🎤 ARTIST<br/><i>Upgraded via request</i>"]
        AD["👑 ADMIN<br/><i>System administrator</i>"]
    end

    subgraph ListenerPerms["🎧 Listener Permissions"]
        L1["🔍 Browse & search songs"]
        L2["📋 Create/manage playlists"]
        L3["❤️ Toggle favorites"]
        L4["📜 View listening history"]
        L5["👥 Follow playlists"]
        L6["👤 Edit profile & picture"]
        L7["📝 Request artist upgrade"]
        L8["💿 Browse albums & artists"]
    end

    subgraph ArtistPerms["🎤 Artist Permissions"]
        A1["⬆️ Upload songs with audio"]
        A2["💿 Create/manage albums"]
        A3["📊 Analytics dashboard"]
        A4["🖼️ Edit artist profile/banner"]
        A5["🔒 Toggle song visibility"]
        A6["🔗 Add social media links"]
    end

    subgraph AdminPerms["👑 Admin Permissions"]
        AD1["📊 Admin dashboard"]
        AD2["👥 Manage all users"]
        AD3["✅ Approve/reject artist requests"]
        AD4["🗑️ Delete any song/playlist"]
        AD5["📈 View growth analytics"]
        AD6["🔄 Change user roles"]
    end

    L --> ListenerPerms
    A --> ArtistPerms
    AD --> AdminPerms

    L -->|"Artist Request<br/>→ Admin Approval"| A

    style Roles fill:#1e1b4b,stroke:#818cf8,color:#e0e7ff,stroke-width:2px
    style ListenerPerms fill:#064e3b,stroke:#34d399,color:#d1fae5,stroke-width:2px
    style ArtistPerms fill:#1e3a5f,stroke:#60a5fa,color:#dbeafe,stroke-width:2px
    style AdminPerms fill:#7f1d1d,stroke:#f87171,color:#fef2f2,stroke-width:2px
    style L fill:#22c55e,stroke:#fff,color:#fff,stroke-width:2px
    style A fill:#3b82f6,stroke:#fff,color:#fff,stroke-width:2px
    style AD fill:#ef4444,stroke:#fff,color:#fff,stroke-width:2px
```

---

## 5. 🧪 Testing Architecture

```mermaid
graph TB
    subgraph UnitTests["🔬 Unit Tests (15 files)"]
        UT1["AuthServiceTest"]
        UT2["UserServiceTest"]
        UT3["AdminServiceTest"]
        UT4["CustomUserDetailsServiceTest"]
        UT5["SongServiceTest"]
        UT6["ArtistCatalogServiceTest"]
        UT7["AlbumCatalogServiceTest"]
        UT8["AlbumServiceImplTest"]
        UT9["ArtistServiceImplTest"]
        UT10["PlaylistServiceTest"]
        UT11["PlaylistFollowServiceTest"]
        UT12["FavoriteServiceTest"]
        UT13["HistoryServiceTest"]
        UT14["FileStorageServiceTest"]
        UT15["AnalyticsServiceTest"]
    end

    subgraph IntegTests["🧩 Integration Tests (17 files)"]
        IT1["AuthControllerIT"]
        IT2["UserControllerIT"]
        IT3["AdminControllerIT"]
        IT4["PageControllerIT"]
        IT5["LibraryControllerIT"]
        IT6["SongControllerIT"]
        IT7["AlbumControllerIT"]
        IT8["ArtistCatalogControllerIT"]
        IT9["ArtistManagementControllerIT"]
        IT10["ArtistSongControllerIT"]
        IT11["ArtistAlbumControllerIT"]
        IT12["GenreControllerIT"]
        IT13["PlaylistControllerIT"]
        IT14["PlaylistFollowControllerIT"]
        IT15["FavoriteControllerIT"]
        IT16["HistoryControllerIT"]
        IT17["AnalyticsControllerIT"]
    end

    subgraph TestUtils["🛠️ Test Utilities (3 files)"]
        TU1["IntegrationTestBase"]
        TU2["TestDataBuilder"]
        TU3["TestConstants"]
    end

    UnitTests -->|"JUnit + Mockito"| Services["⚙️ Services"]
    IntegTests -->|"@SpringBootTest"| API["📡 REST APIs"]
    TestUtils --> UnitTests & IntegTests

    JACOCO["📊 JaCoCo<br/>Coverage ≥ 70%"] -.-> UnitTests & IntegTests

    style UnitTests fill:#064e3b,stroke:#34d399,color:#d1fae5,stroke-width:2px
    style IntegTests fill:#1e3a5f,stroke:#60a5fa,color:#dbeafe,stroke-width:2px
    style TestUtils fill:#78350f,stroke:#fbbf24,color:#fef3c7,stroke-width:2px
    style JACOCO fill:#7f1d1d,stroke:#f87171,color:#fef2f2,stroke-width:2px
```

---

## 6. 📊 File Statistics

| Category | Count | Details |
|:---------|:-----:|:--------|
| 🗃️ Models | 14 | 12 entities + 2 enums |
| 🌐 Controllers | 17 | 5 MVC + 12 REST |
| ⚙️ Services | 17 | Business logic layer |
| 📦 DTOs | 17 | Request/response objects |
| 📚 Repositories | 12 | Spring Data JPA |
| 📄 Templates | 21 | Thymeleaf pages |
| 🧩 Fragments | 7 | Reusable components |
| 🎨 CSS | 19 | 7 foundation + 12 page-specific |
| ⚡ JS | 7 | Player, navigation, favorites, etc. |
| 🧪 Tests | 35 | 15 unit + 17 integration + 3 utilities |
| 🔧 Config | 3 | Security, Web, ModelAdvice |
| ⚠️ Exceptions | 8 | Custom exceptions + handlers |
| 🔄 Migrations | 5 | Flyway SQL files |
| 🧰 Other | 3 | Mapper, Specification, SecurityUtils |
| **TOTAL** | **~155** | Java + HTML + CSS + JS + SQL |

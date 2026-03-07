# 🎵 RevPlay — Music Streaming Web Application

<div align="center">

**A full-featured music streaming platform built with Spring Boot & Thymeleaf**

[![Java](https://img.shields.io/badge/Java-17+-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.4-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white)](https://www.mysql.com/)
[![Thymeleaf](https://img.shields.io/badge/Thymeleaf-3.x-005F0F?style=for-the-badge&logo=thymeleaf&logoColor=white)](https://www.thymeleaf.org/)
[![License](https://img.shields.io/badge/License-MIT-blue?style=for-the-badge)](LICENSE)

</div>

---

## 📋 Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Prerequisites](#prerequisites)
- [Setup & Installation](#setup--installation)
- [Running the Application](#running-the-application)
- [Project Structure](#project-structure)
- [API Documentation](#api-documentation)
- [Testing](#testing)
- [Team](#team)

---

## Overview

**RevPlay** is a comprehensive music streaming web application that allows users to browse, search, and play music, create playlists, follow artists, and interact with a rich music library. Artists can upload music, manage albums, and track analytics. Administrators can manage users, approve artist requests, and monitor platform growth.

Built as a monolithic Spring Boot application with server-side rendering via Thymeleaf, the platform features a full-featured HTML5 audio player with keyboard shortcuts, PJAX-powered SPA-like navigation, responsive design with light/dark themes, and a modular CSS architecture.

---

## Features

### 🎧 Listener Features
- **Music Browsing** — Browse songs by genre, artist, album, or search by keyword
- **Advanced Filtering** — Filter songs by genre, artist, album, and release year using JPA Specifications
- **Music Player** — Full HTML5 Audio player with play/pause, skip, seek, volume control
- **Queue Management** — Add songs to queue, shuffle, repeat (off/one/all), queue sidebar
- **Keyboard Shortcuts** — Space (play/pause), ← → (skip), M (mute), S (shuffle), R (repeat)
- **Playlists** — Create, edit, delete playlists; add/remove/reorder songs; public/private toggle
- **Favorites** — Heart toggle on any song card with optimistic UI and pop animation
- **Listening History** — Timeline view with relative timestamps, play all, shuffle, clear
- **Follow Playlists** — Follow/unfollow public playlists from other users
- **Profile Management** — Edit display name, bio, upload profile picture with live preview

### 🎤 Artist Features
- **Artist Request** — Listeners can request to become an artist (admin approval required)
- **Song Upload** — Upload audio files with metadata (title, genre, duration, cover image)
- **Album Management** — Create/edit/delete albums, add/remove songs
- **Visibility Control** — Set songs as Public, Unlisted, or Private
- **Analytics Dashboard** — Overview stats, per-song play counts, listening trends, top listeners, fan grid
- **Profile Settings** — Upload profile picture, banner image, add social media links

### 👑 Admin Features
- **Admin Dashboard** — 4-tab layout (Overview, Users, Content, Reports)
- **User Management** — Search, filter, paginate users; change roles; delete users
- **Artist Request Management** — Approve/reject artist upgrade requests with notes
- **Content Moderation** — Delete songs and playlists
- **Analytics** — Role distribution, new user growth, top songs by play count

### 🎨 UI/UX Features
- **Responsive Design** — 3 breakpoints (1024px, 768px, 480px)
- **Light/Dark Theme** — Toggle with localStorage persistence
- **PJAX Navigation** — SPA-like page transitions without full page reloads
- **Mobile Sidebar** — Slide-in drawer with hamburger toggle and overlay
- **Modular CSS** — 17 focused CSS files (base, layout, components, icons, player, pages, theme, responsive)
- **Custom Notifications** — Themed confirm dialogs and toast notifications
- **SVG Icons** — Clean SVG icons throughout (no emoji dependencies)

---

## Tech Stack

| Layer | Technology | Purpose |
|-------|-----------|---------|
| **Language** | Java 17+ | Core application language |
| **Framework** | Spring Boot 3.4 | Web framework, DI, auto-configuration |
| **Web** | Spring MVC | REST controllers + MVC page routing |
| **Security** | Spring Security + BCrypt | Authentication, authorization, session management |
| **ORM** | Spring Data JPA / Hibernate | Database access, entity mapping |
| **Database** | MySQL 8 | Relational data storage |
| **Migrations** | Flyway | Database schema versioning |
| **Templating** | Thymeleaf | Server-side HTML rendering |
| **Frontend** | Vanilla JS, HTML5, CSS3 | Interactive UI, audio player, PJAX |
| **Testing** | JUnit + Mockito + JaCoCo | Unit tests, mocking, code coverage |
| **API Docs** | Springdoc OpenAPI (Swagger) | Auto-generated API documentation |
| **Build** | Maven | Dependency management, build lifecycle |
| **Utilities** | Lombok, MapStruct | Boilerplate reduction, DTO mapping |

---

## Architecture

> Complete diagrams available in [`docs/architecture.md`](docs/architecture.md)

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                     Client Browser                          │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌────────────┐  │
│  │ Thymeleaf│  │ CSS (17) │  │  JS (7)  │  │ HTML5 Audio│  │
│  │ HTML(17) │  │ Modules  │  │ Modules  │  │    API     │  │
│  └────┬─────┘  └──────────┘  └────┬─────┘  └────────────┘  │
│       │    MVC / PJAX / AJAX      │                         │
└───────┼───────────────────────────┼─────────────────────────┘
        │                           │
┌───────▼───────────────────────────▼─────────────────────────┐
│              Spring Boot 3.x Application                     │
│  ┌─────────────────────────────────────────────────────┐    │
│  │     Security Layer (Spring Security + BCrypt)       │    │
│  └─────────────────────────┬───────────────────────────┘    │
│  ┌─────────────┐  ┌───────▼──────────┐                      │
│  │ MVC (5)     │  │ REST API (12)    │  ← Controllers       │
│  └──────┬──────┘  └───────┬──────────┘                      │
│         └────────┬────────┘                                  │
│  ┌───────────────▼─────────────────────────────────────┐    │
│  │          Service Layer (17 services)                 │    │
│  └───────────────┬─────────────────────────────────────┘    │
│  ┌───────────────▼───────────────┐  ┌──────────────────┐    │
│  │  JPA Repositories (12)       │  │ FileStorageService│    │
│  │  + SongSpecification         │  │                   │    │
│  └───────────────┬───────────────┘  └────────┬─────────┘    │
└──────────────────┼───────────────────────────┼──────────────┘
                   │                           │
          ┌────────▼────────┐         ┌────────▼────────┐
          │   MySQL 8       │         │  Local Filesystem│
          │  (12 tables)    │         │   /uploads/      │
          │  Flyway managed │         │  (audio, images) │
          └─────────────────┘         └─────────────────┘
```

---

## Prerequisites

Before you begin, ensure you have the following installed:

| Software | Version | Download |
|----------|---------|----------|
| **Java JDK** | 17 or higher | [OpenJDK](https://openjdk.org/) |
| **Maven** | 3.9+ | [Maven](https://maven.apache.org/) |
| **MySQL** | 8.0+ | [MySQL](https://dev.mysql.com/downloads/) |
| **Git** | Latest | [Git](https://git-scm.com/) |
| **IDE** (recommended) | IntelliJ IDEA | [IntelliJ](https://www.jetbrains.com/idea/) |

---

## Setup & Installation

### 1. Clone the Repository
```bash
git clone https://github.com/your-username/revplay.git
cd revplay/Revplay_P2
```

### 2. Create the MySQL Database
```sql
CREATE DATABASE revplay_db;
```
> Flyway will automatically create all tables on first startup.

### 3. Configure Database Credentials
Edit `src/main/resources/application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/revplay_db?createDatabaseIfNotExist=true
    username: root
    password: your_password    # ← Change this
```

### 4. Build the Project
```bash
./mvnw clean install -DskipTests
```
> On Windows, use `mvnw.cmd` instead of `./mvnw`

---

## Running the Application

### Development Mode
```bash
./mvnw spring-boot:run
```

The application will start at: **http://localhost:8080**

### Default Accounts (from seed data)

| Role | Email | Password |
|------|-------|----------|
| Admin | admin@revplay.com | admin123 |
| Artist | artist@revplay.com | artist123 |
| Listener | user@revplay.com | user123 |

> Note: Check `V99__seed_data.sql` for exact seed data credentials.

### Available Routes

| Route | Access | Description |
|-------|--------|-------------|
| `/` | Public | Home page (trending, playlists, artists) |
| `/login` | Public | Login page |
| `/register` | Public | Registration page |
| `/library` | Public | Song library with search & filter |
| `/search` | Public | Search results page |
| `/player` | Public | Music player page |
| `/artist/{id}` | Public | Artist public profile |
| `/playlists` | Authenticated | Playlist management |
| `/favorites` | Authenticated | Favorites page |
| `/history` | Authenticated | Listening history |
| `/profile` | Authenticated | User profile & settings |
| `/artist/dashboard` | Artist | Artist dashboard |
| `/artist/songs` | Artist | Song management |
| `/artist/albums` | Artist | Album management |
| `/admin/dashboard` | Admin | Admin dashboard |
| `/about` | Public | About page |
| `/swagger-ui.html` | Public | API documentation |

---

## Project Structure

```
Revplay_P2/
├── src/
│   ├── main/
│   │   ├── java/com/revplay/
│   │   │   ├── config/          (3 files)  — SecurityConfig, WebConfig, GlobalModelAdvice
│   │   │   ├── controller/      (17 files) — MVC + REST controllers
│   │   │   ├── service/         (17 files) — Business logic layer
│   │   │   ├── repository/      (12 files) — JPA data access
│   │   │   ├── model/           (14 files) — JPA entities + enums
│   │   │   ├── dto/             (17 files) — Request/response objects
│   │   │   ├── exception/       (8 files)  — Custom exceptions + handlers
│   │   │   ├── mapper/          (1 file)   — SongMapper
│   │   │   ├── specification/   (1 file)   — SongSpecification
│   │   │   └── util/            (1 file)   — SecurityUtils
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── logback-spring.xml
│   │       ├── db/migration/    (4 files)  — Flyway SQL migrations
│   │       ├── templates/       (17 pages + 7 fragments)
│   │       └── static/
│   │           ├── css/         (17 files) — Modular CSS architecture
│   │           ├── js/          (7 files)  — Player, navigation, favorites, etc.
│   │           └── images/      — Icons and team photos
│   └── test/
│       └── java/com/revplay/
│           ├── service/         (13 unit tests)
│           ├── controller/      (9 integration tests)
│           └── util/            (3 test utilities)
├── docs/
│   ├── architecture.md          — ERD + architecture diagrams
│   └── RevPlay_Team_Plan.md     — Team roles & responsibilities
├── pom.xml
└── uploads/                     — Runtime file uploads (gitignored)
```

**Total: ~148 files** (Java + HTML + CSS + JS + SQL)

---

## API Documentation

### Swagger UI
With the application running, visit: **http://localhost:8080/swagger-ui.html**

### API Endpoint Groups

| Prefix | Controller | Methods | Description |
|--------|-----------|---------|-------------|
| `/api/auth` | AuthController | 2 | Register, login |
| `/user` | UserController | 5 | Profile, picture, artist-request |
| `/api/songs` | SongController | 4 | Browse, search, filter |
| `/api/albums` | AlbumController | 2 | Browse, detail |
| `/api/artists` | ArtistCatalogController | 2 | Artist profiles |
| `/api/genres` | GenreController | 1 | Genre list |
| `/api/playlists` | PlaylistController | 8 | CRUD, add/remove/reorder songs |
| `/api/playlists/.../follow` | PlaylistFollowController | 3 | Follow/unfollow |
| `/api/favorites` | FavoriteController | 4 | Toggle, list, IDs |
| `/api/history` | HistoryController | 4 | Record, list, clear |
| `/api/artists/me` | ArtistManagementController | 5 | Artist profile management |
| `/api/artists/songs` | ArtistSongController | 4 | Song upload, edit, delete |
| `/api/artists/albums` | ArtistAlbumController | 7 | Album CRUD, song management |
| `/api/artists/analytics` | AnalyticsController | 5 | Stats, trends, fans, listeners |
| `/api/admin` | AdminController | 9+ | Users, content, analytics, requests |

---

## Testing

### Run All Tests
```bash
./mvnw test
```

### Generate Coverage Report
```bash
./mvnw test jacoco:report
```
Coverage report will be at: `target/site/jacoco/index.html`

### Test Summary

| Type | Count | Framework |
|------|-------|-----------|
| Unit Tests | 13 | JUnit + Mockito |
| Integration Tests | 9 | @SpringBootTest |
| Test Utilities | 3 | TestDataBuilder, TestConstants, IntegrationTestBase |
| **Total** | **25** | **Target Coverage: ≥ 70%** |

### Test Coverage by Service

| Service | Tests |
|---------|-------|
| AuthService | 10 tests |
| UserService | 14 tests |
| SongService | ≥3 tests |
| ArtistCatalogService | ≥3 tests |
| AlbumCatalogService | ≥3 tests |
| AlbumServiceImpl | ≥3 tests |
| ArtistServiceImpl | ≥3 tests |
| PlaylistService | ≥3 tests |
| PlaylistFollowService | ≥3 tests |
| FavoriteService | ≥3 tests |
| HistoryService | ≥3 tests |
| FileStorageService | ≥3 tests |
| AnalyticsService | ≥3 tests |

---

## Team

| Member | Role | Responsibility |
|--------|------|---------------|
| **Member 1 (Lead)** | Tech Lead / Frontend / Auth | Full frontend, authentication, admin dashboard, code review |
| **Member 3** | Music Catalog & Search | Songs, albums, artists CRUD, search/filter APIs |
| **Member 4** | Playlists, Favorites & History | Playlist CRUD, favorites, listening history, follows |
| **Member 5** | Artist Features & Analytics | Artist profiles, music upload, album management, analytics |
| **Member 6** | Database, Testing & DevOps | ERD, Flyway, JUnit/Mockito suites, logging, CI |

---

## 📄 License

This project was built as part of an academic sprint project.

---

<div align="center">
  <b>Built with ❤️ by the RevPlay Team</b>
</div>

# RevPlay P2 — 10-Day Sprint Plan

> **Start Date**: Feb 26, 2026 (today) → **Deadline**: Mar 7, 2026
> **Team**: 5 members (You + Members 3, 4, 5, 6)

---

## 🚀 Day 1 — Today (Feb 26): Project Setup & Git Init

### Your Steps to Create the Project (do these in order):

**Step 1: Generate Spring Boot project**
Go to [https://start.spring.io](https://start.spring.io) and select:
- **Project**: Maven
- **Language**: Java
- **Spring Boot**: 3.4.x (latest stable)
- **Group**: `com.revplay`
- **Artifact**: `revplay`
- **Name**: `RevPlay`
- **Package name**: `com.revplay`
- **Packaging**: Jar
- **Java**: 17

**Add these dependencies** (click "Add Dependencies"):
1. Spring Web
2. Spring Data JPA
3. Spring Security
4. Thymeleaf
5. Thymeleaf Extras Spring Security
6. MySQL Driver
7. Lombok
8. Spring Boot DevTools
9. Validation (Bean Validation)
10. Flyway Migration

Click **Generate** → download ZIP → extract into `D:\Java Dev\RevPlay_P2\`

**Step 2: Add additional Maven dependencies**
Open `pom.xml` and add these inside `<dependencies>`:

```xml
<!-- Mockito (already included via starter-test, but explicit for clarity) -->
<!-- JUnit 4 + Mockito come with spring-boot-starter-test -->

<!-- JaCoCo for code coverage (add as plugin, not dependency) -->

<!-- Springdoc OpenAPI / Swagger -->
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.8.5</version>
</dependency>

<!-- MapStruct for DTO mapping -->
<dependency>
    <groupId>org.mapstruct</groupId>
    <artifactId>mapstruct</artifactId>
    <version>1.6.3</version>
</dependency>
<dependency>
    <groupId>org.mapstruct</groupId>
    <artifactId>mapstruct-processor</artifactId>
    <version>1.6.3</version>
    <scope>provided</scope>
</dependency>

<!-- H2 for testing -->
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>test</scope>
</dependency>
```

Add JaCoCo plugin inside `<build><plugins>`:
```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.12</version>
    <executions>
        <execution>
            <goals><goal>prepare-agent</goal></goals>
        </execution>
        <execution>
            <id>report</id>
            <phase>test</phase>
            <goals><goal>report</goal></goals>
        </execution>
    </executions>
</plugin>
```

**Step 3: Create the folder structure**
Run these commands from the project root (`D:\Java Dev\RevPlay_P2\revplay\`):

```powershell
# Java source packages
mkdir -p src/main/java/com/revplay/config
mkdir -p src/main/java/com/revplay/controller
mkdir -p src/main/java/com/revplay/service
mkdir -p src/main/java/com/revplay/repository
mkdir -p src/main/java/com/revplay/model
mkdir -p src/main/java/com/revplay/dto
mkdir -p src/main/java/com/revplay/exception
mkdir -p src/main/java/com/revplay/util

# Resources
mkdir -p src/main/resources/db/migration
mkdir -p src/main/resources/templates/fragments
mkdir -p src/main/resources/static/css
mkdir -p src/main/resources/static/js
mkdir -p src/main/resources/static/images

# Test packages
mkdir -p src/test/java/com/revplay/service
mkdir -p src/test/java/com/revplay/controller
mkdir -p src/test/java/com/revplay/repository

# Docs
mkdir -p docs
```

> [!NOTE]
> On Windows PowerShell, `mkdir -p` might not work. Use `New-Item -ItemType Directory -Force -Path "src\main\java\com\revplay\config"` or simply create these folders in your IDE (IntelliJ/Eclipse).

**Step 4: Create `application.yml`**
Replace `src/main/resources/application.properties` with `application.yml`:

```yaml
spring:
  application:
    name: RevPlay

  datasource:
    url: jdbc:mysql://localhost:3306/revplay_db?createDatabaseIfNotExist=true
    username: root
    password: your_password
    driver-class-name: com.mysql.cj.jdbc.Driver

  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        dialect: org.hibernate.dialect.MySQLDialect

  flyway:
    enabled: true
    locations: classpath:db/migration

  thymeleaf:
    cache: false
    prefix: classpath:/templates/
    suffix: .html

  servlet:
    multipart:
      max-file-size: 50MB
      max-request-size: 50MB

server:
  port: 8080

# File upload directory
app:
  upload:
    dir: ./uploads

logging:
  level:
    com.revplay: DEBUG
    org.springframework.security: INFO
```

**Step 5: Create `.gitignore`**
```
# Maven
target/
!.mvn/wrapper/maven-wrapper.jar

# IDE
.idea/
*.iml
.vscode/
.settings/
.project
.classpath
*.swp
*.swo

# OS
.DS_Store
Thumbs.db

# Compiled
*.class

# Logs
*.log

# Uploads
uploads/

# Environment
.env
application-local.yml
```

**Step 6: Git setup & push**
```powershell
cd "D:\Java Dev\RevPlay_P2\revplay"
git init
git add .
git commit -m "feat: initial project setup with Spring Boot, Thymeleaf, and all dependencies"
git branch -M main
git remote add origin <YOUR_GITHUB_REPO_URL>
git push -u origin main
git checkout -b develop
git push -u origin develop
```

**Step 7: Tell all members to clone**
```
git clone <YOUR_GITHUB_REPO_URL>
cd revplay
git checkout develop
git checkout -b feature/<their-branch-name>
```

Branch names:
- You: `feature/auth-frontend`
- Member 3: `feature/music-catalog`
- Member 4: `feature/playlists`
- Member 5: `feature/artist-analytics`
- Member 6: `feature/db-testing`

---

## 📅 Day-by-Day Plan (Feb 26 – Mar 7)

---

### Day 1 — Feb 26 (Thu) — PROJECT SETUP

| Member | Tasks | Deliverables |
|--------|-------|-------------|
| **You** | Project init (steps above), push to GitHub, create `develop` branch | ✅ Working Spring Boot project on GitHub |
| **Member 3** | Clone repo, set up local dev env (MySQL, IDE), study Song/Album/Artist requirements | ✅ Local env running |
| **Member 4** | Clone repo, set up local dev env, study Playlist/Favorites/History requirements | ✅ Local env running |
| **Member 5** | Clone repo, set up local dev env, study Artist upload/analytics requirements | ✅ Local env running |
| **Member 6** | Clone repo, design ERD (dbdiagram.io), start Flyway `V1__initial_schema.sql` | ✅ ERD draft ready |

> [!IMPORTANT]
> **Member 6 priority**: Get the ERD and `V1__initial_schema.sql` done by end of Day 1 or early Day 2 — everyone depends on the database schema.

---

### Day 2 — Feb 27 (Fri) — ENTITIES & SECURITY

| Member | Tasks | Deliverables |
|--------|-------|-------------|
| **You** | `User` entity, `SecurityConfig`, `AuthController` (register + login), `UserRepository`, `BCryptPasswordEncoder` setup | ✅ Register/Login working, `SecurityConfig` pushed |
| **Member 3** | `Song`, `Album`, `Artist`, `Genre` entities + repositories | ✅ All 4 entities + repos pushed |
| **Member 4** | `Playlist`, `PlaylistSong`, `Favorite`, `ListeningHistory`, `PlaylistFollow` entities + repos | ✅ All 5 entities + repos pushed |
| **Member 5** | `PlayEvent` entity + repo, `FileStorageService` (save files to local disk) | ✅ PlayEvent entity + file upload service pushed |
| **Member 6** | Finalize `V1__initial_schema.sql`, create `V99__seed_data.sql`, push Flyway migrations, `logback-spring.xml`, `GlobalExceptionHandler` | ✅ DB migrations, logging, error handler pushed |

---

### Day 3 — Feb 28 (Sat) — CORE APIs (Part 1)

| Member | Tasks | Deliverables |
|--------|-------|-------------|
| **You** | `UserController` (profile view/edit, stats), `UserService`, `AuthService`, DTOs, login/register Thymeleaf pages (`login.html`, `register.html`) | ✅ Auth fully working with UI |
| **Member 3** | `SongController` + `SongService` — browse all (paginated), get by ID, search by keyword | ✅ 3 song endpoints working |
| **Member 4** | `PlaylistController` + `PlaylistService` — create, get my playlists, get by ID, update, delete | ✅ 5 playlist CRUD endpoints working |
| **Member 5** | `ArtistManagementController` — artist register, get/update profile, social links | ✅ 3 artist profile endpoints working |
| **Member 6** | Test utilities (`TestDataBuilder`, `TestConstants`), integration test base class, JaCoCo config verification | ✅ Test infra ready for all members to use |

---

### Day 4 — Mar 1 (Sun) — CORE APIs (Part 2)

| Member | Tasks | Deliverables |
|--------|-------|-------------|
| **You** | Base Thymeleaf layout (`index.html`), fragments (navbar, footer, player-bar, song-card), CSS design system (`styles.css`) | ✅ Base layout + fragments + design system |
| **Member 3** | Filter API (`/filter?genre=&artist=&album=&year=`), `SongSpecification`, artist profile endpoint, album detail endpoint, genres list | ✅ All 9 endpoints complete |
| **Member 4** | Add/remove songs from playlist, reorder, `FavoriteController` (add, remove, list), public playlists | ✅ Favorites + more playlist endpoints |
| **Member 5** | Song upload endpoint, update/delete song, song visibility toggle | ✅ Song management endpoints working |
| **Member 6** | Write unit tests for `AuthService`, `UserService` (≥3 each), review all PRs for code quality | ✅ Auth tests + first PR review |

---

### Day 5 — Mar 2 (Mon) — FRONTEND + REMAINING APIs

| Member | Tasks | Deliverables |
|--------|-------|-------------|
| **You** | `library.html` (song browsing page), `player.html`, start `player.js` (HTML5 Audio — play/pause/volume/seek) | ✅ Library page + basic player working |
| **Member 3** | Unit tests for `SongService`, `ArtistService`, `AlbumService` (≥3 tests each) | ✅ All service tests written |
| **Member 4** | `HistoryController` (record play, get recent, get all, clear), follow/unfollow playlists | ✅ History + follow endpoints complete |
| **Member 5** | Album CRUD (create, update, delete), add/remove songs from album, list my songs/albums | ✅ Album management complete |
| **Member 6** | Unit tests for `PlaylistService`, `FavoriteService`, `HistoryService` (≥3 each) | ✅ Playlist/Favorites/History tests done |

---

### Day 6 — Mar 3 (Tue) — FRONTEND BUILD-OUT

| Member | Tasks | Deliverables |
|--------|-------|-------------|
| **You** | `playlist.html`, `artist-profile.html`, `artist-dashboard.html`, search page UI | ✅ 4 more pages built |
| **Member 3** | Integration tests for search & filter endpoints, edge cases (empty results, invalid IDs) | ✅ Integration tests complete |
| **Member 4** | Reorder songs in playlist, unit tests for all services | ✅ Reorder working + tests done |
| **Member 5** | `AnalyticsController` — overview (total songs/plays/favorites), per-song play counts, top listeners | ✅ 3 analytics endpoints working |
| **Member 6** | Unit tests for `ArtistManagementService`, `AnalyticsService`, `FileStorageService` | ✅ Artist/Analytics tests done |

---

### Day 7 — Mar 4 (Wed) — PLAYER + ANALYTICS + POLISH

| Member | Tasks | Deliverables |
|--------|-------|-------------|
| **You** | Complete `player.js` — skip forward/backward, queue management, shuffle, repeat modes, progress bar, now-playing display | ✅ Full music player working |
| **Member 3** | Pagination improvements, sorting (by popularity, date), edge case fixes | ✅ Polish complete |
| **Member 4** | Public playlists browse, edge cases (delete playlist with songs, duplicate favorites) | ✅ All edge cases handled |
| **Member 5** | Analytics — fans who favorited, listening trends (daily/weekly/monthly), top listeners | ✅ All 5 analytics endpoints complete |
| **Member 6** | Integration tests for artist upload + analytics, code coverage report (target ≥ 70%) | ✅ Coverage report generated |

---

### Day 8 — Mar 5 (Thu) — INTEGRATION + RESPONSIVE

| Member | Tasks | Deliverables |
|--------|-------|-------------|
| **You** | User profile page, AJAX favorites toggle, responsive CSS (mobile + tablet), profile picture upload UI | ✅ Profile page + responsive design |
| **Member 3** | Bug fixes from integration testing, support You with data loading on frontend pages | ✅ All bugs fixed |
| **Member 4** | Bug fixes, support You with playlist/favorites data on frontend | ✅ All bugs fixed |
| **Member 5** | Bug fixes, security check (only own songs/albums editable), album delete guard (only if empty) | ✅ All bugs fixed + security hardened |
| **Member 6** | Final test review across all modules, missing test coverage, Flyway seed data verification | ✅ Final test audit complete |

---

### Day 9 — Mar 6 (Fri) — FINAL TESTING + DOCS

| Member | Tasks | Deliverables |
|--------|-------|-------------|
| **You** | Full end-to-end testing (register → login → browse → play → playlist → favorites → artist dashboard), fix integration bugs | ✅ E2E flow verified |
| **Member 3** | Final edge case sweep, API doc review (Swagger), help with any remaining bugs | ✅ APIs finalized |
| **Member 4** | Final edge case sweep, help with remaining bugs | ✅ APIs finalized |
| **Member 5** | Final edge case sweep, help with remaining bugs | ✅ APIs finalized |
| **Member 6** | Finalize ERD diagram (`docs/ERD.png`), final JaCoCo coverage report, verify all Flyway migrations run cleanly | ✅ ERD + coverage report finalized |

---

### Day 10 — Mar 7 (Sat) — SUBMISSION DAY 🎉

| Member | Tasks | Deliverables |
|--------|-------|-------------|
| **You** | Write `README.md`, create architecture diagram (`docs/architecture-diagram.png`), merge `develop` → `main`, final demo run | ✅ README + architecture diagram + final merge |
| **Member 3** | Review README, tag final release, support demo | ✅ Release support |
| **Member 4** | Review README, support demo | ✅ Release support |
| **Member 5** | Review README, support demo | ✅ Release support |
| **Member 6** | Verify clean build (`mvn clean install`), all tests pass, coverage ≥ 70% | ✅ Build verified + all tests green |

**Final deliverables checklist:**
- [ ] Working web application demonstration
- [ ] Code repository with clean history
- [ ] ERD (`docs/ERD.png`)
- [ ] Architecture diagram (`docs/architecture-diagram.png`)
- [ ] `README.md` with setup instructions, features, tech stack, screenshots
- [ ] Testing artifacts (JUnit reports, JaCoCo coverage ≥ 70%)

---

## 📊 Summary: Who Does What When

```
         Day1  Day2  Day3  Day4  Day5  Day6  Day7  Day8  Day9  Day10
You      SETUP AUTH  AUTH  TMPL  LIB   PAGES PLAYR RESP  E2E   README
                     +UI   +CSS  +JS   ×4    FULL  +FIX  TEST  SUBMIT

Member3  SETUP ENTI  APIs  APIs  TEST  INTG  POLI  FIX   FINAL DONE
              TIES   ×3    ×6    ×9    TEST  SH

Member4  SETUP ENTI  CRUD  +FAV  HIST  REOR  EDGE  FIX   FINAL DONE
              TIES   ×5    +PUB  +FOL  +TST  CASE

Member5  SETUP ENTI  PROF  SONG  ALBM  ANLY  ANLY  FIX   FINAL DONE
              TIES   ×3    MGMT  CRUD  ×3    ×5

Member6  ERD   FLYW  TEST  AUTH  PL/F  ART/  COVR  AUDIT FINAL BUILD
              AY+LOG INFRA TEST  TEST  ANLT  RPT         ERD   VRFY
```

---

## ⚠️ Daily PR Rules (STRICT)

1. **Every member pushes code to their feature branch DAILY** by end of day
2. **You review PRs every morning** (first 30 min of the day)
3. Merge to `develop` only after your approval
4. If a merge conflict happens → the member who created the conflict resolves it
5. **Never push directly to `develop` or `main`**

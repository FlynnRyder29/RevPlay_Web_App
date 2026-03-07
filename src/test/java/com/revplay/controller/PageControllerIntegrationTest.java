package com.revplay.controller;

import com.revplay.dto.ArtistDTO;
import com.revplay.dto.PlaylistDTO;
import com.revplay.dto.SongDTO;
import com.revplay.exception.RevPlayAccessDeniedHandler;
import com.revplay.exception.RevPlayAuthenticationEntryPoint;
import com.revplay.model.Artist;
import com.revplay.model.PlaylistSong;
import com.revplay.model.Song;
import com.revplay.model.User;
import com.revplay.model.Role;
import com.revplay.repository.ArtistRepository;
import com.revplay.repository.PlaylistSongRepository;
import com.revplay.repository.UserRepository;
import com.revplay.service.*;
import com.revplay.util.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.revplay.util.TestConstants.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.revplay.config.SecurityConfig;
import org.springframework.context.annotation.Import;

@WebMvcTest(PageController.class)
@Import(SecurityConfig.class)
@DisplayName("PageController Integration Tests")
class PageControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private PlaylistService playlistService;
    @MockitoBean private PlaylistFollowService playlistFollowService;
    @MockitoBean private PlaylistSongRepository playlistSongRepository;
    @MockitoBean private SongService songService;
    @MockitoBean private ArtistCatalogService artistCatalogService;
    @MockitoBean private ArtistService artistService;
    @MockitoBean private ArtistRepository artistRepository;
    @MockitoBean private UserRepository userRepository;
    @MockitoBean private FavoriteService favoriteService;
    @MockitoBean private HistoryService historyService;
    @MockitoBean private SecurityUtils securityUtils;

    @MockitoBean private CustomUserDetailsService customUserDetailsService;
    @MockitoBean private RevPlayAuthenticationEntryPoint authEntryPoint;
    @MockitoBean private RevPlayAccessDeniedHandler accessDeniedHandler;

    private SongDTO testSong;
    private PlaylistDTO testPlaylist;
    private ArtistDTO testArtist;
    private User testUser;
    private Artist testArtistEntity;

    @BeforeEach
    void setUp() throws Exception {
        // 403 for role-denied requests
        doAnswer(inv -> {
            jakarta.servlet.http.HttpServletResponse resp =
                    inv.getArgument(1, jakarta.servlet.http.HttpServletResponse.class);
            resp.setStatus(jakarta.servlet.http.HttpServletResponse.SC_FORBIDDEN);
            resp.getWriter().flush();
            return null;
        }).when(accessDeniedHandler).handle(any(), any(), any());

        // redirect unauthenticated MVC requests to login
        doAnswer(inv -> {
            jakarta.servlet.http.HttpServletResponse resp =
                    inv.getArgument(1, jakarta.servlet.http.HttpServletResponse.class);
            resp.sendRedirect("/auth/login");
            return null;
        }).when(authEntryPoint).commence(any(), any(), any());

        // prevent auth bypass from no-op userDetailsService mock
        when(customUserDetailsService.loadUserByUsername(any()))
                .thenThrow(new org.springframework.security.core.userdetails.UsernameNotFoundException("not found"));

        testSong = SongDTO.builder()
                .id(TEST_SONG_ID).title(TEST_SONG_TITLE)
                .genre(TEST_SONG_GENRE).artistName(TEST_ARTIST_NAME).build();

        testPlaylist = PlaylistDTO.builder()
                .id(TEST_PLAYLIST_ID).name(TEST_PLAYLIST_NAME)
                .userId(TEST_USER_ID).publicPlaylist(true).build();

        testArtist = ArtistDTO.builder()
                .id(TEST_ARTIST_ID).artistName(TEST_ARTIST_NAME)
                .bio(TEST_ARTIST_BIO).songs(Collections.emptyList()).albums(Collections.emptyList()).build();

        testUser = User.builder()
                .id(TEST_USER_ID).email(TEST_USER_EMAIL).username(TEST_USER_USERNAME)
                .role(Role.LISTENER).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();

        testArtistEntity = new Artist();
        testArtistEntity.setId(TEST_ARTIST_ID);
        testArtistEntity.setUserId(TEST_USER_ID);
        testArtistEntity.setArtistName(TEST_ARTIST_NAME);
    }

    // ── GET / — Home page ─────────────────────────────────────────

    @Test
    @DisplayName("GET / - authenticated - returns index view with model attributes")
    @WithMockUser(username = TEST_USER_EMAIL, roles = "LISTENER")
    void getHome_authenticated_returnsIndexViewWithAttributes() throws Exception {
        Page<SongDTO> page = new PageImpl<>(List.of(testSong));
        when(songService.getAllSongs(any(Pageable.class))).thenReturn(page);
        when(songService.getTrendingSongs(10)).thenReturn(List.of(testSong));
        when(playlistService.getMyPlaylists()).thenReturn(List.of(testPlaylist));
        when(playlistService.getPublicPlaylists(any())).thenReturn(Collections.emptyList());
        when(artistCatalogService.getFeaturedArtists(8)).thenReturn(List.of(testArtist));

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("recentSongs", "trendingSongs",
                        "myPlaylists", "publicPlaylists", "featuredArtists"));
    }

    @Test
    @DisplayName("GET / - unauthenticated - returns index view without user data")
    void getHome_unauthenticated_returnsIndexView() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));
    }

    @Test
    @DisplayName("GET / - authenticated - playlist service failure is handled gracefully")
    @WithMockUser(username = TEST_USER_EMAIL, roles = "LISTENER")
    void getHome_playlistServiceFails_handledGracefully() throws Exception {
        Page<SongDTO> page = new PageImpl<>(List.of(testSong));
        when(songService.getAllSongs(any())).thenReturn(page);
        when(songService.getTrendingSongs(10)).thenReturn(Collections.emptyList());
        when(playlistService.getMyPlaylists()).thenThrow(new RuntimeException("DB error"));
        when(playlistService.getPublicPlaylists(any())).thenThrow(new RuntimeException("DB error"));
        when(artistCatalogService.getFeaturedArtists(8)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));
    }

    // ── GET /playlists ────────────────────────────────────────────

    @Test
    @DisplayName("GET /playlists - authenticated - returns playlist view")
    @WithMockUser(username = TEST_USER_EMAIL, roles = "LISTENER")
    void getPlaylists_authenticated_returnsPlaylistView() throws Exception {
        when(playlistService.getMyPlaylists()).thenReturn(List.of(testPlaylist));
        when(playlistService.getPublicPlaylists(any())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/playlists"))
                .andExpect(status().isOk())
                .andExpect(view().name("playlist"))
                .andExpect(model().attributeExists("playlists", "publicPlaylists", "activeTab"));
    }

    @Test
    @DisplayName("GET /playlists - unauthenticated - redirects to login")
    void getPlaylists_unauthenticated_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/playlists"))
                .andExpect(status().is3xxRedirection());
    }

    // ── GET /playlists/{id} ───────────────────────────────────────

    @Test
    @DisplayName("GET /playlists/{id} - authenticated - returns playlist detail")
    @WithMockUser(username = TEST_USER_EMAIL, roles = "LISTENER")
    void getPlaylistDetail_authenticated_returnsPlaylistDetail() throws Exception {
        when(playlistService.getPlaylistById(TEST_PLAYLIST_ID)).thenReturn(testPlaylist);
        when(playlistSongRepository.findByPlaylistIdWithSongDetails(TEST_PLAYLIST_ID))
                .thenReturn(Collections.emptyList());
        when(securityUtils.getCurrentUser()).thenReturn(testUser);

        mockMvc.perform(get("/playlists/{id}", TEST_PLAYLIST_ID))
                .andExpect(status().isOk())
                .andExpect(view().name("playlist"))
                .andExpect(model().attributeExists("playlist", "songs", "isOwner"));
    }

    @Test
    @DisplayName("GET /playlists/{id} - owner - isOwner is true")
    @WithMockUser(username = TEST_USER_EMAIL, roles = "LISTENER")
    void getPlaylistDetail_owner_isOwnerTrue() throws Exception {
        when(playlistService.getPlaylistById(TEST_PLAYLIST_ID)).thenReturn(testPlaylist);
        when(playlistSongRepository.findByPlaylistIdWithSongDetails(TEST_PLAYLIST_ID))
                .thenReturn(Collections.emptyList());
        when(securityUtils.getCurrentUser()).thenReturn(testUser);

        mockMvc.perform(get("/playlists/{id}", TEST_PLAYLIST_ID))
                .andExpect(model().attribute("isOwner", true));
    }

    // ── GET /search ───────────────────────────────────────────────

    @Test
    @DisplayName("GET /search - with query - returns search view with results")
    @WithMockUser(username = TEST_USER_EMAIL, roles = "LISTENER")
    void getSearch_withQuery_returnsSearchResults() throws Exception {
        Page<SongDTO> page = new PageImpl<>(List.of(testSong));
        when(songService.searchSongs(eq("test"), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/search").param("q", "test"))
                .andExpect(status().isOk())
                .andExpect(view().name("search"))
                .andExpect(model().attributeExists("results", "query"));
    }

    @Test
    @DisplayName("GET /search - no query - returns search view with null results")
    @WithMockUser(username = TEST_USER_EMAIL, roles = "LISTENER")
    void getSearch_noQuery_returnsSearchViewNullResults() throws Exception {
        mockMvc.perform(get("/search"))
                .andExpect(status().isOk())
                .andExpect(view().name("search"))
                .andExpect(model().attribute("results", org.hamcrest.Matchers.nullValue()));
    }

    @Test
    @DisplayName("GET /search - unauthenticated - returns 200 (public endpoint)")
    void getSearch_unauthenticated_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/search").param("q", "test"))
                .andExpect(status().isOk());
    }

    // ── GET /artist/{id} ──────────────────────────────────────────

    @Test
    @DisplayName("GET /artist/{id} - authenticated - returns artist-profile view")
    @WithMockUser(username = TEST_USER_EMAIL, roles = "LISTENER")
    void getArtistProfile_authenticated_returnsArtistProfileView() throws Exception {
        when(artistCatalogService.getArtistById(TEST_ARTIST_ID)).thenReturn(testArtist);

        mockMvc.perform(get("/artist/{id}", TEST_ARTIST_ID))
                .andExpect(status().isOk())
                .andExpect(view().name("artist-profile"))
                .andExpect(model().attributeExists("artist"));
    }

    // ── GET /artist/dashboard ─────────────────────────────────────

    @Test
    @DisplayName("GET /artist/dashboard - artist with profile - returns artist-dashboard view")
    @WithMockUser(username = TEST_USER_USERNAME, roles = "ARTIST")
    void getArtistDashboard_artistWithProfile_returnsDashboard() throws Exception {
        when(artistService.getMyProfile()).thenReturn(
                com.revplay.dto.ArtistProfileResponse.builder()
                        .id(TEST_ARTIST_ID).artistName(TEST_ARTIST_NAME).build());
        when(userRepository.findByEmailOrUsername(TEST_USER_USERNAME, TEST_USER_USERNAME))
                .thenReturn(Optional.of(testUser));
        when(artistRepository.findByUserId(TEST_USER_ID)).thenReturn(Optional.of(testArtistEntity));
        when(artistCatalogService.getArtistById(TEST_ARTIST_ID)).thenReturn(testArtist);
        when(songService.getArtistAllSongs(TEST_ARTIST_ID)).thenReturn(List.of(testSong));

        mockMvc.perform(get("/artist/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("artist-dashboard"));
    }



    // ── GET /artist/songs ─────────────────────────────────────────

    @Test
    @DisplayName("GET /artist/songs - artist - returns artist-songs view")
    @WithMockUser(username = TEST_USER_USERNAME, roles = "ARTIST")
    void getArtistSongs_artist_returnsArtistSongsView() throws Exception {
        when(userRepository.findByEmailOrUsername(TEST_USER_USERNAME, TEST_USER_USERNAME))
                .thenReturn(Optional.of(testUser));
        when(artistRepository.findByUserId(TEST_USER_ID)).thenReturn(Optional.of(testArtistEntity));
        when(songService.getArtistAllSongs(TEST_ARTIST_ID)).thenReturn(List.of(testSong));
        when(artistCatalogService.getArtistById(TEST_ARTIST_ID)).thenReturn(testArtist);

        mockMvc.perform(get("/artist/songs"))
                .andExpect(status().isOk())
                .andExpect(view().name("artist-songs"));
    }



    // ── GET /artist/albums ────────────────────────────────────────

    @Test
    @DisplayName("GET /artist/albums - artist - returns artist-albums view")
    @WithMockUser(username = TEST_USER_USERNAME, roles = "ARTIST")
    void getArtistAlbums_artist_returnsArtistAlbumsView() throws Exception {
        when(userRepository.findByEmailOrUsername(TEST_USER_USERNAME, TEST_USER_USERNAME))
                .thenReturn(Optional.of(testUser));
        when(artistRepository.findByUserId(TEST_USER_ID)).thenReturn(Optional.of(testArtistEntity));
        when(artistCatalogService.getArtistById(TEST_ARTIST_ID)).thenReturn(testArtist);
        when(songService.getArtistAllSongs(TEST_ARTIST_ID)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/artist/albums"))
                .andExpect(status().isOk())
                .andExpect(view().name("artist-albums"));
    }



    // ── GET /admin ────────────────────────────────────────────────

    @Test
    @DisplayName("GET /admin - admin - returns admin view")
    @WithMockUser(username = TEST_USER_EMAIL, roles = "ADMIN")
    void getAdminPanel_admin_returnsAdminView() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin"));
    }

    @Test
    @DisplayName("GET /admin - listener - returns 403")
    @WithMockUser(username = TEST_USER_EMAIL, roles = "LISTENER")
    void getAdminPanel_listener_returns403() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().isForbidden());
    }

    // ── GET /favorites ────────────────────────────────────────────

    @Test
    @DisplayName("GET /favorites - authenticated - returns favorites view with songs")
    @WithMockUser(username = TEST_USER_EMAIL, roles = "LISTENER")
    void getFavorites_authenticated_returnsFavoritesView() throws Exception {
        when(favoriteService.getMyFavorites()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/favorites"))
                .andExpect(status().isOk())
                .andExpect(view().name("favorites"))
                .andExpect(model().attributeExists("songs"));
    }

    @Test
    @DisplayName("GET /favorites - unauthenticated - redirects to login")
    void getFavorites_unauthenticated_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/favorites"))
                .andExpect(status().is3xxRedirection());
    }

    // ── GET /about ────────────────────────────────────────────────

    @Test
    @DisplayName("GET /about - public - returns about view")
    void getAbout_public_returnsAboutView() throws Exception {
        mockMvc.perform(get("/about"))
                .andExpect(status().isOk())
                .andExpect(view().name("about"));
    }

    // ── GET /history ──────────────────────────────────────────────

    @Test
    @DisplayName("GET /history - authenticated - returns history view with entries")
    @WithMockUser(username = TEST_USER_EMAIL, roles = "LISTENER")
    void getHistory_authenticated_returnsHistoryView() throws Exception {
        when(historyService.getAllHistory()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/history"))
                .andExpect(status().isOk())
                .andExpect(view().name("history"))
                .andExpect(model().attributeExists("historyEntries", "songs"));
    }

    @Test
    @DisplayName("GET /history - unauthenticated - redirects to login")
    void getHistory_unauthenticated_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/history"))
                .andExpect(status().is3xxRedirection());
    }
}
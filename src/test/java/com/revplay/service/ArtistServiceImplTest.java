package com.revplay.service;

import com.revplay.dto.ArtistProfileResponse;
import com.revplay.dto.ArtistRegisterRequest;
import com.revplay.dto.ArtistUpdateRequest;
import com.revplay.exception.DuplicateResourceException;
import com.revplay.exception.ResourceNotFoundException;
import com.revplay.model.Artist;
import com.revplay.model.Role;
import com.revplay.model.User;
import com.revplay.repository.ArtistRepository;
import com.revplay.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ArtistServiceImpl.
 *
 * ArtistServiceImpl.getCurrentUser() reads from SecurityContextHolder directly
 * (same pattern as PlaylistService), then calls
 * userRepository.findByEmailOrUsername(username, username).
 *
 * Setup: wire a real SecurityContext in @BeforeEach, clear in @AfterEach.
 * Do NOT run in parallel — SecurityContextHolder is static global state.
 */
@ExtendWith(MockitoExtension.class)
class ArtistServiceImplTest {

    @Mock private ArtistRepository artistRepository;
    @Mock private UserRepository   userRepository;

    @InjectMocks
    private ArtistServiceImpl artistService;

    // ── Fixtures ──────────────────────────────────────────────────

    private User   currentUser;
    private Artist existingArtist;

    @BeforeEach
    void setUp() {
        currentUser = User.builder()
                .id(1L)
                .username("aria_artist")
                .email("aria@mail.com")
                .passwordHash("hash")
                .role(Role.ARTIST)
                .build();

        existingArtist = new Artist();
        existingArtist.setId(10L);
        existingArtist.setUserId(1L);
        existingArtist.setArtistName("Aria");
        existingArtist.setBio("Indie pop singer.");
        existingArtist.setGenre("Indie");
        existingArtist.setInstagram("https://instagram.com/aria");
        existingArtist.setTwitter("https://twitter.com/aria");
        existingArtist.setYoutube(null);
        existingArtist.setSpotify(null);
        existingArtist.setWebsite(null);

        // Wire SecurityContextHolder → "aria_artist" → currentUser
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("aria_artist");
        SecurityContext ctx = mock(SecurityContext.class);
        when(ctx.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(ctx);

        when(userRepository.findByEmailOrUsername("aria_artist", "aria_artist"))
                .thenReturn(Optional.of(currentUser));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // ── registerArtist ────────────────────────────────────────────

    @Test
    @DisplayName("registerArtist - new user - artist saved and response returned")
    void registerArtist_newUser_savedAndResponseReturned() {
        ArtistRegisterRequest request = new ArtistRegisterRequest();
        request.setArtistName("Aria");
        request.setBio("Indie pop singer.");
        request.setGenre("Indie");

        when(artistRepository.existsByUserId(1L)).thenReturn(false);
        when(artistRepository.save(any(Artist.class))).thenReturn(existingArtist);

        ArtistProfileResponse result = artistService.registerArtist(request);

        assertNotNull(result);
        assertEquals("Aria", result.getArtistName());
        verify(artistRepository, times(1)).save(any(Artist.class));
    }

    @Test
    @DisplayName("registerArtist - saves artist with current user's id")
    void registerArtist_savesWithCurrentUserId() {
        ArtistRegisterRequest request = new ArtistRegisterRequest();
        request.setArtistName("Aria");

        when(artistRepository.existsByUserId(1L)).thenReturn(false);
        when(artistRepository.save(any(Artist.class))).thenReturn(existingArtist);

        artistService.registerArtist(request);

        verify(artistRepository).save(argThat(a ->
                a.getUserId().equals(1L)
        ));
    }

    @Test
    @DisplayName("registerArtist - maps all request fields onto saved artist")
    void registerArtist_mapsAllRequestFields() {
        ArtistRegisterRequest request = new ArtistRegisterRequest();
        request.setArtistName("Aria");
        request.setBio("Indie pop singer.");
        request.setGenre("Indie");
        request.setInstagram("https://instagram.com/aria");
        request.setTwitter("https://twitter.com/aria");
        request.setYoutube("https://youtube.com/aria");
        request.setSpotify("https://spotify.com/aria");
        request.setWebsite("https://aria.com");

        when(artistRepository.existsByUserId(1L)).thenReturn(false);
        when(artistRepository.save(any(Artist.class))).thenReturn(existingArtist);

        artistService.registerArtist(request);

        verify(artistRepository).save(argThat(a ->
                "Aria".equals(a.getArtistName())
                        && "Indie pop singer.".equals(a.getBio())
                        && "Indie".equals(a.getGenre())
                        && "https://instagram.com/aria".equals(a.getInstagram())
                        && "https://twitter.com/aria".equals(a.getTwitter())
        ));
    }

    @Test
    @DisplayName("registerArtist - duplicate user - throws DuplicateResourceException")
    void registerArtist_duplicateUser_throwsDuplicateResourceException() {
        ArtistRegisterRequest request = new ArtistRegisterRequest();
        request.setArtistName("Aria");

        when(artistRepository.existsByUserId(1L)).thenReturn(true);

        assertThrows(DuplicateResourceException.class,
                () -> artistService.registerArtist(request));

        verify(artistRepository, never()).save(any());
    }

    @Test
    @DisplayName("registerArtist - duplicate user - save never called")
    void registerArtist_duplicateUser_saveNeverCalled() {
        ArtistRegisterRequest request = new ArtistRegisterRequest();
        request.setArtistName("Aria");

        when(artistRepository.existsByUserId(1L)).thenReturn(true);

        assertThrows(DuplicateResourceException.class,
                () -> artistService.registerArtist(request));

        verify(artistRepository, never()).save(any());
    }

    @Test
    @DisplayName("registerArtist - response maps all ArtistProfileResponse fields")
    void registerArtist_responseMapsAllFields() {
        ArtistRegisterRequest request = new ArtistRegisterRequest();
        request.setArtistName("Aria");
        request.setBio("Indie pop singer.");
        request.setGenre("Indie");
        request.setInstagram("https://instagram.com/aria");
        request.setTwitter("https://twitter.com/aria");

        when(artistRepository.existsByUserId(1L)).thenReturn(false);
        when(artistRepository.save(any(Artist.class))).thenReturn(existingArtist);

        ArtistProfileResponse result = artistService.registerArtist(request);

        assertEquals(10L,                          result.getId());
        assertEquals("Aria",                       result.getArtistName());
        assertEquals("Indie pop singer.",          result.getBio());
        assertEquals("Indie",                      result.getGenre());
        assertEquals("https://instagram.com/aria", result.getInstagram());
        assertEquals("https://twitter.com/aria",   result.getTwitter());
    }

    // ── getMyProfile ──────────────────────────────────────────────

    @Test
    @DisplayName("getMyProfile - artist exists - returns profile")
    void getMyProfile_artistExists_returnsProfile() {
        when(artistRepository.findByUserId(1L))
                .thenReturn(Optional.of(existingArtist));

        ArtistProfileResponse result = artistService.getMyProfile();

        assertNotNull(result);
        assertEquals("Aria", result.getArtistName());
        assertEquals(10L,    result.getId());
    }

    @Test
    @DisplayName("getMyProfile - maps all response fields correctly")
    void getMyProfile_mapsAllFields() {
        when(artistRepository.findByUserId(1L))
                .thenReturn(Optional.of(existingArtist));

        ArtistProfileResponse result = artistService.getMyProfile();

        assertEquals(10L,                          result.getId());
        assertEquals("Aria",                       result.getArtistName());
        assertEquals("Indie pop singer.",          result.getBio());
        assertEquals("Indie",                      result.getGenre());
        assertEquals("https://instagram.com/aria", result.getInstagram());
        assertEquals("https://twitter.com/aria",   result.getTwitter());
        assertNull(result.getYoutube());
        assertNull(result.getSpotify());
        assertNull(result.getWebsite());
    }

    @Test
    @DisplayName("getMyProfile - no artist profile - throws ResourceNotFoundException")
    void getMyProfile_noArtistProfile_throwsResourceNotFoundException() {
        when(artistRepository.findByUserId(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> artistService.getMyProfile());
    }

    @Test
    @DisplayName("getMyProfile - queries by current user id only")
    void getMyProfile_queriesWithCurrentUserId() {
        when(artistRepository.findByUserId(1L))
                .thenReturn(Optional.of(existingArtist));

        artistService.getMyProfile();

        verify(artistRepository).findByUserId(1L);
        verify(artistRepository, never()).findByUserId(2L);
    }

    // ── updateProfile ─────────────────────────────────────────────

    @Test
    @DisplayName("updateProfile - all fields provided - all fields updated")
    void updateProfile_allFieldsProvided_allUpdated() {
        ArtistUpdateRequest request = new ArtistUpdateRequest();
        request.setArtistName("Aria Updated");
        request.setBio("New bio.");
        request.setGenre("Pop");
        request.setInstagram("https://instagram.com/aria2");
        request.setTwitter("https://twitter.com/aria2");
        request.setYoutube("https://youtube.com/aria");
        request.setSpotify("https://spotify.com/aria");
        request.setWebsite("https://aria.com");

        when(artistRepository.findByUserId(1L))
                .thenReturn(Optional.of(existingArtist));
        when(artistRepository.save(any(Artist.class)))
                .thenReturn(existingArtist);

        artistService.updateProfile(request);

        verify(artistRepository).save(argThat(a ->
                "Aria Updated".equals(a.getArtistName())
                        && "New bio.".equals(a.getBio())
                        && "Pop".equals(a.getGenre())
        ));
    }

    @Test
    @DisplayName("updateProfile - null fields in request - existing values not overwritten")
    void updateProfile_nullFields_existingValuesPreserved() {
        // Only bio is being updated; artistName and genre are null → must stay unchanged
        ArtistUpdateRequest request = new ArtistUpdateRequest();
        request.setArtistName(null);
        request.setBio("Updated bio only.");
        request.setGenre(null);

        when(artistRepository.findByUserId(1L))
                .thenReturn(Optional.of(existingArtist));
        when(artistRepository.save(any(Artist.class)))
                .thenReturn(existingArtist);

        artistService.updateProfile(request);

        // artistName and genre must be untouched
        verify(artistRepository).save(argThat(a ->
                "Aria".equals(a.getArtistName())      // original value preserved
                        && "Updated bio only.".equals(a.getBio())
                        && "Indie".equals(a.getGenre())        // original value preserved
        ));
    }

    @Test
    @DisplayName("updateProfile - all nulls in request - nothing changed, save still called")
    void updateProfile_allNulls_saveCalledWithUnchangedArtist() {
        ArtistUpdateRequest request = new ArtistUpdateRequest();
        // All fields null — nothing should change

        when(artistRepository.findByUserId(1L))
                .thenReturn(Optional.of(existingArtist));
        when(artistRepository.save(any(Artist.class)))
                .thenReturn(existingArtist);

        artistService.updateProfile(request);

        verify(artistRepository, times(1)).save(argThat(a ->
                "Aria".equals(a.getArtistName())
                        && "Indie pop singer.".equals(a.getBio())
        ));
    }

    @Test
    @DisplayName("updateProfile - no artist profile - throws ResourceNotFoundException")
    void updateProfile_noArtistProfile_throwsResourceNotFoundException() {
        when(artistRepository.findByUserId(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> artistService.updateProfile(new ArtistUpdateRequest()));

        verify(artistRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateProfile - save called exactly once")
    void updateProfile_saveCalledExactlyOnce() {
        ArtistUpdateRequest request = new ArtistUpdateRequest();
        request.setArtistName("New Name");

        when(artistRepository.findByUserId(1L))
                .thenReturn(Optional.of(existingArtist));
        when(artistRepository.save(any(Artist.class)))
                .thenReturn(existingArtist);

        artistService.updateProfile(request);

        verify(artistRepository, times(1)).save(any(Artist.class));
    }
}
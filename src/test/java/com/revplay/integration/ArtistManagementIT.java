package com.revplay.integration;

import com.revplay.model.Artist;
import com.revplay.model.User;
import com.revplay.repository.ArtistRepository;
import com.revplay.repository.UserRepository;
import com.revplay.util.IntegrationTestBase;
import com.revplay.util.TestDataBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests — Artist management endpoints (Member 6).
 *
 * Endpoints under test:
 *   POST /api/artists/register   create artist profile for logged-in ARTIST user
 *   GET  /api/artists/me         fetch current artist's own profile
 *   PUT  /api/artists/update     partial update of artist profile (no @Valid — all optional)
 *
 * Security model (SecurityConfig):
 *   /api/artists/me/**        → hasRole('ARTIST')
 *   /api/artists/register     → anyRequest().authenticated() + service enforces ARTIST
 *   Unauthenticated           → 401
 *   LISTENER on artist paths  → 403
 *
 * Principal resolution (ArtistServiceImpl.getCurrentUser):
 *   SecurityContextHolder → getName() → userRepository.findByEmailOrUsername(name, name)
 *   Every test that reaches the service layer must have a User row in the DB
 *   whose username matches the one placed in the SecurityContext.
 *   We use SecurityMockMvcRequestPostProcessors.user(...).roles(...) for per-test
 *   control rather than class-level @WithMockUser.
 *
 * @Transactional on IntegrationTestBase auto-rolls back after every test.
 */
@DisplayName("Artist Management — Integration Tests")
class ArtistManagementIT extends IntegrationTestBase {

    @Autowired private UserRepository   userRepository;
    @Autowired private ArtistRepository artistRepository;

    private static final String REGISTER       = "/api/artists/register";
    private static final String ME             = "/api/artists/me";
    private static final String UPDATE         = "/api/artists/update";
    private static final String ARTIST_USERNAME = "it_artist_mgmt";

    // ── Seed helpers ──────────────────────────────────────────────────────────

    /**
     * Saves a User with username=ARTIST_USERNAME in the DB so
     * ArtistServiceImpl.getCurrentUser() can resolve the SecurityContext principal.
     * passwordHash must be non-null per schema constraint.
     */
    private User seedArtistUser() {
        return userRepository.save(
                TestDataBuilder.aUser()
                        .withId(null)
                        .withUsername(ARTIST_USERNAME)
                        .withEmail(ARTIST_USERNAME + "@revplay.com")
                        .asArtist()
                        .build());
    }

    /** Saves an Artist profile linked to the given userId (for duplicate/get/update tests). */
    private Artist seedArtistProfile(Long userId) {
        return artistRepository.save(
                TestDataBuilder.anArtist()
                        .withId(null)
                        .withUserId(userId)
                        .withArtistName("Aria")
                        .withGenre("Indie")
                        .withBio("Indie pop singer-songwriter.")
                        .build());
    }

    /** Authenticated ARTIST principal whose username matches ARTIST_USERNAME. */
    private SecurityMockMvcRequestPostProcessors.UserRequestPostProcessor asArtist() {
        return user(ARTIST_USERNAME).roles("ARTIST");
    }

    /** Authenticated LISTENER principal — used to assert 403 on artist-only endpoints. */
    private SecurityMockMvcRequestPostProcessors.UserRequestPostProcessor asListener() {
        return user("it_listener_mgmt").roles("LISTENER");
    }

    // =========================================================================
    // POST /api/artists/register
    // =========================================================================

    @Nested
    @DisplayName("POST /api/artists/register")
    class RegisterArtist {

        @Test
        @DisplayName("valid request - returns 201 with artist profile")
        void registerArtist_validRequest_returns201() throws Exception {
            seedArtistUser();

            mockMvc.perform(post(REGISTER)
                            .with(asArtist())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"artistName":"Aria","genre":"Indie",
                                     "bio":"Indie pop singer-songwriter."}
                                    """))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.artistName").value("Aria"))
                    .andExpect(jsonPath("$.genre").value("Indie"))
                    .andExpect(jsonPath("$.bio").value("Indie pop singer-songwriter."));
        }

        @Test
        @DisplayName("valid request with all fields - maps full ArtistProfileResponse")
        void registerArtist_allFields_mapsFullResponse() throws Exception {
            seedArtistUser();

            mockMvc.perform(post(REGISTER)
                            .with(asArtist())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "artistName":"Aria",
                                      "genre":"Indie",
                                      "bio":"Indie pop singer-songwriter.",
                                      "instagram":"https://instagram.com/aria",
                                      "twitter":"https://twitter.com/aria",
                                      "spotify":"https://open.spotify.com/artist/aria",
                                      "youtube":"https://youtube.com/@aria",
                                      "website":"https://aria.music"
                                    }
                                    """))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.artistName").value("Aria"))
                    .andExpect(jsonPath("$.genre").value("Indie"))
                    .andExpect(jsonPath("$.instagram").value("https://instagram.com/aria"))
                    .andExpect(jsonPath("$.twitter").value("https://twitter.com/aria"))
                    .andExpect(jsonPath("$.spotify").value("https://open.spotify.com/artist/aria"))
                    .andExpect(jsonPath("$.youtube").value("https://youtube.com/@aria"))
                    .andExpect(jsonPath("$.website").value("https://aria.music"));
        }

        @Test
        @DisplayName("optional social fields omitted - absent/null in response")
        void registerArtist_optionalFieldsOmitted_nullInResponse() throws Exception {
            seedArtistUser();

            mockMvc.perform(post(REGISTER)
                            .with(asArtist())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"artistName":"Aria"}
                                    """))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.instagram").doesNotExist())
                    .andExpect(jsonPath("$.twitter").doesNotExist())
                    .andExpect(jsonPath("$.spotify").doesNotExist())
                    .andExpect(jsonPath("$.youtube").doesNotExist())
                    .andExpect(jsonPath("$.website").doesNotExist());
        }

        @Test
        @DisplayName("duplicate registration - returns 409 Conflict")
        void registerArtist_duplicateProfile_returns409() throws Exception {
            User user = seedArtistUser();
            seedArtistProfile(user.getId()); // artist profile already exists

            mockMvc.perform(post(REGISTER)
                            .with(asArtist())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"artistName":"Aria Again"}
                                    """))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("duplicate registration - error body contains message")
        void registerArtist_duplicateProfile_errorBodyHasMessage() throws Exception {
            User user = seedArtistUser();
            seedArtistProfile(user.getId());

            mockMvc.perform(post(REGISTER)
                            .with(asArtist())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"artistName":"Aria Again"}
                                    """))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").exists());
        }

        @Test
        @DisplayName("missing artistName (@NotBlank) - returns 400")
        void registerArtist_missingArtistName_returns400() throws Exception {
            seedArtistUser();

            // No artistName field — @Valid + @NotBlank on ArtistRegisterRequest rejects it
            mockMvc.perform(post(REGISTER)
                            .with(asArtist())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"genre":"Indie","bio":"A bio."}
                                    """))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("blank artistName - returns 400")
        void registerArtist_blankArtistName_returns400() throws Exception {
            seedArtistUser();

            mockMvc.perform(post(REGISTER)
                            .with(asArtist())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"artistName":"   ","genre":"Indie"}
                                    """))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("unauthenticated - returns 401")
        void registerArtist_unauthenticated_returns401() throws Exception {
            mockMvc.perform(post(REGISTER)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"artistName":"Aria"}
                                    """))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("LISTENER role - returns 403")
        void registerArtist_listenerRole_returns403() throws Exception {
            mockMvc.perform(post(REGISTER)
                            .with(asListener())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"artistName":"Aria"}
                                    """))
                    .andExpect(status().isForbidden());
        }
    }

    // =========================================================================
    // GET /api/artists/me
    // =========================================================================

    @Nested
    @DisplayName("GET /api/artists/me")
    class GetMyProfile {

        @Test
        @DisplayName("ARTIST with profile - returns 200 with correct profile")
        void getMyProfile_artistWithProfile_returns200() throws Exception {
            User user = seedArtistUser();
            seedArtistProfile(user.getId());

            mockMvc.perform(get(ME).with(asArtist()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.artistName").value("Aria"))
                    .andExpect(jsonPath("$.genre").value("Indie"))
                    .andExpect(jsonPath("$.bio").value("Indie pop singer-songwriter."));
        }

        @Test
        @DisplayName("ARTIST with profile - maps all ArtistProfileResponse fields")
        void getMyProfile_mapsAllResponseFields() throws Exception {
            User user = seedArtistUser();
            seedArtistProfile(user.getId());

            mockMvc.perform(get(ME).with(asArtist()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.artistName").value("Aria"))
                    .andExpect(jsonPath("$.genre").value("Indie"))
                    .andExpect(jsonPath("$.bio").value("Indie pop singer-songwriter."));
        }

        @Test
        @DisplayName("ARTIST with no profile - returns 404")
        void getMyProfile_noArtistProfile_returns404() throws Exception {
            seedArtistUser(); // User in DB but no Artist row

            mockMvc.perform(get(ME).with(asArtist()))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("no profile - error body has status=404 and message")
        void getMyProfile_noProfile_errorBodyPresent() throws Exception {
            seedArtistUser();

            mockMvc.perform(get(ME).with(asArtist()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.message").exists());
        }

        @Test
        @DisplayName("unauthenticated - returns 401")
        void getMyProfile_unauthenticated_returns401() throws Exception {
            mockMvc.perform(get(ME))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("LISTENER role - returns 403 (/api/artists/me/** requires ARTIST)")
        void getMyProfile_listenerRole_returns403() throws Exception {
            mockMvc.perform(get(ME).with(asListener()))
                    .andExpect(status().isForbidden());
        }
    }

    // =========================================================================
    // PUT /api/artists/update
    // =========================================================================

    @Nested
    @DisplayName("PUT /api/artists/update")
    class UpdateProfile {

        @Test
        @DisplayName("valid update - returns 200 with updated fields")
        void updateProfile_validRequest_returns200WithUpdatedFields() throws Exception {
            User user = seedArtistUser();
            seedArtistProfile(user.getId());

            mockMvc.perform(put(UPDATE)
                            .with(asArtist())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"bio":"Updated bio.","genre":"Electronic"}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.bio").value("Updated bio."))
                    .andExpect(jsonPath("$.genre").value("Electronic"));
        }

        @Test
        @DisplayName("partial update - only provided fields change, others unchanged")
        void updateProfile_partialUpdate_onlyProvidedFieldsChange() throws Exception {
            User user = seedArtistUser();
            seedArtistProfile(user.getId());

            // Only bio — artistName must stay "Aria" (null-safe update in service)
            mockMvc.perform(put(UPDATE)
                            .with(asArtist())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"bio":"New bio only."}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.bio").value("New bio only."))
                    .andExpect(jsonPath("$.artistName").value("Aria"));
        }

        @Test
        @DisplayName("update social links - correct values in response")
        void updateProfile_socialLinks_updatedCorrectly() throws Exception {
            User user = seedArtistUser();
            seedArtistProfile(user.getId());

            mockMvc.perform(put(UPDATE)
                            .with(asArtist())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "instagram":"https://instagram.com/aria_new",
                                      "spotify":"https://open.spotify.com/artist/aria_new",
                                      "website":"https://aria-new.music"
                                    }
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.instagram").value("https://instagram.com/aria_new"))
                    .andExpect(jsonPath("$.spotify").value("https://open.spotify.com/artist/aria_new"))
                    .andExpect(jsonPath("$.website").value("https://aria-new.music"));
        }

        @Test
        @DisplayName("empty body - returns 200 with no changes (null-safe update)")
        void updateProfile_emptyBody_returns200NoChanges() throws Exception {
            User user = seedArtistUser();
            seedArtistProfile(user.getId());

            // {} — all request fields null, service skips all updates
            mockMvc.perform(put(UPDATE)
                            .with(asArtist())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.artistName").value("Aria")); // unchanged
        }

        @Test
        @DisplayName("update artistName specifically - returns updated name")
        void updateProfile_updateArtistName_returnsUpdatedName() throws Exception {
            User user = seedArtistUser();
            seedArtistProfile(user.getId());

            mockMvc.perform(put(UPDATE)
                            .with(asArtist())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"artistName":"Aria Reborn"}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.artistName").value("Aria Reborn"));
        }

        @Test
        @DisplayName("no existing profile - returns 404")
        void updateProfile_noArtistProfile_returns404() throws Exception {
            seedArtistUser(); // no Artist row

            mockMvc.perform(put(UPDATE)
                            .with(asArtist())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"bio":"Will not save."}
                                    """))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("unauthenticated - returns 401")
        void updateProfile_unauthenticated_returns401() throws Exception {
            mockMvc.perform(put(UPDATE)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"bio":"Updated."}
                                    """))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("LISTENER role - returns 403")
        void updateProfile_listenerRole_returns403() throws Exception {
            mockMvc.perform(put(UPDATE)
                            .with(asListener())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"bio":"Updated."}
                                    """))
                    .andExpect(status().isForbidden());
        }
    }
}
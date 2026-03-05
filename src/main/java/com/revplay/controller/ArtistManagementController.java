package com.revplay.controller;

import com.revplay.dto.ArtistProfileResponse;
import com.revplay.dto.ArtistRegisterRequest;
import com.revplay.dto.ArtistUpdateRequest;
import com.revplay.exception.BadRequestException;
import com.revplay.exception.ResourceNotFoundException;
import com.revplay.model.Artist;
import com.revplay.model.User;
import com.revplay.repository.ArtistRepository;
import com.revplay.service.ArtistService;
import com.revplay.service.FileStorageService;
import com.revplay.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

@RestController
@RequestMapping("/api/artists")
@RequiredArgsConstructor
public class ArtistManagementController {

    private static final Logger log = LoggerFactory.getLogger(ArtistManagementController.class);

    private static final Set<String> ALLOWED_IMAGE_TYPES =
            Set.of("image/jpeg", "image/png", "image/webp", "image/gif");

    private final ArtistService artistService;
    private final ArtistRepository artistRepository;
    private final FileStorageService fileStorageService;
    private final SecurityUtils securityUtils;

    // ── Register ─────────────────────────────────────────────────────────────
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ArtistProfileResponse registerArtist(
            @Valid @RequestBody ArtistRegisterRequest request) {
        return artistService.registerArtist(request);
    }

    // ── Get My Profile ───────────────────────────────────────────────────────
    @GetMapping("/me")
    @PreAuthorize("hasRole('ARTIST')")
    public ArtistProfileResponse getMyProfile() {
        return artistService.getMyProfile();
    }

    // ── Update Profile (JSON) ────────────────────────────────────────────────
    @PutMapping("/update")
    @PreAuthorize("hasRole('ARTIST')")
    public ArtistProfileResponse updateProfile(
            @RequestBody ArtistUpdateRequest request) {
        return artistService.updateProfile(request);
    }

    // ── Upload Profile Picture ───────────────────────────────────────────────
    @PostMapping("/me/picture")
    @PreAuthorize("hasRole('ARTIST')")
    public ResponseEntity<ArtistProfileResponse> uploadProfilePicture(
            @RequestParam("file") MultipartFile file) {

        log.info("POST /api/artists/me/picture");
        validateImage(file);

        User user = securityUtils.getCurrentUser();
        Artist artist = artistRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Artist", "userId", user.getId()));

        String path = "/uploads/" + fileStorageService.storeFile(file, "artist-pictures");
        artist.setProfilePictureUrl(path);
        artistRepository.save(artist);

        log.info("Profile picture updated for artist {}", artist.getId());
        return ResponseEntity.ok(artistService.getMyProfile());
    }

    // ── Upload Banner Image ──────────────────────────────────────────────────
    @PostMapping("/me/banner")
    @PreAuthorize("hasRole('ARTIST')")
    public ResponseEntity<ArtistProfileResponse> uploadBannerImage(
            @RequestParam("file") MultipartFile file) {

        log.info("POST /api/artists/me/banner");
        validateImage(file);

        User user = securityUtils.getCurrentUser();
        Artist artist = artistRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Artist", "userId", user.getId()));

        String path = "/uploads/" + fileStorageService.storeFile(file, "artist-banners");
        artist.setBannerImageUrl(path);
        artistRepository.save(artist);

        log.info("Banner image updated for artist {}", artist.getId());
        return ResponseEntity.ok(artistService.getMyProfile());
    }

    private void validateImage(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BadRequestException("File is empty");
        }
        if (!ALLOWED_IMAGE_TYPES.contains(file.getContentType())) {
            throw new BadRequestException("Only JPEG, PNG, WebP, and GIF images are allowed");
        }
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new BadRequestException("Image must be under 5MB");
        }
    }
}
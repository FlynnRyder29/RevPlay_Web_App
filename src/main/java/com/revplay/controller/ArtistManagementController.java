package com.revplay.controller;

import com.revplay.dto.ArtistProfileResponse;
import com.revplay.dto.ArtistRegisterRequest;
import com.revplay.service.ArtistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/artists")
@RequiredArgsConstructor
public class ArtistManagementController {

    private final ArtistService artistService;

    // 1️⃣ Register artist profile
    @PostMapping("/register")
    public ArtistProfileResponse registerArtist(
            @Valid @RequestBody ArtistRegisterRequest request) {

        return artistService.registerArtist(request);
    }

    // 2️⃣ Get current logged-in artist profile
    @GetMapping("/me")
    public ArtistProfileResponse getMyProfile() {
        return artistService.getMyProfile();
    }

    // 3️⃣ Update artist profile
    @PutMapping("/update")
    public ArtistProfileResponse updateProfile(
            @Valid @RequestBody ArtistRegisterRequest request) {

        return artistService.updateProfile(request);
    }
}
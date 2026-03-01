package com.revplay.controller;

import com.revplay.dto.SongCreateRequest;
import com.revplay.dto.SongResponse;
import com.revplay.dto.SongUpdateRequest;
import com.revplay.service.SongService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/songs")
@RequiredArgsConstructor
public class SongController {

    private final SongService songService;

    // 1️⃣ Upload Song (Artist only)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SongResponse createSong(
            @Valid @RequestBody SongCreateRequest request) {

        return songService.createSong(request);
    }

    // 2️⃣ Update Song
    @PutMapping("/{id}")
    public SongResponse updateSong(
            @PathVariable Long id,
            @RequestBody SongUpdateRequest request) {

        return songService.updateSong(id, request);
    }

    // 3️⃣ Delete Song
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSong(@PathVariable Long id) {

        songService.deleteSong(id);
    }

    // 4️⃣ Toggle Visibility
    @PatchMapping("/{id}/visibility")
    public SongResponse toggleVisibility(@PathVariable Long id) {

        return songService.toggleVisibility(id);
    }
}
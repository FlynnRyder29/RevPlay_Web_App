package com.revplay.controller;

import com.revplay.model.Genre;
import com.revplay.repository.GenreRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/genres")
@RequiredArgsConstructor
public class GenreController {

    private static final Logger log = LoggerFactory.getLogger(GenreController.class);
    private final GenreRepository genreRepository;

    // NOTE: Genre is a simple read-only lookup table with no business logic.
    // Repository is accessed directly here intentionally to avoid boilerplate
    // service layer for a trivial findAll() call. Can be extracted to GenreService
    // if filtering or caching is needed in future.


    @GetMapping
    public ResponseEntity<List<Genre>> getAllGenres() {
        log.info("GET /api/genres");
        return ResponseEntity.ok(genreRepository.findAll());
    }
}

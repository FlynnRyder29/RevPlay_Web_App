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

    // GET /api/genres — list all genres
    @GetMapping
    public ResponseEntity<List<Genre>> getAllGenres() {
        log.info("GET /api/genres");
        return ResponseEntity.ok(genreRepository.findAll());
    }
}

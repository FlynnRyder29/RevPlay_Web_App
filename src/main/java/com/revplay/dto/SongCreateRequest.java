package com.revplay.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class SongCreateRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String genre;

    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be greater than 0")
    private Integer duration;

    @NotBlank(message = "Audio URL is required")
    private String audioUrl;

    private String coverImageUrl;

    private LocalDate releaseDate;

    private Long albumId; // optional
}
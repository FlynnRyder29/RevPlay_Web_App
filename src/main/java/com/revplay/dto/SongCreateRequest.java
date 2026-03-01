package com.revplay.dto;

import com.revplay.model.Song;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SongCreateRequest {

    @NotBlank
    private String title;

    private String genre;

    @NotNull
    @Positive
    private Integer duration;

    private String audioUrl;
    private String coverImageUrl;
    private LocalDate releaseDate;

    private Long albumId;

    private Song.Visibility visibility; // service sets default if null
}
package com.revplay.dto;

import com.revplay.model.Song;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SongDTO {

    private Long id;
    private String title;
    private String genre;
    private Integer duration;
    private String audioUrl;
    private String coverImageUrl;
    private LocalDate releaseDate;
    private Long playCount;
    private Song.Visibility visibility;

    // Flattened artist info — no nested entity exposed
    private Long artistId;
    private String artistName;

    // Flattened album info — nullable, song may not belong to an album
    private Long albumId;
    private String albumName;

    private LocalDateTime createdAt;
}

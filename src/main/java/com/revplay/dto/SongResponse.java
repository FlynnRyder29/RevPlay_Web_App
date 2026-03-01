package com.revplay.dto;

import com.revplay.model.Song;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SongResponse {

    private Long id;
    private String title;
    private String genre;
    private Integer duration;
    private String audioUrl;
    private String coverImageUrl;
    private LocalDate releaseDate;
    private Long playCount;
    private Song.Visibility visibility;
    private String artistName;
    private LocalDateTime createdAt;
}
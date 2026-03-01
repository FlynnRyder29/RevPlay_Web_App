package com.revplay.dto;

import com.revplay.model.Song;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SongUpdateRequest {

    private String title;
    private String genre;
    private Integer duration;
    private String audioUrl;
    private String coverImageUrl;
    private LocalDate releaseDate;
    private Long albumId;
    private Song.Visibility visibility;
}
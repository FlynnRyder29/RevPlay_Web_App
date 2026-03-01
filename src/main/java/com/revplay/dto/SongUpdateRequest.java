package com.revplay.dto;

import com.revplay.model.Song;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class SongUpdateRequest {

    private String title;
    private String genre;
    private Integer duration;
    private String audioUrl;
    private String coverImageUrl;
    private LocalDate releaseDate;

    // ✅ ADD THESE
    private Long albumId;
    private Song.Visibility visibility;
}
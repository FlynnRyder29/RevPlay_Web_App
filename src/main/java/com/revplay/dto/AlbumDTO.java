package com.revplay.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class AlbumDTO {
    private Long id;
    private String name;
    private String description;
    private String coverImageUrl;
    private LocalDate releaseDate;
    private Long artistId;
    private String artistName;
    private List<SongDTO> tracks;
}

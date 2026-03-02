package com.revplay.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlbumDTO {
    private Long id;

    // Required on create — validated via @Valid in ArtistAlbumController
    @NotBlank(message = "Album name cannot be blank")
    private String name;
    private String description;
    private String coverImageUrl;
    private LocalDate releaseDate;
    private Long artistId;
    private String artistName;

    // Song count inside this album — used in artist album list view
    private int songCount;

    private LocalDateTime createdAt;

    // Populated only in catalog detail view (AlbumCatalogService.getAlbumById)
    // Null in list responses — avoids over-fetching
    private List<SongDTO> tracks;
}

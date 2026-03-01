package com.revplay.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class ArtistDTO {
    private Long id;
    private String artistName;
    private String bio;
    private String genre;
    private String profilePictureUrl;
    private String bannerImageUrl;

    // Only populated for /api/artists/{id}
    private List<SongDTO> songs;
    private List<AlbumDTO> albums;
}

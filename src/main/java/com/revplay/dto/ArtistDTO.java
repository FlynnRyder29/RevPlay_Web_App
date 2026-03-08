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

    // Social links (added)
    private String instagram;
    private String twitter;
    private String youtube;
    private String spotify;
    private String website;

    // Only populated for detail views
    private List<SongDTO> songs;
    private List<AlbumDTO> albums;
}
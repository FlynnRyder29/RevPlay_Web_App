package com.revplay.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ArtistProfileResponse {

    private Long id;

    // User details
    private String username;
    private String email;

    // Artist details
    private String artistName;
    private String bio;
    private String genre;

    private String profilePictureUrl;
    private String bannerImageUrl;

    private String instagram;
    private String twitter;
    private String youtube;
    private String spotify;
    private String website;
}
package com.revplay.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArtistUpdateRequest {

    private String artistName;
    private String bio;
    private String genre;
    private String instagram;
    private String twitter;
    private String youtube;
    private String spotify;
    private String website;
}
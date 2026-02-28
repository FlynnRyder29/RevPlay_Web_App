package com.revplay.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ArtistRegisterRequest {

    @NotBlank(message = "Artist name is required")
    @Size(max = 150, message = "Artist name must not exceed 150 characters")
    private String artistName;

    @Size(max = 1000, message = "Bio is too long")
    private String bio;

    @Size(max = 100, message = "Genre must not exceed 100 characters")
    private String genre;

    private String instagram;
    private String twitter;
    private String youtube;
    private String spotify;
    private String website;
}
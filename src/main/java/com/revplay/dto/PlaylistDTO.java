package com.revplay.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlaylistDTO {

    private Long id;

    @NotBlank(message = "Playlist name is required")
    @Size(max = 100, message = "Playlist name cannot exceed 100 characters")
    private String name;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    @JsonProperty("isPublic")
    private boolean publicPlaylist;

    private String coverImageUrl;

    private Long userId;

    private int songCount;
    private String ownerName;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
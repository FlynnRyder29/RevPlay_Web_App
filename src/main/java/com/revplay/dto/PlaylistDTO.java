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

    // FIX: Rename field from "isPublic" to "publicPlaylist" to avoid Lombok/Jackson
    // is-prefix collision. @JsonProperty ensures JSON key stays "isPublic" for
    // backward compatibility with all frontend code.
    @JsonProperty("isPublic")
    private boolean publicPlaylist;

    private Long userId;

    private int songCount;
    private String ownerName;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
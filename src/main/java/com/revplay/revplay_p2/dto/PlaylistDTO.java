package com.revplay.revplay_p2.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PlaylistDTO {
    private Long id;
    private String name;
    private String description;
    private boolean isPublic;
    private Long userId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
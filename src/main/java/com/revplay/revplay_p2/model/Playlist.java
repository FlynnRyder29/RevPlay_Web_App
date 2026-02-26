package com.revplay.revplay_p2.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "playlists")
@Data
public class Playlist {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    private boolean isPublic;
    private Long userId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
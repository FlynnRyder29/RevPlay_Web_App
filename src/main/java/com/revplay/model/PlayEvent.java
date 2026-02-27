package com.revplay.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "play_events")
public class PlayEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "song_id", nullable = false)
    private Long songId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "played_at", nullable = false)
    private LocalDateTime playedAt;

    public PlayEvent() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PlayEvent(Long songId, Long userId, LocalDateTime playedAt) {
        this.songId = songId;
        this.userId = userId;
        this.playedAt = playedAt;
    }

    // Generate getters and setters
}
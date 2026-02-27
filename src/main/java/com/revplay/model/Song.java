package com.revplay.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "songs")
@Getter
@Setter
@NoArgsConstructor
@Builder
@ToString(exclude = {"genre", "artist", "album"})
@EqualsAndHashCode(exclude = {"genre", "artist", "album"})
public class Song {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    // genre stored as plain String to match seed data (VARCHAR in songs table)
    // genres table is for browsing/filtering via GET /api/genres — separate concern
    @Column(name = "genre")
    private String genre;

    @Column(nullable = false)
    private Integer duration; // in seconds

    @Column(name = "audio_url")
    private String audioUrl;

    @Column(name = "cover_image_url")
    private String coverImageUrl;

    @Column(name = "release_date")
    private LocalDate releaseDate;

    @Column(name = "play_count")
    @Builder.Default
    private Long playCount = 0L;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Visibility visibility = Visibility.PUBLIC;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "artist_id", nullable = false)
    private Artist artist;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "album_id")
    private Album album;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public enum Visibility {
        PUBLIC, UNLISTED, PRIVATE
    }
}

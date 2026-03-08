package com.revplay.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "artists")
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = "userId")
@EqualsAndHashCode(exclude = "userId")
public class Artist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "artist_name", nullable = false)
    private String artistName;

    @Column(columnDefinition = "TEXT")
    private String bio;

    private String genre;

    @Column(name = "profile_picture_url")
    private String profilePictureUrl;

    @Column(name = "banner_image_url")
    private String bannerImageUrl;

    private String instagram;
    private String twitter;
    private String youtube;
    private String spotify;
    private String website;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
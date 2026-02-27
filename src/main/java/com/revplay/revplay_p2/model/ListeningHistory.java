package com.revplay.revplay_p2.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "listening_history")
@Data
public class ListeningHistory {
<<<<<<< HEAD
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
=======
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
>>>>>>> 92cfd1a2bdda491fa75c04f993b7b58af38736c6
    private Long id;
    private Long userId;
    private Long songId;
    private LocalDateTime playedAt;
}
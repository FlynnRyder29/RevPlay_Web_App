package com.revplay.revplay_p2.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class FavoriteDTO {
    private Long id;
    private Long userId;
    private Long songId;
    private LocalDateTime createdAt;
}
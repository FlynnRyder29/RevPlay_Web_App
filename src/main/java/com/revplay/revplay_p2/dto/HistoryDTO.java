package com.revplay.revplay_p2.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class HistoryDTO {
    private Long id;
    private Long userId;
    private Long songId;
    private LocalDateTime playedAt;
}
package com.revplay.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteDTO {
    private Long id;
    private Long songId;
    private String songTitle;
    private String artistName;
    private String coverImageUrl;
    private Integer duration;
    private LocalDateTime createdAt;
}
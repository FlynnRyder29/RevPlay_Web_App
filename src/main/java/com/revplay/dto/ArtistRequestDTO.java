package com.revplay.dto;

import com.revplay.model.RequestStatus;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class ArtistRequestDTO {
    private Long id;
    private Long userId;
    private String username;
    private String email;
    private String displayName;
    private String profilePictureUrl;
    private String artistName;
    private String genre;
    private String reason;
    private RequestStatus status;
    private String adminNote;
    private Long reviewedBy;
    private LocalDateTime reviewedAt;
    private LocalDateTime createdAt;
}
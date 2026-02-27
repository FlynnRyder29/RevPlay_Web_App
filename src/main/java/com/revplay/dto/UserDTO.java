package com.revplay.dto;

import com.revplay.model.Role;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
@Data
@Builder
public class UserDTO {
    private Long id;
    private String email;
    private String username;
    private String displayName;
    private String bio;
    private String profilePictureUrl;
    private Role role;
    private LocalDateTime createdAt;
}

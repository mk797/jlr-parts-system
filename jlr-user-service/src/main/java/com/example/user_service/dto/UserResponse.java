package com.example.user_service.dto;


import com.example.user_service.entity.UserRole;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserResponse {

    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private UserRole role;
    private String dealerId;
    private String phoneNumber;
    private Boolean active;
    private LocalDateTime createdAt;


}

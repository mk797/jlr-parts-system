package com.example.user_service.dto;


import com.example.user_service.entity.UserRole;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponse {

    private String message;
    private Long userId;
    private String email;
    private UserRole role;
    private String dealerId;
    private Long expiresIn;

}

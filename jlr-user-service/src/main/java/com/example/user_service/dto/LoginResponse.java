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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }
}

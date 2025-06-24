package com.example.user_service.dto;


import com.example.user_service.entity.UserRole;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserRegistrationRequest {


    @Email(message = "Email should be valid")
    @NotBlank(message = "Email is required")
    private String email;


    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "password must be minimum 8 characters")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z\\d])[A-Za-z\\d\\W_]+$",
            message = "password must contain uppercase, lowercase, digit, and a special character"
    )
    private String password;


    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name cannot exceed 50 characters")
    private String firstName;

    @NotBlank(message = " Last Name name is required")
    @Size(max = 50, message = "Last name cannot exceed 50 characters")
    private String lastName;

    @NotNull(message = "User role is required")
    private UserRole role;

    @Size(max = 20, message = "phone number cannot exceed 20 characters")
    private String phoneNumber;

    private String dealerId;

}

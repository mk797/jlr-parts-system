package com.example.user_service.dto;


import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ErrorResponse {

    private String message;
    private String error;
    private int status;
    private LocalDateTime timestamp;
    private String path;

    private List<ValidationError> validationErrors;


    @Data
    @Builder
    public static class ValidationError{
        private String field;
        private String message;
        private Object rejectedValue;
    }
}

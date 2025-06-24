package com.example.user_service.config;


import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Data
@Component
@ConfigurationProperties(prefix = "app.jwt")
@Validated
public class JwtProperties {

    @NotEmpty(message = "JWT secret cannot be empty")
    @Size(min = 44, message = "JWT secret should be minimum 44 characters")
    private String Secret;


    @Min(value = 300, message = "Access token expiration must be at least 5 minutes")
    @Max(value = 3600, message = "Access token expiration must be at most 1 hour")
    private int accessTokenExpirationSeconds = 900;


    @Min(value = 86400, message = "Refresh Token must be at least 1 day")
    private int refreshTokenExpirationSeconds = 604800;

    @NotEmpty
    private String issuer;

    @NotEmpty
    private String audience;

    @Valid
    private CookieConfig cookie = new CookieConfig();


    @Data
    public  class CookieConfig{
        @NotEmpty
        private String name = "jlr_auth_token";

        private String domain = "localhost";
        private String path = "/";
        private boolean httpOnly = true;

        private boolean secure = false;

        @Pattern(regexp =  "Strict|Lax|None", message = "SameSite must be Strict, Lax, or None")
        private String sameSite = "Lax";


        @Min(value = 300, message = "Cookie must be at least 5 minutes")
        private int maxAge = 900;
    }
}

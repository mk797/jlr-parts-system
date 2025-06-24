package com.example.user_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Contains general-purpose application beans.
 * The PasswordEncoder is moved here to break the circular dependency
 * between SecurityConfig and UserService.
 */
@Configuration
public class ApplicationConfig {

    /**
     * Defines the password encoder bean for the application.
     * @return A BCryptPasswordEncoder instance.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        // Using a strength of 12 is a good modern default.
        return new BCryptPasswordEncoder(12);
    }
}

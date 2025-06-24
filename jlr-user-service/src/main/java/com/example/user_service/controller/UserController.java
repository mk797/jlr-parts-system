package com.example.user_service.controller;

import com.example.user_service.config.JwtProperties;
import com.example.user_service.dto.LoginRequest;
import com.example.user_service.dto.LoginResponse;
import com.example.user_service.dto.UserRegistrationRequest;
import com.example.user_service.dto.UserResponse;
import com.example.user_service.entity.User;
import com.example.user_service.exception.InvalidCredentialsException;
import com.example.user_service.service.CookieService;
import com.example.user_service.service.JwtService;
import com.example.user_service.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final CookieService cookieService;
    private final JwtProperties jwtProperties;


    @PostMapping("/register")
    public ResponseEntity<UserResponse> registerUser(@Valid @RequestBody UserRegistrationRequest request) {
        log.info("User registration request received for email: {}", request.getEmail());

        User user = convertToEntity(request);
        User registeredUser = userService.registerUser(user);
        UserResponse response = convertToResponse(registeredUser);

        log.info("User registered successfully with ID: {}", registeredUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> loginUser(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response) {

        log.info("User login request received for email: {}", request.getEmail());

        try {
            // Step 1: Authenticate user credentials using Spring Security
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            // Step 2: Get authenticated user details
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            // Step 3: Generate JWT token
            String accessToken = jwtService.generateAccessToken(userDetails);

            // Step 4: Set JWT token in httpOnly cookie
            cookieService.addJwtCookie(response, accessToken);

            // Step 5: Get user entity for response (we need full user details)
            User user = userService.findByEmail(request.getEmail())
                    .orElseThrow(() -> new InvalidCredentialsException("User not found"));

            // Step 6: Create response (keeping your existing method)
            LoginResponse loginResponse = createEnhancedLoginResponse(user);

            log.info("User logged in successfully: {}", request.getEmail());
            return ResponseEntity.ok(loginResponse);

        } catch (Exception e) {
            log.warn("Login failed for email: {} - Reason: {}", request.getEmail(), e.getMessage());
            throw new InvalidCredentialsException("Invalid email or password");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logoutUser(HttpServletResponse response) {
        // Clear JWT cookie
        cookieService.clearJwtCookie(response);

        // Clear security context
        SecurityContextHolder.clearContext();

        log.info("User logged out successfully");
        return ResponseEntity.ok("Logout successful");
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        User user = userService.getById(id);
        UserResponse response = convertToResponse(user);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllActiveUsers() {
        List<User> users = userService.getAllActiveUsers();

        List<UserResponse> responses = users.stream()
                .map(this::convertToResponse)
                .toList();

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser() {
        String currentUserEmail = getCurrentUserEmail();
        User user = userService.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("Current user not found"));

        UserResponse response = convertToResponse(user);
        return ResponseEntity.ok(response);
    }



    private String getCurrentUserEmail() {
        return SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();
    }

    private User convertToEntity(UserRegistrationRequest request) {
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(request.getPassword()); // Will be hashed in service
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setRole(request.getRole());
        user.setDealerId(request.getDealerId());
        user.setPhoneNumber(request.getPhoneNumber());
        return user;
    }

    private UserResponse convertToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .dealerId(user.getDealerId())
                .phoneNumber(user.getPhoneNumber())
                .active(user.getActive())
                .createdAt(user.getCreatedAt())
                .build();
    }

    /**
     * Enhanced login response with JWT-specific information
     */
    private LoginResponse createEnhancedLoginResponse(User user) {

        long expiresIn = jwtProperties.getAccessTokenExpirationSeconds(); // Get from config

        return LoginResponse.builder()
                .message("Login successful")
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .dealerId(user.getDealerId())
                .expiresIn(expiresIn) // 15 minutes - matches JWT expiration
                .build();
    }
}
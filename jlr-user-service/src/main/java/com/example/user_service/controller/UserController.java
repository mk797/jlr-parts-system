package com.example.user_service.controller;


import com.example.user_service.dto.LoginRequest;
import com.example.user_service.dto.LoginResponse;
import com.example.user_service.dto.UserRegistrationRequest;
import com.example.user_service.dto.UserResponse;
import com.example.user_service.entity.User;
import com.example.user_service.exception.InvalidCredentialsException;
import com.example.user_service.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;


    @PostMapping("/register")
    public ResponseEntity<UserResponse> registerUser(@Valid @RequestBody UserRegistrationRequest request){

        User user = convertToEntity(request);
        User registeredUser = userService.registerUser(user);
        UserResponse response = convertToResponse(registeredUser);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> loginUser(@Valid @RequestBody LoginRequest request) throws InvalidCredentialsException, org.apache.http.auth.InvalidCredentialsException {


        User authenticatedUser = userService.authenticateUser(request.getEmail(), request.getPassword());

        LoginResponse response = createLoginResponse(authenticatedUser);

        return ResponseEntity.ok(response);

    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id){

        //Imp pending
        return  ResponseEntity.ok().build();

    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllActiveUsers(){

        List<User> users = userService.getAllActiveUsers();

        List<UserResponse> responses = users.stream()
                .map(this::convertToResponse)
                .toList();

        return ResponseEntity.ok(responses);
    }

    private User convertToEntity(UserRegistrationRequest request){

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

    private UserResponse convertToResponse(User user){
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

    private LoginResponse createLoginResponse(User user){
        return LoginResponse.builder()
                .message("Login successful")
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }





}

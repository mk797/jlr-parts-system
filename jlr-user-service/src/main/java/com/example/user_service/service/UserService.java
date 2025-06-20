package com.example.user_service.service;


import com.example.user_service.entity.User;
import com.example.user_service.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;



@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository  userRepository;
    private final BCryptPasswordEncoder passwordEncoder;


    //Register user
    public User registeruser(User user){
        log.info("Attempting to register user with email: {}", user.getEmail());

        if(userRepository.existsByEmail(user.getEmail())){
            log.warn("Registration failed, email already exist: {}", user.getEmail());
            throw new UserAlreadyExistsException("user with email "+ user.getEmail());
        }

        String hashedPassword = passwordEncoder.encode(user.getPasswordHash());

        user.setPasswordHash(hashedPassword);

        user.setActive(true);

        User savedUser = userRepository.save(user);

        log.info("User registered succesfully with email: {}", savedUser.getEmail());

        return savedUser;

    }




}

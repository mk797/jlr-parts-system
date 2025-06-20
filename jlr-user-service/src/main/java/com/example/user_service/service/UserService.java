package com.example.user_service.service;


import com.example.user_service.entity.User;
import com.example.user_service.entity.UserRole;
import com.example.user_service.exception.UserAlreadyExistsException;
import com.example.user_service.exception.*;
import com.example.user_service.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.auth.InvalidCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;


@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository  userRepository;
    private final BCryptPasswordEncoder passwordEncoder;


    //Register user
    public User registerUser(User user){

        if(userRepository.existsByEmail(user.getEmail())){
            throw new UserAlreadyExistsException("user with email "+ user.getEmail());
        }

        String hashedPassword = passwordEncoder.encode(user.getPasswordHash());

        user.setPasswordHash(hashedPassword);

        user.setActive(true);

        User savedUser = userRepository.save(user);

        return savedUser;

    }

    @Transactional(readOnly = true)
    public User authenticateUser(String email, String plainPassword) throws InvalidCredentialsException {

        Optional<User> userOpt = userRepository.findByEmail(email);

        if(userOpt.isEmpty()){
            throw new InvalidCredentialsException("Invalid email or password");
        }

        User user = userOpt.get();

        if(!user.getActive()){
            throw new UserInactiveException("User account is deactivated");
        }

        if(!passwordEncoder.matches(plainPassword, user.getPasswordHash())){
            throw new InvalidCredentialsException("Invalid Email or Password");
        }

        return user;
    }


    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email){
        return userRepository.findByEmail(email);
    }


    @Transactional(readOnly = true)
    public List<User> getAllActiveUsers(){
        return userRepository.findByActiveTrue();
    }

    @Transactional(readOnly = true)
    public List<User> getUsersByRole(UserRole role){
        return userRepository.findByRole(role);
    }

    @Transactional(readOnly = true)
    public List<User> getDealerManagers(String dealerId){
        return userRepository.findDealerManagersByDealerId(dealerId);
    }

    public User updateUserProfile(Long userId, User userData){

        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found : {}"+ userId));

        existingUser.setFirstName(userData.getFirstName());
        existingUser.setLastName(userData.getLastName());
        existingUser.setPhoneNumber(userData.getPhoneNumber());

        User updatedUser = userRepository.save(existingUser);

        return updatedUser;
    }

    public void changePassword(Long userId, String currentPassword, String newPassword) throws InvalidCredentialsException {

        User currentUser = userRepository.findById(userId)
                .orElseThrow(()-> new UserNotFoundException("User with user ID {} not found "+ userId));

        if(!passwordEncoder.matches(currentPassword, currentUser.getPasswordHash())){
            throw new InvalidCredentialsException("Current password is incorrect");
        }

        String hashedPassword = passwordEncoder.encode(newPassword);

        currentUser.setPasswordHash(hashedPassword);

        userRepository.save(currentUser);
    }

    public void deactivateUser(Long userId){

        User currentUser = userRepository.findById(userId)
                .orElseThrow(()-> new UserNotFoundException("User with user ID {} not found "+ userId));

        currentUser.setActive(false);
        userRepository.save(currentUser);

    }






}

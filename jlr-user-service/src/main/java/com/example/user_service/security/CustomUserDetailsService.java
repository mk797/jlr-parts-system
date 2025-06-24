package com.example.user_service.security;

import com.example.user_service.entity.User;
import com.example.user_service.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserService userService;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException{

        log.debug("Loading the user for email: {}", email);

        User user = userService.findByEmail(email)
                .orElseThrow(()-> new UsernameNotFoundException("User email not found with the email "+ email));

        if(!user.getActive()){
            throw new UsernameNotFoundException("User is not active with the email "+ email);

        }

        return CustomUserDetails.from(user);
    }


}

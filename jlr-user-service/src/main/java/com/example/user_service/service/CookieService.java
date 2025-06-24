package com.example.user_service.service;


import com.example.user_service.config.JwtProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class CookieService {

    private final JwtProperties jwtProperties;


    public  void addJwtCookie(HttpServletResponse response, String token) {

        Cookie cookie = new Cookie(jwtProperties.getCookie().getName(), token);

        // Set cookie properties
        cookie.setHttpOnly(jwtProperties.getCookie().isHttpOnly());
        cookie.setSecure(jwtProperties.getCookie().isSecure());
        cookie.setPath(jwtProperties.getCookie().getPath());
        cookie.setMaxAge(jwtProperties.getCookie().getMaxAge());

        // Set domain if specified
        if (jwtProperties.getCookie().getDomain() != null) {
            cookie.setDomain(jwtProperties.getCookie().getDomain());
        }


        // Add SameSite attribute via header (Spring Boot doesn't support SameSite directly)
        String headerValue = String.format("%s=%s; Path=%s; Max-Age=%d; HttpOnly; SameSite=%s",
                cookie.getName(),
                cookie.getValue(),
                cookie.getPath(),
                cookie.getMaxAge(),
                jwtProperties.getCookie().getSameSite());

        if (cookie.getSecure()) {
            headerValue += "; Secure";
        }

        if (cookie.getDomain() != null) {
            headerValue += "; Domain=" + cookie.getDomain();
        }

        response.addHeader("Set-Cookie", headerValue);

        log.debug("JWT cookie added with security flags");
    }

    /**
     * Clear JWT cookie (for logout)
     */
    public void clearJwtCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(jwtProperties.getCookie().getName(), "");
        cookie.setHttpOnly(true);
        cookie.setSecure(jwtProperties.getCookie().isSecure());
        cookie.setPath(jwtProperties.getCookie().getPath());
        cookie.setMaxAge(0); // Expire immediately

        if (jwtProperties.getCookie().getDomain() != null) {
            cookie.setDomain(jwtProperties.getCookie().getDomain());
        }

        response.addCookie(cookie);

        log.debug("JWT cookie cleared");
    }
}

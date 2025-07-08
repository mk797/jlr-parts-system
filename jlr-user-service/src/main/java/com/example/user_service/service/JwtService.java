    package com.example.user_service.service;

    import com.example.user_service.config.JwtProperties;
    import com.example.user_service.security.CustomUserDetails;
    import io.jsonwebtoken.*;
    import io.jsonwebtoken.security.Keys;
    import lombok.RequiredArgsConstructor;
    import lombok.extern.slf4j.Slf4j;
    import org.springframework.security.core.GrantedAuthority;
    import org.springframework.security.core.userdetails.UserDetails;
    import org.springframework.stereotype.Service;

    import javax.crypto.SecretKey;
    import java.time.Clock;
    import java.time.Instant;
    import java.util.*;
    import java.util.function.Function;
    import java.util.stream.Collectors;

    @Service
    @Slf4j
    @RequiredArgsConstructor
    public class JwtService {

        private final JwtProperties jwtProperties;
        private final Clock clock = Clock.systemUTC();

        // Cache the signing key to avoid recreating
        private volatile SecretKey cachedSigningKey;
        private volatile String cachedSecret;

        /**
         * Generate access token with user claims
         */
        public String generateAccessToken(UserDetails userDetails) {
            Map<String, Object> claims = new HashMap<>();

            // Add roles to token
            Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
            claims.put("roles", authorities.stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList()));

            // Add user ID for service-to-service calls
            if (userDetails instanceof CustomUserDetails customUser) {
                claims.put("userId", customUser.getUserId());
            }

            claims.put("tokenType", "ACCESS");

            return createToken(
                    claims,
                    userDetails.getUsername(),
                    jwtProperties.getAccessTokenExpirationSeconds()
            );
        }

        /**
         * Generate refresh token (minimal claims)
         */
        public String generateRefreshToken(UserDetails userDetails) {
            Map<String, Object> claims = new HashMap<>();
            claims.put("tokenType", "REFRESH");

            return createToken(
                    claims,
                    userDetails.getUsername(),
                    jwtProperties.getRefreshTokenExpirationSeconds()
            );
        }

        /**
         * Validate token against user details
         */
        public boolean validateToken(String token, UserDetails userDetails) {
            try {
                Claims claims = extractAllClaims(token);

                // Validate basic token structure
                validateTokenClaims(claims);

                // Validate user match
                String tokenSubject = claims.getSubject();
                if (!tokenSubject.equals(userDetails.getUsername())) {
                    log.warn("Token subject mismatch. Expected: {}, Got: {}",
                            userDetails.getUsername(), tokenSubject);
                    return false;
                }

                // Validate token type
                String tokenType = claims.get("tokenType", String.class);
                if (!"ACCESS".equals(tokenType)) {
                    log.warn("Invalid token type for access: {}", tokenType);
                    return false;
                }

                return true;

            } catch (JwtException | IllegalArgumentException e) {
                log.error("Token validation failed: {}", e.getMessage());
                return false;
            }
        }

        /**
         * Extract username from token
         */
        public String extractUsername(String token) {
            return extractClaim(token, Claims::getSubject);
        }

        /**
         * Extract user roles from token
         */
        @SuppressWarnings("unchecked")
        public List<String> extractRoles(String token) {
            return extractClaim(token, claims ->
                    (List<String>) claims.get("roles"));
        }

        /**
         * Extract user ID from token
         */
        public Long extractUserId(String token) {
            return extractClaim(token, claims ->
                    claims.get("userId", Long.class));
        }

        /**
         * Check if token is expired
         */
        public boolean isTokenExpired(String token) {
            return extractExpiration(token).before(Date.from(clock.instant()));
        }

        // Private helper methods

        private String createToken(Map<String, Object> claims, String subject, int expirationSeconds) {
            Instant now = clock.instant();
            Instant expiration = now.plusSeconds(expirationSeconds);

            return Jwts.builder()
                    .setClaims(claims)
                    .setSubject(subject)
                    .setIssuer(jwtProperties.getIssuer())
                    .setAudience(jwtProperties.getAudience())
                    .setIssuedAt(Date.from(now))
                    .setNotBefore(Date.from(now))
                    .setExpiration(Date.from(expiration))
                    .setId(UUID.randomUUID().toString()) // JTI for tracking
                    .signWith(getSigningKey())
                    .compact();
        }

        private SecretKey getSigningKey() {
            String currentSecret = jwtProperties.getSecret();

            // Thread-safe lazy initialization with double-checked locking
            if (cachedSigningKey == null || !Objects.equals(currentSecret, cachedSecret)) {
                synchronized (this) {
                    if (cachedSigningKey == null || !Objects.equals(currentSecret, cachedSecret)) {
                        validateSecretKey(currentSecret);
                        byte[] keyBytes = Base64.getDecoder().decode(currentSecret);
                        cachedSigningKey = Keys.hmacShaKeyFor(keyBytes);
                        cachedSecret = currentSecret;
                        log.info("JWT signing key updated");
                    }
                }
            }

            return cachedSigningKey;
        }

        private void validateSecretKey(String secret) {
            try {
                byte[] decoded = Base64.getDecoder().decode(secret);
                if (decoded.length < 32) { // 256 bits = 32 bytes
                    throw new IllegalArgumentException("JWT secret must be at least 256 bits");
                }
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("JWT secret must be valid Base64 encoded 256-bit key", e);
            }
        }

        private void validateTokenClaims(Claims claims) {
            // Validate issuer
            String issuer = claims.getIssuer();
            if (!jwtProperties.getIssuer().equals(issuer)) {
                throw new JwtException("Invalid token issuer: " + issuer);
            }

            // Validate audience
            Set<String> audiences = claims.getAudience();
            if (audiences == null || !audiences.contains(jwtProperties.getAudience())) {
                throw new JwtException("Invalid token audience: " + audiences);
            }
        }

        private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
            Claims claims = extractAllClaims(token);
            return claimsResolver.apply(claims);
        }

        private Claims extractAllClaims(String token) {
            return Jwts.parser()
                    .setSigningKey(getSigningKey())
                    .requireIssuer(jwtProperties.getIssuer())
                    .requireAudience(jwtProperties.getAudience())
                    .clockSkewSeconds(30) // Allow 30 seconds clock skew
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        }

        private Date extractExpiration(String token) {
            return extractClaim(token, Claims::getExpiration);
        }
    }
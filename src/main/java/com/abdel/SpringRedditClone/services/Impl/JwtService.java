package com.abdel.SpringRedditClone.services.Impl;

import com.abdel.SpringRedditClone.entities.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static com.abdel.SpringRedditClone.Config.SecurityConstants.*;

@Service
public class JwtService {
    private static final String jwtSecretKey = TOKEN_SECRET;
    Long jwtExpirationMs = TOKEN_EXPIRATION_TIME;
    Long jwtRefreshExpirationMs = REFRESH_TOKEN_EXPIRATION_TIME;

    Long jwtVerificationExpirationMs = VERIFICATION_TOKEN_EXPIRATION_TIME;


    public String extractEmail(String token) {
        return extractClaim(token, claims -> claims.get("email", String.class));
    }
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    public <T> T extractClaim(String token, Function<Claims, T> claimResolver){
        final Claims claims = extractAllClaims(token);
        return claimResolver.apply(claims);
    }

    public String generateToken(User userEntityDetails){
        return generateToken(new HashMap<>(), userEntityDetails);
    }
    public String generateToken(
            Map<String, Object> extraClaims,
            User userEntity
    ){
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userEntity.getUsername())
                .claim("email", userEntity.getEmail())
                .claim("userRole", userEntity.getAuthorities())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    public String generateRefreshToken(User userEntity){
        return generateRefreshToken(new HashMap<>(), userEntity);
    }

    public String generateRefreshToken(
            Map<String, Object> extraClaims,
            User userEntity
    ){
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userEntity.getUsername())
                .claim("email", userEntity.getEmail())
                .claim("userRole", userEntity.getAuthorities())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtRefreshExpirationMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    public String generateVerificationToken(User userEntity){
        return generateVerificationToken(new HashMap<>(), userEntity);
    }
    public String generateVerificationToken(
            Map<String, Object> extraClaims,
            User userEntity
    ){
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userEntity.getUsername())
                .claim("email", userEntity.getEmail())
                .claim("userRole", userEntity.getAuthorities())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtVerificationExpirationMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isTokenValid(String token, UserDetails userDetails){
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }
    public boolean isVerificationTokenValid(String token, UserDetails userDetails){
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    private Claims extractAllClaims(String token){
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecretKey);
        System.out.println("Decoded Key Bytes: " + Arrays.toString(keyBytes));
        Key signingKey = Keys.hmacShaKeyFor(keyBytes);
        System.out.println("Signing Key: " + signingKey);
        return signingKey;
    }
}


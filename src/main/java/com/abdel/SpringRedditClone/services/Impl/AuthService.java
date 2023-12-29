package com.abdel.SpringRedditClone.services.Impl;

import ch.qos.logback.core.net.SyslogOutputStream;
import com.abdel.SpringRedditClone.dto.AuthenticationResponse;
import com.abdel.SpringRedditClone.dto.LoginRequest;
import com.abdel.SpringRedditClone.dto.RefreshTokenRequest;
import com.abdel.SpringRedditClone.dto.RegisterRequest;
import com.abdel.SpringRedditClone.entities.NotificationEmail;
import com.abdel.SpringRedditClone.entities.User;
import com.abdel.SpringRedditClone.exceptions.SpringRedditException;
import com.abdel.SpringRedditClone.repositories.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties.Jwt;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;


@Service
@AllArgsConstructor
@Transactional
public class AuthService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final MailService mailService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    public void signup(RegisterRequest registerRequest) {
        User userExists = userRepository.findByUsername(registerRequest.getUsername()).orElse(null);
        if (userRepository.findByUsername(registerRequest.getUsername()).isPresent() || userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            throw new SpringRedditException("User with email or username already exists");
        }
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setCreated(Instant.now());
        user.setEnabled(false);

        userRepository.save(user);
        String token = jwtService.generateVerificationToken(user);
        user.setVerificationToken(token);
        userRepository.save(user);
        mailService.sendMail(new NotificationEmail("Please Activate your Account",
                user.getEmail(), "Thank you for signing up to Spring Reddit, " +
                "please click on the below url to activate your account : " +
                "http://localhost:8080/api/auth/accountVerification/" + token));

    }
    @Transactional(readOnly = true)
    public User getCurrentUser() {
        User loggeduser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userRepository.findByUsername(loggeduser.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User name not found - " + loggeduser.getUsername()));
    }
    public void verifyAccount(String token) {
        String username = jwtService.extractUsername(token);
        User user = userRepository.findByUsername(username).orElseThrow(() -> new SpringRedditException("User not found with name - " + username));

        if(jwtService.isVerificationTokenValid(token, user)){
            user.setEnabled(true);
            user.setVerificationToken(null);
            userRepository.save(user);
        }else{
            throw new SpringRedditException("Invalid Token");
        }

    }

    public AuthenticationResponse login(LoginRequest loginRequest) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );
            var user = userRepository.findByUsername(loginRequest.getUsername()).orElse(null);
            assert user != null;
            if (!user.isEnabled()) {
                throw new SpringRedditException("User is not active, please check you email");
            }
            var jwtToken = jwtService.generateToken(user);
            var refreshToken = jwtService.generateRefreshToken(user);

            return AuthenticationResponse.builder()
                    .authenticationToken(jwtToken)
                    .refreshToken(refreshToken)
                    .build();
        } catch (AuthenticationException e) {
            // Handle authentication failure and return a custom message
            throw new SpringRedditException("Authentication failed: Email or password is incorrect");
        }

    }

    public AuthenticationResponse refreshToken(RefreshTokenRequest refreshToken) {

        String username = jwtService.extractUsername(refreshToken.getRefreshToken());
        User user = userRepository.findByUsername(username).orElse(null);
        assert user != null;
        if (!jwtService.isTokenValid(refreshToken.getRefreshToken(), user)) {
            throw new SpringRedditException("Refresh token is not valid");
        }
        String newToken = jwtService.generateToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user);
        return AuthenticationResponse.builder()
                .authenticationToken(newToken)
                .refreshToken(newRefreshToken)
                .build();
    }
    public boolean isLoggedIn() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return !(authentication instanceof AnonymousAuthenticationToken) && authentication.isAuthenticated();
    }

}

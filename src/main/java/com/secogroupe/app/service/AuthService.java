package com.secogroupe.app.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.secogroupe.app.dto.AuthRequest;
import com.secogroupe.app.dto.AuthResponse;
import com.secogroupe.app.dto.RefreshRequest;
import com.secogroupe.app.entity.RefreshToken;
import com.secogroupe.app.security.CustomUserDetailsService;
import com.secogroupe.app.security.JwtService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final RefreshTokenService refreshTokenService;

    public AuthResponse login(AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        String accessToken = jwtService.generateToken(userDetails);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(request.getUsername());

        return new AuthResponse(accessToken, refreshToken.getToken());
    }

    public AuthResponse refresh(RefreshRequest request) {
        RefreshToken refreshToken = refreshTokenService.validateRefreshToken(request.getRefreshToken());
        UserDetails userDetails = userDetailsService.loadUserByUsername(
                refreshToken.getUser().getUsername()
        );

        // Rotation du refresh token : l'ancien est supprimé, un nouveau est émis
        refreshTokenService.deleteByUser(refreshToken.getUser().getUsername());
        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(
                refreshToken.getUser().getUsername()
        );

        String newAccessToken = jwtService.generateToken(userDetails);

        return new AuthResponse(newAccessToken, newRefreshToken.getToken());
    }

    public void logout(String username) {
        refreshTokenService.deleteByUser(username);
    }
}

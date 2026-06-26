package com.secogroupe.app.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.secogroupe.app.dto.AuthRequest;
import com.secogroupe.app.dto.LoginResult;
import com.secogroupe.app.entity.RefreshToken;
import com.secogroupe.app.security.CustomUserDetailsService;
import com.secogroupe.app.security.JwtService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    
    @Autowired
    private JwtService jwtService;
    
    private final CustomUserDetailsService userDetailsService;
    
    @Autowired
    private RefreshTokenService refreshTokenService;
    
    private final LoginHistoryService loginHistoryService;

    public LoginResult login(AuthRequest request, String clientIp, String device) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        } catch (Exception e) {
            loginHistoryService.record(request.getUsername(), clientIp, device, false);
            e.printStackTrace();
            return null;
        }

        loginHistoryService.record(request.getUsername(), clientIp, device, true);

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String accessToken = jwtService.generateToken(userDetails);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(request.getUsername());

        return new LoginResult(accessToken, refreshToken.getToken());
    }

    public LoginResult refresh(String token) {
       
            
            RefreshToken refreshToken = refreshTokenService.findByToken(token);
            refreshToken = refreshTokenService.verifyExpiration(refreshToken);
            if(refreshToken != null){
    
            
                UserDetails userDetails = userDetailsService.loadUserByUsername(
                        refreshToken.getUser().getUsername());
    
                // refreshTokenService.deleteByUser(refreshToken.getUser().getUsername());
                // RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(
                //         refreshToken.getUser().getUsername());
    
                String newAccessToken = jwtService.generateToken(userDetails);
                return new LoginResult(newAccessToken, "");
            }
            

        
        
        return null;
    }

    public void logout(String username) {
        refreshTokenService.deleteByUser(username);
    }
}

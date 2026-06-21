package com.secogroupe.app.service;

import java.time.Instant;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.secogroupe.app.entity.RefreshToken;
import com.secogroupe.app.entity.User;
import com.secogroupe.app.repository.RefreshTokenRepository;
import com.secogroupe.app.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    @Transactional
    public RefreshToken createRefreshToken(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable : " + username));

        // Un seul refresh token actif par utilisateur
        refreshTokenRepository.deleteByUser(user);

        RefreshToken refreshToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .user(user)
                .expiryDate(Instant.now().plusMillis(refreshExpiration))
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    public RefreshToken validateById(Long id) {
        RefreshToken refreshToken = refreshTokenRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Session invalide ou inexistante"));

        if (refreshToken.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new RuntimeException("Session expirée. Veuillez vous reconnecter.");
        }

        return refreshToken;
    }

    @Transactional
    public void deleteByUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable : " + username));
        refreshTokenRepository.deleteByUser(user);
    }

    
    public RefreshToken findByToken(String token){
      return refreshTokenRepository.findByToken(token).orElseThrow(() -> new RuntimeException("Refresh Token introuvable "));
    }

    public RefreshToken verifyExpiration(RefreshToken refreshToken){
        if(refreshToken.getExpiryDate().isBefore(Instant.now())){
            refreshTokenRepository.delete(refreshToken);
            throw new RuntimeException("Refresh token expirer. Veuillez vous reconnecter.");
        }

        return refreshToken;
    }
}

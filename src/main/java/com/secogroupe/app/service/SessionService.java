package com.secogroupe.app.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.secogroupe.app.dto.SessionResponse;
import com.secogroupe.app.entity.RefreshToken;
import com.secogroupe.app.entity.User;
import com.secogroupe.app.repository.RefreshTokenRepository;
import com.secogroupe.app.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<SessionResponse> getAll() {
        return refreshTokenRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void revokeById(Long id) {
        if (!refreshTokenRepository.existsById(id)) {
            throw new RuntimeException("Session introuvable");
        }
        refreshTokenRepository.deleteById(id);
    }

    @Transactional
    public void revokeAllForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
        refreshTokenRepository.deleteByUser(user);
    }

    private SessionResponse toResponse(RefreshToken rt) {
        User user = rt.getUser();
        String roleName = (user.getRoles() != null && !user.getRoles().isEmpty())
                ? user.getRoles().iterator().next().getName()
                : "N/A";
        return SessionResponse.builder()
                .id(rt.getId())
                .userId(user.getId())
                .username(user.getUsername())
                .userEmail(user.getEmail())
                .userRoleName(roleName)
                .userStatus(user.getStatus().name())
                .createdAt(rt.getCreatedAt())
                .expiresAt(rt.getExpiryDate())
                .build();
    }
}

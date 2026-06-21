package com.secogroupe.app.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.secogroupe.app.entity.PasswordResetToken;
import com.secogroupe.app.entity.User;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByToken(String token);

    void deleteByUser(User user);
}

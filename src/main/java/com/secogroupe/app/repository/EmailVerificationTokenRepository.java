package com.secogroupe.app.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.secogroupe.app.entity.EmailVerificationToken;
import com.secogroupe.app.entity.User;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {

    Optional<EmailVerificationToken> findByLinkToken(String linkToken);

    Optional<EmailVerificationToken> findByOtpCodeAndUser_Email(String otpCode, String email);

    void deleteByUser(User user);
}

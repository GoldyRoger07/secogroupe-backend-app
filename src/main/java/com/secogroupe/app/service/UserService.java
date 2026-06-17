package com.secogroupe.app.service;

import java.time.Instant;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.secogroupe.app.dto.RegisterRequest;
import com.secogroupe.app.entity.EmailVerificationToken;
import com.secogroupe.app.entity.Role;
import com.secogroupe.app.entity.User;
import com.secogroupe.app.repository.EmailVerificationTokenRepository;
import com.secogroupe.app.repository.RoleRepository;
import com.secogroupe.app.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final long OTP_EXPIRATION_MS = 15 * 60 * 1000L; // 15 minutes

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final EmailVerificationTokenRepository verificationTokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void register(RegisterRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("Ce nom d'utilisateur est déjà pris");
        }
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Cet email est déjà utilisé");
        }

        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new RuntimeException("Rôle USER introuvable"));

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setEnabled(false); // inactif jusqu'à vérification email
        user.setRoles(Set.of(userRole));

        userRepository.save(user);

        sendVerificationEmail(user);
    }

    @Transactional
    public void verifyByOtp(String email, String otpCode) {
        EmailVerificationToken verificationToken = verificationTokenRepository
                .findByOtpCodeAndUser_Email(otpCode, email)
                .orElseThrow(() -> new RuntimeException("Code OTP invalide ou email incorrect"));

        if (verificationToken.getExpiryDate().isBefore(Instant.now())) {
            verificationTokenRepository.delete(verificationToken);
            throw new RuntimeException("Code OTP expiré. Veuillez demander un nouveau code.");
        }

        activateUser(verificationToken);
    }

    @Transactional
    public void verifyByLink(String linkToken) {
        EmailVerificationToken verificationToken = verificationTokenRepository
                .findByLinkToken(linkToken)
                .orElseThrow(() -> new RuntimeException("Lien de vérification invalide ou déjà utilisé"));

        if (verificationToken.getExpiryDate().isBefore(Instant.now())) {
            verificationTokenRepository.delete(verificationToken);
            throw new RuntimeException("Lien de vérification expiré. Veuillez demander un nouveau code.");
        }

        activateUser(verificationToken);
    }

    @Transactional
    public void resendVerification(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Aucun compte trouvé pour cet email"));

        if (user.isEnabled()) {
            throw new RuntimeException("Ce compte est déjà vérifié");
        }

        verificationTokenRepository.deleteByUser(user);
        sendVerificationEmail(user);
    }

    private void activateUser(EmailVerificationToken verificationToken) {
        User user = verificationToken.getUser();
        user.setEnabled(true);
        userRepository.save(user);
        verificationTokenRepository.delete(verificationToken);
    }

    private void sendVerificationEmail(User user) {
        String otpCode = String.format("%06d", new Random().nextInt(999999));
        String linkToken = UUID.randomUUID().toString();

        EmailVerificationToken token = EmailVerificationToken.builder()
                .otpCode(otpCode)
                .linkToken(linkToken)
                .user(user)
                .expiryDate(Instant.now().plusMillis(OTP_EXPIRATION_MS))
                .build();

        verificationTokenRepository.save(token);
        emailService.sendVerificationEmail(user.getEmail(), user.getUsername(), otpCode, linkToken);
    }
}

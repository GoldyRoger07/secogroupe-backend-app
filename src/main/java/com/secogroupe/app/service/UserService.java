package com.secogroupe.app.service;

import java.time.Instant;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.mail.MailException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.secogroupe.app.dto.PageResponse;
import com.secogroupe.app.dto.RegisterRequest;
import com.secogroupe.app.dto.UserRequest;
import com.secogroupe.app.dto.UserResponse;
import com.secogroupe.app.entity.EmailVerificationToken;
import com.secogroupe.app.entity.Role;
import com.secogroupe.app.entity.User;
import com.secogroupe.app.mapper.UserMapper;
import com.secogroupe.app.repository.EmailVerificationTokenRepository;
import com.secogroupe.app.repository.RoleRepository;
import com.secogroupe.app.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private static final long OTP_EXPIRATION_MS = 15 * 60 * 1000L;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final EmailVerificationTokenRepository verificationTokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    // ──────────────── Auth methods ────────────────

    @Transactional
    public void register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Ce nom d'utilisateur est déjà pris");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Cet email est déjà utilisé");
        }

        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new RuntimeException("Rôle USER introuvable"));

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setEnabled(false);
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

    // ──────────────── CRUD (admin) ────────────────

    public PageResponse<UserResponse> getAllUsers(int page, int size, String sortField, String sortOrder, String filter) {
        Sort sort = (sortField != null && !sortField.isBlank())
                ? Sort.by("desc".equalsIgnoreCase(sortOrder) ? Sort.Direction.DESC : Sort.Direction.ASC, sortField)
                : Sort.unsorted();
        Pageable pageable = PageRequest.of(page, size, sort);
        String f = (filter != null && !filter.isBlank()) ? filter : "";

        Page<User> resultPage = f.isEmpty()
                ? userRepository.findAll(pageable)
                : userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(f, f, pageable);

        return new PageResponse<>(
                resultPage.getContent().stream().map(userMapper::toResponse).toList(),
                resultPage.getTotalElements(),
                resultPage.getTotalPages(),
                page,
                size);
    }

    @Transactional
    public UserResponse createUser(UserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Ce nom d'utilisateur est déjà pris");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Cet email est déjà utilisé");
        }

        Role role = roleRepository.findByName(request.getRoleName())
                .orElseThrow(() -> new RuntimeException("Rôle introuvable: " + request.getRoleName()));

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(
                request.getPassword() != null ? request.getPassword() : UUID.randomUUID().toString()));
        user.setEnabled(true);
        user.setStatus(request.getStatus());
        user.setRoles(Set.of(role));

        return userMapper.toResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponse updateUser(Long id, UserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        if (!user.getUsername().equals(request.getUsername()) && userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Ce nom d'utilisateur est déjà pris");
        }
        if (!user.getEmail().equals(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Cet email est déjà utilisé");
        }

        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setStatus(request.getStatus());
        user.setEnabled(request.getStatus() == com.secogroupe.app.entity.UserStatus.ACTIVE || user.isEnabled());

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        Role role = roleRepository.findByName(request.getRoleName())
                .orElseThrow(() -> new RuntimeException("Rôle introuvable: " + request.getRoleName()));
        user.setRoles(Set.of(role));

        return userMapper.toResponse(userRepository.save(user));
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("Utilisateur introuvable");
        }
        userRepository.deleteById(id);
    }

    // ──────────────── private helpers ────────────────

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
        try {
            emailService.sendVerificationEmail(user.getEmail(), user.getUsername(), otpCode, linkToken);
        } catch (MailException e) {
            log.error("Échec de l'envoi du mail de vérification à {} : {}", user.getEmail(), e.getMessage());
        }
    }
}

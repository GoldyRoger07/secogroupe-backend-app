package com.secogroupe.app.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.secogroupe.app.dto.ImportResult;
import com.secogroupe.app.dto.PageResponse;
import com.secogroupe.app.dto.RegisterRequest;
import com.secogroupe.app.dto.UserRequest;
import com.secogroupe.app.dto.UserResponse;
import com.secogroupe.app.entity.EmailVerificationToken;
import com.secogroupe.app.entity.PasswordResetToken;
import com.secogroupe.app.entity.Role;
import com.secogroupe.app.entity.User;
import com.secogroupe.app.exception.VerificationExpiredException;
import com.secogroupe.app.mapper.UserMapper;
import com.secogroupe.app.repository.EmailVerificationTokenRepository;
import com.secogroupe.app.repository.PasswordResetTokenRepository;
import com.secogroupe.app.repository.RoleRepository;
import com.secogroupe.app.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private static final long OTP_EXPIRATION_MS = 15 * 60 * 1000L;
    private static final long RESET_EXPIRATION_MS = 30 * 60 * 1000L;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final EmailVerificationTokenRepository verificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final RefreshTokenService refreshTokenService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final ObjectMapper objectMapper;

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
            String email = verificationToken.getUser().getEmail();
            verificationTokenRepository.delete(verificationToken);
            throw new VerificationExpiredException(email);
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

    // ──────────────── Password reset ────────────────

    /**
     * Génère un token de réinitialisation et envoie le lien par email.
     * Ne révèle jamais si l'email existe (protection contre l'énumération de comptes).
     */
    @Transactional
    public void requestPasswordReset(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            passwordResetTokenRepository.deleteByUser(user);

            String token = UUID.randomUUID().toString();
            PasswordResetToken resetToken = PasswordResetToken.builder()
                    .token(token)
                    .user(user)
                    .expiryDate(Instant.now().plusMillis(RESET_EXPIRATION_MS))
                    .build();
            passwordResetTokenRepository.save(resetToken);

            try {
                emailService.sendPasswordResetEmail(user.getEmail(), user.getUsername(), token);
            } catch (Exception e) {
                log.error("Échec de l'envoi du mail de réinitialisation à {} : {}", user.getEmail(), e.getMessage());
            }
        });
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Lien de réinitialisation invalide ou déjà utilisé"));

        if (resetToken.getExpiryDate().isBefore(Instant.now())) {
            passwordResetTokenRepository.delete(resetToken);
            throw new RuntimeException("Lien de réinitialisation expiré. Veuillez refaire une demande.");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        passwordResetTokenRepository.delete(resetToken);

        // Invalide les sessions existantes par sécurité
        try {
            refreshTokenService.deleteByUser(user.getUsername());
        } catch (Exception e) {
            log.warn("Impossible d'invalider les sessions de {} : {}", user.getUsername(), e.getMessage());
        }
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

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(
                request.getPassword() != null ? request.getPassword() : UUID.randomUUID().toString()));
        user.setEnabled(true);
        user.setStatus(request.getStatus());
        user.setRoles(resolveRoles(request));

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

        user.setRoles(resolveRoles(request));

        return userMapper.toResponse(userRepository.save(user));
    }

    /** Résout l'ensemble des rôles RBAC à partir de roleNames (ou roleName en repli). */
    private Set<Role> resolveRoles(UserRequest request) {
        java.util.List<String> names = new java.util.ArrayList<>();
        if (request.getRoleNames() != null) {
            request.getRoleNames().stream()
                    .filter(n -> n != null && !n.isBlank())
                    .forEach(names::add);
        }
        if (names.isEmpty() && request.getRoleName() != null && !request.getRoleName().isBlank()) {
            names.add(request.getRoleName());
        }
        if (names.isEmpty()) {
            throw new RuntimeException("Au moins un rôle doit être assigné à l'utilisateur");
        }
        Set<Role> roles = new java.util.HashSet<>();
        for (String name : names) {
            roles.add(roleRepository.findByName(name)
                    .orElseThrow(() -> new RuntimeException("Rôle introuvable: " + name)));
        }
        return roles;
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("Utilisateur introuvable");
        }
        userRepository.deleteById(id);
    }

    @Transactional
    public UserResponse patchStatus(Long id, com.secogroupe.app.entity.UserStatus status) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
        user.setStatus(status);
        user.setEnabled(status == com.secogroupe.app.entity.UserStatus.ACTIVE);
        return userMapper.toResponse(userRepository.save(user));
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
        } catch (Exception e) {
            log.error("Échec de l'envoi du mail de vérification à {} : {}", user.getEmail(), e.getMessage());
        }
    }

    // ──────────────── Export ────────────────

    public byte[] exportCsv() {
        List<User> all = userRepository.findAll(Sort.by(Sort.Direction.ASC, "username"));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (CSVPrinter printer = new CSVPrinter(
                new OutputStreamWriter(baos, StandardCharsets.UTF_8),
                CSVFormat.DEFAULT.builder()
                        .setHeader("id", "username", "email", "roleName", "status", "createdAt")
                        .build())) {
            for (User u : all) {
                String roles = u.getRoles() == null ? "" : u.getRoles().stream()
                        .map(Role::getName).sorted()
                        .collect(java.util.stream.Collectors.joining(";"));
                printer.printRecord(
                        u.getId(), u.getUsername(), u.getEmail(), roles,
                        u.getStatus(), u.getCreatedAt() != null ? u.getCreatedAt().toString() : "");
            }
        } catch (IOException ex) {
            throw new RuntimeException("Erreur export CSV", ex);
        }
        return baos.toByteArray();
    }

    public byte[] exportJson() {
        List<UserResponse> all = userRepository.findAll(Sort.by(Sort.Direction.ASC, "username"))
                .stream().map(userMapper::toResponse).toList();
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(all);
        } catch (JsonProcessingException ex) {
            throw new RuntimeException("Erreur export JSON", ex);
        }
    }

    // ──────────────── Import ────────────────

    @Transactional
    public ImportResult importCsv(MultipartFile file) {
        int imported = 0, skipped = 0;
        List<String> errors = new ArrayList<>();
        try (CSVParser parser = CSVParser.parse(file.getInputStream(), StandardCharsets.UTF_8,
                CSVFormat.DEFAULT.builder()
                        .setHeader().setSkipHeaderRecord(true)
                        .setIgnoreHeaderCase(true).setTrim(true).build())) {
            for (CSVRecord record : parser) {
                try {
                    String username = record.get("username");
                    String email    = record.get("email");
                    if (userRepository.existsByUsername(username) || userRepository.existsByEmail(email)) {
                        skipped++;
                        continue;
                    }
                    UserRequest req = new UserRequest();
                    req.setUsername(username);
                    req.setEmail(email);
                    req.setPassword(safeGet(record, "password"));
                    String roleCol = safeGet(record, "roleName");
                    req.setRoleNames(roleCol != null && !roleCol.isBlank()
                            ? java.util.Arrays.stream(roleCol.split(";")).map(String::trim).filter(s -> !s.isBlank()).toList()
                            : java.util.List.of("USER"));
                    String statusStr = safeGet(record, "status");
                    req.setStatus(statusStr != null && !statusStr.isBlank()
                            ? com.secogroupe.app.entity.UserStatus.valueOf(statusStr.toUpperCase())
                            : com.secogroupe.app.entity.UserStatus.ACTIVE);
                    createUser(req);
                    imported++;
                } catch (Exception e) {
                    errors.add("Ligne " + record.getRecordNumber() + ": " + e.getMessage());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Lecture du fichier CSV impossible", e);
        }
        return new ImportResult(imported, skipped, errors);
    }

    @Transactional
    public ImportResult importJson(MultipartFile file) {
        int imported = 0, skipped = 0;
        List<String> errors = new ArrayList<>();
        try {
            List<UserRequest> requests = objectMapper.readValue(file.getInputStream(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, UserRequest.class));
            int idx = 1;
            for (UserRequest req : requests) {
                try {
                    if (userRepository.existsByUsername(req.getUsername()) || userRepository.existsByEmail(req.getEmail())) {
                        skipped++;
                    } else {
                        createUser(req);
                        imported++;
                    }
                } catch (Exception e) {
                    errors.add("Entrée " + idx + ": " + e.getMessage());
                }
                idx++;
            }
        } catch (IOException e) {
            throw new RuntimeException("Lecture du fichier JSON impossible", e);
        }
        return new ImportResult(imported, skipped, errors);
    }

    private static String safeGet(CSVRecord r, String col) {
        try { return r.get(col); } catch (IllegalArgumentException e) { return null; }
    }
}

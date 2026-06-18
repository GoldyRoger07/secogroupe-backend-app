package com.secogroupe.app.controller;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.secogroupe.app.dto.AppearanceRequest;
import com.secogroupe.app.dto.AppearanceResponse;
import com.secogroupe.app.dto.CompanySettingsRequest;
import com.secogroupe.app.dto.CompanySettingsResponse;
import com.secogroupe.app.dto.LoginHistoryResponse;
import com.secogroupe.app.dto.NotificationsRequest;
import com.secogroupe.app.dto.NotificationsResponse;
import com.secogroupe.app.dto.PasswordChangeRequest;
import com.secogroupe.app.dto.ProfileRequest;
import com.secogroupe.app.dto.ProfileResponse;
import com.secogroupe.app.dto.SecuritySettingsRequest;
import com.secogroupe.app.dto.SecuritySettingsResponse;
import com.secogroupe.app.service.RefreshTokenService;
import com.secogroupe.app.service.SettingsService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/settings")
@RequiredArgsConstructor
public class SettingsController {

    private final SettingsService settingsService;
    private final RefreshTokenService refreshTokenService;

    // ──────────────── General ────────────────

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/general")
    public ResponseEntity<CompanySettingsResponse> getGeneral() {
        return ResponseEntity.ok(settingsService.getGeneral());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/general")
    public ResponseEntity<CompanySettingsResponse> updateGeneral(@Valid @RequestBody CompanySettingsRequest request) {
        return ResponseEntity.ok(settingsService.updateGeneral(request));
    }

    // ──────────────── Profile ────────────────

    @GetMapping("/profile")
    public ResponseEntity<ProfileResponse> getProfile(Principal principal) {
        return ResponseEntity.ok(settingsService.getProfile(principal.getName()));
    }

    @PutMapping("/profile")
    public ResponseEntity<ProfileResponse> updateProfile(Principal principal,
                                                          @Valid @RequestBody ProfileRequest request) {
        return ResponseEntity.ok(settingsService.updateProfile(principal.getName(), request));
    }

    @PostMapping(value = "/profile/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> uploadPhoto(Principal principal,
                                                           @RequestPart("photo") MultipartFile photo) {
        return ResponseEntity.ok(settingsService.uploadProfilePhoto(principal.getName(), photo));
    }

    @PutMapping("/password")
    public ResponseEntity<Void> changePassword(Principal principal,
                                               @Valid @RequestBody PasswordChangeRequest request) {
        settingsService.changePassword(principal.getName(), request);
        return ResponseEntity.noContent().build();
    }

    // ──────────────── Appearance ────────────────

    @GetMapping("/appearance")
    public ResponseEntity<AppearanceResponse> getAppearance(Principal principal) {
        return ResponseEntity.ok(settingsService.getAppearance(principal.getName()));
    }

    @PutMapping("/appearance")
    public ResponseEntity<AppearanceResponse> updateAppearance(Principal principal,
                                                               @RequestBody AppearanceRequest request) {
        return ResponseEntity.ok(settingsService.updateAppearance(principal.getName(), request));
    }

    // ──────────────── Notifications ────────────────

    @GetMapping("/notifications")
    public ResponseEntity<NotificationsResponse> getNotifications(Principal principal) {
        return ResponseEntity.ok(settingsService.getNotifications(principal.getName()));
    }

    @PutMapping("/notifications")
    public ResponseEntity<NotificationsResponse> updateNotifications(Principal principal,
                                                                     @RequestBody NotificationsRequest request) {
        return ResponseEntity.ok(settingsService.updateNotifications(principal.getName(), request));
    }

    // ──────────────── Security ────────────────

    @GetMapping("/security")
    public ResponseEntity<SecuritySettingsResponse> getSecuritySettings(Principal principal) {
        return ResponseEntity.ok(settingsService.getSecuritySettings(principal.getName()));
    }

    @PutMapping("/security")
    public ResponseEntity<SecuritySettingsResponse> updateSecuritySettings(Principal principal,
                                                                           @RequestBody SecuritySettingsRequest request) {
        return ResponseEntity.ok(settingsService.updateSecuritySettings(principal.getName(), request));
    }

    // ──────────────── Login History & Disconnect ────────────────

    @GetMapping("/login-history")
    public ResponseEntity<List<LoginHistoryResponse>> getLoginHistory(Principal principal) {
        return ResponseEntity.ok(settingsService.getLoginHistory(principal.getName()));
    }

    @PostMapping("/disconnect-all")
    public ResponseEntity<Void> disconnectAll(Principal principal) {
        refreshTokenService.deleteByUser(principal.getName());
        return ResponseEntity.noContent().build();
    }
}

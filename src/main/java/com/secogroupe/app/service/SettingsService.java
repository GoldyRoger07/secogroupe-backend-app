package com.secogroupe.app.service;

import java.util.List;
import java.util.Map;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.secogroupe.app.dto.AppearanceRequest;
import com.secogroupe.app.dto.CompanySettingsRequest;
import com.secogroupe.app.dto.CompanySettingsResponse;
import com.secogroupe.app.dto.LoginHistoryResponse;
import com.secogroupe.app.dto.NotificationsRequest;
import com.secogroupe.app.dto.PasswordChangeRequest;
import com.secogroupe.app.dto.ProfileRequest;
import com.secogroupe.app.dto.ProfileResponse;
import com.secogroupe.app.dto.AppearanceResponse;
import com.secogroupe.app.dto.NotificationsResponse;
import com.secogroupe.app.dto.SecuritySettingsRequest;
import com.secogroupe.app.dto.SecuritySettingsResponse;
import com.secogroupe.app.entity.CompanySettings;
import com.secogroupe.app.entity.User;
import com.secogroupe.app.entity.UserPreferences;
import com.secogroupe.app.repository.CompanySettingsRepository;
import com.secogroupe.app.repository.LoginHistoryRepository;
import com.secogroupe.app.repository.UserPreferencesRepository;
import com.secogroupe.app.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SettingsService {

    private final UserRepository userRepository;
    private final CompanySettingsRepository companySettingsRepository;
    private final UserPreferencesRepository preferencesRepository;
    private final LoginHistoryRepository loginHistoryRepository;
    private final PhotoStorageService photoStorageService;
    private final PasswordEncoder passwordEncoder;

    // ──────────────── General (Company) ────────────────

    public CompanySettingsResponse getGeneral() {
        CompanySettings settings = companySettingsRepository.findAll().stream()
                .findFirst()
                .orElseGet(() -> companySettingsRepository.save(new CompanySettings()));
        return toCompanyResponse(settings);
    }

    @Transactional
    public CompanySettingsResponse updateGeneral(CompanySettingsRequest request) {
        CompanySettings settings = companySettingsRepository.findAll().stream()
                .findFirst()
                .orElseGet(CompanySettings::new);
        settings.setCompanyName(request.getCompanyName());
        settings.setCompanyEmail(request.getCompanyEmail());
        settings.setCompanyPhone(request.getCompanyPhone());
        settings.setWebsite(request.getWebsite());
        settings.setAddress(request.getAddress());
        settings.setTimezone(request.getTimezone());
        settings.setDateFormat(request.getDateFormat());
        settings.setLanguage(request.getLanguage());
        return toCompanyResponse(companySettingsRepository.save(settings));
    }

    // ──────────────── Profile ────────────────

    public ProfileResponse getProfile(String username) {
        User user = findUser(username);
        return toProfileResponse(user);
    }

    @Transactional
    public ProfileResponse updateProfile(String username, ProfileRequest request) {
        User user = findUser(username);

        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new RuntimeException("Cet email est déjà utilisé");
            }
            user.setEmail(request.getEmail());
        }

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());
        user.setPosition(request.getPosition());
        user.setBio(request.getBio());
        return toProfileResponse(userRepository.save(user));
    }

    @Transactional
    public Map<String, String> uploadProfilePhoto(String username, MultipartFile photo) {
        User user = findUser(username);
        if (user.getPhotoUrl() != null) {
            photoStorageService.delete(user.getPhotoUrl());
        }
        String url = photoStorageService.store(photo);
        user.setPhotoUrl(url);
        userRepository.save(user);
        return Map.of("photoUrl", url);
    }

    @Transactional
    public void changePassword(String username, PasswordChangeRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Les mots de passe ne correspondent pas");
        }
        User user = findUser(username);
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("Mot de passe actuel incorrect");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    // ──────────────── Appearance ────────────────

    public AppearanceResponse getAppearance(String username) {
        UserPreferences prefs = getOrCreatePreferences(findUser(username));
        return toAppearanceResponse(prefs);
    }

    @Transactional
    public AppearanceResponse updateAppearance(String username, AppearanceRequest request) {
        UserPreferences prefs = getOrCreatePreferences(findUser(username));
        if (request.getTheme() != null) prefs.setTheme(request.getTheme());
        if (request.getDensity() != null) prefs.setDensity(request.getDensity());
        if (request.getFontSize() != null) prefs.setFontSize(request.getFontSize());
        if (request.getSidebarColor() != null) prefs.setSidebarColor(request.getSidebarColor());
        return toAppearanceResponse(preferencesRepository.save(prefs));
    }

    // ──────────────── Notifications ────────────────

    public NotificationsResponse getNotifications(String username) {
        UserPreferences prefs = getOrCreatePreferences(findUser(username));
        return toNotificationsResponse(prefs);
    }

    @Transactional
    public NotificationsResponse updateNotifications(String username, NotificationsRequest request) {
        UserPreferences prefs = getOrCreatePreferences(findUser(username));
        prefs.setEmailEnabled(request.isEmailEnabled());
        prefs.setLoginAlerts(request.isLoginAlerts());
        prefs.setWeeklyReport(request.isWeeklyReport());
        prefs.setNewRegistrations(request.isNewRegistrations());
        prefs.setSecurityAlerts(request.isSecurityAlerts());
        prefs.setMaintenanceAlerts(request.isMaintenanceAlerts());
        prefs.setExportCompleted(request.isExportCompleted());
        return toNotificationsResponse(preferencesRepository.save(prefs));
    }

    // ──────────────── Security ────────────────

    public SecuritySettingsResponse getSecuritySettings(String username) {
        UserPreferences prefs = getOrCreatePreferences(findUser(username));
        return toSecurityResponse(prefs);
    }

    @Transactional
    public SecuritySettingsResponse updateSecuritySettings(String username, SecuritySettingsRequest request) {
        UserPreferences prefs = getOrCreatePreferences(findUser(username));
        prefs.setTwoFactorAuth(request.isTwoFactorAuth());
        if (request.getSessionDuration() != null) prefs.setSessionDuration(request.getSessionDuration());
        prefs.setEnforceStrongPasswords(request.isEnforceStrongPasswords());
        prefs.setPasswordExpiry(request.isPasswordExpiry());
        prefs.setSingleSession(request.isSingleSession());
        return toSecurityResponse(preferencesRepository.save(prefs));
    }

    // ──────────────── Login History ────────────────

    public List<LoginHistoryResponse> getLoginHistory(String username) {
        User user = findUser(username);
        return loginHistoryRepository.findTop20ByUserOrderByDateDesc(user).stream()
                .map(h -> {
                    LoginHistoryResponse r = new LoginHistoryResponse();
                    r.setDate(h.getDate());
                    r.setDevice(h.getDevice());
                    r.setIp(h.getIpAddress());
                    r.setLocation(h.getLocation());
                    r.setSuccess(h.isSuccess());
                    return r;
                })
                .toList();
    }

    // ──────────────── Helpers ────────────────

    private User findUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
    }

    private UserPreferences getOrCreatePreferences(User user) {
        return preferencesRepository.findByUser(user)
                .orElseGet(() -> {
                    UserPreferences p = new UserPreferences();
                    p.setUser(user);
                    return preferencesRepository.save(p);
                });
    }

    private CompanySettingsResponse toCompanyResponse(CompanySettings s) {
        CompanySettingsResponse r = new CompanySettingsResponse();
        r.setId(s.getId());
        r.setCompanyName(s.getCompanyName());
        r.setCompanyEmail(s.getCompanyEmail());
        r.setCompanyPhone(s.getCompanyPhone());
        r.setWebsite(s.getWebsite());
        r.setAddress(s.getAddress());
        r.setTimezone(s.getTimezone());
        r.setDateFormat(s.getDateFormat());
        r.setLanguage(s.getLanguage());
        return r;
    }

    private ProfileResponse toProfileResponse(User u) {
        ProfileResponse r = new ProfileResponse();
        r.setId(u.getId());
        r.setUsername(u.getUsername());
        r.setFirstName(u.getFirstName());
        r.setLastName(u.getLastName());
        r.setEmail(u.getEmail());
        r.setPhone(u.getPhone());
        r.setPosition(u.getPosition());
        r.setBio(u.getBio());
        r.setPhotoUrl(u.getPhotoUrl());
        return r;
    }

    private AppearanceResponse toAppearanceResponse(UserPreferences p) {
        AppearanceResponse r = new AppearanceResponse();
        r.setTheme(p.getTheme());
        r.setDensity(p.getDensity());
        r.setFontSize(p.getFontSize());
        r.setSidebarColor(p.getSidebarColor());
        return r;
    }

    private NotificationsResponse toNotificationsResponse(UserPreferences p) {
        NotificationsResponse r = new NotificationsResponse();
        r.setEmailEnabled(p.isEmailEnabled());
        r.setLoginAlerts(p.isLoginAlerts());
        r.setWeeklyReport(p.isWeeklyReport());
        r.setNewRegistrations(p.isNewRegistrations());
        r.setSecurityAlerts(p.isSecurityAlerts());
        r.setMaintenanceAlerts(p.isMaintenanceAlerts());
        r.setExportCompleted(p.isExportCompleted());
        return r;
    }

    private SecuritySettingsResponse toSecurityResponse(UserPreferences p) {
        SecuritySettingsResponse r = new SecuritySettingsResponse();
        r.setTwoFactorAuth(p.isTwoFactorAuth());
        r.setSessionDuration(p.getSessionDuration());
        r.setEnforceStrongPasswords(p.isEnforceStrongPasswords());
        r.setPasswordExpiry(p.isPasswordExpiry());
        r.setSingleSession(p.isSingleSession());
        return r;
    }
}

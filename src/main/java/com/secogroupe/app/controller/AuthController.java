package com.secogroupe.app.controller;

import java.security.Principal;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

import com.secogroupe.app.dto.AuthRequest;
import com.secogroupe.app.dto.AuthResponse;
import com.secogroupe.app.dto.RefreshRequest;
import com.secogroupe.app.dto.RegisterRequest;
import com.secogroupe.app.dto.VerifyOtpRequest;
import com.secogroupe.app.service.AuthService;
import com.secogroupe.app.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    @PostMapping("/v1/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request) {
        userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Compte créé. Vérifiez votre email pour activer votre compte.");
    }

    @PostMapping("/v1/verify-otp")
    public ResponseEntity<String> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        userService.verifyByOtp(request.getEmail(), request.getOtpCode());
        return ResponseEntity.ok("Compte activé avec succès. Vous pouvez maintenant vous connecter.");
    }

    @GetMapping("/v1/verify-email")
    public ResponseEntity<String> verifyEmail(@RequestParam("token") String token) {
        userService.verifyByLink(token);
        return ResponseEntity.ok("Email vérifié. Votre compte est maintenant actif.");
    }

    @PostMapping("/v1/resend-verification")
    public ResponseEntity<String> resendVerification(@RequestParam("email") String email) {
        userService.resendVerification(email);
        return ResponseEntity.ok("Un nouveau code de vérification a été envoyé à " + email);
    }

    @PostMapping("/v1/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request,
                                              HttpServletRequest httpRequest) {
        String ip = resolveClientIp(httpRequest);
        String device = parseDevice(httpRequest.getHeader("User-Agent"));
        return ResponseEntity.ok(authService.login(request, ip, device));
    }

    private String resolveClientIp(HttpServletRequest req) {
        String forwarded = req.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) return forwarded.split(",")[0].trim();
        return req.getRemoteAddr();
    }

    private String parseDevice(String ua) {
        if (ua == null) return "Unknown";
        String browser = ua.contains("Edg") ? "Edge"
                : ua.contains("Chrome") ? "Chrome"
                : ua.contains("Firefox") ? "Firefox"
                : ua.contains("Safari") ? "Safari"
                : "Browser";
        String os = ua.contains("Windows NT 10") ? "Windows 10/11"
                : ua.contains("Windows") ? "Windows"
                : ua.contains("Mac OS X") ? "macOS"
                : ua.contains("Android") ? "Android"
                : (ua.contains("iPhone") || ua.contains("iPad")) ? "iOS"
                : ua.contains("Linux") ? "Linux"
                : "Unknown OS";
        return browser + " · " + os;
    }

    @PostMapping("/v1/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        return ResponseEntity.ok(authService.refresh(request));
    }

    @PostMapping("/v1/logout")
    public ResponseEntity<Void> logout(Principal principal) {
        authService.logout(principal.getName());
        return ResponseEntity.noContent().build();
    }
}

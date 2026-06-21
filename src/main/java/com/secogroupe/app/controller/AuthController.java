package com.secogroupe.app.controller;

import java.security.Principal;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import com.secogroupe.app.dto.AuthRequest;
import com.secogroupe.app.dto.AuthResponse;
import com.secogroupe.app.dto.LoginResult;
import com.secogroupe.app.dto.RegisterRequest;
import com.secogroupe.app.dto.VerifyOtpRequest;
import com.secogroupe.app.service.AuthService;
import com.secogroupe.app.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final String COOKIE_NAME = "refreshToken";

    private final AuthService authService;
    private final UserService userService;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpirationMs;

    @Value("${app.cookie.secure:true}")
    private boolean cookieSecure;

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request) {
        userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Compte créé. Vérifiez votre email pour activer votre compte.");
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<String> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        userService.verifyByOtp(request.getEmail(), request.getOtpCode());
        return ResponseEntity.ok("Compte activé avec succès. Vous pouvez maintenant vous connecter.");
    }

    @GetMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(@RequestParam("token") String token) {
        userService.verifyByLink(token);
        return ResponseEntity.ok("Email vérifié. Votre compte est maintenant actif.");
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<String> resendVerification(@RequestParam("email") String email) {
        userService.resendVerification(email);
        return ResponseEntity.ok("Un nouveau code de vérification a été envoyé à " + email);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request,
                                              HttpServletRequest httpRequest,
                                              HttpServletResponse httpResponse) {
        String ip = resolveClientIp(httpRequest);
        String device = parseDevice(httpRequest.getHeader("User-Agent"));

        LoginResult result = authService.login(request, ip, device);
        if (result == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        httpResponse.addHeader(HttpHeaders.SET_COOKIE, buildCookie(result.refreshToken()).toString());
        return ResponseEntity.ok(new AuthResponse(result.accessToken()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(HttpServletRequest httpRequest,
                                                HttpServletResponse httpResponse) {
        String refreshTokenId = readCookie(httpRequest);
        if (refreshTokenId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 

        LoginResult result = authService.refresh(refreshTokenId);
        if(result!=null)
            return ResponseEntity.ok(new AuthResponse(result.accessToken()));
            // httpResponse.addHeader(HttpHeaders.SET_COOKIE, buildCookie(result.refreshTokenId()).toString());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(Principal principal, HttpServletResponse httpResponse) {
        authService.logout(principal.getName());
        httpResponse.addHeader(HttpHeaders.SET_COOKIE, clearCookie().toString());
        return ResponseEntity.noContent().build();
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private ResponseCookie buildCookie(String tokenId) {
        return ResponseCookie.from(COOKIE_NAME, String.valueOf(tokenId))
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("None")
                .path("/api/v1/auth/refresh")
                .maxAge(refreshExpirationMs / 1000)
                .build();
    }

    private ResponseCookie clearCookie() {
        return ResponseCookie.from(COOKIE_NAME, "")
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("None")
                .path("/api/v1/auth/refresh")
                .maxAge(0)
                .build();
    }

    private String readCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        return Arrays.stream(cookies)
                .filter(c -> COOKIE_NAME.equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
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
}

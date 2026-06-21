package com.secogroupe.app.service;

import java.nio.charset.StandardCharsets;
import java.nio.ByteBuffer;
import java.security.MessageDigest;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * Génère et valide un code de présence rotatif (style TOTP).
 *
 * Le code change toutes les {@code PERIOD_SECONDS} secondes. Il est dérivé d'un secret
 * et de l'intervalle de temps courant via HMAC-SHA256 : il n'est donc jamais stocké
 * et ne peut pas être deviné. La validation accepte l'intervalle courant et le précédent
 * pour tolérer la latence réseau / le délai de scan.
 */
@Slf4j
@Service
public class AttendanceCodeService {

    public static final long PERIOD_SECONDS = 30;

    private final byte[] key;

    public AttendanceCodeService(@Value("${jwt.secret}") String secret) {
        // Séparation de domaine : on ne réutilise pas tel quel le secret JWT.
        this.key = (secret + "|attendance").getBytes(StandardCharsets.UTF_8);
    }

    /** Code valide pour l'intervalle de temps courant. */
    public String currentCode() {
        return codeForStep(currentStep());
    }

    /** Secondes restantes avant rotation du code courant. */
    public long secondsRemaining() {
        long now = System.currentTimeMillis() / 1000;
        return PERIOD_SECONDS - (now % PERIOD_SECONDS);
    }

    /** Vrai si le code correspond à l'intervalle courant ou au précédent. */
    public boolean isValid(String code) {
        if (code == null || code.isBlank()) return false;
        long step = currentStep();
        return constantTimeEquals(code, codeForStep(step))
                || constantTimeEquals(code, codeForStep(step - 1));
    }

    private long currentStep() {
        return (System.currentTimeMillis() / 1000) / PERIOD_SECONDS;
    }

    private String codeForStep(long step) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key, "HmacSHA256"));
            byte[] digest = mac.doFinal(ByteBuffer.allocate(Long.BYTES).putLong(step).array());
            StringBuilder sb = new StringBuilder("ATT-");
            // 20 caractères hex : non devinable, longueur sans impact (scan QR).
            for (int i = 0; i < 10; i++) {
                sb.append(String.format("%02x", digest[i]));
            }
            return sb.toString();
        } catch (Exception e) {
            log.error("Erreur génération code de présence", e);
            throw new IllegalStateException("Impossible de générer le code de présence", e);
        }
    }

    private boolean constantTimeEquals(String a, String b) {
        return MessageDigest.isEqual(
                a.getBytes(StandardCharsets.UTF_8),
                b.getBytes(StandardCharsets.UTF_8));
    }
}

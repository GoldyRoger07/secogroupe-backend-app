package com.secogroupe.app.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class EmailService {

    @Value("${app.mail.from}")
    private String fromAddress;

    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${app.mail.console-mode:false}")
    private boolean consoleModeEnabled;

    @Value("${resend.api-key}")
    private String resendApiKey;

    private final RestClient restClient = RestClient.create();

    private void send(String to, String subject, String html) {
        send(to, subject, html, null);
    }

    private void send(String to, String subject, String html, String replyTo) {
        Map<String, Object> body = new HashMap<>();
        body.put("from", fromAddress);
        body.put("to", List.of(to));
        body.put("subject", subject);
        body.put("html", html);
        if (replyTo != null) body.put("reply_to", replyTo);

        try {
            restClient.post()
                    .uri("https://api.resend.com/emails")
                    .header("Authorization", "Bearer " + resendApiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            throw new RuntimeException("Échec de l'envoi de l'email : " + e.getMessage(), e);
        }
    }

    public void sendVerificationEmail(String toEmail, String username, String otpCode, String linkToken) {
        String verificationLink = baseUrl + "/auth/v1/verify-email?token=" + linkToken;

        if (consoleModeEnabled) {
            log.info("╔══════════════ EMAIL DE VÉRIFICATION [mode console] ══════════════╗");
            log.info("  À           : {}", toEmail);
            log.info("  Utilisateur : {}", username);
            log.info("  Code OTP    : {}", otpCode);
            log.info("  Lien        : {}", verificationLink);
            log.info("╚══════════════════════════════════════════════════════════════════╝");
            return;
        }

        String html = """
                <!DOCTYPE html>
                <html lang="fr">
                <head><meta charset="UTF-8"></head>
                <body style="font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px;">
                  <div style="max-width: 520px; margin: auto; background: #ffffff; border-radius: 8px;
                              padding: 32px; box-shadow: 0 2px 8px rgba(0,0,0,0.1);">
                    <h2 style="color: #2c3e50;">Bienvenue, %s !</h2>
                    <p style="color: #555;">Votre compte a été créé avec succès. Pour l'activer, vérifiez votre adresse email.</p>

                    <div style="margin: 28px 0; text-align: center;">
                      <p style="color: #555; font-size: 14px; margin-bottom: 8px;">Option 1 — Entrez ce code dans l'application :</p>
                      <div style="display: inline-block; background: #f0f4ff; border: 2px solid #4a90e2;
                                  border-radius: 8px; padding: 14px 32px; letter-spacing: 10px;
                                  font-size: 32px; font-weight: bold; color: #2c3e50;">%s</div>
                      <p style="color: #999; font-size: 12px; margin-top: 8px;">Ce code expire dans <strong>15 minutes</strong>.</p>
                    </div>

                    <div style="margin: 28px 0; text-align: center;">
                      <p style="color: #555; font-size: 14px; margin-bottom: 12px;">Option 2 — Cliquez directement sur ce lien :</p>
                      <a href="%s"
                         style="display: inline-block; background-color: #4a90e2; color: white;
                                text-decoration: none; padding: 12px 28px; border-radius: 6px;
                                font-size: 15px; font-weight: bold;">
                        Vérifier mon email
                      </a>
                    </div>

                    <hr style="border: none; border-top: 1px solid #eee; margin: 24px 0;">
                    <p style="color: #aaa; font-size: 12px;">
                      Si vous n'êtes pas à l'origine de cette inscription, ignorez cet email.<br>
                      Le lien et le code expirent dans 15 minutes.
                    </p>
                  </div>
                </body>
                </html>
                """.formatted(username, otpCode, verificationLink);

        send(toEmail, "Vérifiez votre adresse email — Secogroupe", html);
    }

    public void sendQuoteNotification(com.secogroupe.app.dto.QuoteRequestDto dto) {
        if (consoleModeEnabled) {
            log.info("╔══════════════ NOUVELLE DEMANDE DE DEVIS [mode console] ══════════╗");
            log.info("  Nom         : {} {}", dto.getFirstName(), dto.getLastName());
            log.info("  Email       : {}", dto.getEmail());
            log.info("  Téléphone   : {}", dto.getDirectDialNumber());
            log.info("  Entreprise  : {}", dto.getBusinessName());
            log.info("  Service     : {}", dto.getServiceCategory());
            log.info("  Ville/État  : {}, {}", dto.getCity(), dto.getState());
            log.info("  Newsletter  : {}", dto.isNewsletterOptIn());
            log.info("╚══════════════════════════════════════════════════════════════════╝");
            return;
        }

        String newsletter = dto.isNewsletterOptIn()
                ? "<span style='color:#27ae60;font-weight:bold;'>✔ Oui</span>"
                : "<span style='color:#aaa;'>Non</span>";

        String html = """
                <!DOCTYPE html>
                <html lang="fr">
                <head><meta charset="UTF-8"></head>
                <body style="font-family:Arial,sans-serif;background:#f4f4f4;padding:20px;">
                  <div style="max-width:600px;margin:auto;background:#fff;border-radius:8px;
                              padding:32px;box-shadow:0 2px 8px rgba(0,0,0,0.1);">
                    <h2 style="color:#2c3e50;margin-top:0;">📋 Nouvelle demande de devis</h2>
                    <table style="width:100%%;border-collapse:collapse;">
                      <tr style="background:#f8f9fa;">
                        <td style="padding:10px 14px;font-weight:bold;color:#555;width:40%%;">Service / Catégorie</td>
                        <td style="padding:10px 14px;color:#2c3e50;">%s</td>
                      </tr>
                      <tr>
                        <td style="padding:10px 14px;font-weight:bold;color:#555;">Entreprise / Propriété</td>
                        <td style="padding:10px 14px;color:#2c3e50;">%s</td>
                      </tr>
                      <tr style="background:#f8f9fa;">
                        <td style="padding:10px 14px;font-weight:bold;color:#555;">Contact</td>
                        <td style="padding:10px 14px;color:#2c3e50;">%s %s</td>
                      </tr>
                      <tr>
                        <td style="padding:10px 14px;font-weight:bold;color:#555;">Téléphone</td>
                        <td style="padding:10px 14px;color:#2c3e50;">%s</td>
                      </tr>
                      <tr style="background:#f8f9fa;">
                        <td style="padding:10px 14px;font-weight:bold;color:#555;">Email</td>
                        <td style="padding:10px 14px;">
                          <a href="mailto:%s" style="color:#4a90e2;">%s</a>
                        </td>
                      </tr>
                      <tr>
                        <td style="padding:10px 14px;font-weight:bold;color:#555;">Ville / État</td>
                        <td style="padding:10px 14px;color:#2c3e50;">%s, %s</td>
                      </tr>
                      <tr style="background:#f8f9fa;">
                        <td style="padding:10px 14px;font-weight:bold;color:#555;">Newsletter</td>
                        <td style="padding:10px 14px;">%s</td>
                      </tr>
                    </table>
                    <div style="margin-top:24px;text-align:center;">
                      <a href="mailto:%s"
                         style="background:#4a90e2;color:#fff;padding:12px 28px;border-radius:6px;
                                text-decoration:none;font-weight:bold;display:inline-block;">
                        Répondre à %s
                      </a>
                    </div>
                  </div>
                </body>
                </html>
                """.formatted(
                dto.getServiceCategory(), dto.getBusinessName(),
                dto.getFirstName(), dto.getLastName(),
                dto.getDirectDialNumber() != null ? dto.getDirectDialNumber() : "—",
                dto.getEmail(), dto.getEmail(),
                dto.getCity() != null ? dto.getCity() : "—",
                dto.getState() != null ? dto.getState() : "—",
                newsletter,
                dto.getEmail(), dto.getFirstName());

        send(fromAddress, "Nouvelle demande de devis — " + dto.getFirstName() + " " + dto.getLastName(), html, dto.getEmail());
    }

    public void sendQuoteConfirmation(com.secogroupe.app.dto.QuoteRequestDto dto) {
        if (consoleModeEnabled) {
            log.info("[mode console] Accusé de réception envoyé à {}", dto.getEmail());
            return;
        }

        String html = """
                <!DOCTYPE html>
                <html lang="en">
                <head><meta charset="UTF-8"></head>
                <body style="font-family:Arial,sans-serif;background:#f4f4f4;padding:20px;">
                  <div style="max-width:520px;margin:auto;background:#fff;border-radius:8px;
                              padding:32px;box-shadow:0 2px 8px rgba(0,0,0,0.1);">
                    <h2 style="color:#2c3e50;margin-top:0;">Thank you, %s!</h2>
                    <p style="color:#555;">
                      We have received your quote request for <strong>%s</strong>
                      and our team will get back to you shortly.
                    </p>
                    <div style="background:#f0f4ff;border-left:4px solid #4a90e2;
                                padding:14px 18px;border-radius:4px;margin:20px 0;">
                      <p style="margin:0;color:#555;font-size:14px;">
                        <strong>Service requested:</strong> %s<br>
                        <strong>Business / Property:</strong> %s
                      </p>
                    </div>
                    <p style="color:#555;">
                      If you have any urgent questions, feel free to reach us at
                      <a href="mailto:%s" style="color:#4a90e2;">%s</a>.
                    </p>
                    <hr style="border:none;border-top:1px solid #eee;margin:24px 0;">
                    <p style="color:#aaa;font-size:12px;">
                      You are receiving this email because you submitted a quote request on secogroupe.com.
                    </p>
                  </div>
                </body>
                </html>
                """.formatted(
                dto.getFirstName(),
                dto.getBusinessName(),
                dto.getServiceCategory(),
                dto.getBusinessName(),
                fromAddress, fromAddress);

        send(dto.getEmail(), "We received your quote request — Secogroupe", html);
    }
}

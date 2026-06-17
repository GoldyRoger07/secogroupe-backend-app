package com.secogroupe.app.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromAddress;

    @Value("${app.base-url}")
    private String baseUrl;

    public void sendVerificationEmail(String toEmail, String username, String otpCode, String linkToken) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromAddress);
            helper.setTo(toEmail);
            helper.setSubject("Vérifiez votre adresse email — Secogroupe");

            String verificationLink = baseUrl + "/auth/verify-email?token=" + linkToken;

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

            helper.setText(html, true);
            mailSender.send(message);

        } catch (MessagingException e) {
            throw new RuntimeException("Échec de l'envoi de l'email de vérification : " + e.getMessage(), e);
        }
    }
}

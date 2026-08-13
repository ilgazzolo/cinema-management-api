package com.api.boleteria.service;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * Servicio encargado del envío de correos electrónicos de la aplicación.
 */
@Service
public class EmailService {

    // ---- Personalización del correo de recuperación de contraseña ----
    private static final String RESET_EMAIL_SUBJECT = "Recuperación de contraseña - CinePass";
    private static final String RESET_EMAIL_INTRO = "Recibimos una solicitud para restablecer tu contraseña. Haz click en el botón para crear una contraseña nueva.";
    private static final String RESET_EMAIL_CTA_TEXT = "Restablecer contraseña";
    private static final String RESET_EMAIL_FOOTER = "Este enlace expira en 30 minutos. Si no solicitaste este cambio, podés ignorar este correo.";
    private static final String LOGO_RESOURCE_PATH = "email/logo.png";
    private static final String LOGO_CONTENT_ID = "cinepassLogo";
    // --------------------------------------------------------------------

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Envía el correo con el enlace para restablecer la contraseña.
     *
     * @param to        Email de destino.
     * @param resetLink Enlace completo hacia el formulario de recuperación.
     */
    public void sendPasswordResetEmail(String to, String resetLink) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, MimeMessageHelper.MULTIPART_MODE_RELATED, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(RESET_EMAIL_SUBJECT);
            helper.setText(buildResetEmailPlainText(resetLink), buildResetEmailHtml(resetLink));
            helper.addInline(LOGO_CONTENT_ID, new ClassPathResource(LOGO_RESOURCE_PATH));

            mailSender.send(mimeMessage);
        } catch (Exception e) {
            throw new RuntimeException("No se pudo enviar el correo de recuperación de contraseña.", e);
        }
    }

    /**
     * Arma la versión en texto plano del correo (respaldo para clientes que no
     * renderizan HTML y señal anti-spam, ya que el mail queda como multipart/alternative).
     */
    private String buildResetEmailPlainText(String resetLink) {
        return RESET_EMAIL_INTRO + "\n\n" +
                RESET_EMAIL_CTA_TEXT + ": " + resetLink + "\n\n" +
                RESET_EMAIL_FOOTER;
    }

    /**
     * Arma el cuerpo HTML del correo de recuperación de contraseña.
     */
    private String buildResetEmailHtml(String resetLink) {
        return """
                <!DOCTYPE html>
                <html>
                <body style="margin:0; padding:0; background-color:#f4f4f7; font-family:Arial, Helvetica, sans-serif;">
                  <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="background-color:#f4f4f7; padding:32px 0;">
                    <tr>
                      <td align="center">
                        <table role="presentation" width="480" cellpadding="0" cellspacing="0" style="background-color:#ffffff; border-radius:8px; overflow:hidden;">
                          <tr>
                            <td align="center" style="background-color:#141414; padding:24px;">
                              <img src="cid:%s" alt="CinePass" style="height:48px;" />
                            </td>
                          </tr>
                          <tr>
                            <td style="padding:32px; color:#222222;">
                              <h2 style="margin-top:0;">Recuperá tu contraseña</h2>
                              <p style="font-size:15px; line-height:1.5;">%s</p>
                              <p style="text-align:center; margin:32px 0;">
                                <a href="%s" style="background-color:#e50914; color:#ffffff; text-decoration:none; padding:14px 28px; border-radius:6px; font-weight:bold; display:inline-block;">%s</a>
                              </p>
                              <p style="font-size:13px; color:#777777; line-height:1.5;">%s</p>
                            </td>
                          </tr>
                        </table>
                      </td>
                    </tr>
                  </table>
                </body>
                </html>
                """.formatted(LOGO_CONTENT_ID, RESET_EMAIL_INTRO, resetLink, RESET_EMAIL_CTA_TEXT, RESET_EMAIL_FOOTER);
    }
}

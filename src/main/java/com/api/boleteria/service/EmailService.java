package com.api.boleteria.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Servicio encargado del envío de correos electrónicos de la aplicación.
 */
@Service
public class EmailService {

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
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject("Recuperación de contraseña - Boletería");
        message.setText(
                "Recibimos una solicitud para restablecer tu contraseña.\n\n" +
                        "Hacé clic en el siguiente enlace para crear una nueva contraseña:\n" + resetLink + "\n\n" +
                        "Este enlace expira en 30 minutos. Si no solicitaste este cambio, podés ignorar este correo."
        );
        mailSender.send(message);
    }
}

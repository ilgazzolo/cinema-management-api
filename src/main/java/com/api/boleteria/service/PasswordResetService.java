package com.api.boleteria.service;

import com.api.boleteria.dto.request.ForgotPasswordRequestDTO;
import com.api.boleteria.dto.request.ResetPasswordRequestDTO;
import com.api.boleteria.exception.BadRequestException;
import com.api.boleteria.model.PasswordResetToken;
import com.api.boleteria.model.User;
import com.api.boleteria.repository.IPasswordResetTokenRepository;
import com.api.boleteria.repository.IUserRepository;
import com.api.boleteria.validators.UserValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Servicio que gestiona el flujo de recuperación de contraseña vía email.
 */
@Service
public class PasswordResetService {

    private final IUserRepository userRepository;
    private final IPasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Value("${app.password-reset.frontend-url}")
    private String frontendUrl;

    @Value("${app.password-reset.expiration-minutes}")
    private long expirationMinutes;

    public PasswordResetService(IUserRepository userRepository,
                                 IPasswordResetTokenRepository tokenRepository,
                                 PasswordEncoder passwordEncoder,
                                 EmailService emailService) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    /**
     * Inicia el proceso de recuperación: si el email existe, genera un token de un solo uso
     * y envía un correo con el enlace de recuperación. Por seguridad, no revela si el email
     * está registrado o no.
     *
     * @param req DTO con el email del usuario.
     */
    public void requestReset(ForgotPasswordRequestDTO req) {
        userRepository.findByEmail(req.getEmail()).ifPresent(user -> {
            tokenRepository.deleteByUser(user);

            String token = UUID.randomUUID().toString();
            PasswordResetToken resetToken = new PasswordResetToken(
                    token,
                    user,
                    LocalDateTime.now().plusMinutes(expirationMinutes)
            );
            tokenRepository.save(resetToken);

            String resetLink = frontendUrl + "?token=" + token;
            emailService.sendPasswordResetEmail(user.getEmail(), resetLink);
        });
    }

    /**
     * Completa el proceso de recuperación: valida el token y establece la nueva contraseña.
     *
     * @param req DTO con el token y la nueva contraseña.
     * @throws BadRequestException si el token es inválido, ya fue usado o expiró.
     */
    public void resetPassword(ResetPasswordRequestDTO req) {
        UserValidator.validatePassword(req.getNewPassword());

        PasswordResetToken resetToken = tokenRepository.findByToken(req.getToken())
                .orElseThrow(() -> new BadRequestException("El enlace de recuperación no es válido."));

        if (resetToken.isUsed()) {
            throw new BadRequestException("Este enlace de recuperación ya fue utilizado.");
        }

        if (resetToken.isExpired()) {
            throw new BadRequestException("El enlace de recuperación expiró. Solicitá uno nuevo.");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);

        resetToken.setUsed(true);
        tokenRepository.save(resetToken);
    }
}

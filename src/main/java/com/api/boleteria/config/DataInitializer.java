package com.api.boleteria.config;

import com.api.boleteria.dto.request.RegisterRequestDTO;
import com.api.boleteria.model.enums.Role;
import com.api.boleteria.repository.IUserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import com.api.boleteria.model.User;

@Component
public class DataInitializer implements CommandLineRunner {

    private final IUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(IUserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Método que se ejecuta al iniciar la aplicación.
     *
     * Verifica si existe un usuario con nombre de usuario "admin".
     * Si no existe, crea uno con rol ADMIN y credenciales predeterminadas.
     *
     * Este método es útil para garantizar que siempre haya al menos un administrador en el sistema.
     */
    @Override
    public void run(String... args) throws Exception {
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = new User();
            admin.setName("Admin");
            admin.setSurname("Admin");
            admin.setUsername("admin");
            admin.setEmail("admin@cinepass.com");
            admin.setPassword(passwordEncoder.encode("Admin123!"));
            admin.setRole(Role.ADMIN);

            userRepository.save(admin);
        }
    }

}


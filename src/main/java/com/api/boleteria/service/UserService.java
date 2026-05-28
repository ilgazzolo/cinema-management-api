package com.api.boleteria.service;

import com.api.boleteria.config.JwtUtil;
import com.api.boleteria.dto.detail.UserDetailDTO;
import com.api.boleteria.dto.list.UserListDTO;
import com.api.boleteria.dto.request.LoginRequestDTO;
import com.api.boleteria.dto.request.RegisterRequestDTO;
import com.api.boleteria.exception.BadRequestException; //
import com.api.boleteria.exception.NotFoundException; //
import com.api.boleteria.model.enums.Role;
import com.api.boleteria.model.User; //
import com.api.boleteria.repository.IUserRepository; //
import com.api.boleteria.validators.UserValidator; //
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Servicio para gestionar operaciones relacionadas con Usuarios.
 */
@Service
@AllArgsConstructor
public class UserService implements UserDetailsService {

    private final IUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    //-------------------------------SAVE--------------------------------//

    /**
     * Crea un nuevo usuario.
     *
     * @param req DTO con la información del nuevo usuario.
     * @return UserDetailDTO con los datos del usuario creado.
     */
    public UserDetailDTO save(RegisterRequestDTO req) {
        UserValidator.validateFields(req);

        if (userRepository.findByEmail(req.getEmail()).isPresent()) {
            throw new BadRequestException("El email ya está registrado.");
        }

        if (userRepository.findByUsername(req.getUsername()).isPresent()) {
            throw new BadRequestException("El nombre de usuario ya está en uso.");
        }

        User entity = mapToEntity(req);

        User saved = userRepository.save(entity);
        return mapToDetailDTO(saved);
    }



    //-------------------------------FIND--------------------------------//

    /**
     * Muestra todos los usuarios.
     *
     * @return Lista de UserListDTO con la información de los usuarios guardados.
     */
    public List<UserListDTO> findAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::mapToListDTO)
                .toList();
    }

    /**
     * Busca un usuario por su ID.
     *
     * @param id ID del usuario a buscar.
     * @return UserDetailDTO con la información del usuario encontrado.
     * @throws BadRequestException si el ID es inválido.
     * @throws NotFoundException si no se encuentra un usuario con el ID proporcionado.
     */
    public UserDetailDTO findById(Long id) {
        UserValidator.validateId(id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("El usuario con ID: " + id + " no fue encontrado."));
        return mapToDetailDTO(user);
    }

    /**
     * Busca un usuario por su nombre de usuario.
     *
     * @param username Nombre de usuario a buscar.
     * @return UserDetailDTO con la información del usuario encontrado.
     * @throws BadRequestException si el nombre de usuario es inválido.
     * @throws NotFoundException si no se encuentra un usuario con el nombre proporcionado.
     */
    public UserDetailDTO findByUsername(String username) {
        UserValidator.validateUsername(username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("El usuario con nombre: " + username + " no fue encontrado"));
        return mapToDetailDTO(user);
    }


    /**
     * Obtiene los datos del perfil del usuario autenticado actualmente.
     *
     * @return DTO con los datos del usuario (nombre, apellido, username, email, rol).
     * @throws NotFoundException si el usuario autenticado no existe en la base de datos.
     */
    public UserDetailDTO findProfile() {
        User user = findAuthenticatedUser();
        return mapToDetailDTO(user);
    }

    /**
     * Obtiene el usuario actualmente autenticado en el sistema.
     *
     * @return Entidad User del usuario autenticado.
     * @throws NotFoundException si el usuario no existe.
     */
    public User findAuthenticatedUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Usuario con nombre de usuario: " + username + " no encontrado."));
    }



    //-------------------------------UPDATE--------------------------------//

    /**
     * Actualización de usuario autenticado.
     *
     * @param req DTO del usuario con cambios realizados.
     * @return UserDetailDTO con la información actualizada del usuario especificado.
     * @throws BadRequestException si el nuevo username o email ya están en uso por otro usuario,
     *                             o si la contraseña actual no es correcta al intentar cambiarla.
     */
    public UserDetailDTO update(RegisterRequestDTO req) {
        User user = findAuthenticatedUser();

        // Validaciones específicas para actualización (password opcional)
        UserValidator.validateUpdateFields(req);

        // Validar si el nuevo username ya existe para otro usuario
        if (!user.getUsername().equals(req.getUsername())
                && userRepository.existsByUsernameAndIdNot(req.getUsername(), user.getId())) {
            throw new BadRequestException("El nombre de usuario '" + req.getUsername() + "' ya está en uso por otro usuario.");
        }

        // Validar si el nuevo email ya existe para otro usuario
        if (!user.getEmail().equals(req.getEmail())
                && userRepository.existsByEmailAndIdNot(req.getEmail(), user.getId())) {
            throw new BadRequestException("El email '" + req.getEmail() + "' ya está registrado por otro usuario.");
        }

        //  Si quiere cambiar contraseña, validar contraseña actual
        if (req.getPassword() != null && !req.getPassword().isBlank()) {

            // Debe enviar currentPassword
            if (req.getCurrentPassword() == null || req.getCurrentPassword().isBlank()) {
                throw new BadRequestException("Debes ingresar tu contraseña actual para cambiarla.");
            }

            // currentPassword debe coincidir con la del usuario
            if (!passwordEncoder.matches(req.getCurrentPassword(), user.getPassword())) {
                throw new BadRequestException("La contraseña actual no es correcta.");
            }
        }

        // Actualizar entidad
        updateEntityFromDto(req, user);

        User updated = userRepository.save(user);
        return mapToDetailDTO(updated);
    }



    /**
     * Cambia el rol de un usuario a ADMIN si el usuario existe.
     *
     * @param username Nombre de usuario a modificar.
     * @return true si el usuario fue encontrado y su rol fue cambiado; false si no se encontró.
     */

    public UserDetailDTO makeUserAdmin(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        if (user.getRole() == Role.ADMIN) {
            throw new IllegalStateException("El usuario ya es ADMIN");
        }

        user.setRole(Role.ADMIN);
        User updatedUser = userRepository.save(user);

        return mapToDetailDTO(updatedUser);
    }



    //-------------------------------AUTH--------------------------------//

    /**
     * Carga los detalles de un usuario a partir de su nombre de usuario.
     * Este método es utilizado por Spring Security durante el proceso de autenticación.
     *
     * @param usernameOrEmail Nombre de usuario.
     * @return UserDetails con la información del usuario autenticado.
     * @throws NotFoundException si el usuario no existe.
     */
    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) {
        User user = userRepository.findByUsername(usernameOrEmail)
                .or(() -> userRepository.findByEmail(usernameOrEmail))
                .orElseThrow(() -> new NotFoundException("El usuario: " + usernameOrEmail + " no fue encontrado."));

        GrantedAuthority authority = new SimpleGrantedAuthority(user.getRole().getRoleName());

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                List.of(authority)
        );
    }

    /**
     * Realiza el proceso de login autenticando al usuario y generando un token JWT si las credenciales son válidas.
     *
     * @param req DTO con los datos de login (username y password).
     * @param authManager AuthenticationManager configurado por Spring Security.
     * @return Mapa con el token JWT generado.
     */
    public Map<String, String> login(LoginRequestDTO req, AuthenticationManager authManager) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getUsernameOrEmail(), req.getPassword())
        );

        UserDetails user = (UserDetails) auth.getPrincipal();
        String jwt = JwtUtil.createToken(
                user.getUsername(),
                user.getAuthorities().stream()
                        .map(a -> a.getAuthority().replace("ROLE_", ""))
                        .collect(Collectors.toList())
        );

        return Map.of("token", jwt);
    }



    //-------------------------------MAPS--------------------------------//

    /**
     * Mapea una entidad User a su DTO detallado.
     *
     * @param user Entidad User a mapear.
     * @return UserDetailDTO con la información del usuario.
     */
    private UserDetailDTO mapToDetailDTO(User user) {
        return new UserDetailDTO(
                user.getId(),
                user.getName(),
                user.getSurname(),
                user.getUsername(),
                user.getEmail(),
                user.getRole().name(),
                user.getPoints()
        );
    }

    /**
     * Mapea una entidad User a su DTO de lista.
     *
     * @param user Entidad User a mapear.
     * @return UserListDTO con la información básica del usuario.
     */
    private UserListDTO mapToListDTO(User user) {
        return new UserListDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole().name(),
                user.getPoints()
        );
    }

    private User mapToEntity(RegisterRequestDTO req) {
        return new User(
                req.getName(),
                req.getSurname(),
                req.getUsername(),
                req.getEmail(),
                passwordEncoder.encode(req.getPassword())
        );
    }

    private void updateEntityFromDto(RegisterRequestDTO dto, User user) {
        user.setName(dto.getName());
        user.setSurname(dto.getSurname());
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());

        // Si la nueva contraseña NO viene informada, se mantiene la actual
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        // Si está vacía → no se toca user.setPassword(...)
    }



}
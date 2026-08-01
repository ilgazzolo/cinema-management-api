package com.api.boleteria.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Clase de configuración de seguridad para la aplicación.
 *
 * Define las reglas de seguridad HTTP, deshabilita CSRF, login por formulario y HTTP Basic,
 * configura el manejo de sesiones como stateless, y establece permisos y autenticación para rutas específicas.
 * También registra el filtro JwtAuthFilter para validar tokens JWT en cada solicitud.
 */

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    /**
     * Configura la cadena de filtros de seguridad HTTP.
     *
     * @param http objeto HttpSecurity para configurar la seguridad HTTP.
     * @return SecurityFilterChain configurada con reglas y filtros definidos.
     * @throws Exception si ocurre un error en la configuración.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors().and()
                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())

                .sessionManagement(sm ->
                        sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) ->
                                response.sendError(HttpServletResponse.SC_UNAUTHORIZED))
                        .accessDeniedHandler((request, response, accessDeniedException) ->
                                response.sendError(HttpServletResponse.SC_FORBIDDEN)))

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/cinemas/**").permitAll()
                        .requestMatchers("/api/movies/**").permitAll()
                        .requestMatchers("/api/functions/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/products", "/api/products/**").permitAll()
                        .requestMatchers("/api/payments/webhooks/**").permitAll()
                        .requestMatchers("/api/tickets/**").authenticated()
                        .requestMatchers("/api/userManagement/**").authenticated()
                        .requestMatchers("/api/payments/**").authenticated()
                        .anyRequest().authenticated())

                .addFilterBefore(jwtAuthFilter(),
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Crea un bean del filtro JwtAuthFilter para validar tokens JWT.
     *
     * @return instancia de JwtAuthFilter.
     */
    @Bean
    public JwtAuthFilter jwtAuthFilter() {return new JwtAuthFilter();}

    /**
     * Proporciona el AuthenticationManager necesario para la autenticación.
     *
     * @param config configuración de autenticación de Spring.
     * @return AuthenticationManager configurado.
     * @throws Exception si ocurre un error al obtener el AuthenticationManager.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }


}

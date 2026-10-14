package com.laesquina.inventario.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuracion de Spring Security (Avance 2).
 *
 * Mecanismo: HTTP Basic. En cada solicitud el cliente envia usuario y contrasena
 * en la cabecera Authorization; Spring Security los valida contra la tabla usuario
 * ANTES de que la solicitud llegue al controlador.
 *
 * Roles: ADMIN (gestiona el catalogo) y OPERADOR (consulta y registra movimientos).
 * No hay endpoints publicos: toda solicitud sin credenciales recibe 401.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String ADMIN = "ADMIN";
    private static final String OPERADOR = "OPERADOR";

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, RespuestasSeguridad respuestas) throws Exception {
        http
            // API REST sin sesiones ni cookies: la proteccion CSRF no aplica y bloquearia los POST.
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .httpBasic(basic -> basic.authenticationEntryPoint(respuestas))
            .exceptionHandling(e -> e
                    .authenticationEntryPoint(respuestas)   // 401
                    .accessDeniedHandler(respuestas))       // 403
            .authorizeHttpRequests(auth -> auth
                // Pagina interna de errores de Spring Boot (no expone datos del inventario)
                .requestMatchers("/error").permitAll()
                // Consultas: ADMIN y OPERADOR
                .requestMatchers(HttpMethod.GET, "/api/productos/**", "/api/categorias/**",
                        "/api/movimientos/**").hasAnyRole(ADMIN, OPERADOR)
                // Registro de movimientos (incluye el lote): ADMIN y OPERADOR
                .requestMatchers(HttpMethod.POST, "/api/movimientos", "/api/movimientos/lote")
                        .hasAnyRole(ADMIN, OPERADOR)
                // Gestion del catalogo (POST, PUT, DELETE): solo ADMIN
                .requestMatchers("/api/productos/**", "/api/categorias/**").hasRole(ADMIN)
                // Cualquier otra ruta (incluida la interfaz web): usuario autenticado
                .anyRequest().authenticated()
            );
        return http.build();
    }

    // Cifrado de contrasenas con BCrypt: la contrasena original nunca se guarda.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

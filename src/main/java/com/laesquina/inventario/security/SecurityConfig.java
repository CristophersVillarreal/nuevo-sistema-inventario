package com.laesquina.inventario.security;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuracion de Spring Security.
 *
 * Mecanismo: JWT. El cliente inicia sesion una sola vez en POST /api/auth/login con
 * usuario y contrasena; recibe un token firmado y lo envia en cada solicitud en la
 * cabecera "Authorization: Bearer <token>". JwtAuthenticationFilter valida el token
 * ANTES de que la solicitud llegue al controlador.
 *
 * Roles: ADMIN (gestiona el catalogo y los usuarios) y OPERADOR (consulta y registra
 * movimientos). Solo el inicio de sesion y las fotos de perfil son publicos.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String ADMIN = "ADMIN";
    private static final String OPERADOR = "OPERADOR";

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, RespuestasSeguridad respuestas,
                                           JwtAuthenticationFilter jwtFilter) throws Exception {
        http
            .cors(Customizer.withDefaults())
            // API REST sin sesiones ni cookies: la proteccion CSRF no aplica y bloquearia los POST.
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // El filtro JWT se ejecuta antes del filtro estandar de usuario y contrasena.
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
            .exceptionHandling(e -> e
                    .authenticationEntryPoint(respuestas)   // 401
                    .accessDeniedHandler(respuestas))       // 403
            .authorizeHttpRequests(auth -> auth
                // Inicio de sesion: publico (es donde se obtiene el token)
                .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                // Pagina interna de errores de Spring Boot (no expone datos del inventario)
                .requestMatchers("/error").permitAll()
                // Fotos de perfil: las muestra la interfaz en etiquetas <img>, sin token
                .requestMatchers("/fotos-perfil/**").permitAll()
                // Gestion de usuarios: solo ADMIN
                .requestMatchers("/api/usuarios/**").hasRole(ADMIN)
                // Perfil propio: cualquier usuario autenticado
                .requestMatchers("/api/perfil/**").authenticated()
                // Consultas: ADMIN y OPERADOR
                .requestMatchers(HttpMethod.GET, "/api/productos/**", "/api/categorias/**",
                        "/api/movimientos/**").hasAnyRole(ADMIN, OPERADOR)
                // Registro de movimientos (incluye el lote): ADMIN y OPERADOR
                .requestMatchers(HttpMethod.POST, "/api/movimientos", "/api/movimientos/lote")
                        .hasAnyRole(ADMIN, OPERADOR)
                // Gestion del catalogo (POST, PUT, DELETE): solo ADMIN
                .requestMatchers("/api/productos/**", "/api/categorias/**").hasRole(ADMIN)
                // Asistente con IA: ADMIN y OPERADOR
                .requestMatchers(HttpMethod.POST, "/api/asistente/consultar").hasAnyRole(ADMIN, OPERADOR)
                // Cualquier otra ruta: usuario autenticado
                .anyRequest().authenticated()
            );
        return http.build();
    }

    // Lo usa AuthController para verificar usuario y contrasena (contra la tabla usuario y BCrypt).
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuracion) throws Exception {
        return configuracion.getAuthenticationManager();
    }

    // El filtro JWT solo debe correr dentro de Spring Security, no como filtro suelto del servidor.
    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> registroFiltroJwt(JwtAuthenticationFilter filtro) {
        FilterRegistrationBean<JwtAuthenticationFilter> registro = new FilterRegistrationBean<>(filtro);
        registro.setEnabled(false);
        return registro;
    }

    // Cifrado de contrasenas con BCrypt: la contrasena original nunca se guarda.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

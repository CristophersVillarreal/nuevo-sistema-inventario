package com.laesquina.inventario.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.laesquina.inventario.controller.ErrorRespuesta;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Respuestas JSON de seguridad, con el mismo formato que el resto de errores:
 *  - 401 Unauthorized: no se pudo identificar al usuario (sin token, o token alterado o expirado).
 *  - 403 Forbidden: el usuario esta identificado, pero su rol no tiene permiso.
 */
@Component
public class RespuestasSeguridad implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public RespuestasSeguridad(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    // 401: se ejecuta cuando la solicitud no trae un token JWT valido.
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException ex) throws IOException {
        // Indica al cliente que debe autenticarse con un token Bearer (JWT).
        response.setHeader("WWW-Authenticate", "Bearer");
        Object motivo = request.getAttribute(JwtAuthenticationFilter.MOTIVO_401);
        String mensaje = motivo != null ? motivo.toString()
                : "Debe iniciar sesión y enviar el token en la cabecera Authorization";
        escribir(response, HttpStatus.UNAUTHORIZED, mensaje, request);
    }

    // 403: se ejecuta cuando el usuario autenticado no tiene el rol requerido.
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException ex) throws IOException {
        escribir(response, HttpStatus.FORBIDDEN,
                "Su rol no tiene permiso para realizar esta operación", request);
    }

    private void escribir(HttpServletResponse response, HttpStatus s, String mensaje,
                          HttpServletRequest request) throws IOException {
        response.setStatus(s.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getOutputStream(),
                new ErrorRespuesta(s.value(), s.getReasonPhrase(), mensaje, request.getRequestURI()));
    }
}

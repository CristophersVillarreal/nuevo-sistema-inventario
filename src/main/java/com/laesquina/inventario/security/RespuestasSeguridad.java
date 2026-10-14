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
 *  - 401 Unauthorized: no se pudo identificar al usuario (sin credenciales o incorrectas).
 *  - 403 Forbidden: el usuario esta identificado, pero su rol no tiene permiso.
 */
@Component
public class RespuestasSeguridad implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public RespuestasSeguridad(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    // 401: se ejecuta cuando falta la autenticacion o las credenciales son incorrectas.
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException ex) throws IOException {
        // Indica al cliente (navegador o Postman) que debe autenticarse con HTTP Basic.
        response.setHeader("WWW-Authenticate", "Basic realm=\"La Esquina\"");
        escribir(response, HttpStatus.UNAUTHORIZED,
                "Debe autenticarse con un usuario y contraseña válidos", request);
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

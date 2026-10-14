package com.laesquina.inventario.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Manejador global de errores de la API.
 * Convierte las excepciones en respuestas JSON con el codigo HTTP adecuado.
 */
@RestControllerAdvice
public class ManejadorErrores {

    // Recurso no encontrado (producto, categoria) -> 404
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErrorRespuesta> noEncontrado(NoSuchElementException ex, HttpServletRequest req) {
        return responder(HttpStatus.NOT_FOUND, ex.getMessage(), req);
    }

    // Regla de negocio incumplida (p. ej. stock insuficiente) -> 400
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorRespuesta> solicitudInvalida(IllegalArgumentException ex, HttpServletRequest req) {
        return responder(HttpStatus.BAD_REQUEST, ex.getMessage(), req);
    }

    // Conflicto con datos existentes (nombre repetido, producto con historial) -> 409
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorRespuesta> conflicto(IllegalStateException ex, HttpServletRequest req) {
        return responder(HttpStatus.CONFLICT, ex.getMessage(), req);
    }

    // Campos que no cumplen las validaciones (@NotBlank, @Positive...) -> 400 con el detalle por campo
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorRespuesta> datosInvalidos(MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String, String> detalles = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(e -> detalles.putIfAbsent(e.getField(), e.getDefaultMessage()));
        HttpStatus s = HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(s).body(new ErrorRespuesta(s.value(), s.getReasonPhrase(),
                "Datos inválidos", req.getRequestURI(), detalles));
    }

    // JSON mal formado o valor no permitido (p. ej. tipo "OTRO") -> 400
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorRespuesta> jsonInvalido(HttpMessageNotReadableException ex, HttpServletRequest req) {
        return responder(HttpStatus.BAD_REQUEST,
                "El cuerpo de la solicitud no es válido. Revise el formato JSON y los valores enviados", req);
    }

    // Parametro de ruta con tipo incorrecto (p. ej. /api/productos/abc) -> 400
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorRespuesta> parametroInvalido(MethodArgumentTypeMismatchException ex,
                                                            HttpServletRequest req) {
        return responder(HttpStatus.BAD_REQUEST, "Valor inválido para el parámetro " + ex.getName(), req);
    }

    private ResponseEntity<ErrorRespuesta> responder(HttpStatus s, String mensaje, HttpServletRequest req) {
        return ResponseEntity.status(s)
                .body(new ErrorRespuesta(s.value(), s.getReasonPhrase(), mensaje, req.getRequestURI()));
    }
}

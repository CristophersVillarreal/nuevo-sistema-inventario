package com.laesquina.inventario.controller;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Formato unico de las respuestas de error de la API (400, 401, 403, 404, 409).
 * Ejemplo:
 * {"estado":403,"error":"Forbidden","mensaje":"...","ruta":"/api/productos","fecha":"..."}
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorRespuesta(int estado, String error, String mensaje, String ruta,
                             LocalDateTime fecha, Map<String, String> detalles) {

    public ErrorRespuesta(int estado, String error, String mensaje, String ruta) {
        this(estado, error, mensaje, ruta, LocalDateTime.now(), null);
    }

    public ErrorRespuesta(int estado, String error, String mensaje, String ruta, Map<String, String> detalles) {
        this(estado, error, mensaje, ruta, LocalDateTime.now(), detalles);
    }
}

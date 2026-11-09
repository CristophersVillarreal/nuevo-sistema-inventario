package com.laesquina.inventario.controller;

import com.laesquina.inventario.service.AsistenteService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/asistente")
public class AsistenteController {

    private final AsistenteService asistenteService;

    public AsistenteController(AsistenteService asistenteService) {
        this.asistenteService = asistenteService;
    }

    @PostMapping("/consultar")
    public Map<String, String> consultar(@RequestBody Map<String, String> body) {
        String pregunta = body.get("pregunta");
        String respuesta = asistenteService.responder(pregunta);
        return Map.of("respuesta", respuesta);
    }
}
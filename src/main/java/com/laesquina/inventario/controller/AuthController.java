package com.laesquina.inventario.controller;

import com.laesquina.inventario.model.Usuario;
import com.laesquina.inventario.repository.UsuarioRepository;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class AuthController {

    private final UsuarioRepository usuarioRepository;

    public AuthController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping("/api/auth/me")
    public Map<String, Object> yo(Authentication authentication) {
        String rol = authentication.getAuthorities().stream()
                .findFirst()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .orElse("");

        Usuario u = usuarioRepository.findByUsername(authentication.getName()).orElse(null);

        return Map.of(
                "usuario", authentication.getName(),
                "rol", rol,
                "nombre", u != null && u.getNombre() != null ? u.getNombre() : "",
                "apellidos", u != null && u.getApellidos() != null ? u.getApellidos() : "",
                "foto", u != null && u.getFoto() != null ? u.getFoto() : ""
        );
    }
}
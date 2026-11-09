package com.laesquina.inventario.controller;

import com.laesquina.inventario.model.Usuario;
import com.laesquina.inventario.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/perfil")
public class PerfilController {

    private final UsuarioRepository usuarioRepository;
    private static final String CARPETA_FOTOS = "fotos-perfil";

    public PerfilController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping
    public Map<String, Object> verMiPerfil(Authentication authentication) {
        Usuario u = buscarUsuarioActual(authentication);
        return aMapa(u);
    }

    @PutMapping
    public Map<String, Object> actualizarMiPerfil(Authentication authentication, @RequestBody Map<String, String> body) {
        Usuario u = buscarUsuarioActual(authentication);
        u.setNombre(body.get("nombre"));
        u.setApellidos(body.get("apellidos"));
        return aMapa(usuarioRepository.save(u));
    }

    @PostMapping("/foto")
    public Map<String, Object> subirFoto(Authentication authentication, @RequestParam("archivo") MultipartFile archivo) throws IOException {
        Usuario u = buscarUsuarioActual(authentication);

        Path carpeta = Paths.get(CARPETA_FOTOS);
        if (!Files.exists(carpeta)) {
            Files.createDirectories(carpeta);
        }

        String extension = archivo.getOriginalFilename() != null && archivo.getOriginalFilename().contains(".")
                ? archivo.getOriginalFilename().substring(archivo.getOriginalFilename().lastIndexOf('.'))
                : "";
        String nombreArchivo = UUID.randomUUID() + extension;
        Files.copy(archivo.getInputStream(), carpeta.resolve(nombreArchivo));

        u.setFoto(nombreArchivo);
        return aMapa(usuarioRepository.save(u));
    }

    private Usuario buscarUsuarioActual(Authentication authentication) {
        return usuarioRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    }

    private Map<String, Object> aMapa(Usuario u) {
        return Map.of(
                "username", u.getUsername(),
                "nombre", u.getNombre() != null ? u.getNombre() : "",
                "apellidos", u.getApellidos() != null ? u.getApellidos() : "",
                "foto", u.getFoto() != null ? u.getFoto() : "",
                "rol", u.getRol().getNombre().replace("ROLE_", "")
        );
    }
}
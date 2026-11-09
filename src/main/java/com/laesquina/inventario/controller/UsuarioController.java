package com.laesquina.inventario.controller;

import com.laesquina.inventario.model.Rol;
import com.laesquina.inventario.model.Usuario;
import com.laesquina.inventario.repository.RolRepository;
import com.laesquina.inventario.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioController(UsuarioRepository usuarioRepository, RolRepository rolRepository,
                             PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Lista de usuarios, sin exponer la contrasena cifrada
    @GetMapping
    public List<Map<String, Object>> listar() {
        return usuarioRepository.findAll().stream()
                .map(this::aMapa)
                .toList();
    }

    @PostMapping
    public Map<String, Object> crear(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        if (usuarioRepository.existsByUsername(username)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ese nombre de usuario ya existe");
        }
        Rol rol = rolRepository.findByNombre("ROLE_" + body.get("rol"))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rol invalido"));

        Usuario usuario = new Usuario(
                username,
                passwordEncoder.encode(body.get("password")),
                rol,
                body.get("nombre"),
                body.get("apellidos")
        );
        return aMapa(usuarioRepository.save(usuario));
    }

    // Activar / desactivar una cuenta (no la borra)
    @PutMapping("/{id}/estado")
    public Map<String, Object> cambiarEstado(@PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
        usuario.setActivo(body.get("activo"));
        return aMapa(usuarioRepository.save(usuario));
    }

    private Map<String, Object> aMapa(Usuario u) {
        return Map.of(
                "id", u.getId(),
                "username", u.getUsername(),
                "nombre", u.getNombre() != null ? u.getNombre() : "",
                "apellidos", u.getApellidos() != null ? u.getApellidos() : "",
                "rol", u.getRol().getNombre().replace("ROLE_", ""),
                "activo", u.isActivo()
        );
    }
}
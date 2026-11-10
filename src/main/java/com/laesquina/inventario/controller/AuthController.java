package com.laesquina.inventario.controller;

import com.laesquina.inventario.model.Usuario;
import com.laesquina.inventario.repository.UsuarioRepository;
import com.laesquina.inventario.security.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class AuthController {

    private final UsuarioRepository usuarioRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthController(UsuarioRepository usuarioRepository, AuthenticationManager authenticationManager,
                          JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    // Datos del cuerpo del inicio de sesion: {"username": "...", "password": "..."}
    public record SolicitudLogin(String username, String password) {
    }

    /**
     * POST /api/auth/login (publico): verifica usuario y contrasena contra la tabla usuario
     * (BCrypt) y, si son correctos, devuelve un token JWT junto con los datos del usuario.
     */
    @PostMapping("/api/auth/login")
    public ResponseEntity<?> login(@RequestBody SolicitudLogin solicitud, HttpServletRequest req) {
        if (solicitud == null || solicitud.username() == null || solicitud.password() == null) {
            return error(HttpStatus.BAD_REQUEST, "Debe enviar username y password", req);
        }
        Authentication autenticacion;
        try {
            autenticacion = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(solicitud.username(), solicitud.password()));
        } catch (DisabledException e) {
            return error(HttpStatus.UNAUTHORIZED, "La cuenta está desactivada", req);
        } catch (BadCredentialsException e) {
            return error(HttpStatus.UNAUTHORIZED, "Usuario o contraseña incorrectos", req);
        }

        String rol = rolDe(autenticacion);
        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("token", jwtService.generarToken(autenticacion.getName(), rol));
        respuesta.put("tipo", "Bearer");
        respuesta.put("expiraEnSegundos", jwtService.getDuracionSegundos());
        respuesta.putAll(datosUsuario(autenticacion.getName(), rol));
        return ResponseEntity.ok(respuesta);
    }

    // GET /api/auth/me: datos del usuario dueno del token.
    @GetMapping("/api/auth/me")
    public Map<String, Object> yo(Authentication authentication) {
        return datosUsuario(authentication.getName(), rolDe(authentication));
    }

    private String rolDe(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .findFirst()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .orElse("");
    }

    private Map<String, Object> datosUsuario(String username, String rol) {
        Usuario u = usuarioRepository.findByUsername(username).orElse(null);
        Map<String, Object> datos = new LinkedHashMap<>();
        datos.put("usuario", username);
        datos.put("rol", rol);
        datos.put("nombre", u != null && u.getNombre() != null ? u.getNombre() : "");
        datos.put("apellidos", u != null && u.getApellidos() != null ? u.getApellidos() : "");
        datos.put("foto", u != null && u.getFoto() != null ? u.getFoto() : "");
        return datos;
    }

    private ResponseEntity<ErrorRespuesta> error(HttpStatus s, String mensaje, HttpServletRequest req) {
        return ResponseEntity.status(s)
                .body(new ErrorRespuesta(s.value(), s.getReasonPhrase(), mensaje, req.getRequestURI()));
    }
}

package com.laesquina.inventario.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.laesquina.inventario.model.Rol;
import com.laesquina.inventario.model.Usuario;
import com.laesquina.inventario.repository.RolRepository;
import com.laesquina.inventario.repository.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas de seguridad con JWT, usando los usuarios REALES creados en la BD (contrasena BCrypt).
 * Cada prueba inicia sesion en POST /api/auth/login y usa el token recibido.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SeguridadTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper json;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private RolRepository rolRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${app.usuarios.admin.username}")
    private String admin;
    @Value("${app.usuarios.admin.password}")
    private String adminPass;
    @Value("${app.usuarios.operador.username}")
    private String operador;
    @Value("${app.usuarios.operador.password}")
    private String operadorPass;

    private static final String PRODUCTO_NUEVO = """
            {"nombre":"Galletas Soda","precio":1.2,"stock":15,"stockMinimo":5,
             "ubicacion":"Pasillo 1 - Estante D","categoria":{"id":1}}""";

    // Inicia sesion y devuelve la cabecera "Bearer <token>".
    private String bearer(String usuario, String clave) throws Exception {
        String cuerpo = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(new AuthLogin(usuario, clave))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return "Bearer " + json.readTree(cuerpo).get("token").asText();
    }

    private record AuthLogin(String username, String password) {
    }

    // ---------------- Inicio de sesion ----------------

    @Test
    @DisplayName("Login correcto -> 200 con token JWT y rol")
    void loginCorrecto() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(new AuthLogin(admin, adminPass))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.tipo").value("Bearer"))
                .andExpect(jsonPath("$.rol").value("ADMIN"));
    }

    @Test
    @DisplayName("Login con contraseña incorrecta -> 401")
    void loginIncorrecto() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(new AuthLogin(operador, "clave-equivocada"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value("Usuario o contraseña incorrectos"));
    }

    @Test
    @DisplayName("El token lleva el usuario y el rol, firmados")
    void contenidoDelToken() throws Exception {
        String token = bearer(operador, operadorPass).substring(7);
        assertThat(jwtService.validar(token).getSubject()).isEqualTo(operador);
        assertThat(jwtService.validar(token).get("rol", String.class)).isEqualTo("OPERADOR");
    }

    // ---------------- Matriz de autorizacion ----------------

    @Test
    @DisplayName("Caso 1: operador consulta productos -> 200")
    void operadorConsulta() throws Exception {
        mvc.perform(get("/api/productos").header("Authorization", bearer(operador, operadorPass)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Caso 2: admin registra producto -> 201")
    void adminRegistra() throws Exception {
        mvc.perform(post("/api/productos").header("Authorization", bearer(admin, adminPass))
                        .contentType(MediaType.APPLICATION_JSON).content(PRODUCTO_NUEVO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    @DisplayName("Caso 3: operador intenta registrar producto -> 403")
    void operadorNoRegistra() throws Exception {
        mvc.perform(post("/api/productos").header("Authorization", bearer(operador, operadorPass))
                        .contentType(MediaType.APPLICATION_JSON).content(PRODUCTO_NUEVO))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.estado").value(403));
    }

    @Test
    @DisplayName("Caso 4: operador intenta eliminar producto -> 403")
    void operadorNoElimina() throws Exception {
        mvc.perform(delete("/api/productos/1").header("Authorization", bearer(operador, operadorPass)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Caso 5: operador registra movimiento -> 201")
    void operadorRegistraMovimiento() throws Exception {
        mvc.perform(post("/api/movimientos").header("Authorization", bearer(operador, operadorPass))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tipo\":\"ENTRADA\",\"cantidad\":3,\"producto\":{\"id\":1}}"))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Operador intenta modificar una categoria -> 403")
    void operadorNoModificaCategoria() throws Exception {
        mvc.perform(put("/api/categorias/1").header("Authorization", bearer(operador, operadorPass))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"nombre\":\"X\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Operador intenta ver la gestion de usuarios -> 403")
    void operadorNoGestionaUsuarios() throws Exception {
        mvc.perform(get("/api/usuarios").header("Authorization", bearer(operador, operadorPass)))
                .andExpect(status().isForbidden());
    }

    // ---------------- Tokens invalidos ----------------

    @Test
    @DisplayName("Caso 6: solicitud sin token -> 401")
    void sinToken() throws Exception {
        mvc.perform(get("/api/productos"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.estado").value(401));
    }

    @Test
    @DisplayName("Token alterado (firma invalida) -> 401")
    void tokenAlterado() throws Exception {
        String token = bearer(admin, adminPass);
        String alterado = token.substring(0, token.length() - 3) + (token.endsWith("A") ? "BBB" : "AAA");
        mvc.perform(get("/api/productos").header("Authorization", alterado))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value("El token no es válido"));
    }

    @Test
    @DisplayName("Token expirado -> 401")
    void tokenExpirado() throws Exception {
        String vencido = jwtService.generarToken(admin, "ADMIN", Duration.ofMinutes(-1));
        mvc.perform(get("/api/productos").header("Authorization", "Bearer " + vencido))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value("El token expiró; inicie sesión nuevamente"));
    }

    @Test
    @DisplayName("Cuenta desactivada: no inicia sesion y su token deja de servir -> 401")
    void cuentaDesactivada() throws Exception {
        String nombre = "temporal" + System.nanoTime();
        Rol rolOperador = rolRepository.findByNombre(Rol.OPERADOR).orElseThrow();
        Usuario u = usuarioRepository.save(new Usuario(nombre, passwordEncoder.encode("Temporal2026!"), rolOperador));
        String token = bearer(nombre, "Temporal2026!");

        u.setActivo(false);
        usuarioRepository.save(u);

        mvc.perform(get("/api/productos").header("Authorization", token))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value("La cuenta está desactivada"));
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(new AuthLogin(nombre, "Temporal2026!"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value("La cuenta está desactivada"));
    }
}

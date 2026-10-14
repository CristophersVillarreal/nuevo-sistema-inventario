package com.laesquina.inventario.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas de seguridad con los usuarios REALES creados en la BD (contrasena BCrypt).
 * Reproducen la matriz de casos del rubro 8.6 del informe.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SeguridadTest {

    @Autowired
    private MockMvc mvc;

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

    @Test
    @DisplayName("Caso 1: operador consulta productos -> 200")
    void operadorConsulta() throws Exception {
        mvc.perform(get("/api/productos").with(httpBasic(operador, operadorPass)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Caso 2: admin registra producto -> 201")
    void adminRegistra() throws Exception {
        mvc.perform(post("/api/productos").with(httpBasic(admin, adminPass))
                        .contentType(MediaType.APPLICATION_JSON).content(PRODUCTO_NUEVO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    @DisplayName("Caso 3: operador intenta registrar producto -> 403")
    void operadorNoRegistra() throws Exception {
        mvc.perform(post("/api/productos").with(httpBasic(operador, operadorPass))
                        .contentType(MediaType.APPLICATION_JSON).content(PRODUCTO_NUEVO))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.estado").value(403));
    }

    @Test
    @DisplayName("Caso 4: operador intenta eliminar producto -> 403")
    void operadorNoElimina() throws Exception {
        mvc.perform(delete("/api/productos/1").with(httpBasic(operador, operadorPass)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Caso 5: operador registra movimiento -> 201")
    void operadorRegistraMovimiento() throws Exception {
        mvc.perform(post("/api/movimientos").with(httpBasic(operador, operadorPass))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tipo\":\"ENTRADA\",\"cantidad\":3,\"producto\":{\"id\":1}}"))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Caso 6: solicitud sin credenciales -> 401")
    void sinCredenciales() throws Exception {
        mvc.perform(get("/api/productos"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.estado").value(401));
    }

    @Test
    @DisplayName("Caso 7: credenciales incorrectas -> 401")
    void credencialesIncorrectas() throws Exception {
        mvc.perform(get("/api/productos").with(httpBasic(operador, "clave-equivocada")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Operador intenta modificar una categoria -> 403")
    void operadorNoModificaCategoria() throws Exception {
        mvc.perform(put("/api/categorias/1").with(httpBasic(operador, operadorPass))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"nombre\":\"X\"}"))
                .andExpect(status().isForbidden());
    }
}

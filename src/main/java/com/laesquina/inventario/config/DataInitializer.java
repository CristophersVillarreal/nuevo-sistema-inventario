package com.laesquina.inventario.config;

import com.laesquina.inventario.model.Categoria;
import com.laesquina.inventario.model.Producto;
import com.laesquina.inventario.model.Rol;
import com.laesquina.inventario.model.Usuario;
import com.laesquina.inventario.repository.CategoriaRepository;
import com.laesquina.inventario.repository.ProductoRepository;
import com.laesquina.inventario.repository.RolRepository;
import com.laesquina.inventario.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Carga los datos iniciales la PRIMERA vez que arranca la aplicacion.
 *
 * Cada bloque solo se ejecuta si su tabla esta vacia; por eso, al reiniciar,
 * NO se duplican datos ni se pierden los cambios hechos desde la API.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final RolRepository rolRepository;
    private final UsuarioRepository usuarioRepository;
    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.usuarios.admin.username}")
    private String adminUsername;
    @Value("${app.usuarios.admin.password}")
    private String adminPassword;
    @Value("${app.usuarios.operador.username}")
    private String operadorUsername;
    @Value("${app.usuarios.operador.password}")
    private String operadorPassword;

    public DataInitializer(RolRepository rolRepository, UsuarioRepository usuarioRepository,
                           CategoriaRepository categoriaRepository, ProductoRepository productoRepository,
                           PasswordEncoder passwordEncoder) {
        this.rolRepository = rolRepository;
        this.usuarioRepository = usuarioRepository;
        this.categoriaRepository = categoriaRepository;
        this.productoRepository = productoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (rolRepository.count() == 0) {
            rolRepository.save(new Rol(Rol.ADMIN));
            rolRepository.save(new Rol(Rol.OPERADOR));
            log.info("Roles iniciales creados: ROLE_ADMIN y ROLE_OPERADOR");
        }

        if (usuarioRepository.count() == 0) {
            Rol admin = rolRepository.findByNombre(Rol.ADMIN).orElseThrow();
            Rol operador = rolRepository.findByNombre(Rol.OPERADOR).orElseThrow();
            // La contrasena se guarda cifrada con BCrypt.
            usuarioRepository.save(new Usuario(adminUsername, passwordEncoder.encode(adminPassword), admin));
            usuarioRepository.save(new Usuario(operadorUsername, passwordEncoder.encode(operadorPassword), operador));
            log.info("Usuarios de prueba creados: {} (ADMIN) y {} (OPERADOR)", adminUsername, operadorUsername);
        }

        if (categoriaRepository.count() == 0 && productoRepository.count() == 0) {
            Categoria abarrotes = categoriaRepository.save(new Categoria("Abarrotes"));
            Categoria bebidas = categoriaRepository.save(new Categoria("Bebidas"));
            Categoria lacteos = categoriaRepository.save(new Categoria("Lacteos"));

            // Los mismos productos del Avance 1, ahora con vencimiento y ubicacion.
            LocalDate hoy = LocalDate.now();
            productoRepository.save(new Producto("Arroz Costeño 1kg", abarrotes, 4.50, 30, 10,
                    hoy.plusMonths(8), "Pasillo 1 - Estante A"));
            productoRepository.save(new Producto("Aceite Primor 1L", abarrotes, 9.90, 8, 10,
                    hoy.plusMonths(6), "Pasillo 1 - Estante B"));
            productoRepository.save(new Producto("Leche Gloria 400g", lacteos, 3.80, 25, 12,
                    hoy.plusDays(12), "Pasillo 3 - Estante A"));
            productoRepository.save(new Producto("Coca Cola 500ml", bebidas, 2.50, 5, 15,
                    hoy.plusDays(25), "Pasillo 2 - Refrigeradora"));
            productoRepository.save(new Producto("Fideos Don Vittorio", abarrotes, 3.20, 40, 10,
                    hoy.plusMonths(10), "Pasillo 1 - Estante C"));
            productoRepository.save(new Producto("Agua San Luis 625ml", bebidas, 1.50, 60, 20,
                    hoy.plusMonths(12), "Pasillo 2 - Estante A"));
            log.info("Categorias y productos de prueba creados");
        }
    }
}

package com.laesquina.inventario.repository;

import com.laesquina.inventario.model.Categoria;
import com.laesquina.inventario.model.MovimientoInventario;
import com.laesquina.inventario.model.Producto;
import com.laesquina.inventario.model.TipoMovimiento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pruebas de las consultas JPQL de los repositorios.
 * @DataJpaTest levanta solo la capa JPA con una BD en memoria vacia.
 */
@DataJpaTest
@ActiveProfiles("test")
class ProductoRepositoryTest {

    @Autowired
    private ProductoRepository productoRepository;
    @Autowired
    private CategoriaRepository categoriaRepository;
    @Autowired
    private MovimientoInventarioRepository movimientoRepository;

    private Producto coca;

    @BeforeEach
    void preparar() {
        Categoria bebidas = categoriaRepository.save(new Categoria("Bebidas"));
        LocalDate hoy = LocalDate.now();
        coca = productoRepository.save(new Producto("Coca Cola 500ml", bebidas, 2.5, 5, 15, hoy.plusDays(25), "P2"));
        productoRepository.save(new Producto("Aceite Primor 1L", bebidas, 9.9, 8, 10, hoy.plusMonths(6), "P1"));
        productoRepository.save(new Producto("Agua San Luis", bebidas, 1.5, 60, 20, hoy.plusMonths(12), "P2"));
        productoRepository.save(new Producto("Leche Gloria", bebidas, 3.8, 25, 12, hoy.plusDays(12), "P3"));
        productoRepository.save(new Producto("Sal de mesa", bebidas, 1.0, 50, 10, null, "P1"));
    }

    @Test
    @DisplayName("Q1: stock bajo devuelve solo productos con stock <= minimo, ordenados por stock")
    void stockBajo() {
        List<Producto> resultado = productoRepository.buscarStockBajo();

        assertThat(resultado).extracting(Producto::getNombre)
                .containsExactly("Coca Cola 500ml", "Aceite Primor 1L");
    }

    @Test
    @DisplayName("Q3: por vencer devuelve los que vencen dentro del plazo, del mas proximo al mas lejano")
    void porVencer() {
        List<Producto> resultado = productoRepository.buscarPorVencer(LocalDate.now().plusDays(30));

        assertThat(resultado).extracting(Producto::getNombre)
                .containsExactly("Leche Gloria", "Coca Cola 500ml");
    }

    @Test
    @DisplayName("Q2: historial devuelve los movimientos del producto, del mas reciente al mas antiguo")
    void historial() {
        MovimientoInventario antiguo = new MovimientoInventario(TipoMovimiento.ENTRADA, 20, coca);
        antiguo.setFecha(LocalDateTime.now().minusDays(1));
        MovimientoInventario reciente = new MovimientoInventario(TipoMovimiento.SALIDA, 3, coca);
        reciente.setFecha(LocalDateTime.now());
        movimientoRepository.save(antiguo);
        movimientoRepository.save(reciente);

        List<MovimientoInventario> resultado = movimientoRepository.buscarHistorial(coca.getId());

        assertThat(resultado).extracting(MovimientoInventario::getTipo)
                .containsExactly(TipoMovimiento.SALIDA, TipoMovimiento.ENTRADA);
    }
}

package com.laesquina.inventario.service;

import com.laesquina.inventario.model.MovimientoInventario;
import com.laesquina.inventario.model.Producto;
import com.laesquina.inventario.model.TipoMovimiento;
import com.laesquina.inventario.repository.MovimientoInventarioRepository;
import com.laesquina.inventario.repository.ProductoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Pruebas de la logica transaccional de movimientos.
 *
 * IMPORTANTE: esta clase NO usa @Transactional en las pruebas, para que cada
 * llamada al servicio confirme o revierta su propia transaccion, igual que en
 * produccion. Asi se comprueba el rollback real en la base de datos.
 */
@SpringBootTest
@ActiveProfiles("test")
class MovimientoServiceTest {

    @Autowired
    private MovimientoService movimientoService;
    @Autowired
    private ProductoRepository productoRepository;
    @Autowired
    private MovimientoInventarioRepository movimientoRepository;

    @Test
    @DisplayName("ENTRADA valida: aumenta el stock y guarda el movimiento")
    void entradaValida() {
        Producto coca = buscar("Coca Cola 500ml");
        int stockAntes = coca.getStock();
        long movimientosAntes = movimientoRepository.count();

        movimientoService.registrar(movimiento(TipoMovimiento.ENTRADA, 20, coca.getId()));

        assertThat(stockActual(coca.getId())).isEqualTo(stockAntes + 20);
        assertThat(movimientoRepository.count()).isEqualTo(movimientosAntes + 1);
    }

    @Test
    @DisplayName("SALIDA mayor al stock: se rechaza y no cambia nada en la BD")
    void salidaMayorAlStock() {
        Producto aceite = buscar("Aceite Primor 1L");
        int stockAntes = aceite.getStock();
        long movimientosAntes = movimientoRepository.count();

        assertThatThrownBy(() -> movimientoService.registrar(
                movimiento(TipoMovimiento.SALIDA, stockAntes + 100, aceite.getId())))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Stock insuficiente");

        assertThat(stockActual(aceite.getId())).isEqualTo(stockAntes);
        assertThat(movimientoRepository.count()).isEqualTo(movimientosAntes);
    }

    @Test
    @DisplayName("LOTE con un item invalido: ROLLBACK de todo, incluidos los items ya aplicados")
    void loteConItemInvalidoHaceRollback() {
        Producto arroz = buscar("Arroz Costeño 1kg");
        Producto fideos = buscar("Fideos Don Vittorio");
        Producto agua = buscar("Agua San Luis 625ml");
        int arrozAntes = arroz.getStock();
        int fideosAntes = fideos.getStock();
        int aguaAntes = agua.getStock();
        long movimientosAntes = movimientoRepository.count();

        // Items 1 y 2 son validos; el item 3 pide sacar mas de lo que hay.
        List<MovimientoInventario> lote = List.of(
                movimiento(TipoMovimiento.ENTRADA, 10, arroz.getId()),
                movimiento(TipoMovimiento.ENTRADA, 5, fideos.getId()),
                movimiento(TipoMovimiento.SALIDA, aguaAntes + 1, agua.getId()));

        assertThatThrownBy(() -> movimientoService.registrarLote(lote))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ítem 3");

        // Los items 1 y 2 se habian aplicado dentro de la transaccion, pero se revirtieron.
        assertThat(stockActual(arroz.getId())).isEqualTo(arrozAntes);
        assertThat(stockActual(fideos.getId())).isEqualTo(fideosAntes);
        assertThat(stockActual(agua.getId())).isEqualTo(aguaAntes);
        assertThat(movimientoRepository.count()).isEqualTo(movimientosAntes);
    }

    @Test
    @DisplayName("LOTE valido: aplica todos los movimientos")
    void loteValido() {
        Producto arroz = buscar("Arroz Costeño 1kg");
        Producto leche = buscar("Leche Gloria 400g");
        int arrozAntes = arroz.getStock();
        int lecheAntes = leche.getStock();

        List<MovimientoInventario> registrados = movimientoService.registrarLote(List.of(
                movimiento(TipoMovimiento.ENTRADA, 10, arroz.getId()),
                movimiento(TipoMovimiento.SALIDA, 2, leche.getId())));

        assertThat(registrados).hasSize(2);
        assertThat(stockActual(arroz.getId())).isEqualTo(arrozAntes + 10);
        assertThat(stockActual(leche.getId())).isEqualTo(lecheAntes - 2);
    }

    @Test
    @DisplayName("Producto inexistente: se rechaza con NoSuchElementException (404)")
    void productoInexistente() {
        assertThatThrownBy(() -> movimientoService.registrar(movimiento(TipoMovimiento.ENTRADA, 1, 9999L)))
                .isInstanceOf(NoSuchElementException.class);
    }

    private Producto buscar(String nombre) {
        return productoRepository.findAll().stream()
                .filter(p -> p.getNombre().equals(nombre))
                .findFirst().orElseThrow();
    }

    private int stockActual(Long id) {
        return productoRepository.findById(id).orElseThrow().getStock();
    }

    private MovimientoInventario movimiento(TipoMovimiento tipo, int cantidad, Long productoId) {
        Producto ref = new Producto();
        ref.setId(productoId);
        return new MovimientoInventario(tipo, cantidad, ref);
    }
}

package com.laesquina.inventario.repository;

import com.laesquina.inventario.model.MovimientoInventario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Repositorio de movimientos de inventario.
 */
public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, Long> {

    // JPQL Q2 (RF05): historial de un producto, del mas reciente al mas antiguo.
    // m.producto.id recorre la relacion sin escribir un JOIN.
    @Query("SELECT m FROM MovimientoInventario m WHERE m.producto.id = :id ORDER BY m.fecha DESC, m.id DESC")
    List<MovimientoInventario> buscarHistorial(@Param("id") Long id);

    // Todos los movimientos, del mas reciente al mas antiguo (consulta derivada del nombre).
    List<MovimientoInventario> findAllByOrderByFechaDescIdDesc();

    // Usado para impedir borrar un producto que ya tiene historial.
    boolean existsByProductoId(Long productoId);
}

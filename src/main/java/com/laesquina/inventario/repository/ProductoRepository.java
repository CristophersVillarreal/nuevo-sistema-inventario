package com.laesquina.inventario.repository;

import com.laesquina.inventario.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * Repositorio de productos con Spring Data JPA (Avance 2).
 *
 * Reemplaza a la lista en memoria del Avance 1. Al extender JpaRepository,
 * Spring genera save, findAll, findById, deleteById, etc. sin escribir SQL.
 * Las consultas personalizadas se escriben en JPQL con @Query.
 */
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    // JPQL Q1 (RF03): productos cuyo stock esta en o por debajo del minimo.
    // En el Avance 1 este filtro se hacia en Java; ahora lo resuelve la base de datos.
    @Query("SELECT p FROM Producto p WHERE p.stock <= p.stockMinimo ORDER BY p.stock")
    List<Producto> buscarStockBajo();

    // JPQL Q3 (RF04): productos que vencen hasta la fecha limite
    // (incluye los ya vencidos, que tambien deben retirarse), del mas proximo al mas lejano.
    @Query("SELECT p FROM Producto p WHERE p.fechaVencimiento IS NOT NULL "
            + "AND p.fechaVencimiento <= :limite ORDER BY p.fechaVencimiento")
    List<Producto> buscarPorVencer(@Param("limite") LocalDate limite);

    // Usado para impedir borrar una categoria que todavia tiene productos.
    boolean existsByCategoriaId(Long categoriaId);
}

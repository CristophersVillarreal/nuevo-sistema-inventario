package com.laesquina.inventario.repository;

import com.laesquina.inventario.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio de categorias.
 */
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);
}

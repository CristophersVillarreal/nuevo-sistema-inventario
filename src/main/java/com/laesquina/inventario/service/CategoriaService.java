package com.laesquina.inventario.service;

import com.laesquina.inventario.model.Categoria;
import com.laesquina.inventario.repository.CategoriaRepository;
import com.laesquina.inventario.repository.ProductoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * Capa de servicio: reglas de negocio de categorias.
 */
@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;

    public CategoriaService(CategoriaRepository categoriaRepository, ProductoRepository productoRepository) {
        this.categoriaRepository = categoriaRepository;
        this.productoRepository = productoRepository;
    }

    @Transactional(readOnly = true)
    public List<Categoria> listarTodas() {
        return categoriaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Categoria obtenerPorId(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Categoría no encontrada con id " + id));
    }

    @Transactional
    public Categoria crear(Categoria categoria) {
        if (categoriaRepository.existsByNombreIgnoreCase(categoria.getNombre())) {
            throw new IllegalStateException("Ya existe una categoría con el nombre " + categoria.getNombre());
        }
        categoria.setId(null);
        return categoriaRepository.save(categoria);
    }

    @Transactional
    public Categoria actualizar(Long id, Categoria datos) {
        Categoria existente = obtenerPorId(id);
        if (categoriaRepository.existsByNombreIgnoreCaseAndIdNot(datos.getNombre(), id)) {
            throw new IllegalStateException("Ya existe una categoría con el nombre " + datos.getNombre());
        }
        existente.setNombre(datos.getNombre());
        return categoriaRepository.save(existente);
    }

    @Transactional
    public void eliminar(Long id) {
        obtenerPorId(id);
        // No se borra una categoria que todavia agrupa productos.
        if (productoRepository.existsByCategoriaId(id)) {
            throw new IllegalStateException("No se puede eliminar la categoría " + id
                    + " porque tiene productos asociados");
        }
        categoriaRepository.deleteById(id);
    }
}

package com.laesquina.inventario.service;

import com.laesquina.inventario.model.Categoria;
import com.laesquina.inventario.model.Producto;
import com.laesquina.inventario.repository.CategoriaRepository;
import com.laesquina.inventario.repository.MovimientoInventarioRepository;
import com.laesquina.inventario.repository.ProductoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Capa de servicio: reglas de negocio de productos.
 * Usa inyeccion por constructor (no @Autowired en campos).
 *
 * Avance 2: los datos ya no viven en una lista, sino en MySQL a traves
 * de los repositorios JPA. El controlador no tuvo que cambiar su forma de trabajar.
 */
@Service
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final MovimientoInventarioRepository movimientoRepository;

    public ProductoService(ProductoRepository productoRepository,
                           CategoriaRepository categoriaRepository,
                           MovimientoInventarioRepository movimientoRepository) {
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
        this.movimientoRepository = movimientoRepository;
    }

    @Transactional(readOnly = true)
    public List<Producto> listarTodos() {
        return productoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Producto obtenerPorId(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Producto no encontrado con id " + id));
    }

    @Transactional
    public Producto crear(Producto producto) {
        // Aseguramos que se cree como nuevo (sin id) y con una categoria existente.
        producto.setId(null);
        producto.setCategoria(buscarCategoria(producto.getCategoria()));
        return productoRepository.save(producto);
    }

    @Transactional
    public Producto actualizar(Long id, Producto datos) {
        Producto existente = obtenerPorId(id);
        existente.setNombre(datos.getNombre());
        existente.setCategoria(buscarCategoria(datos.getCategoria()));
        existente.setPrecio(datos.getPrecio());
        existente.setStock(datos.getStock());
        existente.setStockMinimo(datos.getStockMinimo());
        existente.setFechaVencimiento(datos.getFechaVencimiento());
        existente.setUbicacion(datos.getUbicacion());
        return productoRepository.save(existente);
    }

    @Transactional
    public void eliminar(Long id) {
        // Verifica que exista antes de eliminar.
        obtenerPorId(id);
        // Si ya tiene movimientos, borrarlo romperia el historial (clave foranea).
        if (movimientoRepository.existsByProductoId(id)) {
            throw new IllegalStateException("No se puede eliminar el producto " + id
                    + " porque tiene movimientos de inventario registrados");
        }
        productoRepository.deleteById(id);
    }

    // RF03: productos por debajo (o igual) de su stock minimo (JPQL Q1).
    @Transactional(readOnly = true)
    public List<Producto> listarStockBajo() {
        return productoRepository.buscarStockBajo();
    }

    // RF04: productos que vencen dentro de los proximos "dias" (JPQL Q3).
    @Transactional(readOnly = true)
    public List<Producto> listarPorVencer(int dias) {
        if (dias < 0) {
            throw new IllegalArgumentException("El parámetro dias no puede ser negativo");
        }
        return productoRepository.buscarPorVencer(LocalDate.now().plusDays(dias));
    }

    // El cliente envia solo {"id": n}; aqui se busca la categoria real en la BD.
    private Categoria buscarCategoria(Categoria categoria) {
        if (categoria == null || categoria.getId() == null) {
            throw new IllegalArgumentException("La categoría es obligatoria");
        }
        return categoriaRepository.findById(categoria.getId())
                .orElseThrow(() -> new NoSuchElementException(
                        "Categoría no encontrada con id " + categoria.getId()));
    }
}

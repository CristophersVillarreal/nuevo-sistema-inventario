package com.laesquina.inventario.service;

import com.laesquina.inventario.model.Producto;
import com.laesquina.inventario.repository.ProductoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * Capa de servicio: reglas de negocio de productos.
 * Usa inyeccion por constructor (no @Autowired en campos).
 */
@Service
public class ProductoService {

    private final ProductoRepository productoRepository;

    public ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    public List<Producto> listarTodos() {
        return productoRepository.listar();
    }

    public Producto obtenerPorId(Long id) {
        return productoRepository.buscarPorId(id)
                .orElseThrow(() -> new NoSuchElementException("Producto no encontrado con id " + id));
    }

    public Producto crear(Producto producto) {
        // Aseguramos que se cree como nuevo (sin id).
        producto.setId(null);
        return productoRepository.guardar(producto);
    }

    public Producto actualizar(Long id, Producto datos) {
        Producto existente = obtenerPorId(id);
        existente.setNombre(datos.getNombre());
        existente.setCategoria(datos.getCategoria());
        existente.setPrecio(datos.getPrecio());
        existente.setStock(datos.getStock());
        existente.setStockMinimo(datos.getStockMinimo());
        return productoRepository.guardar(existente);
    }

    public void eliminar(Long id) {
        // Verifica que exista antes de eliminar.
        obtenerPorId(id);
        productoRepository.eliminar(id);
    }

    // Productos por debajo (o igual) de su stock minimo: alertas.
    public List<Producto> listarStockBajo() {
        return productoRepository.listar().stream()
                .filter(Producto::isStockBajo)
                .toList();
    }
}

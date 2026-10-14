package com.laesquina.inventario.repository;

import com.laesquina.inventario.model.Producto;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Repositorio de productos EN MEMORIA (Fase 1).
 *
 * Guarda los productos en una lista y simula una base de datos.
 * En Fase 2 esta clase se reemplaza por Spring Data JPA sin tocar
 * el servicio ni el controlador.
 */
@Repository
public class ProductoRepository {

    // "Tabla" en memoria.
    private final List<Producto> productos = new ArrayList<>();
    // Generador de ids autoincrementales.
    private final AtomicLong secuencia = new AtomicLong(0);

    public ProductoRepository() {
        // Datos de ejemplo precargados para la demostracion.
        guardar(new Producto(null, "Arroz Costeño 1kg", "Abarrotes", 4.50, 30, 10));
        guardar(new Producto(null, "Aceite Primor 1L", "Abarrotes", 9.90, 8, 10));
        guardar(new Producto(null, "Leche Gloria 400g", "Lacteos", 3.80, 25, 12));
        guardar(new Producto(null, "Coca Cola 500ml", "Bebidas", 2.50, 5, 15));
        guardar(new Producto(null, "Fideos Don Vittorio", "Abarrotes", 3.20, 40, 10));
        guardar(new Producto(null, "Agua San Luis 625ml", "Bebidas", 1.50, 60, 20));
    }

    public List<Producto> listar() {
        return new ArrayList<>(productos);
    }

    public Optional<Producto> buscarPorId(Long id) {
        return productos.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst();
    }

    public Producto guardar(Producto producto) {
        if (producto.getId() == null) {
            // Nuevo: asignar id.
            producto.setId(secuencia.incrementAndGet());
            productos.add(producto);
        } else {
            // Existente: reemplazar en su posicion.
            eliminar(producto.getId());
            productos.add(producto);
            // Mantener la secuencia por encima de ids manuales.
            if (producto.getId() > secuencia.get()) {
                secuencia.set(producto.getId());
            }
        }
        return producto;
    }

    public boolean eliminar(Long id) {
        return productos.removeIf(p -> p.getId().equals(id));
    }
}

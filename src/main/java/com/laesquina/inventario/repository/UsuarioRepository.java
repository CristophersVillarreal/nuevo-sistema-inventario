package com.laesquina.inventario.repository;

import com.laesquina.inventario.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repositorio de usuarios.
 */
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Spring Security lo usa para identificar al usuario que inicia sesion.
    Optional<Usuario> findByUsername(String username);
}

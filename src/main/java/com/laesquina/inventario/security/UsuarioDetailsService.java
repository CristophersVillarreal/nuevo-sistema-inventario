package com.laesquina.inventario.security;

import com.laesquina.inventario.model.Usuario;
import com.laesquina.inventario.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Le dice a Spring Security como buscar a un usuario: lo lee de la tabla usuario
 * en MySQL (no de memoria) junto con su rol. Spring compara luego la contrasena
 * enviada con el hash BCrypt guardado. Las cuentas desactivadas (activo = false)
 * no pueden iniciar sesion.
 */
@Service
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        return User.withUsername(usuario.getUsername())
                .password(usuario.getPassword())
                .authorities(usuario.getRol().getNombre())
                .disabled(!usuario.isActivo())
                .build();
    }
}
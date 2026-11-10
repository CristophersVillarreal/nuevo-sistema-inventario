package com.laesquina.inventario.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Lee la cabecera "Authorization: Bearer <token>" de cada solicitud.
 *
 * Si el token es valido, carga al usuario desde la BD y lo registra como autenticado,
 * con su rol. Si falta, esta alterado o expiro, la solicitud sigue sin autenticar y
 * Spring Security responde 401 (con el motivo que deja este filtro).
 * Se vuelve a leer el usuario en cada solicitud para que una cuenta desactivada
 * pierda el acceso aunque su token todavia no haya expirado.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    // Atributo con el motivo del rechazo; lo usa RespuestasSeguridad para el mensaje del 401.
    public static final String MOTIVO_401 = "jwt.motivo";

    private final JwtService jwtService;
    private final UsuarioDetailsService usuarioDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService, UsuarioDetailsService usuarioDetailsService) {
        this.jwtService = jwtService;
        this.usuarioDetailsService = usuarioDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String cabecera = request.getHeader("Authorization");
        if (cabecera == null || !cabecera.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        String token = cabecera.substring(7);
        try {
            String usuario = jwtService.validar(token).getSubject();
            UserDetails detalles = usuarioDetailsService.loadUserByUsername(usuario);
            if (!detalles.isEnabled()) {
                request.setAttribute(MOTIVO_401, "La cuenta está desactivada");
            } else {
                UsernamePasswordAuthenticationToken autenticacion =
                        new UsernamePasswordAuthenticationToken(detalles, null, detalles.getAuthorities());
                autenticacion.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(autenticacion);
            }
        } catch (ExpiredJwtException e) {
            request.setAttribute(MOTIVO_401, "El token expiró; inicie sesión nuevamente");
        } catch (JwtException | IllegalArgumentException e) {
            request.setAttribute(MOTIVO_401, "El token no es válido");
        } catch (UsernameNotFoundException e) {
            request.setAttribute(MOTIVO_401, "El usuario del token ya no existe");
        }
        chain.doFilter(request, response);
    }
}

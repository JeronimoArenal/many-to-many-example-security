package com.example.security.jwt;

import com.example.security.service.CustomUserDetailsService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * El filtro intercepta las peticiones HTTP.
 * Hereda de OncePerRequestFilter para garantizar que se ejecute exactamente una vez por cada petición HTTP.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Busca en la petición HTTP el header "Authorization" (donde debería venir el token)
        String authHeader = request.getHeader("Authorization");
        // Si no hay encabezado, o si no empieza con "Bearer ", se ignora este filtro.
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extraemos el token limpio.
        String token = authHeader.substring(7);
        try {
            // Extraemos el username
            String username = jwtService.extractUsername(token);
            // Verificamos que el usuario no sea nulo y que no esté YA autenticado.
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // Obtenemos los roles, permisos y credenciales del usuario usando el username extraído del token
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                // Verificamos a traves de nuestro metodo del JwtService que el token es valido
                if (jwtService.isTokenValid(token, userDetails)) {
                    // Creamos el token de autenticación que contiene al usuario y sus permisos (Authorities)
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );
                    // Añade metadatos de la petición web actual (como la dirección IP o el ID de sesión) al objeto de autenticación
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // Guardamos al usuario autenticado en el SecurityContext de Spring.
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }

        } catch (JwtException | IllegalArgumentException e) {
            // JWT inválido, expirado o malformado. No se establece autenticación.
        }

        filterChain.doFilter(request, response);
    }
}
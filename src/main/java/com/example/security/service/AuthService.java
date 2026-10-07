package com.example.security.service;

import com.example.security.identity.CustomUserDetails;
import com.example.security.jwt.JwtService;
import com.example.security.model.User;
import com.example.security.payload.response.LoginResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Realiza la autenticación del usuario mediante sus credenciales (email y contraseña).
 * Si las credenciales son válidas, genera un token JWT y devuelve los datos de la sesión.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;


    public LoginResponse login(String email, String password) {

        // 1. Delegamos la autenticación al AuthenticationManager de Spring Security.
        // Buscamos el UserDetailsService configurado, carga el usuario por email y valida la contraseña.
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password));

        // 2. Obtenemos el usuario autenticado, devuelto por el flujo de Spring Security.
        // Casteamos a la clase personalizada 'CustomUserDetails' para poder extraer la entidad User original.
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        User user = userDetails.getUser();

        // 3. Generamos el token
        String token = jwtService.generateToken(auth.getName());

        // 4. Mapeamos la colección de entidades 'Role' a un Set de Strings (ej: ["ROLE_USER", "ROLE_ADMIN"])
        Set<String> roles = user.getRoles().stream()
                .map(role -> role.getCode())
                .collect(Collectors.toSet());

        // 5. Devoolvemos los datos públicos del response del usuario.
        return new LoginResponse(
                token,
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                roles
        );
    }
}
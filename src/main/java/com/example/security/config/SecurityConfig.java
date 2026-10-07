package com.example.security.config;

import com.example.security.jwt.JwtAuthenticationFilter;
import com.example.security.service.CustomUserDetailsService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter; // Requerido para enlazar el filtro

/** ORQUESTADOR CENTRAL DE SEGURIDAD (ENFOQUE MONOLÍTICO HÍBRIDO).
 *
 * Esta clase centraliza tanto los componentes necesarios para emitir tokens (Authorization Server)
 * como los filtros obligatorios para proteger y validar las APIs del negocio (Resource Server).
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Enciende el procesamiento de @PreAuthorize en tus controladores
@ConditionalOnProperty(name = "app.security.enabled", havingValue = "true", matchIfMissing = true)
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthFilter; // Añade la propiedad del filtro


    //........................... Constructor ...............................
    public SecurityConfig(
            CustomUserDetailsService userDetailsService,
            JwtAuthenticationFilter jwtAuthFilter) {
        this.userDetailsService = userDetailsService;
        this.jwtAuthFilter = jwtAuthFilter;
    }

    /** 1. AUTHENTICATION MANAGER
     * Es el componente que recibe una petición de autenticación
     * y delega en el AuthenticationProvider.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) {
        return config.getAuthenticationManager();
    }

    /** 2. PROVEEDOR DE AUTENTICACIÓN
     * Conecta:
     * - UserDetailsService → obtiene el usuario
     * - PasswordEncoder → verifica la contraseña
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /** 3. CODIFICADOR DE CONTRASEÑAS
     * Se utiliza para comparar la contraseña recibida
     * con el hash almacenado en la base de datos.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /** 4. CADENA DE FILTROS HTTP
     * Define qué endpoints están protegidos,
     * que la aplicación sea STATELESS
     * y dónde se ejecuta nuestro filtro JWT.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception { // Añadido throws Exception por la firma de build()
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/login",
                                "/api/auth/register"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                // Registra el proveedor de autenticación tradicional basado en base de datos
                .authenticationProvider(authenticationProvider())
                // INTERCEPTOR CLAVE DEL RESOURCE SERVER:
                // ACTIVACIÓN JWT: Registramos el filtro antes de la autenticación por usuario/contraseña estándar
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}

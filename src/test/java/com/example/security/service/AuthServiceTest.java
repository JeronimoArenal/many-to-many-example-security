package com.example.security.service;

import com.example.security.identity.CustomUserDetails;
import com.example.security.jwt.JwtService;
import com.example.security.model.Role;
import com.example.security.model.User;
import com.example.security.payload.response.LoginResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private Authentication authentication;

    @Mock
    private CustomUserDetails userDetails;

    @Mock
    private User user;

    @Mock
    private Role roleUser;

    @Mock
    private Role roleAdmin;

    @InjectMocks
    private AuthService authService;


    @Test
    void login_shouldAuthenticateAndReturnLoginResponse() {

        // Given
        String email = "test@example.com";
        String password = "password123";
        String token = "jwt-token";

        UUID userId = UUID.randomUUID();

        Set<Role> roles = Set.of(
                roleUser,
                roleAdmin
        );

        // Authentication
        when(authenticationManager.authenticate(
                any(UsernamePasswordAuthenticationToken.class)
        )).thenReturn(authentication);

        when(authentication.getPrincipal())
                .thenReturn(userDetails);

        when(authentication.getName())
                .thenReturn(email);

        // CustomUserDetails -> User
        when(userDetails.getUser())
                .thenReturn(user);

        // User
        when(user.getId())
                .thenReturn(userId);

        when(user.getUsername())
                .thenReturn("testuser");

        when(user.getEmail())
                .thenReturn(email);

        when(user.getRoles())
                .thenReturn(roles);

        // Roles
        when(roleUser.getCode())
                .thenReturn("ROLE_USER");

        when(roleAdmin.getCode())
                .thenReturn("ROLE_ADMIN");

        // JWT
        when(jwtService.generateToken(email))
                .thenReturn(token);


        // When
        LoginResponse result =
                authService.login(email, password);


        // Then
        assertNotNull(result);

        assertEquals(token, result.getToken());
        assertEquals("Bearer", result.getType());
        assertEquals(userId, result.getId());
        assertEquals("testuser", result.getUsername());
        assertEquals(email, result.getEmail());

        assertEquals(
                Set.of("ROLE_USER", "ROLE_ADMIN"),
                result.getRoles()
        );


        // Verify AuthenticationManager
        verify(authenticationManager).authenticate(
                new UsernamePasswordAuthenticationToken(
                        email,
                        password
                )
        );

        // Verify JWT generation
        verify(jwtService).generateToken(email);
    }


    @Test
    void login_shouldThrowExceptionWhenAuthenticationFails() {

        // Given
        String email = "test@example.com";
        String password = "wrong-password";

        AuthenticationException exception =
                mockAuthenticationException();

        when(authenticationManager.authenticate(
                any(UsernamePasswordAuthenticationToken.class)
        )).thenThrow(exception);


        // When / Then
        assertThrows(
                AuthenticationException.class,
                () -> authService.login(email, password)
        );


        // Verify authentication was attempted
        verify(authenticationManager).authenticate(
                new UsernamePasswordAuthenticationToken(
                        email,
                        password
                )
        );


        // JWT must NOT be generated
        verifyNoInteractions(jwtService);
    }


    private AuthenticationException mockAuthenticationException() {
        return new AuthenticationException("Bad credentials") {
        };
    }
}
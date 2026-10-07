package com.example.controller.security;

import com.example.security.model.Role;
import com.example.security.model.User;
import com.example.security.payload.request.RegisterRequest;
import com.example.security.payload.request.RoleCreateRequest;
import com.example.security.payload.request.UserCreateRequest;
import com.example.security.payload.response.LoginResponse;
import com.example.security.payload.response.MessageResponse;
import com.example.security.service.AuthService;
import com.example.security.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthController authController;


    // ============================================================
    // POST /api/auth/login
    // ============================================================

    @Test
    @DisplayName("Debe realizar login y devolver el LoginResponse")
    void shouldLoginSuccessfully() {

        String email = "user@test.com";
        String password = "password123";
        String token = "jwt-token-test";

        UUID userId = UUID.randomUUID();

        Set<String> roles = Set.of("ROLE_USER");

        AuthController.LoginRequest request =
                new AuthController.LoginRequest(
                        email,
                        password
                );

        LoginResponse loginResponse =
                new LoginResponse(
                        token,
                        userId,
                        "testuser",
                        email,
                        roles
                );

        when(authService.login(email, password))
                .thenReturn(loginResponse);

        LoginResponse response =
                authController.login(request);

        assertThat(response)
                .isNotNull();

        assertThat(response.getToken())
                .isEqualTo(token);

        assertThat(response.getType())
                .isEqualTo("Bearer");

        assertThat(response.getId())
                .isEqualTo(userId);

        assertThat(response.getUsername())
                .isEqualTo("testuser");

        assertThat(response.getEmail())
                .isEqualTo(email);

        assertThat(response.getRoles())
                .isEqualTo(Set.of("ROLE_USER"));

        verify(authService)
                .login(email, password);
    }


    // ============================================================
    // POST /api/auth/register
    // ============================================================

    @Test
    @DisplayName("Debe registrar un usuario y devolver HTTP 201")
    void shouldRegisterUserSuccessfully() {

        RegisterRequest request =
                new RegisterRequest(
                        "testuser",
                        "user@test.com",
                        "password123"
                );

        ResponseEntity<MessageResponse> response =
                authController.registerUser(request);

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.CREATED);

        assertThat(response.getBody())
                .isNotNull();

        assertThat(response.getBody().getMessage())
                .isEqualTo(
                        "Usuario registrado correctamente."
                );

        verify(userService)
                .registerUser(request);
    }


    // ============================================================
    // POST /api/auth/roles
    // ============================================================

    @Test
    @DisplayName("Debe crear un rol y devolver HTTP 201")
    void shouldCreateRoleSuccessfully() {

        RoleCreateRequest request =
                new RoleCreateRequest();

        request.setCode("ROLE_ADMIN");
        request.setDescription("Administrador");

        Role role = new Role();

        role.setCode("ROLE_ADMIN");
        role.setDescription("Administrador");

        when(userService.createRole(
                request.getCode(),
                request.getDescription()
        )).thenReturn(role);

        ResponseEntity<MessageResponse> response =
                authController.createRole(request);

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.CREATED);

        assertThat(response.getBody())
                .isNotNull();

        assertThat(response.getBody().getMessage())
                .isEqualTo(
                        "Rol 'ROLE_ADMIN' creado con éxito."
                );

        verify(userService)
                .createRole(
                        "ROLE_ADMIN",
                        "Administrador"
                );
    }


    // ============================================================
    // POST /api/auth/users
    // ============================================================

    @Test
    @DisplayName("Debe crear un usuario con sus roles")
    void shouldCreateUserSuccessfully() {

        UserCreateRequest request =
                new UserCreateRequest();

        request.setUsername("testuser");
        request.setEmail("user@test.com");
        request.setPassword("password123");
        request.setRolesCodes(
                Set.of("ROLE_USER")
        );

        User user = new User();

        user.setUsername("testuser");
        user.setEmail("user@test.com");

        when(userService.createUserWithRoleCodes(
                request.getUsername(),
                request.getEmail(),
                request.getPassword(),
                request.getRolesCodes()
        )).thenReturn(user);

        MessageResponse response =
                authController.createUser(request);

        assertThat(response)
                .isNotNull();

        assertThat(response.getMessage())
                .isEqualTo(
                        "Usuario 'testuser' registrado correctamente."
                );

        verify(userService)
                .createUserWithRoleCodes(
                        "testuser",
                        "user@test.com",
                        "password123",
                        Set.of("ROLE_USER")
                );
    }


    // ============================================================
    // POST /api/auth/users - SIN ROLES
    // ============================================================

    @Test
    @DisplayName("Debe crear un usuario sin roles")
    void shouldCreateUserWithoutRoles() {

        UserCreateRequest request =
                new UserCreateRequest();

        request.setUsername("noroles");
        request.setEmail("noroles@test.com");
        request.setPassword("password123");
        request.setRolesCodes(
                Set.of()
        );

        User user = new User();

        user.setUsername("noroles");

        when(userService.createUserWithRoleCodes(
                request.getUsername(),
                request.getEmail(),
                request.getPassword(),
                request.getRolesCodes()
        )).thenReturn(user);

        MessageResponse response =
                authController.createUser(request);

        assertThat(response)
                .isNotNull();

        assertThat(response.getMessage())
                .isEqualTo(
                        "Usuario 'noroles' registrado correctamente."
                );

        verify(userService)
                .createUserWithRoleCodes(
                        "noroles",
                        "noroles@test.com",
                        "password123",
                        Set.of()
                );
    }
}

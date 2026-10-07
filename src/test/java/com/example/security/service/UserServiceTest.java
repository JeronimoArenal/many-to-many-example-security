package com.example.security.service;

import com.example.exception.EmailAlreadyExistsException;
import com.example.exception.ResourceNotFoundException;
import com.example.exception.UsernameAlreadyExistsException;
import com.example.security.model.Role;
import com.example.security.model.User;
import com.example.security.payload.request.RegisterRequest;
import com.example.security.repository.RoleRepository;
import com.example.security.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder encoder;

    @InjectMocks
    private UserService userService;


    // ============================================================
    // createUser
    // ============================================================

    @Test
    void createUser_shouldCreateAndSaveUser() {
        // Given
        String username = "john";
        String email = "john@example.com";
        String password = "password123";
        String encodedPassword = "encoded-password";

        Role role = new Role();
        role.setCode("ROLE_USER");

        Set<Role> roles = Set.of(role);

        when(userRepository.existsByUsername(username))
                .thenReturn(false);

        when(userRepository.existsByEmail(email))
                .thenReturn(false);

        when(encoder.encode(password))
                .thenReturn(encodedPassword);

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        User result = userService.createUser(
                username,
                email,
                password,
                roles
        );

        // Then
        assertNotNull(result);

        assertEquals(username, result.getUsername());
        assertEquals(email, result.getEmail());
        assertEquals(encodedPassword, result.getPasswordHash());

        assertTrue(result.isEnabled());
        assertFalse(result.isAccountLocked());
        assertFalse(result.isAccountExpired());
        assertFalse(result.isCredentialsExpired());

        assertEquals(roles, result.getRoles());

        verify(userRepository).existsByUsername(username);
        verify(userRepository).existsByEmail(email);
        verify(encoder).encode(password);
        verify(userRepository).save(result);
    }


    @Test
    void createUser_shouldThrowExceptionWhenUsernameAlreadyExists() {
        // Given
        String username = "john";
        String email = "john@example.com";
        String password = "password123";

        when(userRepository.existsByUsername(username))
                .thenReturn(true);

        // When / Then
        UsernameAlreadyExistsException exception =
                assertThrows(
                        UsernameAlreadyExistsException.class,
                        () -> userService.createUser(
                                username,
                                email,
                                password,
                                Set.of()
                        )
                );

        assertEquals(
                "El username '" + username + "' ya existe.",
                exception.getMessage()
        );

        verify(userRepository).existsByUsername(username);

        verify(userRepository, never())
                .existsByEmail(anyString());

        verifyNoInteractions(encoder);
    }


    @Test
    void createUser_shouldThrowExceptionWhenEmailAlreadyExists() {
        // Given
        String username = "john";
        String email = "john@example.com";
        String password = "password123";

        when(userRepository.existsByUsername(username))
                .thenReturn(false);

        when(userRepository.existsByEmail(email))
                .thenReturn(true);

        // When / Then
        EmailAlreadyExistsException exception =
                assertThrows(
                        EmailAlreadyExistsException.class,
                        () -> userService.createUser(
                                username,
                                email,
                                password,
                                Set.of()
                        )
                );

        assertEquals(
                "El email '" + email + "' ya existe.",
                exception.getMessage()
        );

        verify(userRepository).existsByUsername(username);
        verify(userRepository).existsByEmail(email);

        verifyNoInteractions(encoder);

        verify(userRepository, never())
                .save(any(User.class));
    }


    // ============================================================
    // registerUser
    // ============================================================

    @Test
    void registerUser_shouldFindUserRoleAndCreateUser() {
        // Given
        RegisterRequest request = new RegisterRequest(
                "john",
                "john@example.com",
                "password123"
        );

        Role userRole = new Role();
        userRole.setCode("ROLE_USER");

        when(roleRepository.findByCode("ROLE_USER"))
                .thenReturn(Optional.of(userRole));

        when(userRepository.existsByUsername("john"))
                .thenReturn(false);

        when(userRepository.existsByEmail("john@example.com"))
                .thenReturn(false);

        when(encoder.encode("password123"))
                .thenReturn("encoded-password");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        User result = userService.registerUser(request);

        // Then
        assertNotNull(result);

        assertEquals("john", result.getUsername());
        assertEquals("john@example.com", result.getEmail());
        assertEquals("encoded-password", result.getPasswordHash());

        assertTrue(
                result.getRoles().contains(userRole)
        );

        verify(roleRepository).findByCode("ROLE_USER");
        verify(userRepository).existsByUsername("john");
        verify(userRepository).existsByEmail("john@example.com");
        verify(encoder).encode("password123");
        verify(userRepository).save(result);
    }


    @Test
    void registerUser_shouldThrowExceptionWhenUserRoleDoesNotExist() {
        // Given
        RegisterRequest request = new RegisterRequest(
                "john",
                "john@example.com",
                "password123"
        );

        when(roleRepository.findByCode("ROLE_USER"))
                .thenReturn(Optional.empty());

        // When / Then
        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> userService.registerUser(request)
                );

        assertEquals(
                "El rol ROLE_USER no existe.",
                exception.getMessage()
        );

        verify(roleRepository).findByCode("ROLE_USER");

        verifyNoInteractions(userRepository);
        verifyNoInteractions(encoder);
    }


    // ============================================================
    // createRole
    // ============================================================

    @Test
    void createRole_shouldCreateAndSaveRole() {
        // Given
        String code = "ROLE_ADMIN";
        String description = "Administrator";

        when(roleRepository.save(any(Role.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Role result = userService.createRole(
                code,
                description
        );

        // Then
        assertNotNull(result);

        assertEquals(code, result.getCode());
        assertEquals(description, result.getDescription());

        verify(roleRepository).save(result);
    }


    // ============================================================
    // createUserWithRoleCodes
    // ============================================================

    @Test
    void createUserWithRoleCodes_shouldFindRolesAndCreateUser() {
        // Given
        String username = "john";
        String email = "john@example.com";
        String password = "password123";

        Role userRole = new Role();
        userRole.setCode("ROLE_USER");

        Role adminRole = new Role();
        adminRole.setCode("ROLE_ADMIN");

        Set<String> roleCodes = Set.of(
                "ROLE_USER",
                "ROLE_ADMIN"
        );

        when(roleRepository.findByCode("ROLE_USER"))
                .thenReturn(Optional.of(userRole));

        when(roleRepository.findByCode("ROLE_ADMIN"))
                .thenReturn(Optional.of(adminRole));

        when(userRepository.existsByUsername(username))
                .thenReturn(false);

        when(userRepository.existsByEmail(email))
                .thenReturn(false);

        when(encoder.encode(password))
                .thenReturn("encoded-password");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        User result = userService.createUserWithRoleCodes(
                username,
                email,
                password,
                roleCodes
        );

        // Then
        assertNotNull(result);

        assertEquals(username, result.getUsername());
        assertEquals(email, result.getEmail());
        assertEquals("encoded-password", result.getPasswordHash());

        assertEquals(
                Set.of(userRole, adminRole),
                result.getRoles()
        );

        verify(roleRepository).findByCode("ROLE_USER");
        verify(roleRepository).findByCode("ROLE_ADMIN");

        verify(userRepository).existsByUsername(username);
        verify(userRepository).existsByEmail(email);

        verify(encoder).encode(password);
        verify(userRepository).save(result);
    }


    @Test
    void createUserWithRoleCodes_shouldThrowExceptionWhenRoleDoesNotExist() {

        // Given
        String username = "john";
        String email = "john@example.com";
        String password = "password123";

        Set<String> roleCodes = Set.of(
                "ROLE_USER",
                "ROLE_ADMIN"
        );

        Role userRole = new Role();
        userRole.setCode("ROLE_USER");

        when(roleRepository.findByCode("ROLE_USER"))
                .thenReturn(Optional.of(userRole));

        when(roleRepository.findByCode("ROLE_ADMIN"))
                .thenReturn(Optional.empty());

        // When / Then
        ResourceNotFoundException exception = assertThrows(
                        ResourceNotFoundException.class,
                        () -> userService.createUserWithRoleCodes(
                                username,
                                email,
                                password,
                                roleCodes
                        )
                );

        assertEquals("El rol 'ROLE_ADMIN' no existe.", exception.getMessage());

        verify(roleRepository).findByCode("ROLE_USER");
        verify(roleRepository).findByCode("ROLE_ADMIN");
        verifyNoInteractions(encoder);
        verify(userRepository, never()).save(any(User.class));
    }


//    @Test
//    void createUserWithRoleCodes_shouldWorkWithNullRoles() {
//        // Given
//        String username = "john";
//        String email = "john@example.com";
//        String password = "password123";
//
//        when(userRepository.existsByUsername(username))
//                .thenReturn(false);
//
//        when(userRepository.existsByEmail(email))
//                .thenReturn(false);
//
//        when(encoder.encode(password))
//                .thenReturn("encoded-password");
//
//        when(userRepository.save(any(User.class)))
//                .thenAnswer(invocation -> invocation.getArgument(0));
//
//        // When
//        User result = userService.createUserWithRoleCodes(
//                username,
//                email,
//                password,
//                null
//        );
//
//        // Then
//        assertNotNull(result);
//        assertTrue(result.getRoles().isEmpty());
//
//        verify(userRepository).existsByUsername(username);
//        verify(userRepository).existsByEmail(email);
//        verify(encoder).encode(password);
//        verify(userRepository).save(result);
//
//        verifyNoInteractions(roleRepository);
//    }
}
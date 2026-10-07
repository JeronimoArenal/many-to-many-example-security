package com.example.security.service;

import com.example.security.identity.CustomUserDetails;
import com.example.security.model.Role;
import com.example.security.model.User;
import com.example.security.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private User user;

    @Mock
    private Role roleUser;

    @Mock
    private Role roleAdmin;

    @InjectMocks
    private CustomUserDetailsService userDetailsService;

    @Test
    void loadUserByUsername_shouldReturnUserDetailsWithAuthorities() {
        // Given
        String email = "test@example.com";

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(user.getRoles())
                .thenReturn(Set.of(roleUser, roleAdmin));

        when(roleUser.getCode())
                .thenReturn("ROLE_USER");

        when(roleAdmin.getCode())
                .thenReturn("ROLE_ADMIN");

        // When
        UserDetails result =
                userDetailsService.loadUserByUsername(email);

        // Then
        assertNotNull(result);
        assertInstanceOf(CustomUserDetails.class, result);

        assertEquals(
                2,
                result.getAuthorities().size()
        );

        assertTrue(
                result.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .toList()
                        .containsAll(
                                List.of("ROLE_USER", "ROLE_ADMIN")
                        )
        );

        verify(userRepository).findByEmail(email);
        verify(user).getRoles();
        verify(roleUser).getCode();
        verify(roleAdmin).getCode();
    }

    @Test
    void loadUserByUsername_shouldThrowExceptionWhenUserDoesNotExist() {
        // Given
        String email = "notfound@example.com";

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.empty());

        // When / Then
        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername(email)
        );

        assertEquals(
                "User not found: " + email,
                exception.getMessage()
        );

        verify(userRepository).findByEmail(email);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void loadUserByUsername_shouldReturnUserDetailsWithoutAuthoritiesWhenUserHasNoRoles() {
        // Given
        String email = "noroles@example.com";

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(user.getRoles())
                .thenReturn(Set.of());

        // When
        UserDetails result =
                userDetailsService.loadUserByUsername(email);

        // Then
        assertNotNull(result);
        assertInstanceOf(CustomUserDetails.class, result);
        assertTrue(result.getAuthorities().isEmpty());

        verify(userRepository).findByEmail(email);
        verify(user).getRoles();
    }
}
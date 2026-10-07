package com.example.security.service;

import com.example.exception.EmailAlreadyExistsException;
import com.example.exception.ResourceNotFoundException;
import com.example.exception.UsernameAlreadyExistsException;
import com.example.security.model.Role;
import com.example.security.model.User;
import com.example.security.payload.request.RegisterRequest;
import com.example.security.repository.RoleRepository;
import com.example.security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder encoder;

    //....................... createUser .......................................
    @Transactional
    public User createUser(String username, String email, String password, Set<Role> roles) {

        if (userRepository.existsByUsername(username)) {
            throw new UsernameAlreadyExistsException("El username '" + username + "' ya existe.");
        }

        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException("El email '" + email + "' ya existe.");
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(encoder.encode(password));

        user.setEnabled(true);
        user.setAccountLocked(false);
        user.setAccountExpired(false);
        user.setCredentialsExpired(false);

        user.getRoles().addAll(roles);

        return userRepository.save(user);
    }

    //....................... registerUser .......................................
    public User registerUser(RegisterRequest request) {

        Role userRole = roleRepository.findByCode("ROLE_USER")
                .orElseThrow(() -> new ResourceNotFoundException("El rol ROLE_USER no existe.")
                );

        return createUser(
                request.username(),
                request.email(),
                request.password(),
                Set.of(userRole)
        );
    }

    //....................... createRole .......................................
    public Role createRole(String code, String description) {
        Role role = new Role();
        role.setCode(code);
        role.setDescription(description); // Corregido: usando el campo correcto de tu entidad
        return roleRepository.save(role);
    }

    //....................... createUserWithRoleCodes .......................................
    @Transactional
    public User createUserWithRoleCodes(String username, String email, String password, Set<String> rolesCodes) {

        Set<Role> rolesEntities = new HashSet<>();

        // Si la lista es nula o viene vacía, asignamos el rol por defecto ROLE_USER
        if (rolesCodes == null || rolesCodes.isEmpty()) {
            Role defaultRole = roleRepository.findByCode("ROLE_USER")
                    .orElseThrow(() -> new ResourceNotFoundException("El rol 'ROLE_USER' no existe."));
            rolesEntities.add(defaultRole);
        } else {
            // Si viene con datos, iteramos de forma segura para buscar cada uno
            for (String code : rolesCodes) {
                Role role = roleRepository.findByCode(code)
                        .orElseThrow(() -> new ResourceNotFoundException("El rol '" + code + "' no existe."));
                rolesEntities.add(role);
            }
        }

        return createUser(username, email, password, rolesEntities);
    }


}

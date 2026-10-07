package com.example.util;

import com.example.security.model.Role;
import com.example.security.model.User;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class SecurityTestDataFactory {

    private SecurityTestDataFactory() {
        // Utility class
    }

    // =========================
    // ROLES
    // =========================

    public static Role createUserRole() {
        Role role = new Role();
        role.setCode("ROLE_USER");
        role.setDescription("Usuario normal");
        return role;
    }

    public static Role createAdminRole() {
        Role role = new Role();
        role.setCode("ROLE_ADMIN");
        role.setDescription("Administrador");
        return role;
    }

    public static Role createEditorRole() {
        Role role = new Role();
        role.setCode("ROLE_EDITOR");
        role.setDescription("Editor");
        return role;
    }

    // =========================
    // USERS
    // =========================

    public static User createUser() {
        User user = new User();

        user.setUsername("testuser");
        user.setEmail("user@test.com");
        user.setPasswordHash("$2a$10$test-password-hash");
        user.setEnabled(true);
        user.setAccountLocked(false);
        user.setAccountExpired(false);
        user.setCredentialsExpired(false);

        user.setRoles(new HashSet<>(Set.of(
                createUserRole()
        )));

        return user;
    }

    public static User createAdmin() {
        User user = new User();

        user.setUsername("testadmin");
        user.setEmail("admin@test.com");
        user.setPasswordHash("$2a$10$test-password-hash");
        user.setEnabled(true);
        user.setAccountLocked(false);
        user.setAccountExpired(false);
        user.setCredentialsExpired(false);

        user.setRoles(new HashSet<>(Set.of(
                createUserRole(),
                createAdminRole()
        )));

        return user;
    }

    public static User createDisabledUser() {
        User user = new User();

        user.setUsername("disableduser");
        user.setEmail("disabled@test.com");
        user.setPasswordHash("$2a$10$test-password-hash");
        user.setEnabled(false);
        user.setAccountLocked(false);
        user.setAccountExpired(false);
        user.setCredentialsExpired(false);

        user.setRoles(new HashSet<>(Set.of(
                createUserRole()
        )));

        return user;
    }

    public static User createLockedUser() {
        User user = new User();

        user.setUsername("lockeduser");
        user.setEmail("locked@test.com");
        user.setPasswordHash("$2a$10$test-password-hash");
        user.setEnabled(true);
        user.setAccountLocked(true);
        user.setAccountExpired(false);
        user.setCredentialsExpired(false);

        user.setRoles(new HashSet<>(Set.of(
                createUserRole()
        )));

        return user;
    }

    // =========================
    // LISTS
    // =========================

    public static List<User> createUserList() {
        return List.of(
                createUser(),
                createAdmin()
        );
    }

    public static List<Role> createRoleList() {
        return List.of(
                createUserRole(),
                createAdminRole()
        );
    }
}

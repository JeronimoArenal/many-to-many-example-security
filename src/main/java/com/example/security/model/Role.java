package com.example.security.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * El perfil/grupo: Actúa como un contenedor. Agrupa un conjunto de permisos bajo un nombre
 * (ej. ROLE_ADMIN, ROLE_OPERATOR).
 */
@Getter @Setter
@Entity
@Table(name = "security_roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 60)
    private String code;

    @Column(length = 200)
    private String description;

//    @ManyToMany(fetch = FetchType.LAZY)
//    @JoinTable(name = "security_role_permissions",
//            joinColumns = @JoinColumn(name = "role_id"),
//            inverseJoinColumns = @JoinColumn(name = "permission_id")
//    )
//    private Set<Permission> permissions = new HashSet<>();
}
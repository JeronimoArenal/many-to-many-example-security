package com.example.security.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * LA ACCION CONCRETA: Define acciones específicas sobre recursos (ej. WRITE_USERS, READ_PRODUCTS).
 */
@Getter @Setter
@Entity
@Table(name = "security_permissions")
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(length = 200)
    private String description;
}

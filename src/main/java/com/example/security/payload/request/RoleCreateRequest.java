package com.example.security.payload.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoleCreateRequest {
    private String code;         // Ejemplo: "ROLE_ADMIN"
    private String description;  // Ejemplo: "Perfil con acceso total al sistema"
}

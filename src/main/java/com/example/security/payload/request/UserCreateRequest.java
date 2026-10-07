package com.example.security.payload.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserCreateRequest {
    private String username;
    private String email;
    private String password;
    private Set<String> rolesCodes; // Recibe códigos de roles ["ROLE_USER", "ROLE_ADMIN"]
}
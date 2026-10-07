package com.example.security.payload.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class LoginResponse {

    private String token;

    private String type = "Bearer";

    private UUID id;

    private String username;

    private String email;

    private Set<String> roles;

    public LoginResponse(String token, UUID id, String username, String email, Set<String> roles) {
        this.token = token;
        this.id = id;
        this.username = username;
        this.email = email;
        this.roles = roles;
    }
}

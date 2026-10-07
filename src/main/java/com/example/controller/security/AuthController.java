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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    public record LoginRequest(String email, String password) {}

    //....................... login .......................................
    @PostMapping("/login")
    public LoginResponse login(
            @Valid @RequestBody LoginRequest request
    ) {

        return authService.login(
                request.email(),
                request.password()
        );
    }

    //....................... registerUser .......................................
    @PostMapping("/register")
    public ResponseEntity<MessageResponse> registerUser(
            @Valid @RequestBody RegisterRequest request
    ) {

        userService.registerUser(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        new MessageResponse(
                                "Usuario registrado correctamente."
                        )
                );
    }

    //....................... crear rol ...................................
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/roles")
    public ResponseEntity<MessageResponse> createRole(
            @RequestBody RoleCreateRequest request
    ) {

        Role newRole = userService.createRole(
                request.getCode(),
                request.getDescription()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        new MessageResponse(
                                "Rol '" + newRole.getCode()
                                        + "' creado con éxito."
                        )
                );
    }

    //....................... crear usuario (con o sin roles) .............
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    public MessageResponse createUser(@RequestBody UserCreateRequest request) {

        User newUser = userService.createUserWithRoleCodes(
                request.getUsername(),
                request.getEmail(),
                request.getPassword(),
                request.getRolesCodes()
        );

        return new MessageResponse(
                "Usuario '" + newUser.getUsername()
                        + "' registrado correctamente."
        );
    }
}
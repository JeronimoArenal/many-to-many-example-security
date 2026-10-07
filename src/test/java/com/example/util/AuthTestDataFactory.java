package com.example.util;

import com.example.security.payload.request.LoginRequest;
import com.example.security.payload.request.RegisterRequest;

public final class AuthTestDataFactory {

    private AuthTestDataFactory() {
        // Utility class
    }

    public static final String DEFAULT_PASSWORD = "123456";

    public static LoginRequest createValidLoginRequest() {
        return new LoginRequest(
                "user@test.com",
                DEFAULT_PASSWORD
        );
    }

    public static LoginRequest createAdminLoginRequest() {
        return new LoginRequest(
                "admin@test.com",
                DEFAULT_PASSWORD
        );
    }

    public static LoginRequest createInvalidEmailLoginRequest() {
        return new LoginRequest(
                "invalid-email",
                DEFAULT_PASSWORD
        );
    }

    public static LoginRequest createEmptyEmailLoginRequest() {
        return new LoginRequest(
                "",
                DEFAULT_PASSWORD
        );
    }

    public static LoginRequest createEmptyPasswordLoginRequest() {
        return new LoginRequest(
                "user@test.com",
                ""
        );
    }

    public static RegisterRequest createValidRegisterRequest() {
        return new RegisterRequest(
                "newuser",
                "newuser@test.com",
                DEFAULT_PASSWORD
        );
    }

    public static RegisterRequest createRegisterRequestWithInvalidEmail() {
        return new RegisterRequest(
                "newuser",
                "invalid-email",
                DEFAULT_PASSWORD
        );
    }

    public static RegisterRequest createRegisterRequestWithEmptyUsername() {
        return new RegisterRequest(
                "",
                "newuser@test.com",
                DEFAULT_PASSWORD
        );
    }

    public static RegisterRequest createRegisterRequestWithShortPassword() {
        return new RegisterRequest(
                "newuser",
                "newuser@test.com",
                "123"
        );
    }
}

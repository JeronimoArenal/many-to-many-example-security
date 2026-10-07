package com.example.exception.dto;

import java.time.LocalDateTime;

public record ErrorResponse(
        LocalDateTime timestamp,
        String error,
        String message,
        int status
) {}

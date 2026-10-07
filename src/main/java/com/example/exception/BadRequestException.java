package com.example.exception;

/**
 * Excepción lanzada cuando la petición del cliente no es válida.
 * El GlobalExceptionHandler la transforma en una respuesta HTTP 400.
 */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}

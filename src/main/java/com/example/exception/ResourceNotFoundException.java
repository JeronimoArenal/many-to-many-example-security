package com.example.exception;

/**
 * Excepción lanzada cuando no se encuentra un recurso solicitado.
 * El GlobalExceptionHandler la transforma en una respuesta HTTP 404.
 */
public class ResourceNotFoundException extends RuntimeException{

    public ResourceNotFoundException(String message) {
        super(message);
    }

}

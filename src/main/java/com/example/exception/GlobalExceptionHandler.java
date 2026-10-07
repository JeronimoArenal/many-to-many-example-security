package com.example.exception;

import com.example.exception.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Spring supports exception handling by a global Exception Handler (@ExceptionHandler) with Controller Advice (@RestControllerAdvice).
 * The @RestControllerAdvice annotation is a specialization of @Component annotation so that it is auto-detected via classpath scanning.
 * It is a kind of interceptor that surrounds the logic in our Controllers and allows us to apply some common logic to them.
 *
 * Rest Controller Advice’s methods (annotated with @ExceptionHandler) are shared globally across multiple @Controller components
 * to capture exceptions and translate them to HTTP responses.
 * The @ExceptionHandler annotation indicates which type of Exception we want to handle.
 * The exception instance and the request will be injected via method arguments.
 * By using two annotations together, we can:Data Management
 *     • control the body of the response along with status code
 *     • handle several exceptions in the same method
 *
 * How about @ResponseStatus?
 * @RestControllerAdvice annotation tells a controller that the object returned is automatically serialized into JSON
 * and passed to the HttpResponse object. You only need to return
 *
 * Java  body object instead of ResponseEntity object. But the status could be always OK (200) although the data corresponds to an
 * exception signal (404 – Not Found for example). @ResponseStatus can help to set the HTTP status code for the response:
 *
 * If you use @RestControllerAdvice without @ResponseBody and @ResponseStatus, you can return ResponseEntity object instead
 */

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {


    //....................... handleAccessDenied .......................................
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse(
                        LocalDateTime.now(),
                        "Acceso no autorizado",
                        "No tienes permisos suficientes para acceder a este recurso.",
                        HttpStatus.FORBIDDEN.value()
                ));
    }

    //....................... handleGeneral .......................................
    @ExceptionHandler(Exception.class)
//    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex){

        log.error("Excepción no controlada", ex);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(LocalDateTime.now(), "Error interno del servidor",
                        "Ha ocurrido un error inesperado", HttpStatus.INTERNAL_SERVER_ERROR.value()
                ));
    }

    //....................... handleDataIntegrity .......................................
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(
                        LocalDateTime.now(),
                        "Violación de integridad de datos",
                        "Datos duplicados o restricción infringida",
                        HttpStatus.CONFLICT.value()
                ));
    }

    //....................... handle .......................................
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handle(ResourceNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(
                        LocalDateTime.now(),
                        "Recurso no encontrado",
                        ex.getMessage(),
                        HttpStatus.NOT_FOUND.value()
                ));
    }

    //....................... handleBadRequest .......................................
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(BadRequestException ex) {

        return ResponseEntity
                .badRequest()
                .body(new ErrorResponse(
                        LocalDateTime.now(),
                        "Petición incorrecta",
                        ex.getMessage(),
                        HttpStatus.BAD_REQUEST.value()
                ));
    }

    //....................... handleValidationErrors .......................................
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException ex) {

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", java.time.LocalDateTime.now());
        response.put("error", "Error de Validación");
        response.put("status", HttpStatus.BAD_REQUEST.value());

        // Este mapa guarda cada campo con su mensaje personalizado"
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error -> {
            errors.put(error.getField(), error.getDefaultMessage());
        });

        response.put("errors", errors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }


    //....................... handleMalformedRequests (Múltiples Excepciones en un método) .......................................
    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class })
    public ResponseEntity<ErrorResponse> handleMalformedRequests(Exception ex) {

        String mensajeDetalle = "El cuerpo o los parámetros de la petición contienen un formato incorrecto.";

        // Comparamos usando la clase exacta del error de conversión de argumentos de Spring
        if (ex instanceof MethodArgumentTypeMismatchException mismatchEx) {
            String tipoRequerido = mismatchEx.getRequiredType() != null ? mismatchEx.getRequiredType().getSimpleName() : "desconocido";
            mensajeDetalle = String.format("El parámetro '%s' con el valor '%s' no pudo ser convertido al tipo requerido (%s).",
                    mismatchEx.getName(), mismatchEx.getValue(), tipoRequerido);
        } else if (ex instanceof HttpMessageNotReadableException) {
            mensajeDetalle = "Estructura JSON inválida. Revisa que los corchetes, comas y tipos de datos en el cuerpo sean correctos.";
        }

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST) // Ambas corresponden a un fallo del cliente (400)
                .body(new ErrorResponse(
                        LocalDateTime.now(),
                        "Petición Mal Formada",
                        mensajeDetalle,
                        HttpStatus.BAD_REQUEST.value()
                ));
    }

    //....................... handleEmailAlreadyExists .......................................
    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleEmailAlreadyExists(EmailAlreadyExistsException ex) {

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(
                        LocalDateTime.now(),
                        "Email ya registrado",
                        ex.getMessage(),
                        HttpStatus.CONFLICT.value()
                ));
    }

    //....................... handleUsernameAlreadyExists .......................................
    @ExceptionHandler(UsernameAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUsernameAlreadyExists(UsernameAlreadyExistsException ex) {

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(
                        LocalDateTime.now(),
                        "Username ya registrado",
                        ex.getMessage(),
                        HttpStatus.CONFLICT.value()
                ));
    }
}

package com.tasksync.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private Map<String, Object> buildError(String message, HttpStatus status) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        return body;
    }

    // ----------- Custom Application Exceptions -----------
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<?> handleCustomException(CustomException ex) {
        return new ResponseEntity<>(buildError(ex.getMessage(), HttpStatus.BAD_REQUEST),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<?> handleUnauthorizedException(UnauthorizedException ex) {
        return new ResponseEntity<>(buildError(ex.getMessage(), HttpStatus.UNAUTHORIZED),
                HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(com.tasksync.exception.AccessDeniedException.class)
    public ResponseEntity<?> handleCustomAccessDenied(com.tasksync.exception.AccessDeniedException ex) {
        return new ResponseEntity<>(buildError(ex.getMessage(), HttpStatus.FORBIDDEN),
                HttpStatus.FORBIDDEN);
    }

    // ----------- Spring Security Access Denied -----------
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleSpringAccessDenied(AccessDeniedException ex) {
        return new ResponseEntity<>(buildError("Access denied", HttpStatus.FORBIDDEN),
                HttpStatus.FORBIDDEN);
    }

    // ----------- Generic Exceptions -----------
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGenericException(Exception ex) {
        return new ResponseEntity<>(buildError(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

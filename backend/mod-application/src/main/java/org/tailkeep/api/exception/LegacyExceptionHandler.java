package org.tailkeep.api.exception;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.tailkeep.adapter.web.shared.ApiError;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;

// Legacy: handles the exceptions of code that is not migrated yet. The web adapter handles the rest and runs last.
@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class LegacyExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationExceptions(MethodArgumentNotValidException e, HttpServletRequest request) {
        String message = e.getBindingResult().getFieldErrors()
            .stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .reduce((a, b) -> a + "; " + b)
            .orElse("Validation failed");

        ApiError error = new ApiError(
            request.getRequestURI(),
            message,
            HttpStatus.BAD_REQUEST.value(),
            LocalDateTime.now().toString()
        );

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(InsufficientAuthenticationException.class)
    public ResponseEntity<ApiError> handleInsufficientAuthenticationException(InsufficientAuthenticationException e, HttpServletRequest request) {
        ApiError error = new ApiError(
            request.getRequestURI(),
            "Authentication is required to access this resource",
            HttpStatus.UNAUTHORIZED.value(),
            LocalDateTime.now().toString()
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiError> handleBadCredentialsException(BadCredentialsException e, HttpServletRequest request) {
        ApiError error = new ApiError(
            request.getRequestURI(),
            e.getMessage(),
            HttpStatus.UNAUTHORIZED.value(),
            LocalDateTime.now().toString()
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiError> handleUnauthorizedException(UnauthorizedException e, HttpServletRequest request) {
        ApiError error = new ApiError(
            request.getRequestURI(),
            e.getMessage(),
            HttpStatus.UNAUTHORIZED.value(),
            LocalDateTime.now().toString()
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(UsernameAlreadyExistsException.class)
    public ResponseEntity<ApiError> handleUsernameAlreadyExistsException(
            UsernameAlreadyExistsException e, 
            HttpServletRequest request
    ) {
        ApiError error = new ApiError(
            request.getRequestURI(),
            e.getMessage(),
            HttpStatus.CONFLICT.value(),
            LocalDateTime.now().toString()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(PasswordMismatchException.class)
    public ResponseEntity<ApiError> handlePasswordMismatchException(PasswordMismatchException e, HttpServletRequest request) {
        ApiError error = new ApiError(
            request.getRequestURI(),
            e.getMessage(),
            HttpStatus.BAD_REQUEST.value(),
            LocalDateTime.now().toString()
        );

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(InvalidCurrentPasswordException.class)
    public ResponseEntity<ApiError> handleInvalidCurrentPasswordException(InvalidCurrentPasswordException e, HttpServletRequest request) {
        ApiError error = new ApiError(
            request.getRequestURI(),
            e.getMessage(),
            HttpStatus.BAD_REQUEST.value(),
            LocalDateTime.now().toString()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(PasswordLengthException.class)
    public ResponseEntity<ApiError> handlePasswordLengthException(PasswordLengthException e, HttpServletRequest request) {
        ApiError error = new ApiError(
            request.getRequestURI(),
            e.getMessage(),
            HttpStatus.BAD_REQUEST.value(),
            LocalDateTime.now().toString()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(UnsupportedOperationException.class)
    public ResponseEntity<ApiError> handleUnsupportedOperation(UnsupportedOperationException ex, HttpServletRequest request) {
        ApiError error = new ApiError(
            request.getRequestURI(),
            ex.getMessage(),
            HttpStatus.BAD_REQUEST.value(),
            LocalDateTime.now().toString()
        );

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(InvalidArgumentException.class)
    public ResponseEntity<ApiError> handleInvalidArgumentException(InvalidArgumentException ex, HttpServletRequest request) {
        ApiError error = new ApiError(
            request.getRequestURI(),
            ex.getMessage(),
            HttpStatus.BAD_REQUEST.value(),
            LocalDateTime.now().toString()
        );

        return ResponseEntity.badRequest().body(error);
    }

}

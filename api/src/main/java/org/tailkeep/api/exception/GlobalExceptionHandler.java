package org.tailkeep.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidUrlException.class)
    public ResponseEntity<ApiError> handleInvalidUrlException(InvalidUrlException e, HttpServletRequest request) {
        ApiError error = new ApiError(
            request.getRequestURI(),
            e.getMessage(),
            HttpStatus.BAD_REQUEST.value(),
            LocalDateTime.now()
        );

        return ResponseEntity.badRequest().body(error);
    }

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
            LocalDateTime.now()
        );

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleMessageNotReadableException(HttpMessageNotReadableException e, HttpServletRequest request) {
        ApiError error = new ApiError(
            request.getRequestURI(),
            "Invalid request body: JSON is required",
            HttpStatus.BAD_REQUEST.value(),
            LocalDateTime.now()
        );

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
    public ResponseEntity<ApiError> handleNotFoundException(Exception e, HttpServletRequest request) {
        ApiError error = new ApiError(
            request.getRequestURI(),
            "The requested resource was not found",
            HttpStatus.NOT_FOUND.value(),
            LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(InsufficientAuthenticationException.class)
    public ResponseEntity<ApiError> handleInsufficientAuthenticationException(InsufficientAuthenticationException e, HttpServletRequest request) {
        ApiError error = new ApiError(
            request.getRequestURI(),
            "Authentication is required to access this resource",
            HttpStatus.UNAUTHORIZED.value(),
            LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiError> handleBadCredentialsException(BadCredentialsException e, HttpServletRequest request) {
        ApiError error = new ApiError(
            request.getRequestURI(),
            e.getMessage(),
            HttpStatus.UNAUTHORIZED.value(),
            LocalDateTime.now()
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
            LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(PasswordMismatchException.class)
    public ResponseEntity<ApiError> handlePasswordMismatchException(PasswordMismatchException e, HttpServletRequest request) {
        ApiError error = new ApiError(
            request.getRequestURI(),
            e.getMessage(),
            HttpStatus.BAD_REQUEST.value(),
            LocalDateTime.now()
        );

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(InvalidCurrentPasswordException.class)
    public ResponseEntity<ApiError> handleInvalidCurrentPasswordException(InvalidCurrentPasswordException e, HttpServletRequest request) {
        ApiError error = new ApiError(
            request.getRequestURI(),
            e.getMessage(),
            HttpStatus.UNAUTHORIZED.value(),
            LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(PasswordLengthException.class)
    public ResponseEntity<ApiError> handlePasswordLengthException(PasswordLengthException e, HttpServletRequest request) {
        ApiError error = new ApiError(
            request.getRequestURI(),
            e.getMessage(),
            HttpStatus.BAD_REQUEST.value(),
            LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleResourceNotFoundException(
            ResourceNotFoundException e,
            HttpServletRequest request
    ) {
        ApiError error = new ApiError(
            request.getRequestURI(),
            e.getMessage(),
            HttpStatus.NOT_FOUND.value(),
            LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleAllExceptions(Exception e, HttpServletRequest request) {
        log.error("Unexpected error occurred", e);
        
        ApiError error = new ApiError(
            request.getRequestURI(),
            "Something went wrong while executing the operation.",
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            LocalDateTime.now()
        );

        return ResponseEntity.internalServerError().body(error);
    }
}

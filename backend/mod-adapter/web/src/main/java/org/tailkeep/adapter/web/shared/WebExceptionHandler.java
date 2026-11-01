package org.tailkeep.adapter.web.shared;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.tailkeep.domain.shared.ResourceNotFoundException;

import java.time.LocalDateTime;

/**
 * Turns domain and web framework exceptions into {@link ApiError} bodies. It runs last, so it ends with
 * the catch-all and any other advice with a more specific handler wins.
 */
@ControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE)
public class WebExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(WebExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleResourceNotFound(ResourceNotFoundException e, HttpServletRequest request) {
        return respond(HttpStatus.NOT_FOUND, e.getMessage(), request);
    }

    @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
    public ResponseEntity<ApiError> handleNotFound(Exception e, HttpServletRequest request) {
        return respond(HttpStatus.NOT_FOUND, "The requested resource was not found", request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleMessageNotReadable(HttpMessageNotReadableException e,
            HttpServletRequest request) {
        return respond(HttpStatus.BAD_REQUEST, "Invalid request body: JSON is required", request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(MethodArgumentTypeMismatchException e,
            HttpServletRequest request) {
        return respond(HttpStatus.BAD_REQUEST, "Invalid parameter format", request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleAll(Exception e, HttpServletRequest request) {
        log.error("Unexpected error occurred", e);
        return respond(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong while executing the operation.",
                request);
    }

    private static ResponseEntity<ApiError> respond(HttpStatus status, String message, HttpServletRequest request) {
        ApiError error = new ApiError(request.getRequestURI(), message, status.value(),
                LocalDateTime.now().toString());
        return ResponseEntity.status(status).body(error);
    }
}

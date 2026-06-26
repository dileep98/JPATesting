package com.dk.jpatesting.controller.exception;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @Autowired
    private Environment env;

    @Value("${app.error.include-methods:true}")
    private boolean includeMethodsInError;

    @PostConstruct
    public void init() {
        log.info("app.error.include-methods = {}", includeMethodsInError);
        log.info("active profiles = {}", Arrays.toString(env.getActiveProfiles()));
    }

    /**     * Handle 404 - Resource not found (wrong path)     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNoHandlerFound(
            NoHandlerFoundException ex,
            WebRequest webRequest) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.NOT_FOUND.value());
        body.put("error", "Resource Not Found");
        body.put("message", "The requested path does not exist: "+ex.getMessage());
        body.put("path", ex.getRequestURL());

        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String, Object>> handleMethodNotAllowed(
            HttpRequestMethodNotSupportedException ex,
            WebRequest webRequest) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.METHOD_NOT_ALLOWED.value());
        body.put("error", "Method Not Allowed");
        body.put("message", "HTTP method '"+ex.getMethod()+"' is not supported for this endpoint");

        // Safely build supported methods list
        List<String> supported = ex.getSupportedHttpMethods() != null
                ? ex.getSupportedHttpMethods().stream()
                .map(HttpMethod::toString)
                .toList()
                : List.of();

        // Add to JSON body only if config allows it
        if (isDetailedErrorsEnabled()) {
            body.put("supportedMethods", supported);
        }

        // Build response with Allow header if we have supported methods
        ResponseEntity.BodyBuilder responseBuilder = ResponseEntity
                .status(HttpStatus.METHOD_NOT_ALLOWED);

        if (!supported.isEmpty()) {
            responseBuilder = responseBuilder.header("Allow", String.join(", ", supported));
        }

        return responseBuilder.body(body);
    }

    private boolean isDetailedErrorsEnabled() {
        return includeMethodsInError;
    }
}

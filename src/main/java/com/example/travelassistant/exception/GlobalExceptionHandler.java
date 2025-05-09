package com.example.travelassistant.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Map<String, Object>> handleApiException(ApiException e) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", true);
        response.put("message", e.getMessage());

        return ResponseEntity.status(e.getStatus()).body(response);
    }

    @ExceptionHandler(ExternalApiException.class)
    public ResponseEntity<Map<String, Object>> handleExternalApiException(ExternalApiException e) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", true);
        response.put("message", e.getMessage());
        response.put("apiName", e.getApiName());

        return ResponseEntity.status(e.getStatus()).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception e) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", true);
        response.put("message", "Um erro inesperado ocorreu: " + e.getMessage());

        return ResponseEntity.status(500).body(response);
    }
}
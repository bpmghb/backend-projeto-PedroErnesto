package com.example.travelassistant.exception;

import org.springframework.http.HttpStatus;

public class ExternalApiException extends ApiException {
    private final String apiName;

    public ExternalApiException(String message, String apiName) {
        super(message, HttpStatus.SERVICE_UNAVAILABLE);
        this.apiName = apiName;
    }

    public String getApiName() {
        return apiName;
    }
}
package com.example.travelassistant.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Value("${weather.api.key}")
    private String weatherApiKey;

    @Value("${weather.api.url}")
    private String weatherApiUrl;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    public String getWeatherApiKey() {
        return weatherApiKey;
    }

    public String getWeatherApiUrl() {
        return weatherApiUrl;
    }

    public String getGeminiApiKey() {
        return geminiApiKey;
    }

    public String getGeminiApiUrl() {
        return geminiApiUrl;
    }
}

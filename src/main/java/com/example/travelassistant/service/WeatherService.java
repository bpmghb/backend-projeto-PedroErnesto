package com.example.travelassistant.service;

import com.example.travelassistant.config.AppConfig;
import com.example.travelassistant.exception.ExternalApiException;
import com.example.travelassistant.model.response.WeatherResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class WeatherService {

    private final WebClient weatherApiClient;
    private final AppConfig appConfig;

    @Autowired
    public WeatherService(WebClient weatherApiClient, AppConfig appConfig) {
        this.weatherApiClient = weatherApiClient;
        this.appConfig = appConfig;
    }

    public WeatherResponse getCurrentWeather(String location) {
        return weatherApiClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host("api.weatherapi.com")
                        .path("/v1/current.json")
                        .queryParam("key", appConfig.getWeatherApiKey())
                        .queryParam("q", location)
                        .queryParam("aqi", "no")
                        .build())
                .retrieve()
                .bodyToMono(WeatherResponse.class)
                .onErrorResume(e -> Mono.error(new ExternalApiException(
                        "Erro ao buscar dados meteorológicos: " + e.getMessage(),
                        "WeatherAPI")))
                .block();
    }

    public WeatherResponse getForecast(String location, int days) {
        return weatherApiClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host("api.weatherapi.com")
                        .path("/v1/forecast.json")
                        .queryParam("key", appConfig.getWeatherApiKey())
                        .queryParam("q", location)
                        .queryParam("days", days)
                        .queryParam("aqi", "no")
                        .queryParam("alerts", "no")
                        .build())
                .retrieve()
                .bodyToMono(WeatherResponse.class)
                .onErrorResume(e -> Mono.error(new ExternalApiException(
                        "Erro ao buscar previsão meteorológica: " + e.getMessage(),
                        "WeatherAPI")))
                .block();
    }
}
package com.example.travelassistant.model.response;

import lombok.Data;
import java.util.List;

@Data
public class BaggageRecommendationResponse {
    private String destination;
    private String travelPeriod;
    private WeatherSummary weatherSummary;
    private List<ClothingItem> essentialClothing;
    private List<String> accessories;
    private List<String> toiletries;
    private List<String> electronics;
    private List<String> documents;
    private String specialRecommendations;
    private String packingTips;

    @Data
    public static class WeatherSummary {
        private String description;
        private double averageTemperature;
        private double minTemperature;
        private double maxTemperature;
        private String precipitation;
        private String humidity;
        private String wind;
    }

    @Data
    public static class ClothingItem {
        private String type;
        private int quantity;
        private String description;
    }
}
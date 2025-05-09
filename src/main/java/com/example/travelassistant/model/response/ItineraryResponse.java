package com.example.travelassistant.model.response;

import lombok.Data;
import java.util.List;

@Data
public class ItineraryResponse {
    private String destination;
    private String travelPeriod;
    private WeatherSummary weatherSummary;
    private List<DayPlan> dayPlans;
    private List<String> generalTips;
    private String localCuisineRecommendations;
    private String transportationTips;

    @Data
    public static class WeatherSummary {
        private String description;
        private List<DailyWeather> dailyWeather;
    }

    @Data
    public static class DailyWeather {
        private String date;
        private String condition;
        private double minTemp;
        private double maxTemp;
        private String rainfall;
    }

    @Data
    public static class DayPlan {
        private String date;
        private String weatherDescription;
        private List<Activity> morningActivities;
        private List<Activity> afternoonActivities;
        private List<Activity> eveningActivities;
        private String weatherBasedRecommendation;
    }

    @Data
    public static class Activity {
        private String name;
        private String description;
        private String location;
        private String indoorOutdoor; // "indoor", "outdoor", or "both"
        private String weatherConsideration;
    }
}
package com.example.travelassistant.model.response;

import lombok.Data;
import java.util.List;

@Data
public class WeatherResponse {
    private Location location;
    private Current current;
    private Forecast forecast;

    @Data
    public static class Location {
        private String name;
        private String region;
        private String country;
        private double lat;
        private double lon;
        private String tzId;
        private long localtimeEpoch;
        private String localtime;
    }

    @Data
    public static class Current {
        private long lastUpdatedEpoch;
        private String lastUpdated;
        private double tempC;
        private double tempF;
        private int isDay;
        private Condition condition;
        private double windMph;
        private double windKph;
        private int windDegree;
        private String windDir;
        private double pressureMb;
        private double pressureIn;
        private double precipMm;
        private double precipIn;
        private int humidity;
        private int cloud;
        private double feelslikeC;
        private double feelslikeF;
        private double visKm;
        private double visMiles;
        private double uv;
        private double gustMph;
        private double gustKph;
    }

    @Data
    public static class Condition {
        private String text;
        private String icon;
        private int code;
    }

    @Data
    public static class Forecast {
        private List<ForecastDay> forecastday;
    }

    @Data
    public static class ForecastDay {
        private String date;
        private long dateEpoch;
        private Day day;
        private Astro astro;
        private List<Hour> hour;
    }

    @Data
    public static class Day {
        private double maxtempC;
        private double maxtempF;
        private double mintempC;
        private double mintempF;
        private double avgtempC;
        private double avgtempF;
        private double maxwindMph;
        private double maxwindKph;
        private double totalprecipMm;
        private double totalprecipIn;
        private double totalsnowCm;
        private double avgvisKm;
        private double avgvisMiles;
        private double avghumidity;
        private int dailyWillItRain;
        private int dailyChanceOfRain;
        private int dailyWillItSnow;
        private int dailyChanceOfSnow;
        private Condition condition;
        private double uv;
    }

    @Data
    public static class Astro {
        private String sunrise;
        private String sunset;
        private String moonrise;
        private String moonset;
        private String moonPhase;
        private String moonIllumination;
        private int isMoonUp;
        private int isSunUp;
    }

    @Data
    public static class Hour {
        private long timeEpoch;
        private String time;
        private double tempC;
        private double tempF;
        private int isDay;
        private Condition condition;
        private double windMph;
        private double windKph;
        private int windDegree;
        private String windDir;
        private double pressureMb;
        private double pressureIn;
        private double precipMm;
        private double precipIn;
        private int humidity;
        private int cloud;
        private double feelslikeC;
        private double feelslikeF;
        private double windchillC;
        private double windchillF;
        private double heatindexC;
        private double heatindexF;
        private double dewpointC;
        private double dewpointF;
        private int willItRain;
        private int chanceOfRain;
        private int willItSnow;
        private int chanceOfSnow;
        private double visKm;
        private double visMiles;
        private double gustMph;
        private double gustKph;
        private double uv;
    }
}
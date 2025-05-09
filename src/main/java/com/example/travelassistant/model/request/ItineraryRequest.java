package com.example.travelassistant.model.request;

import lombok.Data;

@Data
public class ItineraryRequest {
    private String city;
    private String country;
    private String startDate;
    private String endDate;
    private String interests; // museums, nature, food, etc.
    private Integer budget; // 1-5 scale (1: low, 5: high)
    private String travelStyle; // relaxed, intensive, cultural, etc.
}

package com.example.travelassistant.model.request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class TravelInfoRequest {
    private String city;
    private String country;
    private LocalDate startDate;
    private LocalDate endDate;
    private String travelPurpose; // business, leisure, adventure, etc.
    private String userPreferences; // preferences for packing, e.g., "pack light", "formal attire needed"
}
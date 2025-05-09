package com.example.travelassistant.model.storage;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TravelQuery {
    private String id;
    private String destination;
    private String startDate;
    private String endDate;
    private String requestType; // "baggage" or "itinerary"
    private String requestJson;
    private String responseJson;
    private LocalDateTime timestamp;
}

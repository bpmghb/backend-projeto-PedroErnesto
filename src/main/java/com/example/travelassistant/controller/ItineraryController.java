package com.example.travelassistant.controller;

import com.example.travelassistant.model.request.ItineraryRequest;
import com.example.travelassistant.model.response.ItineraryResponse;
import com.example.travelassistant.model.storage.QueryRepository;
import com.example.travelassistant.model.storage.TravelQuery;
import com.example.travelassistant.service.ItineraryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/roteiro")
public class ItineraryController {

    private final ItineraryService itineraryService;
    private final QueryRepository queryRepository;

    @Autowired
    public ItineraryController(ItineraryService itineraryService, QueryRepository queryRepository) {
        this.itineraryService = itineraryService;
        this.queryRepository = queryRepository;
    }

    @PostMapping
    public ResponseEntity<ItineraryResponse> generateItinerary(@RequestBody ItineraryRequest request) {
        ItineraryResponse response = itineraryService.generateItinerary(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/destinos")
    public ResponseEntity<List<String>> getDestinations() {
        List<String> destinations = queryRepository.findByRequestType("itinerary").stream()
                .map(TravelQuery::getDestination)
                .distinct()
                .toList();

        return ResponseEntity.ok(destinations);
    }

    @GetMapping("/historico")
    public ResponseEntity<List<TravelQuery>> getHistory() {
        List<TravelQuery> queries = queryRepository.findByRequestType("itinerary");
        return ResponseEntity.ok(queries);
    }
}

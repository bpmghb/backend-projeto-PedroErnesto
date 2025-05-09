package com.example.travelassistant.controller;

import com.example.travelassistant.model.request.TravelInfoRequest;
import com.example.travelassistant.model.response.BaggageRecommendationResponse;
import com.example.travelassistant.model.storage.QueryRepository;
import com.example.travelassistant.model.storage.TravelQuery;
import com.example.travelassistant.service.BaggageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bagagem")
public class BaggageController {

    private final BaggageService baggageService;
    private final QueryRepository queryRepository;

    @Autowired
    public BaggageController(BaggageService baggageService, QueryRepository queryRepository) {
        this.baggageService = baggageService;
        this.queryRepository = queryRepository;
    }

    @PostMapping
    public ResponseEntity<BaggageRecommendationResponse> getBaggageRecommendation(@RequestBody TravelInfoRequest request) {
        BaggageRecommendationResponse response = baggageService.getBaggageRecommendation(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/destinos")
    public ResponseEntity<List<String>> getDestinations() {
        List<String> destinations = queryRepository.findByRequestType("baggage").stream()
                .map(TravelQuery::getDestination)
                .distinct()
                .toList();

        return ResponseEntity.ok(destinations);
    }

    @GetMapping("/historico")
    public ResponseEntity<List<TravelQuery>> getHistory() {
        List<TravelQuery> queries = queryRepository.findByRequestType("baggage");
        return ResponseEntity.ok(queries);
    }
}
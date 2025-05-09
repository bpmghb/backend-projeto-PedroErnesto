package com.example.travelassistant.model.storage;

import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class QueryRepository {
    private final List<TravelQuery> queries = new ArrayList<>();

    public TravelQuery save(TravelQuery query) {
        if (query.getId() == null) {
            query.setId(UUID.randomUUID().toString());
        }
        queries.add(query);
        return query;
    }

    public List<TravelQuery> findAll() {
        return new ArrayList<>(queries);
    }

    public List<TravelQuery> findByDestination(String destination) {
        return queries.stream()
                .filter(q -> q.getDestination().equalsIgnoreCase(destination))
                .collect(Collectors.toList());
    }

    public List<TravelQuery> findByRequestType(String requestType) {
        return queries.stream()
                .filter(q -> q.getRequestType().equalsIgnoreCase(requestType))
                .collect(Collectors.toList());
    }
}

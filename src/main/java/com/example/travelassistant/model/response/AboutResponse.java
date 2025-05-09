package com.example.travelassistant.model.response;

import lombok.Data;
import java.util.List;

@Data
public class AboutResponse {
    private String name;
    private String description;
    private String version;
    private String organization;
    private String contactEmail;
    private List<Endpoint> endpoints;

    @Data
    public static class Endpoint {
        private String path;
        private String method;
        private String description;
    }
}
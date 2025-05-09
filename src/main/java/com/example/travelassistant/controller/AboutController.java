package com.example.travelassistant.controller;

import com.example.travelassistant.model.response.AboutResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/sobre")
public class AboutController {

    @GetMapping
    public ResponseEntity<AboutResponse> getAboutInfo() {
        AboutResponse aboutResponse = new AboutResponse();
        aboutResponse.setName("Travel Assistant API");
        aboutResponse.setDescription("API para assistente de viagens com recomendação de bagagem e roteiros baseados no clima");
        aboutResponse.setVersion("1.0.0");
        aboutResponse.setOrganization("Aluno: Pedro Ernesto");
        aboutResponse.setContactEmail("pedrobrernesto@hotmail.com");

        List<AboutResponse.Endpoint> endpoints = List.of(
                createEndpoint("/bagagem", "POST", "Gerar recomendação de bagagem com base no clima"),
                createEndpoint("/bagagem/destinos", "GET", "Listar destinos já consultados para bagagem"),
                createEndpoint("/bagagem/historico", "GET", "Listar histórico de consultas de bagagem"),
                createEndpoint("/roteiro", "POST", "Gerar roteiro de viagem com base no clima"),
                createEndpoint("/roteiro/destinos", "GET", "Listar destinos já consultados para roteiros"),
                createEndpoint("/roteiro/historico", "GET", "Listar histórico de consultas de roteiros"),
                createEndpoint("/sobre", "GET", "Informações sobre a API")
        );

        aboutResponse.setEndpoints(endpoints);

        return ResponseEntity.ok(aboutResponse);
    }

    private AboutResponse.Endpoint createEndpoint(String path, String method, String description) {
        AboutResponse.Endpoint endpoint = new AboutResponse.Endpoint();
        endpoint.setPath(path);
        endpoint.setMethod(method);
        endpoint.setDescription(description);
        return endpoint;
    }
}
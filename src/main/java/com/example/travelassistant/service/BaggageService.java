package com.example.travelassistant.service;

import com.example.travelassistant.model.request.TravelInfoRequest;
import com.example.travelassistant.model.response.BaggageRecommendationResponse;
import com.example.travelassistant.model.response.WeatherResponse;
import com.example.travelassistant.model.storage.QueryRepository;
import com.example.travelassistant.model.storage.TravelQuery;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
public class BaggageService {

    private final WeatherService weatherService;
    private final GeminiAIService geminiAIService;
    private final QueryRepository queryRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    public BaggageService(
            WeatherService weatherService,
            GeminiAIService geminiAIService,
            QueryRepository queryRepository,
            ObjectMapper objectMapper) {
        this.weatherService = weatherService;
        this.geminiAIService = geminiAIService;
        this.queryRepository = queryRepository;
        this.objectMapper = objectMapper;
    }

    public BaggageRecommendationResponse getBaggageRecommendation(TravelInfoRequest request) {
        try {
            // Get weather forecast for the destination
            String location = request.getCity() + "," + request.getCountry();
            long daysBetween = ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate()) + 1;
            int days = (int) Math.min(daysBetween, 14); // API has a limit of 14 days

            WeatherResponse weatherResponse = weatherService.getForecast(location, days);

            // Build prompt for Gemini AI
            String prompt = buildBaggagePrompt(request, weatherResponse);

            // Get recommendation from Gemini AI
            String geminiResponse = geminiAIService.generateContent(prompt);

            // Parse Gemini response to structured format
            BaggageRecommendationResponse response;

            if ("FALLBACK_MODE".equals(geminiResponse)) {
                // Se a API Gemini falhou, use o modo fallback
                System.out.println("Usando modo fallback para recomendação de bagagem");
                response = geminiAIService.generateFallbackBaggageRecommendation(request, weatherResponse);
            } else {
                // Parse da resposta da API Gemini
                response = parseGeminiResponse(geminiResponse, request, weatherResponse);
            }

            // Save query to repository
            saveQuery(request, response, "bagagem");

            return response;

        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar recomendação de bagagem: " + e.getMessage(), e);
        }
    }

    private String buildBaggagePrompt(TravelInfoRequest request, WeatherResponse weatherResponse) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("Você é um assistente de viagem especializado em recomendações de bagagem baseadas no clima. ");
        prompt.append("Por favor, gere uma recomendação de bagagem detalhada para uma viagem com as seguintes informações:\n\n");

        prompt.append("Destino: ").append(request.getCity()).append(", ").append(request.getCountry()).append("\n");
        prompt.append("Período: ").append(request.getStartDate()).append(" a ").append(request.getEndDate()).append("\n");
        prompt.append("Propósito da viagem: ").append(request.getTravelPurpose()).append("\n");
        prompt.append("Preferências do usuário: ").append(request.getUserPreferences()).append("\n\n");

        prompt.append("Dados meteorológicos para o período:\n");
        if (weatherResponse.getForecast() != null && weatherResponse.getForecast().getForecastday() != null) {
            for (WeatherResponse.ForecastDay day : weatherResponse.getForecast().getForecastday()) {
                prompt.append("- ").append(day.getDate()).append(": ");
                prompt.append("Temperatura mín: ").append(day.getDay().getMintempC()).append("°C, ");
                prompt.append("máx: ").append(day.getDay().getMaxtempC()).append("°C. ");
                prompt.append("Condição: ").append(day.getDay().getCondition().getText()).append(". ");
                prompt.append("Chance de chuva: ").append(day.getDay().getDailyChanceOfRain()).append("%\n");
            }
        }

        prompt.append("\nPor favor, forneça uma recomendação de bagagem detalhada no formato JSON com os seguintes campos:\n");
        prompt.append("1. destination (destino)\n");
        prompt.append("2. travelPeriod (período da viagem)\n");
        prompt.append("3. weatherSummary - com description, averageTemperature, minTemperature, maxTemperature, precipitation, humidity, wind\n");
        prompt.append("4. essentialClothing - lista de itens com type, quantity e description\n");
        prompt.append("5. accessories - lista de acessórios recomendados\n");
        prompt.append("6. toiletries - lista de itens de higiene pessoal\n");
        prompt.append("7. electronics - lista de eletrônicos recomendados\n");
        prompt.append("8. documents - lista de documentos necessários\n");
        prompt.append("9. specialRecommendations - recomendações específicas para o destino e clima\n");
        prompt.append("10. packingTips - dicas gerais para fazer a mala\n\n");

        prompt.append("Importante: retorne SOMENTE o JSON, sem explicações adicionais. O JSON deve estar bem formatado e válido.");

        return prompt.toString();
    }

    private BaggageRecommendationResponse parseGeminiResponse(String geminiResponse, TravelInfoRequest request, WeatherResponse weatherResponse) {
        try {
            // Tentativa de extrair o JSON da resposta
            String jsonContent = extractJsonFromString(geminiResponse);

            // Parse do JSON para o objeto de resposta
            return objectMapper.readValue(jsonContent, BaggageRecommendationResponse.class);

        } catch (Exception e) {
            // Fallback: criar uma resposta manual caso o parsing falhe
            System.out.println("Erro ao fazer parsing da resposta do Gemini: " + e.getMessage());
            return geminiAIService.generateFallbackBaggageRecommendation(request, weatherResponse);
        }
    }

    private String extractJsonFromString(String text) {
        // Remover qualquer markup de código, se presente
        String cleanText = text;
        if (text.contains("```json")) {
            cleanText = text.substring(text.indexOf("```json") + 7);
            if (cleanText.contains("```")) {
                cleanText = cleanText.substring(0, cleanText.lastIndexOf("```"));
            }
        }

        // Encontrar onde começa e termina o JSON na resposta limpa
        int startIdx = cleanText.indexOf('{');
        int endIdx = cleanText.lastIndexOf('}') + 1;

        if (startIdx >= 0 && endIdx > startIdx) {
            return cleanText.substring(startIdx, endIdx);
        }

        throw new IllegalArgumentException("Não foi possível extrair JSON válido da resposta");
    }

    private void saveQuery(TravelInfoRequest request, BaggageRecommendationResponse response, String requestType) {
        try {
            TravelQuery query = new TravelQuery();
            query.setDestination(request.getCity() + ", " + request.getCountry());
            query.setStartDate(request.getStartDate().format(DateTimeFormatter.ISO_DATE));
            query.setEndDate(request.getEndDate().format(DateTimeFormatter.ISO_DATE));
            query.setRequestType(requestType);
            query.setRequestJson(objectMapper.writeValueAsString(request));
            query.setResponseJson(objectMapper.writeValueAsString(response));
            query.setTimestamp(LocalDateTime.now());

            queryRepository.save(query);
        } catch (Exception e) {
            // Log error but don't fail the request
            System.err.println("Erro ao salvar consulta: " + e.getMessage());
        }
    }
}
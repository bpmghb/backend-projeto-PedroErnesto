package com.example.travelassistant.service;

import com.example.travelassistant.config.AppConfig;
import com.example.travelassistant.exception.ExternalApiException;
import com.example.travelassistant.model.request.TravelInfoRequest;
import com.example.travelassistant.model.response.BaggageRecommendationResponse;
import com.example.travelassistant.model.response.WeatherResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Service
public class GeminiAIService {

    private final WebClient geminiApiClient;
    private final AppConfig appConfig;
    private final ObjectMapper objectMapper;

    @Autowired
    public GeminiAIService(WebClient geminiApiClient, AppConfig appConfig) {
        this.geminiApiClient = geminiApiClient;
        this.appConfig = appConfig;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Gera conteúdo usando a API Gemini ou retorna uma mensagem de fallback em caso de falha.
     */
    public String generateContent(String prompt) {
        try {
            // Tentativa com o modelo gemini-2.0-flash (versão gratuita)
            try {
                String response = callGeminiApi("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent", prompt);
                if (response != null) {
                    return response;
                }
            } catch (Exception e) {
                System.out.println("Tentativa com gemini-2.0-flash falhou: " + e.getMessage());
            }

            // Se chegou aqui, a tentativa falhou
            throw new ExternalApiException("Falha ao chamar API Gemini", "Gemini AI");
        } catch (Exception e) {
            // Em caso de falha, retorna uma mensagem indicando o uso do modo fallback
            System.out.println("Usando modo fallback para geração de conteúdo: " + e.getMessage());
            return "FALLBACK_MODE";
        }
    }

    /**
     * Método auxiliar para chamar a API Gemini com uma URL específica.
     */
    private String callGeminiApi(String url, String prompt) {
        try {
            ObjectNode requestBody = objectMapper.createObjectNode();

            ArrayNode contents = objectMapper.createArrayNode();
            ObjectNode content = objectMapper.createObjectNode();

            ArrayNode parts = objectMapper.createArrayNode();
            ObjectNode part = objectMapper.createObjectNode();
            part.put("text", prompt);
            parts.add(part);

            content.set("parts", parts);
            contents.add(content);

            requestBody.set("contents", contents);

            String fullUrl = url + "?key=" + appConfig.getGeminiApiKey();

            System.out.println("Chamando API Gemini: " + url);
            System.out.println("Payload: " + requestBody.toString());

            JsonNode response = geminiApiClient
                    .post()
                    .uri(fullUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(requestBody.toString()))
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            System.out.println("Resposta da API: " + response);

            if (response != null &&
                    response.has("candidates") &&
                    response.get("candidates").size() > 0 &&
                    response.get("candidates").get(0).has("content") &&
                    response.get("candidates").get(0).get("content").has("parts") &&
                    response.get("candidates").get(0).get("content").get("parts").size() > 0 &&
                    response.get("candidates").get(0).get("content").get("parts").get(0).has("text")) {

                return response.get("candidates").get(0).get("content").get("parts").get(0).get("text").asText();
            }

            return null;
        } catch (Exception e) {
            throw new RuntimeException("Erro na chamada para " + url + ": " + e.getMessage(), e);
        }
    }

    /**
     * Gera uma recomendação de bagagem simplificada quando a API Gemini falha.
     */
    public BaggageRecommendationResponse generateFallbackBaggageRecommendation(TravelInfoRequest request, WeatherResponse weatherResponse) {
        BaggageRecommendationResponse response = new BaggageRecommendationResponse();

        // Dados básicos
        response.setDestination(request.getCity() + ", " + request.getCountry());
        response.setTravelPeriod(request.getStartDate() + " a " + request.getEndDate());

        // Resumo do clima
        BaggageRecommendationResponse.WeatherSummary summary = new BaggageRecommendationResponse.WeatherSummary();

        if (weatherResponse.getForecast() != null && weatherResponse.getForecast().getForecastday() != null && !weatherResponse.getForecast().getForecastday().isEmpty()) {
            double totalTemp = 0;
            double minTemp = Double.MAX_VALUE;
            double maxTemp = Double.MIN_VALUE;

            for (WeatherResponse.ForecastDay day : weatherResponse.getForecast().getForecastday()) {
                totalTemp += day.getDay().getAvgtempC();
                minTemp = Math.min(minTemp, day.getDay().getMintempC());
                maxTemp = Math.max(maxTemp, day.getDay().getMaxtempC());
            }

            double avgTemp = totalTemp / weatherResponse.getForecast().getForecastday().size();

            String climaDesc = "Clima variável";
            if (avgTemp > 25) {
                climaDesc = "Clima quente";
            } else if (avgTemp > 15) {
                climaDesc = "Clima ameno";
            } else if (avgTemp > 5) {
                climaDesc = "Clima frio";
            } else {
                climaDesc = "Clima muito frio";
            }

            summary.setDescription(climaDesc + " para o período da viagem");
            summary.setAverageTemperature(avgTemp);
            summary.setMinTemperature(minTemp);
            summary.setMaxTemperature(maxTemp);
            summary.setPrecipitation("Verifique a previsão diária para detalhes");
            summary.setHumidity("Média para o período");
            summary.setWind("Verifique a previsão diária para detalhes");
        } else {
            summary.setDescription("Dados climáticos não disponíveis");
            summary.setAverageTemperature(20); // Valores padrão
            summary.setMinTemperature(15);
            summary.setMaxTemperature(25);
            summary.setPrecipitation("Informação não disponível");
            summary.setHumidity("Informação não disponível");
            summary.setWind("Informação não disponível");
        }

        response.setWeatherSummary(summary);

        // Itens essenciais baseados no clima
        List<BaggageRecommendationResponse.ClothingItem> essentialClothing = new ArrayList<>();

        // Temperatura média
        double avgTemp = summary.getAverageTemperature();

        // Roupas baseadas na temperatura média
        if (avgTemp > 25) { // Quente
            essentialClothing.add(createClothingItem("Camisetas leves", 7, "Uma para cada dia"));
            essentialClothing.add(createClothingItem("Shorts/Bermudas", 4, "Para dias quentes"));
            essentialClothing.add(createClothingItem("Roupas de banho", 2, "Para piscina ou praia"));
        } else if (avgTemp > 15) { // Ameno
            essentialClothing.add(createClothingItem("Camisetas", 5, "Para diversas ocasiões"));
            essentialClothing.add(createClothingItem("Calças leves", 3, "Jeans ou calças confortáveis"));
            essentialClothing.add(createClothingItem("Camisas de manga longa", 2, "Para noites mais frescas"));
        } else { // Frio
            essentialClothing.add(createClothingItem("Blusas de lã", 3, "Para se manter aquecido"));
            essentialClothing.add(createClothingItem("Calças", 3, "Jeans ou calças quentes"));
            essentialClothing.add(createClothingItem("Casaco", 1, "Casaco quente para temperaturas baixas"));
            essentialClothing.add(createClothingItem("Cachecol e luvas", 1, "Para proteção extra"));
        }

        // Itens básicos independentes do clima
        essentialClothing.add(createClothingItem("Roupa íntima", 7, "Uma para cada dia da semana"));
        essentialClothing.add(createClothingItem("Meias", 7, "Um par para cada dia"));

        response.setEssentialClothing(essentialClothing);

        // Outros itens básicos
        List<String> accessories = new ArrayList<>();
        if (avgTemp > 20) {
            accessories.add("Óculos de sol");
            accessories.add("Chapéu/boné");
            accessories.add("Protetor solar");
        } else {
            accessories.add("Cachecol");
            accessories.add("Gorro");
            accessories.add("Luvas");
        }
        accessories.add("Relógio");
        accessories.add("Bolsa/mochila para passeios");

        response.setAccessories(accessories);

        response.setToiletries(List.of(
                "Escova de dentes",
                "Pasta de dente",
                "Sabonete",
                "Shampoo",
                "Desodorante",
                "Hidratante"
        ));

        response.setElectronics(List.of(
                "Celular",
                "Carregador",
                "Adaptador de tomada",
                "Power bank",
                "Câmera (opcional)"
        ));

        response.setDocuments(List.of(
                "Passaporte",
                "Carteira de identidade",
                "Cartões",
                "Seguro viagem",
                "Vouchers de reserva"
        ));

        // Recomendações específicas baseadas no clima
        String specialRecommendations;
        if (avgTemp > 25) {
            specialRecommendations = "Prepare-se para o calor com roupas leves e de cores claras. Leve protetor solar e mantenha-se hidratado. Considere um chapéu para proteção contra o sol.";
        } else if (avgTemp > 15) {
            specialRecommendations = "O clima será ameno, então leve roupas versáteis que possam ser usadas em camadas. Manhãs e noites podem ser mais frescas.";
        } else {
            specialRecommendations = "Prepare-se para o frio com roupas quentes e em camadas. Um bom casaco, cachecol e luvas são essenciais. Meias térmicas também são recomendadas.";
        }
        response.setSpecialRecommendations(specialRecommendations);

        // Dicas gerais
        response.setPackingTips(
                "Faça uma lista de verificação antes de sair. " +
                        "Separe líquidos em embalagens pequenas para transporte em avião. " +
                        "Enrole as roupas para economizar espaço. " +
                        "Coloque itens pesados na parte inferior da mala."
        );

        return response;
    }

    private BaggageRecommendationResponse.ClothingItem createClothingItem(String type, int quantity, String description) {
        BaggageRecommendationResponse.ClothingItem item = new BaggageRecommendationResponse.ClothingItem();
        item.setType(type);
        item.setQuantity(quantity);
        item.setDescription(description);
        return item;
    }
}
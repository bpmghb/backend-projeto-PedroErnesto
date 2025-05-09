package com.example.travelassistant.service;

import com.example.travelassistant.model.request.ItineraryRequest;
import com.example.travelassistant.model.response.ItineraryResponse;
import com.example.travelassistant.model.response.WeatherResponse;
import com.example.travelassistant.model.storage.QueryRepository;
import com.example.travelassistant.model.storage.TravelQuery;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
public class ItineraryService {

    private final WeatherService weatherService;
    private final GeminiAIService geminiAIService;
    private final QueryRepository queryRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    public ItineraryService(
            WeatherService weatherService,
            GeminiAIService geminiAIService,
            QueryRepository queryRepository) {
        this.weatherService = weatherService;
        this.geminiAIService = geminiAIService;
        this.queryRepository = queryRepository;
        this.objectMapper = new ObjectMapper();
    }

    public ItineraryResponse generateItinerary(ItineraryRequest request) {
        try {
            // Get weather forecast for the destination
            String location = request.getCity() + "," + request.getCountry();
            LocalDate startDate = LocalDate.parse(request.getStartDate());
            LocalDate endDate = LocalDate.parse(request.getEndDate());
            long daysBetween = ChronoUnit.DAYS.between(startDate, endDate) + 1;
            int days = (int) Math.min(daysBetween, 14); // API has a limit of 14 days

            WeatherResponse weatherResponse = weatherService.getForecast(location, days);

            // Build prompt for Gemini AI
            String prompt = buildItineraryPrompt(request, weatherResponse);

            // Get recommendation from Gemini AI
            String geminiResponse = geminiAIService.generateContent(prompt);

            // Parse Gemini response to structured format
            ItineraryResponse response = parseGeminiResponse(geminiResponse, request, weatherResponse);

            // Save query to repository
            saveQuery(request, response);

            return response;

        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar roteiro de viagem: " + e.getMessage(), e);
        }
    }

    private String buildItineraryPrompt(ItineraryRequest request, WeatherResponse weatherResponse) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("Você é um assistente de viagem especializado em criar roteiros personalizados baseados no clima. ");
        prompt.append("Por favor, gere um roteiro detalhado para uma viagem com as seguintes informações:\n\n");

        prompt.append("Destino: ").append(request.getCity()).append(", ").append(request.getCountry()).append("\n");
        prompt.append("Período: ").append(request.getStartDate()).append(" a ").append(request.getEndDate()).append("\n");
        prompt.append("Interesses: ").append(request.getInterests()).append("\n");
        prompt.append("Orçamento (1-5): ").append(request.getBudget()).append("\n");
        prompt.append("Estilo de viagem: ").append(request.getTravelStyle()).append("\n\n");

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

        prompt.append("\nPor favor, forneça um roteiro detalhado no formato JSON com os seguintes campos:\n");
        prompt.append("1. destination (destino)\n");
        prompt.append("2. travelPeriod (período da viagem)\n");
        prompt.append("3. weatherSummary - com description e dailyWeather (lista de previsões diárias)\n");
        prompt.append("4. dayPlans - lista de planos diários, cada um com:\n");
        prompt.append("   - date (data)\n");
        prompt.append("   - weatherDescription (descrição do clima do dia)\n");
        prompt.append("   - morningActivities (lista de atividades matutinas)\n");
        prompt.append("   - afternoonActivities (lista de atividades vespertinas)\n");
        prompt.append("   - eveningActivities (lista de atividades noturnas)\n");
        prompt.append("   - weatherBasedRecommendation (recomendação baseada no clima do dia)\n");
        prompt.append("5. generalTips - lista de dicas gerais para o destino\n");
        prompt.append("6. localCuisineRecommendations - recomendações gastronômicas locais\n");
        prompt.append("7. transportationTips - dicas de transporte local\n\n");

        prompt.append("Cada atividade deve conter:\n");
        prompt.append("- name (nome da atividade)\n");
        prompt.append("- description (descrição breve)\n");
        prompt.append("- location (localização)\n");
        prompt.append("- indoorOutdoor (\"indoor\", \"outdoor\" ou \"both\")\n");
        prompt.append("- weatherConsideration (consideração climática para a atividade)\n\n");

        prompt.append("Importante: retorne SOMENTE o JSON, sem explicações adicionais. O JSON deve estar bem formatado e válido.");

        return prompt.toString();
    }

    private ItineraryResponse parseGeminiResponse(String geminiResponse, ItineraryRequest request, WeatherResponse weatherResponse) {
        try {
            // Tentativa de extrair o JSON da resposta
            String jsonContent = extractJsonFromString(geminiResponse);

            // Parse do JSON para o objeto de resposta
            return objectMapper.readValue(jsonContent, ItineraryResponse.class);

        } catch (Exception e) {
            // Fallback: criar uma resposta manual caso o parsing falhe
            return createFallbackResponse(request, weatherResponse);
        }
    }

    private String extractJsonFromString(String text) {
        // Encontrar onde começa e termina o JSON na resposta
        int startIdx = text.indexOf('{');
        int endIdx = text.lastIndexOf('}') + 1;

        if (startIdx >= 0 && endIdx > startIdx) {
            return text.substring(startIdx, endIdx);
        }

        throw new IllegalArgumentException("Não foi possível extrair JSON válido da resposta");
    }

    private ItineraryResponse createFallbackResponse(ItineraryRequest request, WeatherResponse weatherResponse) {
        ItineraryResponse response = new ItineraryResponse();

        // Dados básicos
        response.setDestination(request.getCity() + ", " + request.getCountry());
        response.setTravelPeriod(request.getStartDate() + " a " + request.getEndDate());

        // Resumo do clima
        ItineraryResponse.WeatherSummary summary = new ItineraryResponse.WeatherSummary();
        summary.setDescription("Previsão do tempo para o período da viagem");

        List<ItineraryResponse.DailyWeather> dailyWeather = new ArrayList<>();
        if (weatherResponse.getForecast() != null && weatherResponse.getForecast().getForecastday() != null) {
            for (WeatherResponse.ForecastDay day : weatherResponse.getForecast().getForecastday()) {
                ItineraryResponse.DailyWeather dayWeather = new ItineraryResponse.DailyWeather();
                dayWeather.setDate(day.getDate());
                dayWeather.setCondition(day.getDay().getCondition().getText());
                dayWeather.setMinTemp(day.getDay().getMintempC());
                dayWeather.setMaxTemp(day.getDay().getMaxtempC());
                dayWeather.setRainfall(day.getDay().getDailyChanceOfRain() + "%");

                dailyWeather.add(dayWeather);
            }
        }

        summary.setDailyWeather(dailyWeather);
        response.setWeatherSummary(summary);

        // Planos diários
        List<ItineraryResponse.DayPlan> dayPlans = new ArrayList<>();

        // Criar um plano básico para cada dia
        LocalDate startDate = LocalDate.parse(request.getStartDate());
        LocalDate endDate = LocalDate.parse(request.getEndDate());
        LocalDate currentDate = startDate;

        int dayIndex = 0;
        while (!currentDate.isAfter(endDate) && dayIndex < dailyWeather.size()) {
            ItineraryResponse.DayPlan dayPlan = new ItineraryResponse.DayPlan();
            dayPlan.setDate(currentDate.toString());
            dayPlan.setWeatherDescription("Previsão para o dia: " + dailyWeather.get(dayIndex).getCondition());

            // Atividades básicas
            List<ItineraryResponse.Activity> morningActivities = new ArrayList<>();
            morningActivities.add(createActivity(
                    "Café da manhã",
                    "Experimente a gastronomia local",
                    "Hotel ou café próximo",
                    "indoor",
                    "Atividade interna não afetada pelo clima"
            ));
            morningActivities.add(createActivity(
                    "Passeio cultural",
                    "Visite um ponto turístico popular",
                    "Centro da cidade",
                    "both",
                    "Verifique o clima antes de sair"
            ));

            List<ItineraryResponse.Activity> afternoonActivities = new ArrayList<>();
            afternoonActivities.add(createActivity(
                    "Almoço",
                    "Restaurante recomendado",
                    "Área turística",
                    "indoor",
                    "Atividade interna não afetada pelo clima"
            ));
            afternoonActivities.add(createActivity(
                    "Passeio ao ar livre",
                    "Explore parques ou atrações locais",
                    "Região central",
                    "outdoor",
                    "Considere alternativas internas em caso de chuva"
            ));

            List<ItineraryResponse.Activity> eveningActivities = new ArrayList<>();
            eveningActivities.add(createActivity(
                    "Jantar",
                    "Experimente a culinária local",
                    "Restaurante recomendado",
                    "indoor",
                    "Atividade interna não afetada pelo clima"
            ));
            eveningActivities.add(createActivity(
                    "Caminhada noturna",
                    "Aprecie as luzes da cidade",
                    "Centro histórico",
                    "outdoor",
                    "Verifique previsão de chuva antes de sair"
            ));

            dayPlan.setMorningActivities(morningActivities);
            dayPlan.setAfternoonActivities(afternoonActivities);
            dayPlan.setEveningActivities(eveningActivities);

            // Recomendação baseada no clima
            String weatherBasedRecommendation;
            if (dailyWeather.get(dayIndex).getCondition().toLowerCase().contains("chuva") ||
                    dailyWeather.get(dayIndex).getCondition().toLowerCase().contains("rain")) {
                weatherBasedRecommendation = "Dia com previsão de chuva. Considere atividades internas ou leve um guarda-chuva.";
            } else if (dailyWeather.get(dayIndex).getMaxTemp() > 30) {
                weatherBasedRecommendation = "Dia quente. Leve protetor solar, óculos de sol e mantenha-se hidratado.";
            } else if (dailyWeather.get(dayIndex).getMinTemp() < 10) {
                weatherBasedRecommendation = "Dia frio. Leve roupas adequadas e considere atividades internas.";
            } else {
                weatherBasedRecommendation = "Clima agradável para atividades ao ar livre.";
            }

            dayPlan.setWeatherBasedRecommendation(weatherBasedRecommendation);

            dayPlans.add(dayPlan);
            currentDate = currentDate.plusDays(1);
            dayIndex++;
        }

        response.setDayPlans(dayPlans);

        // Dicas gerais
        response.setGeneralTips(List.of(
                "Verifique a previsão do tempo diariamente",
                "Leve roupas adequadas para o clima local",
                "Tenha um plano alternativo para dias chuvosos",
                "Respeite os costumes locais"
        ));

        response.setLocalCuisineRecommendations(
                "Experimente os pratos típicos da região. Pesquise restaurantes bem avaliados próximos a cada atração.");

        response.setTransportationTips(
                "Verifique as opções de transporte público disponíveis. Considere aplicativos de transporte para maior conveniência.");

        return response;
    }

    private ItineraryResponse.Activity createActivity(
            String name, String description, String location, String indoorOutdoor, String weatherConsideration) {
        ItineraryResponse.Activity activity = new ItineraryResponse.Activity();
        activity.setName(name);
        activity.setDescription(description);
        activity.setLocation(location);
        activity.setIndoorOutdoor(indoorOutdoor);
        activity.setWeatherConsideration(weatherConsideration);
        return activity;
    }

    private void saveQuery(ItineraryRequest request, ItineraryResponse response) {
        try {
            TravelQuery query = new TravelQuery();
            query.setDestination(request.getCity() + ", " + request.getCountry());
            query.setStartDate(request.getStartDate());
            query.setEndDate(request.getEndDate());
            query.setRequestType("itinerary");
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
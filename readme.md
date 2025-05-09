# Travel Assistant API

API de assistente de viagens com recomendação de bagagem e roteiros baseados no clima, utilizando WeatherAPI e Gemini AI para fornecer sugestões personalizadas.

## Funcionalidades

- Consulta de previsão do tempo para destinos de viagem
- Recomendações de bagagem baseadas no clima previsto
- Geração de roteiros personalizados considerando o clima
- Armazenamento do histórico de consultas

## Tecnologias Utilizadas

- **Backend**: Java 21 com Spring Boot 3.4.5
- **APIs Externas**:
    - [WeatherAPI](https://www.weatherapi.com/) - Previsões meteorológicas
    - [Gemini AI](https://ai.google.dev/) - Geração de conteúdo com IA

## Configuração do Projeto

### Pré-requisitos

- Java 21+
- Maven
- Chaves de API para WeatherAPI e Gemini AI

### Variáveis de Ambiente

Configure as seguintes variáveis no arquivo `application.properties`:

```properties
server.port=8000
weather.api.key=sua_chave_weatherapi
weather.api.url=https://api.weatherapi.com/v1
gemini.api.key=sua_chave_gemini
gemini.api.url=https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent
```

### Instalação e Execução

1. Clone o repositório
2. Configure as chaves de API no arquivo `application.properties`
3. Execute o comando:

```bash
mvn spring-boot:run
```

O servidor estará disponível em: http://localhost:8000

## Estrutura do Projeto

```
src/main/java/com/example/travelassistant/
├── config/         # Configurações gerais e WebClient
├── controller/     # Endpoints da API
├── service/        # Lógica de negócio
├── model/          # Classes de modelo (request, response, storage)
└── exception/      # Tratamento de exceções
```

## Endpoints da API

### Bagagem

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/bagagem` | Gerar recomendação de bagagem com base no clima |
| GET | `/bagagem/destinos` | Listar destinos já consultados |
| GET | `/bagagem/historico` | Listar histórico de consultas |

### Roteiro

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/roteiro` | Gerar roteiro de viagem com base no clima |
| GET | `/roteiro/destinos` | Listar destinos já consultados |
| GET | `/roteiro/historico` | Listar histórico de consultas |

### Informações

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/sobre` | Informações sobre a API |

## Modelos de Dados

### Requisições

#### TravelInfoRequest (POST /bagagem)
```json
{
  "city": "Paris",
  "country": "França",
  "startDate": "2023-07-01",
  "endDate": "2023-07-07",
  "travelPurpose": "turismo",
  "userPreferences": "prefiro viajar leve"
}
```

#### ItineraryRequest (POST /roteiro)
```json
{
  "city": "Roma",
  "country": "Itália",
  "startDate": "2023-08-15",
  "endDate": "2023-08-20",
  "interests": "museus, história, gastronomia",
  "budget": 3,
  "travelStyle": "cultural"
}
```

### Respostas

#### BaggageRecommendationResponse (Resposta de /bagagem)
```json
{
  "destination": "Paris, França",
  "travelPeriod": "2023-07-01 a 2023-07-07",
  "weatherSummary": {
    "description": "Verão com temperaturas moderadas",
    "averageTemperature": 24.5,
    "minTemperature": 18.2,
    "maxTemperature": 29.8,
    "precipitation": "Chance moderada de chuvas rápidas",
    "humidity": "Média de 65%",
    "wind": "Ventos leves a moderados"
  },
  "essentialClothing": [
    {
      "type": "Camisetas leves",
      "quantity": 7,
      "description": "Uma para cada dia, preferencialmente de algodão"
    },
    {
      "type": "Calças",
      "quantity": 3,
      "description": "Jeans e calças leves para diferentes ocasiões"
    },
    {
      "type": "Roupa íntima",
      "quantity": 8,
      "description": "Uma para cada dia mais uma extra"
    }
  ],
  "accessories": [
    "Óculos de sol",
    "Chapéu ou boné",
    "Guarda-chuva compacto",
    "Lenço ou echarpe leve"
  ],
  "toiletries": [
    "Escova de dentes e pasta",
    "Sabonete",
    "Shampoo e condicionador",
    "Protetor solar"
  ],
  "electronics": [
    "Celular e carregador",
    "Adaptador de tomada europeu",
    "Câmera fotográfica",
    "Power bank"
  ],
  "documents": [
    "Passaporte",
    "Seguro viagem",
    "Cartões de crédito",
    "Cópias dos documentos"
  ],
  "specialRecommendations": "Paris pode ter chuvas repentinas no verão. Considere levar um guarda-chuva compacto e sapatos confortáveis para caminhar nas ruas de paralelepípedo.",
  "packingTips": "Muitos hotéis em Paris fornecem secador de cabelo, então você pode economizar espaço não levando um. Verifique se seu hotel oferece esse serviço."
}
```

#### ItineraryResponse (Resposta de /roteiro)
```json
{
  "destination": "Roma, Itália",
  "travelPeriod": "2023-08-15 a 2023-08-20",
  "weatherSummary": {
    "description": "Verão quente e ensolarado",
    "dailyWeather": [
      {
        "date": "2023-08-15",
        "condition": "Ensolarado",
        "minTemp": 22.4,
        "maxTemp": 33.1,
        "rainfall": "5%"
      },
      {
        "date": "2023-08-16",
        "condition": "Parcialmente nublado",
        "minTemp": 21.8,
        "maxTemp": 32.5,
        "rainfall": "10%"
      }
    ]
  },
  "dayPlans": [
    {
      "date": "2023-08-15",
      "weatherDescription": "Dia quente e ensolarado",
      "morningActivities": [
        {
          "name": "Visita ao Coliseu",
          "description": "Explorar o anfiteatro romano mais famoso do mundo",
          "location": "Piazza del Colosseo",
          "indoorOutdoor": "outdoor",
          "weatherConsideration": "Leve água, protetor solar e chapéu. Visite cedo para evitar o calor intenso."
        },
        {
          "name": "Fórum Romano",
          "description": "Ruínas do centro da antiga Roma",
          "location": "Via della Salara Vecchia",
          "indoorOutdoor": "outdoor",
          "weatherConsideration": "Área com pouca sombra, prepare-se para o calor."
        }
      ],
      "afternoonActivities": [
        {
          "name": "Almoço em Trastevere",
          "description": "Refeição em um bairro tradicional",
          "location": "Trastevere",
          "indoorOutdoor": "both",
          "weatherConsideration": "Escolha um restaurante com área externa sombreada."
        },
        {
          "name": "Museu do Vaticano",
          "description": "Coleção de arte e esculturas",
          "location": "Viale Vaticano",
          "indoorOutdoor": "indoor",
          "weatherConsideration": "Ótima opção para escapar do calor da tarde."
        }
      ],
      "eveningActivities": [
        {
          "name": "Jantar na Piazza Navona",
          "description": "Gastronomia italiana em praça histórica",
          "location": "Piazza Navona",
          "indoorOutdoor": "outdoor",
          "weatherConsideration": "Agradável no fim da tarde quando esfria."
        }
      ],
      "weatherBasedRecommendation": "Devido ao calor intenso, comece as atividades ao ar livre cedo pela manhã, reserve os museus para o período da tarde, e aproveite o ar livre novamente no início da noite."
    }
  ],
  "generalTips": [
    "Beba bastante água durante o dia",
    "As principais atrações ficam lotadas; compre ingressos com antecedência",
    "Muitos locais exigem roupas que cubram ombros e joelhos",
    "Tenha sempre alguns euros em dinheiro para pequenas compras"
  ],
  "localCuisineRecommendations": "Experimente a autêntica pasta carbonara, amatriciana e cacio e pepe em trattorias tradicionais. Para sobremesa, o gelato artesanal é imperdível, principalmente nas gelaterias longe das áreas turísticas.",
  "transportationTips": "Roma é facilmente explorável a pé no centro histórico. Para distâncias maiores, use o metrô (Metro) que é eficiente e econômico. Evite táxis em áreas congestionadas como o Vaticano ou Coliseu."
}
```

#### AboutResponse (Resposta de /sobre)
```json
{
  "name": "Travel Assistant API",
  "description": "API para assistente de viagens com recomendação de bagagem e roteiros baseados no clima",
  "version": "1.0.0",
  "organization": "Exemplo Org",
  "contactEmail": "contato@exemplo.com",
  "endpoints": [
    {
      "path": "/bagagem",
      "method": "POST",
      "description": "Gerar recomendação de bagagem com base no clima"
    },
    {
      "path": "/roteiro",
      "method": "POST",
      "description": "Gerar roteiro de viagem com base no clima"
    },
    {
      "path": "/sobre",
      "method": "GET",
      "description": "Informações sobre a API"
    }
  ]
}
```

## APIs Externas

### WeatherAPI

A WeatherAPI é utilizada para obter dados meteorológicos atuais e previsões para os destinos das viagens. A integração utiliza os seguintes endpoints:

- `/current.json` - Condições meteorológicas atuais
- `/forecast.json` - Previsão para até 14 dias

Documentação oficial: [WeatherAPI Documentation](https://www.weatherapi.com/docs/)

### Gemini AI

A API Gemini AI é utilizada para gerar conteúdo personalizado com base nos dados meteorológicos e nas preferências do usuário. A IA analisa as informações e cria:

- Recomendações de bagagem adaptadas ao clima
- Roteiros de viagem com atividades adequadas às condições meteorológicas

Documentação oficial: [Google AI for Developers](https://ai.google.dev/docs)

## Boas Práticas Implementadas

- **Separação em Camadas**: Arquitetura clara com separação entre controllers e services
- **Tratamento de Erros**: Sistema robusto para lidar com falhas nas APIs externas
- **Documentação**: Endpoints e modelos claramente documentados
- **Validação de Dados**: Verificação dos dados de entrada para evitar erros
- **Código Limpo**: Seguindo convenções de nomenclatura e organização do Spring

.
package weatherapp.service;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import tools.jackson.databind.JsonNode;
import weatherapp.model.Coordinates;

@Service
public class GeocodingService {

    private final WebClient webClient;

    public GeocodingService() {

        this.webClient = WebClient.builder()
                .baseUrl("https://nominatim.openstreetmap.org")
                .defaultHeader(
                        "User-Agent",
                        "WeatherInformationSystem/1.0 sareyeshwar@gmail.com"
                )
                .build();
    }

    public Coordinates geocode(String location) {

        JsonNode response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/search")
                        .queryParam("q", location)
                        .queryParam("format", "jsonv2")
                        .queryParam("limit", 1)
                        .build())
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

        if (response == null || response.isEmpty()) {
            throw new RuntimeException(
                    "Location not found: " + location
            );
        }

        JsonNode result = response.get(0);

        double latitude =
                Double.parseDouble(result.get("lat").asString());

        double longitude =
                Double.parseDouble(result.get("lon").asString());

        String displayName =
                result.get("display_name").asString();

        return new Coordinates(
                latitude,
                longitude,
                displayName
        );
    }
}
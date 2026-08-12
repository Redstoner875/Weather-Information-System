package weatherapp.service;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import tools.jackson.databind.JsonNode;
import weatherapp.model.Coordinates;

/**
 * Service for converting a location string into geographic coordinates (geocoding).
 * 
 * This service uses the free Nominatim OpenStreetMap API to perform geocoding.
 * It takes a user-entered location string like "Orlando, FL" and returns
 * the latitude, longitude, and full display name of the location.
 * 
 * The coordinates are then used by WeatherService to query the NWS weather API.
 */
@Service
public class GeocodingService {

    private final WebClient webClient;

    /**
     * Constructor that creates a WebClient configured for the Nominatim API.
     * Note: This creates a new WebClient instance (not using the @Bean from WebClientConfig).
     * Nominatim has different requirements than NWS, so it has its own configuration.
     */
    public GeocodingService() {

        this.webClient = WebClient.builder()
                .baseUrl("https://nominatim.openstreetmap.org")
                .defaultHeader(
                        "User-Agent",
                        "WeatherInformationSystem/1.0 sareyeshwar@gmail.com"
                )
                .build();
    }

    /**
     * Converts a location string into geographic coordinates.
     * 
     * Flow:
     * 1. User enters "Orlando, FL" in the web form
     * 2. This method sends a GET request to Nominatim's /search endpoint
     * 3. Nominatim returns JSON with matching locations
     * 4. We parse the first result and extract latitude, longitude, display_name
     * 5. Return a Coordinates object with these values
     * 
     * @param location A location string (e.g., "Orlando, FL", "New York", "40.7128, -74.0060")
     * @return A Coordinates object with latitude, longitude, and display name
     * @throws RuntimeException if location not found or API returns no results
     */
    public Coordinates geocode(String location) {

        // Make HTTP GET request to Nominatim's /search endpoint
        // The query parameters are:
        //   q: the search string (the location to find)
        //   format: "jsonv2" (requests JSON format response)
        //   limit: 1 (only return the top/best match)
        JsonNode response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/search")
                        .queryParam("q", location)
                        .queryParam("format", "jsonv2")
                        .queryParam("addressdetails", 1)
                        .queryParam("limit", 1)
                        .build())
                .retrieve()  // Execute the request and get the response
                .bodyToMono(JsonNode.class)  // Convert the response body to a JSON object
                .block();  // Block until the response arrives (synchronous operation)

        if (response == null || response.isEmpty()) {
            throw new RuntimeException(
                    "Location not found: " + location
            );
        }

        // Nominatim returns an array of results. Get the first (best) result.
        JsonNode result = response.get(0);

        // Extract latitude from JSON response and convert from String to double
        double latitude =
                Double.parseDouble(result.get("lat").asString());

        // Extract longitude from JSON response and convert from String to double
        double longitude =
                Double.parseDouble(result.get("lon").asString());

        // Extract the formatted display name (e.g., "Orlando, Orange County, Florida, United States")
        JsonNode address =
                result.path("address");

        String city = firstNonBlank(
                address,
                "city",
                "town",
                "village",
                "municipality",
                "hamlet"
        );

        String state =
                address.path("state").asString();

        String displayName;

        if (!city.isBlank() && !state.isBlank()) {

        displayName = city + ", " + state;

        } else {

        // Fall back to Nominatim's full display name
        displayName =
                result.path("display_name").asString();
        }

        return new Coordinates(
                latitude,
                longitude,
                displayName
        );
    }

    private String firstNonBlank(
        JsonNode address,
        String... fields) {

    for (String field : fields) {

        String value =
                address.path(field).asString();

        if (value != null && !value.isBlank()) {
            return value;
        }
    }

    return "";
}

}
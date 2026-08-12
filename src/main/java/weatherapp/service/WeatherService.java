package weatherapp.service;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import tools.jackson.databind.JsonNode;
import weatherapp.model.ForecastPeriod;

/**
 * Service for fetching weather forecast data from the National Weather Service (NWS) API.
 * 
 * The NWS API uses a two-step process:
 * 1. Call /points/{latitude},{longitude} to get metadata about the location,
 *    including the URL of the forecast endpoint for that location.
 * 2. Call the forecast URL to get the actual forecast data.
 * 
 * This design allows NWS to serve forecasts from different regional forecast offices.
 */
@Service
public class WeatherService {

    private final WebClient nwsWebClient;

    /**
     * Constructor for dependency injection.
     * Spring injects the nwsWebClient bean that was configured in WebClientConfig.
     * 
     * @param nwsWebClient The WebClient bean configured to call the NWS API
     */
    public WeatherService(WebClient nwsWebClient) {
        this.nwsWebClient = nwsWebClient;
    }

    /**
     * Step 1 of NWS forecast retrieval: Get the forecast URL for a location.
     * 
     * The NWS /points endpoint takes latitude and longitude and returns metadata,
     * including a URL we should use for the actual forecast. Different regions are
     * handled by different forecast offices, so this endpoint tells us which URL to use.
     * 
     * @param latitude The latitude of the location
     * @param longitude The longitude of the location
     * @return The URL to call for the forecast data
     * @throws RuntimeException if the API returns no response or forecast URL
     */
    public String getForecastUrl(double latitude, double longitude) {

        // NWS API works best with coordinates limited to 4 decimal places
        // Round the latitude and longitude to avoid overly precise coordinates
        // that might not match NWS's internal grid
        double lat = Math.round(latitude * 10000.0) / 10000.0;
        double lon = Math.round(longitude * 10000.0) / 10000.0;

        System.out.println("Latitude: " + lat);
        System.out.println("Longitude: " + lon);

        // Call the NWS /points endpoint
        // Example URL: https://api.weather.gov/points/28.5383,-81.3792
        // This returns JSON with properties including forecast URL
        JsonNode response = nwsWebClient.get()
                .uri("/points/" + lat + "," + lon)
                .retrieve()  // Execute the request
                .bodyToMono(JsonNode.class)  // Convert response body to JSON object
                .block();  // Wait for response synchronously

        if (response == null) {
            throw new RuntimeException(
                    "NWS /points returned no response"
            );
        }

        // Extract the forecast URL from the response
        // Response structure: { "properties": { "forecast": "https://api.weather.gov/gridpoints/TBW/85,56/forecast" } }
        String forecastUrl = response
                .path("properties")
                .path("forecast")
                .asString();

        if (forecastUrl == null || forecastUrl.isBlank()) {
            throw new RuntimeException(
                    "NWS did not return a forecast URL"
            );
        }

        System.out.println("Forecast URL: " + forecastUrl);

        return forecastUrl;
    }

    /**
     * Step 2 of NWS forecast retrieval: Get the actual forecast data.
     * 
     * This method orchestrates the two-step process:
     * 1. Call getForecastUrl() to get the forecast endpoint URL
     * 2. Call that forecast URL to get the actual forecast periods
     * 3. Parse the JSON response and convert each period into a ForecastPeriod Java object
     * 
     * @param latitude The latitude of the location
     * @param longitude The longitude of the location
     * @return A list of ForecastPeriod objects, usually 14 periods (7 days, day/night)
     * @throws RuntimeException if API calls fail or response structure is unexpected
     */
    public List<ForecastPeriod> getForecast(
            double latitude,
            double longitude) {

        // Step 1: Get the forecast URL for this location
        String forecastUrl =
                getForecastUrl(latitude, longitude);

        // Step 2: Call the forecast URL and get the forecast data
        // The forecast URL typically looks like:
        // https://api.weather.gov/gridpoints/TBW/85,56/forecast
        JsonNode response = nwsWebClient.get()
                .uri(URI.create(forecastUrl))  // Use the full URL from step 1
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

        if (response == null) {
            throw new RuntimeException(
                    "NWS forecast returned no response"
            );
        }

        // Extract the "periods" array from the response
        // Response structure: { "properties": { "periods": [ { ... }, { ... }, ... ] } }
        JsonNode periods = response
                .path("properties")
                .path("periods");

        if (!periods.isArray()) {
            throw new RuntimeException(
                    "NWS forecast did not contain forecast periods"
            );
        }

        // Convert each JSON period object into a ForecastPeriod Java object
        List<ForecastPeriod> forecast = new ArrayList<>();

        for (JsonNode period : periods) {

            // Extract each field from the JSON and create a ForecastPeriod object
            ForecastPeriod forecastPeriod =
                    new ForecastPeriod(
                            period.path("name").asString(),                  // e.g., "Tonight"
                            period.path("temperature").asInt(),               // e.g., 75
                            period.path("temperatureUnit").asString(),        // e.g., "F"
                            period.path("windSpeed").asString(),              // e.g., "10 mph"
                            period.path("windDirection").asString(),          // e.g., "NE"
                            period.path("shortForecast").asString(),          // e.g., "Partly Cloudy"
                            period.path("detailedForecast").asString(),       // Longer description
                            period.path("icon").asString()                    // URL to weather icon
                    );

            forecast.add(forecastPeriod);
        }

        return forecast;
    }
}
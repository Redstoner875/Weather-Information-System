package weatherapp.service;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import tools.jackson.databind.JsonNode;
import weatherapp.model.CurrentWeather;
import weatherapp.model.ForecastPeriod;

@Service
public class WeatherService {

    private final WebClient nwsWebClient;

    public WeatherService(WebClient nwsWebClient) {
        this.nwsWebClient = nwsWebClient;
    }

    /**
     * Calls the NWS /points endpoint for a latitude and longitude.
     *
     * The /points response contains metadata such as:
     * - forecast URL
     * - hourly forecast URL
     * - observation stations URL
     */
    private JsonNode getPointData(
            double latitude,
            double longitude) {

        // Limit coordinates to 4 decimal places before sending to NWS.
        double lat = Math.round(latitude * 10000.0) / 10000.0;
        double lon = Math.round(longitude * 10000.0) / 10000.0;

        JsonNode response = nwsWebClient.get()
                .uri("/points/" + lat + "," + lon)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

        if (response == null) {
            throw new RuntimeException(
                    "NWS /points returned no response"
            );
        }

        return response;
    }

    /**
     * Gets the forecast URL returned by the NWS /points endpoint.
     */
    private String getForecastUrl(
            double latitude,
            double longitude) {

        JsonNode pointData =
                getPointData(latitude, longitude);

        String forecastUrl = pointData
                .path("properties")
                .path("forecast")
                .asString();

        if (forecastUrl == null || forecastUrl.isBlank()) {
            throw new RuntimeException(
                    "NWS did not return a forecast URL"
            );
        }

        return forecastUrl;
    }

    /**
     * Gets the multi-day forecast from NWS.
     */
    public List<ForecastPeriod> getForecast(
            double latitude,
            double longitude) {

        // First get the location-specific forecast URL.
        String forecastUrl =
                getForecastUrl(latitude, longitude);

        // Then request the actual forecast.
        JsonNode response = nwsWebClient.get()
                .uri(URI.create(forecastUrl))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

        if (response == null) {
            throw new RuntimeException(
                    "NWS forecast returned no response"
            );
        }

        JsonNode periods = response
                .path("properties")
                .path("periods");

        if (!periods.isArray()) {
            throw new RuntimeException(
                    "NWS forecast did not contain forecast periods"
            );
        }

        List<ForecastPeriod> forecast =
                new ArrayList<>();

        // Convert every JSON forecast period into a Java object.
        for (JsonNode period : periods) {

            ForecastPeriod forecastPeriod =
                    new ForecastPeriod(
                            period.path("name").asString(),
                            period.path("startTime").asString(),
                            period.path("temperature").asInt(),
                            period.path("temperatureUnit").asString(),
                            period.path("windSpeed").asString(),
                            period.path("windDirection").asString(),
                            period.path("shortForecast").asString(),
                            period.path("detailedForecast").asString(),
                            period.path("icon").asString()
                    );

            forecast.add(forecastPeriod);
        }

        return forecast;
    }

    /**
     * Gets the latest observed weather conditions near the requested location.
     *
     * Flow:
     *
     * coordinates
     * -> /points
     * -> observationStations
     * -> first station
     * -> latest observation
     */
    public CurrentWeather getCurrentWeather(
            double latitude,
            double longitude) {

        /*
         * STEP 1:
         * Get NWS metadata for the requested coordinates.
         */
        JsonNode pointData =
                getPointData(latitude, longitude);

        /*
         * STEP 2:
         * Get the URL containing nearby observation stations.
         */
        String stationsUrl = pointData
                .path("properties")
                .path("observationStations")
                .asString();

        if (stationsUrl == null || stationsUrl.isBlank()) {
            throw new RuntimeException(
                    "NWS did not return observation stations"
            );
        }

        /*
         * STEP 3:
         * Request the nearby weather stations.
         */
        JsonNode stationsResponse = nwsWebClient.get()
                .uri(URI.create(stationsUrl))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

        if (stationsResponse == null) {
            throw new RuntimeException(
                    "Could not retrieve observation stations"
            );
        }

        JsonNode stations =
                stationsResponse.path("features");

        if (!stations.isArray() || stations.isEmpty()) {
            throw new RuntimeException(
                    "No observation stations found"
            );
        }

        /*
         * Use the first station returned by NWS.
         */
        JsonNode station =
                stations.get(0);

        String stationId = station
                .path("properties")
                .path("stationIdentifier")
                .asString();

        String stationName = station
                .path("properties")
                .path("name")
                .asString();

        if (stationId == null || stationId.isBlank()) {
            throw new RuntimeException(
                    "Observation station did not contain an ID"
            );
        }

        /*
         * STEP 4:
         * Get the latest observation from this station.
         */
        JsonNode observation = nwsWebClient.get()
                .uri(
                        "/stations/"
                        + stationId
                        + "/observations/latest"
                )
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

        if (observation == null) {
            throw new RuntimeException(
                    "Could not retrieve current weather"
            );
        }

        JsonNode properties =
                observation.path("properties");

        /*
         * NWS observation values may sometimes be null,
         * so use getNullableDouble().
         */

        Double temperatureC =
                getNullableDouble(
                        properties
                                .path("temperature")
                                .path("value")
                );

        Double humidity =
                getNullableDouble(
                        properties
                                .path("relativeHumidity")
                                .path("value")
                );

        Double windKmh =
                getNullableDouble(
                        properties
                                .path("windSpeed")
                                .path("value")
                );

        String description = properties
                .path("textDescription")
                .asString();

        /*
         * Convert Celsius to Fahrenheit.
         */
        Double temperatureF =
                temperatureC == null
                        ? null
                        : (temperatureC * 9.0 / 5.0) + 32.0;

        /*
         * Convert kilometers per hour to miles per hour.
         */
        Double windMph =
                windKmh == null
                        ? null
                        : windKmh * 0.621371;

        return new CurrentWeather(
                stationName,
                description,
                temperatureF,
                humidity,
                windMph
        );
    }

    /**
     * Safely converts a JSON numeric value into a Double.
     *
     * NWS observations can contain null measurements.
     */
    private Double getNullableDouble(JsonNode node) {

        if (node == null
                || node.isNull()
                || node.isMissingNode()) {

            return null;
        }

        return node.asDouble();
    }
}
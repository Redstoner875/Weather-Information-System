package weatherapp.service;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import tools.jackson.databind.JsonNode;
import weatherapp.model.ForecastPeriod;

@Service
public class WeatherService {

    private final WebClient nwsWebClient;

    public WeatherService(WebClient nwsWebClient) {
        this.nwsWebClient = nwsWebClient;
    }

    /*
     * Step 1:
     * latitude/longitude -> NWS /points endpoint
     *
     * The /points response tells us which forecast URL
     * should be used for this location.
     */
    public String getForecastUrl(double latitude, double longitude) {

        // NWS works best with coordinates limited to 4 decimal places
        double lat = Math.round(latitude * 10000.0) / 10000.0;
        double lon = Math.round(longitude * 10000.0) / 10000.0;

        System.out.println("Latitude: " + lat);
        System.out.println("Longitude: " + lon);

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

    /*
     * Step 2:
     * Call the forecast URL that NWS gave us.
     *
     * Then convert each forecast period from JSON
     * into a ForecastPeriod Java object.
     */
    public List<ForecastPeriod> getForecast(
            double latitude,
            double longitude) {

        String forecastUrl =
                getForecastUrl(latitude, longitude);

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

        List<ForecastPeriod> forecast = new ArrayList<>();

        for (JsonNode period : periods) {

            ForecastPeriod forecastPeriod =
                    new ForecastPeriod(
                            period.path("name").asString(),
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
}
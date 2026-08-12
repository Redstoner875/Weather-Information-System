package weatherapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Spring configuration class that sets up reusable beans.
 * 
 * @Configuration tells Spring this class contains bean definitions.
 * Beans created here are added to the Spring container and can be injected
 * into other classes via dependency injection.
 */
@Configuration
public class WebClientConfig {

    /**
     * Creates and configures a WebClient bean for making HTTP requests to the NWS (National Weather Service) API.
     * 
     * @Bean tells Spring to manage this object and make it available for dependency injection.
     * When another class needs a WebClient, Spring will provide this configured instance.
     * 
     * @return A WebClient configured with:
     *   - baseUrl: All requests will be relative to "https://api.weather.gov"
     *   - User-Agent header: Identifies the application to the API
     *   - Accept header: Requests GeoJSON format responses from NWS
     */
    @Bean
    public WebClient nwsWebClient() {

        return WebClient.builder()
                // Base URL for all NWS API calls (e.g., GET /points/{lat},{lon})
                .baseUrl("https://api.weather.gov")
                // Custom User-Agent header (NWS requires this to identify your application)
                .defaultHeader(
                        "User-Agent",
                        "WeatherInformationSystem/1.0 sareyeshwar@gmail.com"
                )
                // Request GeoJSON formatted responses from the NWS API
                .defaultHeader(
                        "Accept",
                        "application/geo+json"
                )
                .build();
    }
}
package weatherapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient nwsWebClient() {

        return WebClient.builder()
                .baseUrl("https://api.weather.gov")
                .defaultHeader(
                        "User-Agent",
                        "WeatherInformationSystem/1.0 sareyeshwar@gmail.com"
                )
                .defaultHeader(
                        "Accept",
                        "application/geo+json"
                )
                .build();
    }
}
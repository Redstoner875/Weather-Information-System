package weatherapp.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import weatherapp.model.Coordinates;
import weatherapp.model.CurrentWeather;
import weatherapp.model.ForecastPeriod;
import weatherapp.service.GeocodingService;
import weatherapp.service.WeatherService;

@Controller
public class WeatherController {

    private final GeocodingService geocodingService;
    private final WeatherService weatherService;

    @Value("${google.maps.api.key}")
    private String googleMapsApiKey;

    /**
     * Spring injects both services into the controller.
     */
    public WeatherController(
            GeocodingService geocodingService,
            WeatherService weatherService) {

        this.geocodingService = geocodingService;
        this.weatherService = weatherService;
    }

    /**
     * Displays the initial home page.
     *
     * localhost:8080/
     */
    @GetMapping("/")
    public String home() {
        return "index";
    }

    /**
     * Handles weather searches.
     *
     * Example:
     *
     * /weather?location=Orlando,FL
     */
    @GetMapping("/weather")
    public String getWeather(
            @RequestParam String location,
            Model model) {

        try {

            /*
             * STEP 1:
             * Convert a location such as:
             *
             * "Orlando, FL"
             *
             * into latitude and longitude.
             */
            Coordinates coordinates =
                    geocodingService.geocode(location);

            /*
             * STEP 2:
             * Get the multi-day NWS forecast.
             */
            List<ForecastPeriod> forecast =
                    weatherService.getForecast(
                            coordinates.getLatitude(),
                            coordinates.getLongitude()
                    );

            /*
             * STEP 3:
             * Add the basic location and forecast information
             * to the Thymeleaf Model.
             */
            model.addAttribute(
                    "search",
                    location
            );

            model.addAttribute(
                    "coordinates",
                    coordinates
            );

            model.addAttribute(
                    "forecast",
                    forecast
            );

            model.addAttribute(
                "googleMapsApiKey",
                googleMapsApiKey
        );

            /*
             * STEP 4:
             * Try to get current observed weather.
             *
             * This is separate so a missing observation does not
             * prevent the forecast from being displayed.
             */
            try {

                CurrentWeather currentWeather =
                        weatherService.getCurrentWeather(
                                coordinates.getLatitude(),
                                coordinates.getLongitude()
                        );

                model.addAttribute(
                        "currentWeather",
                        currentWeather
                );

            } catch (Exception currentWeatherException) {

                model.addAttribute(
                        "currentWeatherError",
                        "Current conditions are temporarily unavailable."
                );

                currentWeatherException.printStackTrace();
            }

        } catch (Exception e) {

            /*
             * This handles problems such as:
             * - invalid location
             * - geocoding failure
             * - NWS forecast failure
             */
            model.addAttribute(
                    "error",
                    "Could not get weather for " + location
            );

            e.printStackTrace();
        }

        /*
         * Render:
         *
         * src/main/resources/templates/index.html
         */
        return "index";
    }
}
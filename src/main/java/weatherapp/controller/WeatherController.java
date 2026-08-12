package weatherapp.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import weatherapp.model.Coordinates;
import weatherapp.model.ForecastPeriod;
import weatherapp.service.GeocodingService;
import weatherapp.service.WeatherService;

/**
 * Spring MVC Controller that handles HTTP requests and coordinates the weather lookup flow.
 * 
 * @Controller indicates this class handles web requests and returns views (HTML pages).
 * Spring automatically creates an instance and injects dependencies (GeocodingService, WeatherService).
 */
@Controller
public class WeatherController {

    // These services are injected by Spring's dependency injection system
    // (constructor parameters marked as final)
    private final GeocodingService geocodingService;
    private final WeatherService weatherService;

    /**
     * Constructor for dependency injection.
     * Spring passes in the GeocodingService and WeatherService beans
     * so this controller can use them to process requests.
     */
    public WeatherController(
            GeocodingService geocodingService,
            WeatherService weatherService) {

        this.geocodingService = geocodingService;
        this.weatherService = weatherService;
    }

    /**
     * Handles GET requests to the root path (localhost:8080).
     * Simply returns the home page template.
     * 
     * @return "index" - the name of the Thymeleaf template file (index.html)
     *         Spring will look for this in src/main/resources/templates/
     */
    @GetMapping("/")
    public String home() {
        return "index";
    }

    /**
     * Handles GET requests to /weather endpoint with a location parameter.
     * This is the main endpoint that orchestrates the weather lookup process.
     * 
     * Example: localhost:8080/weather?location=Orlando,FL
     * 
     * @param location The city/location string from the query parameter (e.g., "Orlando, FL")
     * @param model A Spring Model object used to pass data to the view (index.html)
     * @return "index" - returns to the same Thymeleaf template, but now with data
     */
    @GetMapping("/weather")
    public String getWeather(
            @RequestParam String location,  // Extract the "location" query parameter from the URL
            Model model) {  // Spring provides the Model object to pass data to the view

        try {

            /*
             * STEP 1: Convert location string to coordinates
             * "Orlando, FL" -> calls Nominatim OpenStreetMap API -> returns latitude/longitude
             */
            Coordinates coordinates =
                    geocodingService.geocode(location);

            /*
             * STEP 2: Get forecast data using coordinates
             * latitude/longitude -> calls NWS API (/points endpoint first, then forecast endpoint)
             * -> returns list of ForecastPeriod objects
             */
            List<ForecastPeriod> forecast =
                    weatherService.getForecast(
                            coordinates.getLatitude(),
                            coordinates.getLongitude()
                    );

            /*
             * STEP 3: Add the data to the Model so Thymeleaf can access it in the view
             * The Model acts as a bridge between the controller and the HTML template.
             * Thymeleaf uses these attributes with ${attributeName} syntax.
             */
            model.addAttribute(
                    "search",  // The original search string (for display)
                    location
            );

            model.addAttribute(
                    "coordinates",  // The Coordinates object with lat/lon/displayName
                    coordinates
            );

            model.addAttribute(
                    "forecast",  // The list of ForecastPeriod objects
                    forecast
            );

        } catch (Exception e) {
            // If anything goes wrong (invalid location, API error, etc.),
            // pass an error message to the view instead of the weather data
            model.addAttribute(
                    "error",
                    "Could not get weather for " + location
            );

            e.printStackTrace();
        }

        // Return to index.html. Thymeleaf will either display the weather data
        // or display the error message based on what was added to the Model.
        return "index";
    }
}
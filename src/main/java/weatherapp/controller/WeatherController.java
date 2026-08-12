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

@Controller
public class WeatherController {

    private final GeocodingService geocodingService;
    private final WeatherService weatherService;

    public WeatherController(
            GeocodingService geocodingService,
            WeatherService weatherService) {

        this.geocodingService = geocodingService;
        this.weatherService = weatherService;
    }

    /*
     * localhost:8080
     */
    @GetMapping("/")
    public String home() {
        return "index";
    }

    /*
     * Example:
     *
     * localhost:8080/weather?location=Orlando,FL
     */
    @GetMapping("/weather")
    public String getWeather(
            @RequestParam String location,
            Model model) {

        try {

            /*
             * Step 1:
             * "Orlando, FL"
             *
             * ->
             *
             * latitude / longitude
             */
            Coordinates coordinates =
                    geocodingService.geocode(location);

            /*
             * Step 2:
             * latitude / longitude
             *
             * ->
             *
             * NWS forecast
             */
            List<ForecastPeriod> forecast =
                    weatherService.getForecast(
                            coordinates.getLatitude(),
                            coordinates.getLongitude()
                    );

            /*
             * Step 3:
             * Give the data to index.html
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

        } catch (Exception e) {

            model.addAttribute(
                    "error",
                    "Could not get weather for " + location
            );

            e.printStackTrace();
        }

        return "index";
    }
}
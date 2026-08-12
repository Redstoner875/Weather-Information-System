package weatherapp.model;

/**
 * Model class representing a single weather forecast period.
 * 
 * The NWS weather API returns forecast data divided into periods (usually day/night periods).
 * This class holds all the weather information for one forecast period.
 * 
 * A list of ForecastPeriod objects is passed to the Thymeleaf view where each period
 * is displayed as a card or section showing temperature, wind, and forecast description.
 */
public class ForecastPeriod {

    private String name;                  // Period name (e.g., "Tonight", "Thursday", "Thursday Night")
    private int temperature;              // Temperature value (e.g., 82)
    private String temperatureUnit;       // Unit of temperature ("F" for Fahrenheit)
    private String windSpeed;             // Wind speed with units (e.g., "10 mph")
    private String windDirection;         // Direction wind is coming from (e.g., "NE", "SW")
    private String shortForecast;         // Brief description (e.g., "Partly Cloudy")
    private String detailedForecast;      // Longer description with more details
    private String icon;                  // URL to a weather icon image from NWS

    /**
     * Constructor for creating a ForecastPeriod from NWS API JSON response data.
     * 
     * @param name The name/label for this forecast period
     * @param temperature The temperature as an integer
     * @param temperatureUnit The temperature unit (typically "F")
     * @param windSpeed The wind speed with units as a string
     * @param windDirection The wind direction (e.g., "NE")
     * @param shortForecast Brief weather description
     * @param detailedForecast Detailed weather description
     * @param icon URL to the NWS weather icon for this forecast
     */
    public ForecastPeriod(
            String name,
            int temperature,
            String temperatureUnit,
            String windSpeed,
            String windDirection,
            String shortForecast,
            String detailedForecast,
            String icon) {

        this.name = name;
        this.temperature = temperature;
        this.temperatureUnit = temperatureUnit;
        this.windSpeed = windSpeed;
        this.windDirection = windDirection;
        this.shortForecast = shortForecast;
        this.detailedForecast = detailedForecast;
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public int getTemperature() {
        return temperature;
    }

    public String getTemperatureUnit() {
        return temperatureUnit;
    }

    public String getWindSpeed() {
        return windSpeed;
    }

    public String getWindDirection() {
        return windDirection;
    }

    public String getShortForecast() {
        return shortForecast;
    }

    public String getDetailedForecast() {
        return detailedForecast;
    }

    public String getIcon() {
        return icon;
    }
}
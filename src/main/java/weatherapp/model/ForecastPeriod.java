package weatherapp.model;

public class ForecastPeriod {

    private String name;
    private int temperature;
    private String temperatureUnit;
    private String windSpeed;
    private String windDirection;
    private String shortForecast;
    private String detailedForecast;
    private String icon;

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
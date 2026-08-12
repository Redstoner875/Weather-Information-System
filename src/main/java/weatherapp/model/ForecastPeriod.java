package weatherapp.model;

public class ForecastPeriod {

    private final String name;
    private final String startTime;
    private final int temperature;
    private final String temperatureUnit;
    private final String windSpeed;
    private final String windDirection;
    private final String shortForecast;
    private final String detailedForecast;
    private final String icon;

    public ForecastPeriod(
            String name,
            String startTime,
            int temperature,
            String temperatureUnit,
            String windSpeed,
            String windDirection,
            String shortForecast,
            String detailedForecast,
            String icon) {

        this.name = name;
        this.startTime = startTime;
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

    public String getStartTime() {
        return startTime;
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
package weatherapp.model;

public class CurrentWeather {

    private final String stationName;
    private final String description;
    private final Double temperature;
    private final Double humidity;
    private final Double windSpeed;

    public CurrentWeather(
            String stationName,
            String description,
            Double temperature,
            Double humidity,
            Double windSpeed) {

        this.stationName = stationName;
        this.description = description;
        this.temperature = temperature;
        this.humidity = humidity;
        this.windSpeed = windSpeed;
    }

    public String getStationName() {
        return stationName;
    }

    public String getDescription() {
        return description;
    }

    public Double getTemperature() {
        return temperature;
    }

    public Double getHumidity() {
        return humidity;
    }

    public Double getWindSpeed() {
        return windSpeed;
    }
}
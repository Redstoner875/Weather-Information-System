package weatherapp.model;

/**
 * Model class representing geographic coordinates.
 * 
 * This class holds the result of a geocoding operation: converting a location string
 * (like "Orlando, FL") into precise latitude and longitude coordinates.
 * 
 * The coordinates are then used to query the NWS weather API for forecast data.
 * This class is passed to the Thymeleaf view to display the location details.
 */
public class Coordinates {

    private final double latitude;      // North-South position (-90 to 90 degrees)
    private final double longitude;     // East-West position (-180 to 180 degrees)
    private final String displayName;   // Human-readable location name (e.g., "Orlando, Florida, USA")

    /**
     * Constructor for creating a Coordinates object from geocoding results.
     * 
     * @param latitude The latitude in decimal degrees
     * @param longitude The longitude in decimal degrees
     * @param displayName The display name returned by the geocoding service
     */
    public Coordinates(double latitude, double longitude, String displayName) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.displayName = displayName;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getDisplayName() {
        return displayName;
    }
}
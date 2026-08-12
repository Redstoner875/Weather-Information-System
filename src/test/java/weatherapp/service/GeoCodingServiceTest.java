package weatherapp.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import weatherapp.model.Coordinates;

class GeocodingServiceTest {

    @Test
    void shouldGeocodeOrlando() {

        GeocodingService geoCodingService =
                new GeocodingService();

        Coordinates coordinates =
                geoCodingService.geocode("aurora, illinois");

        System.out.println(
                "Latitude: " + coordinates.getLatitude()
        );

        System.out.println(
                "Longitude: " + coordinates.getLongitude()
        );

        System.out.println(
                "Location: " + coordinates.getDisplayName()
        );

        assertNotNull(coordinates);

        // Orlando should roughly be around these coordinates
        assertTrue(coordinates.getLatitude() > 28);
        assertTrue(coordinates.getLatitude() < 29);

        assertTrue(coordinates.getLongitude() > -82);
        assertTrue(coordinates.getLongitude() < -80);
    }
}
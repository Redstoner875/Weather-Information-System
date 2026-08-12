package weatherapp.service;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import tools.jackson.databind.JsonNode;
import weatherapp.model.Coordinates;

@Service
public class GeocodingService {

    private final WebClient webClient;

    public GeocodingService() {

        this.webClient = WebClient.builder()
                .baseUrl("https://nominatim.openstreetmap.org")
                .defaultHeader(
                        "User-Agent",
                        "WeatherInformationSystem/1.0 your-email@example.com"
                )
                .build();
    }

    /**
     * Converts a U.S. location or ZIP code into coordinates.
     *
     * Supported examples:
     *
     * Orlando, FL
     * Las Vegas
     * Aurora, Illinois
     * 89183
     * 60505
     */
    public Coordinates geocode(String location) {

        // Remove spaces before/after the user's input.
        final String searchLocation = location.trim();

        if (searchLocation.isBlank()) {
            throw new RuntimeException(
                    "Please enter a location."
            );
        }

        /*
         * Detect a normal 5-digit U.S. ZIP code.
         */
        boolean isZipCode =
                searchLocation.matches("\\d{5}");

        /*
         * If the user entered something entirely numeric,
         * but it is NOT 5 digits, reject it instead of allowing
         * Nominatim to interpret it as something else.
         *
         * Example:
         *
         * 89183  -> valid
         * 8918   -> invalid
         * 891830 -> invalid
         */
        if (searchLocation.matches("\\d+")
                && !isZipCode) {

            throw new RuntimeException(
                    "Please enter a valid 5-digit U.S. ZIP code."
            );
        }

        JsonNode response;

        if (isZipCode) {

            /*
             * ZIP CODE SEARCH
             *
             * Do NOT use q here.
             *
             * Nominatim's structured postalcode search is used
             * instead.
             *
             * countrycodes=us guarantees that a number like
             * 89183 cannot resolve to a postal code in another
             * country.
             */
            response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/search")
                            .queryParam(
                                    "postalcode",
                                    searchLocation
                            )
                            .queryParam(
                                    "countrycodes",
                                    "us"
                            )
                            .queryParam(
                                    "format",
                                    "jsonv2"
                            )
                            .queryParam(
                                    "addressdetails",
                                    1
                            )
                            .queryParam(
                                    "limit",
                                    1
                            )
                            .build()
                    )
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

        } else {

            /*
             * NORMAL LOCATION SEARCH
             *
             * Examples:
             *
             * Las Vegas
             * Orlando, FL
             * Aurora, Illinois
             *
             * countrycodes=us means Nominatim will only
             * return locations inside the United States.
             */
            response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/search")
                            .queryParam(
                                    "q",
                                    searchLocation
                            )
                            .queryParam(
                                    "countrycodes",
                                    "us"
                            )
                            .queryParam(
                                    "format",
                                    "jsonv2"
                            )
                            .queryParam(
                                    "addressdetails",
                                    1
                            )
                            .queryParam(
                                    "limit",
                                    1
                            )
                            .build()
                    )
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();
        }

        /*
         * Nothing matched the search.
         */
        if (response == null
                || response.isEmpty()) {

            if (isZipCode) {

                throw new RuntimeException(
                        "U.S. ZIP code not found: "
                        + searchLocation
                );
            }

            throw new RuntimeException(
                    "U.S. location not found: "
                    + searchLocation
            );
        }

        /*
         * We requested limit=1, so use the first result.
         */
        JsonNode result =
                response.get(0);

        /*
         * Read latitude and longitude.
         *
         * Keep using asString() with your Jackson version.
         */
        double latitude =
                Double.parseDouble(
                        result
                                .path("lat")
                                .asString()
                );

        double longitude =
                Double.parseDouble(
                        result
                                .path("lon")
                                .asString()
                );

        /*
         * addressdetails=1 gives us structured address
         * information.
         */
        JsonNode address =
                result.path("address");

        /*
         * Different locations may be classified differently
         * by OpenStreetMap.
         *
         * For example, one location might have "city" while
         * another might have "town".
         */
        String city = firstNonBlank(
                address,
                "city",
                "town",
                "village",
                "municipality",
                "hamlet"
        );

        String state =
                address
                        .path("state")
                        .asString();

        String displayName;

        /*
         * Preferred format:
         *
         * Las Vegas, Nevada
         * Orlando, Florida
         * Aurora, Illinois
         */
        if (!city.isBlank()
                && !state.isBlank()) {

            displayName =
                    city + ", " + state;

        } else {

            /*
             * ZIP-code results sometimes contain less structured
             * city information.
             *
             * If we cannot construct City, State ourselves,
             * fall back to Nominatim's display name.
             */
            displayName =
                    result
                            .path("display_name")
                            .asString();
        }

        return new Coordinates(
                latitude,
                longitude,
                displayName
        );
    }

    /**
     * Returns the first address field that contains a value.
     *
     * This is useful because OpenStreetMap does not classify
     * every populated place using the exact same field.
     */
    private String firstNonBlank(
            JsonNode address,
            String... fields) {

        for (String field : fields) {

            String value =
                    address
                            .path(field)
                            .asString();

            if (value != null
                    && !value.isBlank()) {

                return value;
            }
        }

        return "";
    }
}
# Weather Information System

https://weather-information-system.onrender.com/

A Spring Boot web application that provides weather forecasts for user-specified locations. Users can enter a city or location such as `Orlando, FL`, and the application converts that location into geographic coordinates using Nominatim before retrieving forecast data from the National Weather Service API. :contentReference[oaicite:0]{index=0}

## Features

- Search for weather by city or location name
- Convert location names to latitude and longitude using Nominatim / OpenStreetMap
- Retrieve weather forecasts from the National Weather Service API
- Display multiple forecast periods, including daytime and nighttime forecasts
- Display temperature and temperature units
- Display wind speed and direction
- Display short and detailed weather descriptions
- Display NWS weather icons
- Render weather data dynamically using Thymeleaf

## Tech Stack

- **Java 21**
- **Spring Boot 4.1.0**
- **Spring MVC**
- **Spring WebFlux**
- **WebClient**
- **Thymeleaf**
- **Maven**
- **Nominatim / OpenStreetMap API**
- **National Weather Service API**

## Application Flow

```text
User enters a location
        |
        v
WeatherController
        |
        v
GeocodingService
        |
        v
Nominatim API
        |
        v
Latitude + Longitude
        |
        v
WeatherService
        |
        v
NWS /points/{latitude},{longitude}
        |
        v
Forecast URL
        |
        v
NWS Forecast API
        |
        v
ForecastPeriod objects
        |
        v
Spring Model
        |
        v
Thymeleaf
        |
        v
Weather displayed in browser

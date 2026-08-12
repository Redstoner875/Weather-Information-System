package weatherapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Spring Boot application class for the Weather Information System.
 * 
 * @SpringBootApplication enables Spring's component scanning, auto-configuration,
 * and allows beans to be defined in this package and below.
 */
@SpringBootApplication
public class WeatherappApplication {

	/**
	 * Launches the Spring Boot application.
	 * Spring automatically scans for @Controller, @Service, @Configuration classes,
	 * and wires them together (dependency injection).
	 */
	public static void main(String[] args) {
		SpringApplication.run(WeatherappApplication.class, args);
	}

}

package co.edu.unbosque.wheeltrees;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class WheeltreesApplication {

	public static void main(String[] args) {
		SpringApplication.run(WheeltreesApplication.class, args);
	}
}
package co.edu.unbosque.wheeltrees;

import java.util.TimeZone;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class WheeltreesApplication {

	public static void main(String[] args) {
		// El backend corre en Azure App Service, que por defecto usa UTC.
		// El frontend envía fechas/horas como hora local de Colombia (sin
		// offset de zona horaria), y toda la app compara contra
		// LocalDateTime.now(). Sin esto, "ahora" en el servidor queda ~5
		// horas adelante de la hora real de Colombia, y cualquier viaje
		// publicado para "dentro de poco" aparece como si fuera en el
		// pasado. Se fija ANTES de iniciar Spring para que ningún bean
		// (datasource, Hibernate, etc.) capture la zona horaria vieja.
		TimeZone.setDefault(TimeZone.getTimeZone("America/Bogota"));
		SpringApplication.run(WheeltreesApplication.class, args);
	}
}
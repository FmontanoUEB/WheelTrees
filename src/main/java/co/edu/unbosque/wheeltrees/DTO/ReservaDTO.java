package co.edu.unbosque.wheeltrees.DTO;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

public class ReservaDTO {

	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class SolicitarReservaRequest {
		@NotNull
		private UUID viajeId;
		@Size(max = 300)
		private String notasPasajero;
	}

	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class ResponderReservaRequest {
		@NotNull
		private boolean aceptar;
	}

	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class ReservaResponse {
		private String id;
		private String viajeId;
		private String origenViaje;
		private LocalDateTime fechaHoraSalida;
		private String pasajeroNombre;
		private String pasajeroEmail;
		private String estado;
		private String notasPasajero;
		private LocalDateTime creadoEn;
	}
}
package co.edu.unbosque.wheeltrees.DTO;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

public class CalificacionDTO {

	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class CrearCalificacionRequest {
		@NotNull
		private UUID reservaId;

		@NotNull
		@Min(1)
		@Max(5)
		private Integer puntuacion;

		@Size(max = 300)
		private String comentario;
	}

	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class CalificacionResponse {
		private String id;
		private String reservaId;
		private String calificadorId;
		private String calificadorNombre;
		private String calificadoId;
		private String calificadoNombre;
		private int puntuacion;
		private String comentario;
		private LocalDateTime creadoEn;
	}

	/** Una reserva completada donde el usuario autenticado todavía no calificó a la contraparte. */
	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class CalificacionPendiente {
		private String reservaId;
		private String viajeId;
		private String origenViaje;
		private LocalDateTime fechaHoraSalida;
		/** A quién le falta calificar el usuario autenticado. */
		private String aCalificarId;
		private String aCalificarNombre;
	}
}

package co.edu.unbosque.wheeltrees.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

public class MensajeDTO {

	/** Enviado por el cliente, tanto por WebSocket como por el POST de respaldo. */
	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class EnviarMensajeRequest {
		@NotNull
		private UUID destinatarioId;

		@NotBlank
		@Size(max = 1000)
		private String contenido;
	}

	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class MensajeResponse {
		private String id;
		private String remitenteId;
		private String remitenteNombre;
		private String destinatarioId;
		private String contenido;
		private LocalDateTime enviadoEn;
	}

	/** Una fila en la lista "Mis chats": un resumen por persona, no por viaje. */
	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class ChatResumen {
		private String otroUsuarioId;
		private String otroUsuarioNombre;
		private String ultimoMensaje;
		private LocalDateTime ultimoMensajeEn;
		private long noLeidos;
	}

	/** Error de negocio enviado por la cola privada del WebSocket. */
	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class ChatError {
		private String mensaje;
	}
}

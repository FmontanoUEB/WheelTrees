package co.edu.unbosque.wheeltrees.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Mensaje de chat directo entre dos personas (remitente → destinatario).
 * NO está ligado a una Reserva ni a un Viaje puntual: dos usuarios que ya
 * compartieron al menos un viaje (con una reserva CONFIRMADA, en cualquier
 * dirección) tienen una única conversación continua entre ellos, sin
 * importar cuántos viajes reserven juntos después.
 */
@Entity
@Table(name = "mensajes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Mensaje {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "remitente_id", nullable = false)
	private Usuario remitente;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "destinatario_id", nullable = false)
	private Usuario destinatario;

	@Column(name = "contenido", nullable = false, length = 1000)
	private String contenido;

	@Column(name = "enviado_en", nullable = false, updatable = false)
	private LocalDateTime enviadoEn;

	@Column(name = "leido", nullable = false)
	private boolean leido = false;

	@PrePersist
	protected void onCreate() {
		this.enviadoEn = LocalDateTime.now();
	}
}

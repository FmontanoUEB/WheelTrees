package co.edu.unbosque.wheeltrees.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

import co.edu.unbosque.wheeltrees.security.CifradoAttributeConverter;
import jakarta.persistence.Convert;

/**
 * Calificación recíproca al finalizar un viaje: el pasajero califica al
 * conductor y el conductor califica al pasajero, ambas ligadas a la misma
 * Reserva (la relación única entre ese pasajero y ese viaje).
 *
 * La restricción única (reserva_id, calificador_id) evita que la misma
 * persona califique dos veces la misma reserva, pero permite que existan
 * dos filas por reserva: una del pasajero -> conductor y otra del
 * conductor -> pasajero.
 */
@Entity
@Table(name = "calificaciones", uniqueConstraints = @UniqueConstraint(columnNames = { "reserva_id", "calificador_id" }))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Calificacion {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "reserva_id", nullable = false)
	private Reserva reserva;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "calificador_id", nullable = false)
	private Usuario calificador;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "calificado_id", nullable = false)
	private Usuario calificado;

	@Column(name = "puntuacion", nullable = false)
	private int puntuacion;
	@Convert(converter = CifradoAttributeConverter.class)
	@Column(name = "comentario", length = 500)
	private String comentario;
	@Column(name = "creado_en", nullable = false, updatable = false)
	private LocalDateTime creadoEn;

	@PrePersist
	protected void onCreate() {
		this.creadoEn = LocalDateTime.now();
	}
}

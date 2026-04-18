package co.edu.unbosque.wheeltrees.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "reservas", uniqueConstraints = @UniqueConstraint(columnNames = { "viaje_id", "pasajero_id" }))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reserva {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "viaje_id", nullable = false)
	private Viaje viaje;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "pasajero_id", nullable = false)
	private Usuario pasajero;

	@Enumerated(EnumType.STRING)
	@Column(name = "estado", nullable = false)
	private EstadoReserva estado = EstadoReserva.PENDIENTE;

	@Column(name = "notas_pasajero", length = 300)
	private String notasPasajero;

	@Column(name = "creado_en", nullable = false, updatable = false)
	private LocalDateTime creadoEn;

	@Column(name = "actualizado_en")
	private LocalDateTime actualizadoEn;

	@PrePersist
	protected void onCreate() {
		this.creadoEn = LocalDateTime.now();
		this.actualizadoEn = LocalDateTime.now();
	}

	@PreUpdate
	protected void onUpdate() {
		this.actualizadoEn = LocalDateTime.now();
	}
}
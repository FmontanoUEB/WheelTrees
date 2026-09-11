package co.edu.unbosque.wheeltrees.repository;

import co.edu.unbosque.wheeltrees.model.EstadoReserva;
import co.edu.unbosque.wheeltrees.model.Reserva;
import co.edu.unbosque.wheeltrees.model.Usuario;
import co.edu.unbosque.wheeltrees.model.Viaje;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReservaRepository extends JpaRepository<Reserva, UUID> {
	List<Reserva> findByPasajero(Usuario pasajero);

	List<Reserva> findByViaje(Viaje viaje);

	boolean existsByViajeAndPasajero(Viaje viaje, Usuario pasajero);

	Optional<Reserva> findByViajeAndPasajero(Viaje viaje, Usuario pasajero);

	List<Reserva> findByPasajeroIdAndEstado(UUID pasajeroId, EstadoReserva estado);

	List<Reserva> findByViajeConductorIdAndEstado(UUID conductorId, EstadoReserva estado);

	/**
	 * true si "a" y "b" han compartido al menos un viaje con reserva
	 * CONFIRMADA, sin importar quién fue el conductor y quién el pasajero.
	 * Esta es la única condición para habilitar el chat entre dos personas.
	 */
	@Query("SELECT COUNT(r) > 0 FROM Reserva r WHERE r.estado = 'CONFIRMADA' AND " +
			"((r.pasajero.id = :a AND r.viaje.conductor.id = :b) OR " +
			" (r.pasajero.id = :b AND r.viaje.conductor.id = :a))")
	boolean existeRelacionConfirmada(@Param("a") UUID a, @Param("b") UUID b);

	/**
	 * Ids de todas las personas con las que "usuarioId" tiene al menos una
	 * reserva CONFIRMADA (como conductor o como pasajero). Puede traer
	 * duplicados si comparten varios viajes — se deduplica en el servicio.
	 */
	@Query("SELECT CASE WHEN r.pasajero.id = :usuarioId THEN r.viaje.conductor.id ELSE r.pasajero.id END " +
			"FROM Reserva r WHERE r.estado = 'CONFIRMADA' " +
			"AND (r.pasajero.id = :usuarioId OR r.viaje.conductor.id = :usuarioId)")
	List<UUID> findContrapartesConfirmadas(@Param("usuarioId") UUID usuarioId);
}
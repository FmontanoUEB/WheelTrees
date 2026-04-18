package co.edu.unbosque.wheeltrees.repository;

import co.edu.unbosque.wheeltrees.model.Reserva;
import co.edu.unbosque.wheeltrees.model.Usuario;
import co.edu.unbosque.wheeltrees.model.Viaje;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReservaRepository extends JpaRepository<Reserva, UUID> {
	List<Reserva> findByPasajero(Usuario pasajero);

	List<Reserva> findByViaje(Viaje viaje);

	boolean existsByViajeAndPasajero(Viaje viaje, Usuario pasajero);

	Optional<Reserva> findByViajeAndPasajero(Viaje viaje, Usuario pasajero);
}
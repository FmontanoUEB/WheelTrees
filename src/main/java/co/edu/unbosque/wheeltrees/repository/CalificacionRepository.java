package co.edu.unbosque.wheeltrees.repository;

import co.edu.unbosque.wheeltrees.model.Calificacion;
import co.edu.unbosque.wheeltrees.model.Reserva;
import co.edu.unbosque.wheeltrees.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CalificacionRepository extends JpaRepository<Calificacion, java.util.UUID> {

	boolean existsByReservaAndCalificador(Reserva reserva, Usuario calificador);

	List<Calificacion> findByCalificado(Usuario calificado);
}

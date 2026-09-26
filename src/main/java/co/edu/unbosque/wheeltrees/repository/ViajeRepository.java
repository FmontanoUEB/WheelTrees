package co.edu.unbosque.wheeltrees.repository;

import co.edu.unbosque.wheeltrees.model.EstadoViaje;
import co.edu.unbosque.wheeltrees.model.TipoVehiculo;
import co.edu.unbosque.wheeltrees.model.Viaje;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ViajeRepository extends JpaRepository<Viaje, UUID> {

	// Buscar viajes disponibles a futuro con cupos
	List<Viaje> findByEstadoAndCuposDisponiblesGreaterThanAndFechaHoraSalidaAfter(EstadoViaje estado, int cupos,
			LocalDateTime desde);

	// Viajes publicados por un conductor
	List<Viaje> findByConductorIdOrderByFechaHoraSalidaAsc(UUID conductorId);

	// Búsqueda por texto en origen (para buscar por barrio/localidad)
	@Query("SELECT v FROM Viaje v WHERE v.estado = 'PROGRAMADO' " + "AND v.cuposDisponibles > 0 "
			+ "AND v.fechaHoraSalida > :desde " + "AND LOWER(v.origenDescripcion) LIKE LOWER(CONCAT('%', :texto, '%'))")
	List<Viaje> buscarPorOrigen(@Param("texto") String texto, @Param("desde") LocalDateTime desde);
	
	@Query("SELECT v FROM Viaje v WHERE v.estado = 'PROGRAMADO' " +
		       "AND v.cuposDisponibles > 0 " +
		       "AND v.fechaHoraSalida > :desde " +
		       "AND v.vehiculo.tipo = :tipo")
		List<Viaje> buscarPorTipoVehiculo(@Param("tipo") TipoVehiculo tipo,
		                                   @Param("desde") LocalDateTime desde);
}
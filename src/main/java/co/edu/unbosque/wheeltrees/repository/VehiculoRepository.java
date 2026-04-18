package co.edu.unbosque.wheeltrees.repository;

import co.edu.unbosque.wheeltrees.model.Usuario;
import co.edu.unbosque.wheeltrees.model.Vehiculo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface VehiculoRepository extends JpaRepository<Vehiculo, UUID> {
	List<Vehiculo> findByConductorAndActivoTrue(Usuario conductor);

	boolean existsByPlaca(String placa);
}
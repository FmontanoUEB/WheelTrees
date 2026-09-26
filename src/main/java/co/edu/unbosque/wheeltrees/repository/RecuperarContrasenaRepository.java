package co.edu.unbosque.wheeltrees.repository;

import co.edu.unbosque.wheeltrees.model.RecuperarContrasena;
import co.edu.unbosque.wheeltrees.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RecuperarContrasenaRepository extends JpaRepository<RecuperarContrasena, UUID> {

	Optional<RecuperarContrasena> findTopByUsuarioAndCodigoOtpAndUsadoFalseOrderByCreadoEnDesc(Usuario usuario,
			String codigoOtp);

	List<RecuperarContrasena> findByUsuarioAndUsadoFalse(Usuario usuario);

	void deleteByUsuario(Usuario usuario);
}
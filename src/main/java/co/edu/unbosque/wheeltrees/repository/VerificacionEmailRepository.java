package co.edu.unbosque.wheeltrees.repository;

import co.edu.unbosque.wheeltrees.model.VerificacionEmail;
import co.edu.unbosque.wheeltrees.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VerificacionEmailRepository extends JpaRepository<VerificacionEmail, UUID> {

	Optional<VerificacionEmail> findTopByUsuarioAndUsadoFalseOrderByCreadoEnDesc(Usuario usuario);

	Optional<VerificacionEmail> findTopByUsuarioAndCodigoOtpAndUsadoFalseOrderByCreadoEnDesc(Usuario usuario,
			String codigoOtp);

	List<VerificacionEmail> findByUsuarioAndUsadoFalse(Usuario usuario);
}
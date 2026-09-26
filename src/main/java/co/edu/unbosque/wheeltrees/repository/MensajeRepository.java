package co.edu.unbosque.wheeltrees.repository;

import co.edu.unbosque.wheeltrees.model.Mensaje;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MensajeRepository extends JpaRepository<Mensaje, UUID> {

	/** Historial completo entre dos personas, en cualquier dirección. */
	@Query("SELECT m FROM Mensaje m " +
			"WHERE (m.remitente.id = :a AND m.destinatario.id = :b) " +
			"   OR (m.remitente.id = :b AND m.destinatario.id = :a) " +
			"ORDER BY m.enviadoEn ASC")
	List<Mensaje> historialEntre(@Param("a") UUID a, @Param("b") UUID b);

	/** Último mensaje cruzado entre dos personas (para la lista "Mis chats"). */
	@Query("SELECT m FROM Mensaje m " +
			"WHERE (m.remitente.id = :a AND m.destinatario.id = :b) " +
			"   OR (m.remitente.id = :b AND m.destinatario.id = :a) " +
			"ORDER BY m.enviadoEn DESC LIMIT 1")
	Optional<Mensaje> ultimoMensajeEntre(@Param("a") UUID a, @Param("b") UUID b);

	long countByRemitenteIdAndDestinatarioIdAndLeidoFalse(UUID remitenteId, UUID destinatarioId);

	@Modifying
	@Query("UPDATE Mensaje m SET m.leido = true " +
			"WHERE m.destinatario.id = :usuarioId AND m.remitente.id = :otroId AND m.leido = false")
	void marcarLeidos(@Param("usuarioId") UUID usuarioId, @Param("otroId") UUID otroId);
}

package co.edu.unbosque.wheeltrees.service;

import co.edu.unbosque.wheeltrees.DTO.MensajeDTO.*;
import co.edu.unbosque.wheeltrees.model.Mensaje;
import co.edu.unbosque.wheeltrees.model.Usuario;
import co.edu.unbosque.wheeltrees.repository.MensajeRepository;
import co.edu.unbosque.wheeltrees.repository.ReservaRepository;
import co.edu.unbosque.wheeltrees.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MensajeService {

	private final MensajeRepository mensajeRepository;
	private final ReservaRepository reservaRepository;
	private final UsuarioRepository usuarioRepository;

	/**
	 * Envía un mensaje directo de "remitenteId" a "destinatarioId". No está
	 * ligado a ningún viaje puntual: basta con que las dos personas hayan
	 * compartido alguna vez un viaje con reserva CONFIRMADA.
	 */
	@Transactional
	public MensajeResponse enviar(UUID remitenteId, EnviarMensajeRequest request) {
		UUID destinatarioId = request.getDestinatarioId();
		validarPuedenChatear(remitenteId, destinatarioId);

		Usuario remitente = usuarioRepository.findById(remitenteId)
				.orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
		Usuario destinatario = usuarioRepository.findById(destinatarioId)
				.orElseThrow(() -> new IllegalArgumentException("Destinatario no encontrado"));

		Mensaje mensaje = Mensaje.builder()
				.remitente(remitente)
				.destinatario(destinatario)
				.contenido(request.getContenido().trim())
				.leido(false)
				.build();

		return toResponse(mensajeRepository.save(mensaje));
	}

	/** Historial completo entre el usuario autenticado y "otroUsuarioId". */
	@Transactional(readOnly = true)
	public List<MensajeResponse> historial(UUID usuarioId, UUID otroUsuarioId) {
		validarPuedenChatear(usuarioId, otroUsuarioId);
		return mensajeRepository.historialEntre(usuarioId, otroUsuarioId)
				.stream().map(this::toResponse).toList();
	}

	@Transactional
	public void marcarLeidos(UUID usuarioId, UUID otroUsuarioId) {
		validarPuedenChatear(usuarioId, otroUsuarioId);
		mensajeRepository.marcarLeidos(usuarioId, otroUsuarioId);
	}

	/**
	 * Una fila por cada persona con la que el usuario tiene al menos una
	 * reserva CONFIRMADA (como conductor o pasajero), sin importar el viaje.
	 */
	@Transactional(readOnly = true)
	public List<ChatResumen> misChats(UUID usuarioId) {
		// LinkedHashMap para deduplicar contrapartes conservando el orden.
		Map<UUID, Usuario> contrapartes = new LinkedHashMap<>();
		for (UUID id : reservaRepository.findContrapartesConfirmadas(usuarioId)) {
			if (!contrapartes.containsKey(id)) {
				usuarioRepository.findById(id).ifPresent(u -> contrapartes.put(id, u));
			}
		}

		return contrapartes.values().stream().map(otro -> {
			var ultimo = mensajeRepository.ultimoMensajeEntre(usuarioId, otro.getId());
			long noLeidos = mensajeRepository.countByRemitenteIdAndDestinatarioIdAndLeidoFalse(otro.getId(), usuarioId);

			return ChatResumen.builder()
					.otroUsuarioId(otro.getId().toString())
					.otroUsuarioNombre(otro.getNombre() + " " + otro.getApellido())
					.ultimoMensaje(ultimo.map(Mensaje::getContenido).orElse(null))
					.ultimoMensajeEn(ultimo.map(Mensaje::getEnviadoEn).orElse(null))
					.noLeidos(noLeidos)
					.build();
		}).toList();
	}

	/**
	 * Único requisito para poder chatear: haber compartido alguna vez un
	 * viaje con reserva CONFIRMADA (en cualquier dirección). No importa si
	 * hay viajes nuevos, cancelados o pendientes de por medio.
	 */
	private void validarPuedenChatear(UUID a, UUID b) {
		if (a.equals(b)) {
			throw new IllegalArgumentException("No puedes chatear contigo mismo");
		}
		if (!reservaRepository.existeRelacionConfirmada(a, b)) {
			throw new IllegalArgumentException(
					"Solo puedes chatear con alguien con quien hayas compartido un viaje confirmado");
		}
	}

	private MensajeResponse toResponse(Mensaje m) {
		return MensajeResponse.builder()
				.id(m.getId().toString())
				.remitenteId(m.getRemitente().getId().toString())
				.remitenteNombre(m.getRemitente().getNombre() + " " + m.getRemitente().getApellido())
				.destinatarioId(m.getDestinatario().getId().toString())
				.contenido(m.getContenido())
				.enviadoEn(m.getEnviadoEn())
				.build();
	}
}

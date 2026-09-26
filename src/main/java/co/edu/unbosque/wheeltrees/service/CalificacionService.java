package co.edu.unbosque.wheeltrees.service;

import co.edu.unbosque.wheeltrees.DTO.CalificacionDTO.*;
import co.edu.unbosque.wheeltrees.model.*;
import co.edu.unbosque.wheeltrees.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CalificacionService {

	private final CalificacionRepository calificacionRepository;
	private final ReservaRepository reservaRepository;
	private final UsuarioRepository usuarioRepository;

	@Transactional
	public CalificacionResponse calificar(UUID calificadorId, CrearCalificacionRequest request) {

		Usuario calificador = usuarioRepository.findById(calificadorId)
				.orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

		Reserva reserva = reservaRepository.findById(request.getReservaId())
				.orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada"));

		if (reserva.getEstado() != EstadoReserva.COMPLETADA) {
			throw new IllegalArgumentException("Solo puedes calificar una vez que el viaje haya terminado");
		}

		// Determina quién es la contraparte según si el que califica es el
		// pasajero de esta reserva o el conductor del viaje asociado. Si no
		// es ninguno de los dos, no tiene nada que hacer aquí.
		Usuario calificado;
		if (reserva.getPasajero().getId().equals(calificadorId)) {
			calificado = reserva.getViaje().getConductor();
		} else if (reserva.getViaje().getConductor().getId().equals(calificadorId)) {
			calificado = reserva.getPasajero();
		} else {
			throw new IllegalArgumentException("No tienes permiso para calificar esta reserva");
		}

		if (calificacionRepository.existsByReservaAndCalificador(reserva, calificador)) {
			throw new IllegalArgumentException("Ya calificaste esta reserva");
		}

		Calificacion calificacion = Calificacion.builder()
				.reserva(reserva)
				.calificador(calificador)
				.calificado(calificado)
				.puntuacion(request.getPuntuacion())
				.comentario(request.getComentario())
				.build();

		calificacion = calificacionRepository.save(calificacion);

		recalcularPromedio(calificado);

		return toResponse(calificacion);
	}

	/**
	 * Recalcula el promedio y el total de calificaciones recibidas por un
	 * usuario. Se recorre la lista completa en vez de llevar un acumulador
	 * incremental: el volumen de calificaciones por usuario en esta app es
	 * bajo (viajes universitarios, no una flota masiva), así que es más
	 * simple y menos propenso a errores de redondeo que mantener sumas
	 * parciales.
	 */
	private void recalcularPromedio(Usuario calificado) {
		List<Calificacion> recibidas = calificacionRepository.findByCalificado(calificado);
		double promedio = recibidas.stream().mapToInt(Calificacion::getPuntuacion).average().orElse(0);
		calificado.setCalificacionPromedio(promedio);
		calificado.setTotalCalificaciones(recibidas.size());
		usuarioRepository.save(calificado);
	}

	/**
	 * Reservas COMPLETADA donde el usuario autenticado (como pasajero o
	 * como conductor) todavía no ha calificado a la contraparte. Se usa
	 * para mostrarle al usuario el prompt de "califica tu viaje" al entrar
	 * a la app o al finalizar un viaje.
	 */
	@Transactional(readOnly = true)
	public List<CalificacionPendiente> pendientes(UUID usuarioId) {
		Usuario usuario = usuarioRepository.findById(usuarioId)
				.orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

		List<Reserva> comoPasajero = reservaRepository.findByPasajeroIdAndEstado(usuarioId, EstadoReserva.COMPLETADA);
		List<Reserva> comoConductor = reservaRepository.findByViajeConductorIdAndEstado(usuarioId, EstadoReserva.COMPLETADA);

		List<CalificacionPendiente> pendientes = new ArrayList<>();

		for (Reserva r : comoPasajero) {
			if (!calificacionRepository.existsByReservaAndCalificador(r, usuario)) {
				Usuario conductor = r.getViaje().getConductor();
				pendientes.add(CalificacionPendiente.builder()
						.reservaId(r.getId().toString())
						.viajeId(r.getViaje().getId().toString())
						.origenViaje(r.getViaje().getOrigenDescripcion())
						.fechaHoraSalida(r.getViaje().getFechaHoraSalida())
						.aCalificarId(conductor.getId().toString())
						.aCalificarNombre(conductor.getNombre() + " " + conductor.getApellido())
						.build());
			}
		}

		for (Reserva r : comoConductor) {
			if (!calificacionRepository.existsByReservaAndCalificador(r, usuario)) {
				Usuario pasajero = r.getPasajero();
				pendientes.add(CalificacionPendiente.builder()
						.reservaId(r.getId().toString())
						.viajeId(r.getViaje().getId().toString())
						.origenViaje(r.getViaje().getOrigenDescripcion())
						.fechaHoraSalida(r.getViaje().getFechaHoraSalida())
						.aCalificarId(pasajero.getId().toString())
						.aCalificarNombre(pasajero.getNombre() + " " + pasajero.getApellido())
						.build());
			}
		}

		return pendientes;
	}

	private CalificacionResponse toResponse(Calificacion c) {
		return CalificacionResponse.builder()
				.id(c.getId().toString())
				.reservaId(c.getReserva().getId().toString())
				.calificadorId(c.getCalificador().getId().toString())
				.calificadorNombre(c.getCalificador().getNombre() + " " + c.getCalificador().getApellido())
				.calificadoId(c.getCalificado().getId().toString())
				.calificadoNombre(c.getCalificado().getNombre() + " " + c.getCalificado().getApellido())
				.puntuacion(c.getPuntuacion())
				.comentario(c.getComentario())
				.creadoEn(c.getCreadoEn())
				.build();
	}
}

package co.edu.unbosque.wheeltrees.service;

import co.edu.unbosque.wheeltrees.DTO.ViajeDTO.*;
import co.edu.unbosque.wheeltrees.model.*;
import co.edu.unbosque.wheeltrees.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ViajeService {

	private final ViajeRepository viajeRepository;
	private final VehiculoRepository vehiculoRepository;
	private final UsuarioRepository usuarioRepository;
	private final ReservaRepository reservaRepository;
	private final SimpMessagingTemplate messagingTemplate;

	@Transactional
	public ViajeResponse publicar(UUID conductorId, PublicarViajeRequest request) {

		Usuario conductor = usuarioRepository.findById(conductorId)
				.orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

		// Solo los usuarios con rol CONDUCTOR (o AMBOS, por compatibilidad con
		// cuentas antiguas) pueden publicar viajes. Un PASAJERO solo puede
		// buscar viajes disponibles y reservar cupos.
		if (conductor.getRol() != RolUsuario.CONDUCTOR && conductor.getRol() != RolUsuario.AMBOS) {
			throw new IllegalArgumentException("Solo los conductores pueden publicar viajes");
		}

		Vehiculo vehiculo = vehiculoRepository.findById(request.getVehiculoId())
				.orElseThrow(() -> new IllegalArgumentException("Vehículo no encontrado"));

		if (!vehiculo.getConductor().getId().equals(conductorId)) {
			throw new IllegalArgumentException("Este vehículo no te pertenece");
		}

		if (!vehiculo.isActivo()) {
			throw new IllegalArgumentException("El vehículo está desactivado");
		}

		if (request.getCuposTotales() > vehiculo.getCapacidadPasajeros()) {
			throw new IllegalArgumentException(
					"Los cupos no pueden superar la capacidad del vehículo (" + vehiculo.getCapacidadPasajeros() + ")");
		}

		if (request.getFechaHoraSalida().isBefore(LocalDateTime.now().plusMinutes(15))) {
			throw new IllegalArgumentException("La hora de salida debe ser al menos 15 minutos en el futuro");
		}

		Viaje viaje = Viaje.builder().conductor(conductor).vehiculo(vehiculo)
				.origenDescripcion(request.getOrigenDescripcion()).origenLat(request.getOrigenLat())
				.origenLng(request.getOrigenLng()).destinoDescripcion(request.getDestinoDescripcion())
				.destinoLat(request.getDestinoLat()).destinoLng(request.getDestinoLng())
				.fechaHoraSalida(request.getFechaHoraSalida()).cuposTotales(request.getCuposTotales())
				.cuposDisponibles(request.getCuposTotales()).aportePorPasajero(request.getAportePorPasajero())
				.notas(request.getNotas()).estado(EstadoViaje.PROGRAMADO).build();

		return toResponse(viajeRepository.save(viaje));
	}

	// FIX: se agregó @Transactional(readOnly = true). Sin esto, la sesión de
	// Hibernate se cierra antes de que toResponse() acceda a las relaciones
	// LAZY (conductor, vehiculo) -> LazyInitializationException ("no Session").
	@Transactional(readOnly = true)
	public List<ViajeResponse> buscarDisponibles(String origen, TipoVehiculo tipo) {
		LocalDateTime ahora = LocalDateTime.now();

		List<Viaje> viajes;

		if (tipo != null && origen != null && !origen.isBlank()) {
			// filtro por origen Y tipo
			viajes = viajeRepository.buscarPorOrigen(origen, ahora).stream()
					.filter(v -> v.getVehiculo().getTipo() == tipo).toList();
		} else if (tipo != null) {
			// solo filtro por tipo
			viajes = viajeRepository.buscarPorTipoVehiculo(tipo, ahora);
		} else if (origen != null && !origen.isBlank()) {
			// solo filtro por origen
			viajes = viajeRepository.buscarPorOrigen(origen, ahora);
		} else {
			// sin filtros
			viajes = viajeRepository.findByEstadoAndCuposDisponiblesGreaterThanAndFechaHoraSalidaAfter(
					EstadoViaje.PROGRAMADO, 0, ahora);
		}

		return viajes.stream().map(this::toResponse).toList();
	}

	// FIX: mismo motivo que buscarDisponibles().
	@Transactional(readOnly = true)
	public List<ViajeResponse> misViajes(UUID conductorId) {
		return viajeRepository.findByConductorIdOrderByFechaHoraSalidaAsc(conductorId).stream().map(this::toResponse)
				.toList();
	}

	// FIX: mismo motivo que buscarDisponibles().
	@Transactional(readOnly = true)
	public ViajeResponse detalle(UUID viajeId) {
		return toResponse(viajeRepository.findById(viajeId)
				.orElseThrow(() -> new IllegalArgumentException("Viaje no encontrado")));
	}

	@Transactional
	public void cancelar(UUID conductorId, UUID viajeId) {
		Viaje viaje = viajeRepository.findById(viajeId)
				.orElseThrow(() -> new IllegalArgumentException("Viaje no encontrado"));

		if (!viaje.getConductor().getId().equals(conductorId)) {
			throw new IllegalArgumentException("No tienes permiso para cancelar este viaje");
		}

		if (viaje.getEstado() == EstadoViaje.COMPLETADO) {
			throw new IllegalArgumentException("No puedes cancelar un viaje ya completado");
		}

		viaje.setEstado(EstadoViaje.CANCELADO);
		viajeRepository.save(viaje);
		publicarEventoUbicacion(viaje);
	}

	@Transactional
	public ViajeResponse iniciar(UUID conductorId, UUID viajeId) {
		Viaje viaje = viajeRepository.findById(viajeId)
				.orElseThrow(() -> new IllegalArgumentException("Viaje no encontrado"));
		if (!viaje.getConductor().getId().equals(conductorId))
			throw new IllegalArgumentException("No tienes permiso");
		if (viaje.getEstado() != EstadoViaje.PROGRAMADO)
			throw new IllegalArgumentException("El viaje no está en estado PROGRAMADO");
		viaje.setEstado(EstadoViaje.EN_CURSO);
		Viaje guardado = viajeRepository.save(viaje);
		// Avisa a quien ya esté suscrito (pantalla de seguimiento del
		// pasajero abierta esperando) que el viaje acaba de arrancar.
		publicarEventoUbicacion(guardado);
		return toResponse(guardado);
	}

	@Transactional
	public ViajeResponse completar(UUID conductorId, UUID viajeId) {
		Viaje viaje = viajeRepository.findById(viajeId)
				.orElseThrow(() -> new IllegalArgumentException("Viaje no encontrado"));
		if (!viaje.getConductor().getId().equals(conductorId))
			throw new IllegalArgumentException("No tienes permiso");
		if (viaje.getEstado() != EstadoViaje.EN_CURSO)
			throw new IllegalArgumentException("El viaje no está EN_CURSO");
		viaje.setEstado(EstadoViaje.COMPLETADO);

		// Cualquier reserva CONFIRMADA sin decisión de abordaje (el conductor
		// nunca la marcó explícitamente) se cierra como "no se presentó",
		// para no dejar reservas en un estado ambiguo al terminar el viaje.
		for (Reserva r : reservaRepository.findByViaje(viaje)) {
			if (r.getEstado() == EstadoReserva.CONFIRMADA && r.getAbordo() == null) {
				r.setAbordo(false);
				reservaRepository.save(r);
			}
		}

		Viaje guardado = viajeRepository.save(viaje);
		// Avisa a la pantalla de seguimiento del pasajero que el viaje
		// terminó, para que deje de esperar más posiciones y se cierre.
		publicarEventoUbicacion(guardado);
		return toResponse(guardado);
	}

	/**
	 * El conductor reporta su posición GPS mientras el viaje está EN_CURSO.
	 * Se llama desde el WebSocket (ViajeWebSocketController), no por REST,
	 * porque llega muchas veces por minuto y no necesita respuesta HTTP.
	 */
	@Transactional
	public UbicacionEvento actualizarUbicacion(UUID conductorId, UbicacionRequest request) {
		Viaje viaje = viajeRepository.findById(request.getViajeId())
				.orElseThrow(() -> new IllegalArgumentException("Viaje no encontrado"));

		if (!viaje.getConductor().getId().equals(conductorId)) {
			throw new IllegalArgumentException("No tienes permiso para reportar la ubicación de este viaje");
		}
		if (viaje.getEstado() != EstadoViaje.EN_CURSO) {
			throw new IllegalArgumentException("Solo se puede reportar ubicación mientras el viaje está EN_CURSO");
		}

		viaje.setUbicacionLat(request.getLat());
		viaje.setUbicacionLng(request.getLng());
		viaje.setUbicacionActualizadaEn(LocalDateTime.now());
		Viaje guardado = viajeRepository.save(viaje);

		UbicacionEvento evento = publicarEventoUbicacion(guardado);
		return evento;
	}

	/**
	 * GET de respaldo: última posición conocida del viaje. Lo usa la
	 * pantalla del pasajero al abrir (antes de que llegue algo por
	 * WebSocket) y sirve como plan B si el WebSocket no logra conectar.
	 */
	@Transactional(readOnly = true)
	public UbicacionEvento obtenerUbicacion(UUID viajeId) {
		Viaje viaje = viajeRepository.findById(viajeId)
				.orElseThrow(() -> new IllegalArgumentException("Viaje no encontrado"));
		return UbicacionEvento.builder()
				.viajeId(viaje.getId().toString())
				.estado(viaje.getEstado().name())
				.lat(viaje.getUbicacionLat())
				.lng(viaje.getUbicacionLng())
				.actualizadaEn(viaje.getUbicacionActualizadaEn())
				.build();
	}

	/** Arma y difunde el evento de ubicación/estado a /topic/viaje.{id}.ubicacion */
	private UbicacionEvento publicarEventoUbicacion(Viaje viaje) {
		UbicacionEvento evento = UbicacionEvento.builder()
				.viajeId(viaje.getId().toString())
				.estado(viaje.getEstado().name())
				.lat(viaje.getUbicacionLat())
				.lng(viaje.getUbicacionLng())
				.actualizadaEn(viaje.getUbicacionActualizadaEn())
				.build();
		messagingTemplate.convertAndSend("/topic/viaje." + viaje.getId() + ".ubicacion", evento);
		return evento;
	}

	// FIX: se agregaron origenLat/origenLng/destinoLat/destinoLng al DTO de
	// respuesta. Sin esto, el frontend nunca recibe coordenadas y no puede
	// pintar el viaje en el mapa, aunque el error 500 ya esté resuelto.
	private ViajeResponse toResponse(Viaje v) {
		return ViajeResponse.builder().id(v.getId().toString())
				.conductorId(v.getConductor().getId().toString())
				.conductorNombre(v.getConductor().getNombre() + " " + v.getConductor().getApellido())
				.vehiculoPlaca(v.getVehiculo().getPlaca())
				.vehiculoDescripcion(v.getVehiculo().getMarca() + " " + v.getVehiculo().getModelo() + " "
						+ v.getVehiculo().getColor())
				.origenDescripcion(v.getOrigenDescripcion())
				.origenLat(v.getOrigenLat())
				.origenLng(v.getOrigenLng())
				.destinoDescripcion(v.getDestinoDescripcion())
				.destinoLat(v.getDestinoLat())
				.destinoLng(v.getDestinoLng())
				.fechaHoraSalida(v.getFechaHoraSalida()).cuposDisponibles(v.getCuposDisponibles())
				.cuposTotales(v.getCuposTotales()).aportePorPasajero(v.getAportePorPasajero())
				.estado(v.getEstado().name()).notas(v.getNotas())
				.ubicacionLat(v.getUbicacionLat()).ubicacionLng(v.getUbicacionLng())
				.ubicacionActualizadaEn(v.getUbicacionActualizadaEn()).build();
	}
}
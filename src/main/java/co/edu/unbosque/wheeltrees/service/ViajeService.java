package co.edu.unbosque.wheeltrees.service;

import co.edu.unbosque.wheeltrees.DTO.ViajeDTO.*;
import co.edu.unbosque.wheeltrees.model.*;
import co.edu.unbosque.wheeltrees.repository.*;
import lombok.RequiredArgsConstructor;
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

	@Transactional
	public ViajeResponse publicar(UUID conductorId, PublicarViajeRequest request) {

		Usuario conductor = usuarioRepository.findById(conductorId)
				.orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

		if (conductor.getRol() != RolUsuario.CONDUCTOR) {
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

	public List<ViajeResponse> buscarDisponibles(String origen) {
		LocalDateTime ahora = LocalDateTime.now();

		List<Viaje> viajes = (origen != null && !origen.isBlank()) ? viajeRepository.buscarPorOrigen(origen, ahora)
				: viajeRepository.findByEstadoAndCuposDisponiblesGreaterThanAndFechaHoraSalidaAfter(
						EstadoViaje.PROGRAMADO, 0, ahora);

		return viajes.stream().map(this::toResponse).toList();
	}

	public List<ViajeResponse> misViajes(UUID conductorId) {
		return viajeRepository.findByConductorIdOrderByFechaHoraSalidaAsc(conductorId).stream().map(this::toResponse)
				.toList();
	}

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
	}

	private ViajeResponse toResponse(Viaje v) {
		return ViajeResponse.builder().id(v.getId().toString())
				.conductorNombre(v.getConductor().getNombre() + " " + v.getConductor().getApellido())
				.vehiculoPlaca(v.getVehiculo().getPlaca())
				.vehiculoDescripcion(v.getVehiculo().getMarca() + " " + v.getVehiculo().getModelo() + " "
						+ v.getVehiculo().getColor())
				.origenDescripcion(v.getOrigenDescripcion()).destinoDescripcion(v.getDestinoDescripcion())
				.fechaHoraSalida(v.getFechaHoraSalida()).cuposDisponibles(v.getCuposDisponibles())
				.cuposTotales(v.getCuposTotales()).aportePorPasajero(v.getAportePorPasajero())
				.estado(v.getEstado().name()).notas(v.getNotas()).build();
	}
}
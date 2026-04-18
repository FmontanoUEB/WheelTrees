package co.edu.unbosque.wheeltrees.service;

import co.edu.unbosque.wheeltrees.DTO.ReservaDTO.*;
import co.edu.unbosque.wheeltrees.model.*;
import co.edu.unbosque.wheeltrees.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReservaService {

    private final ReservaRepository reservaRepository;
    private final ViajeRepository viajeRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public ReservaResponse solicitar(UUID pasajeroId, SolicitarReservaRequest request) {

        Usuario pasajero = usuarioRepository.findById(pasajeroId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        if (pasajero.getRol() != RolUsuario.PASAJERO) {
            throw new IllegalArgumentException("Solo los pasajeros pueden reservar cupos");
        }

        Viaje viaje = viajeRepository.findById(request.getViajeId())
                .orElseThrow(() -> new IllegalArgumentException("Viaje no encontrado"));

        if (viaje.getEstado() != EstadoViaje.PROGRAMADO) {
            throw new IllegalArgumentException("Este viaje ya no está disponible");
        }

        if (viaje.getCuposDisponibles() <= 0) {
            throw new IllegalArgumentException("No hay cupos disponibles en este viaje");
        }

        if (viaje.getConductor().getId().equals(pasajeroId)) {
            throw new IllegalArgumentException("No puedes reservar en tu propio viaje");
        }

        if (reservaRepository.existsByViajeAndPasajero(viaje, pasajero)) {
            throw new IllegalArgumentException("Ya tienes una reserva en este viaje");
        }

        Reserva reserva = Reserva.builder()
                .viaje(viaje)
                .pasajero(pasajero)
                .estado(EstadoReserva.PENDIENTE)
                .notasPasajero(request.getNotasPasajero())
                .build();

        // Descontar cupo inmediatamente al solicitar
        viaje.setCuposDisponibles(viaje.getCuposDisponibles() - 1);
        viajeRepository.save(viaje);

        return toResponse(reservaRepository.save(reserva));
    }

    // Conductor acepta o rechaza una reserva
    @Transactional
    public ReservaResponse responder(UUID conductorId, UUID reservaId, ResponderReservaRequest request) {

        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada"));

        if (!reserva.getViaje().getConductor().getId().equals(conductorId)) {
            throw new IllegalArgumentException("No tienes permiso para responder esta reserva");
        }

        if (reserva.getEstado() != EstadoReserva.PENDIENTE) {
            throw new IllegalArgumentException("Esta reserva ya fue respondida");
        }

        if (request.isAceptar()) {
            reserva.setEstado(EstadoReserva.CONFIRMADA);
        } else {
            reserva.setEstado(EstadoReserva.RECHAZADA);
            // Devolver el cupo si se rechaza
            Viaje viaje = reserva.getViaje();
            viaje.setCuposDisponibles(viaje.getCuposDisponibles() + 1);
            viajeRepository.save(viaje);
        }

        return toResponse(reservaRepository.save(reserva));
    }

    // Pasajero cancela su propia reserva
    @Transactional
    public ReservaResponse cancelar(UUID pasajeroId, UUID reservaId) {

        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada"));

        if (!reserva.getPasajero().getId().equals(pasajeroId)) {
            throw new IllegalArgumentException("No tienes permiso para cancelar esta reserva");
        }

        if (reserva.getEstado() == EstadoReserva.CANCELADA
                || reserva.getEstado() == EstadoReserva.COMPLETADA) {
            throw new IllegalArgumentException("Esta reserva no puede cancelarse");
        }

        reserva.setEstado(EstadoReserva.CANCELADA);

        // Devolver el cupo si estaba pendiente o confirmada
        Viaje viaje = reserva.getViaje();
        if (viaje.getEstado() == EstadoViaje.PROGRAMADO) {
            viaje.setCuposDisponibles(viaje.getCuposDisponibles() + 1);
            viajeRepository.save(viaje);
        }

        return toResponse(reservaRepository.save(reserva));
    }

    // Pasajero ve sus reservas
    public List<ReservaResponse> misReservas(UUID pasajeroId) {
        Usuario pasajero = usuarioRepository.findById(pasajeroId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        return reservaRepository.findByPasajero(pasajero)
                .stream().map(this::toResponse).toList();
    }

    // Conductor ve las reservas de su viaje
    public List<ReservaResponse> reservasDeViaje(UUID conductorId, UUID viajeId) {
        Viaje viaje = viajeRepository.findById(viajeId)
                .orElseThrow(() -> new IllegalArgumentException("Viaje no encontrado"));

        if (!viaje.getConductor().getId().equals(conductorId)) {
            throw new IllegalArgumentException("No tienes permiso para ver estas reservas");
        }

        return reservaRepository.findByViaje(viaje)
                .stream().map(this::toResponse).toList();
    }

    private ReservaResponse toResponse(Reserva r) {
        return ReservaResponse.builder()
                .id(r.getId().toString())
                .viajeId(r.getViaje().getId().toString())
                .origenViaje(r.getViaje().getOrigenDescripcion())
                .fechaHoraSalida(r.getViaje().getFechaHoraSalida())
                .pasajeroNombre(r.getPasajero().getNombre() + " " + r.getPasajero().getApellido())
                .pasajeroEmail(r.getPasajero().getEmail())
                .estado(r.getEstado().name())
                .notasPasajero(r.getNotasPasajero())
                .creadoEn(r.getCreadoEn())
                .build();
    }
}
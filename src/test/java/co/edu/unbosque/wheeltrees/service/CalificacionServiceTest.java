package co.edu.unbosque.wheeltrees.service;

import co.edu.unbosque.wheeltrees.DTO.CalificacionDTO.CalificacionPendiente;
import co.edu.unbosque.wheeltrees.DTO.CalificacionDTO.CalificacionResponse;
import co.edu.unbosque.wheeltrees.DTO.CalificacionDTO.CrearCalificacionRequest;
import co.edu.unbosque.wheeltrees.model.*;
import co.edu.unbosque.wheeltrees.repository.CalificacionRepository;
import co.edu.unbosque.wheeltrees.repository.ReservaRepository;
import co.edu.unbosque.wheeltrees.repository.UsuarioRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CalificacionServiceTest {

	@Mock
	private CalificacionRepository calificacionRepository;
	@Mock
	private ReservaRepository reservaRepository;
	@Mock
	private UsuarioRepository usuarioRepository;

	@InjectMocks
	private CalificacionService calificacionService;

	private UUID pasajeroId;
	private UUID conductorId;
	private Usuario pasajero;
	private Usuario conductor;
	private Viaje viaje;
	private Reserva reserva;

	@BeforeEach
	void setUp() {
		pasajeroId = UUID.randomUUID();
		conductorId = UUID.randomUUID();

		pasajero = Usuario.builder().id(pasajeroId).nombre("Ana").apellido("Ríos").rol(RolUsuario.PASAJERO).build();
		conductor = Usuario.builder().id(conductorId).nombre("Carlos").apellido("Pérez").rol(RolUsuario.CONDUCTOR)
				.build();

		Vehiculo vehiculo = Vehiculo.builder().id(UUID.randomUUID()).conductor(conductor).build();
		viaje = Viaje.builder().id(UUID.randomUUID()).conductor(conductor).vehiculo(vehiculo)
				.origenDescripcion("Suba").fechaHoraSalida(LocalDateTime.now()).build();
		reserva = Reserva.builder().id(UUID.randomUUID()).viaje(viaje).pasajero(pasajero)
				.estado(EstadoReserva.COMPLETADA).build();
	}

	// ---------- calificar ----------

	@Test
	void calificarDebeFallarSiElCalificadorNoExiste() {
		when(usuarioRepository.findById(pasajeroId)).thenReturn(Optional.empty());
		CrearCalificacionRequest request = CrearCalificacionRequest.builder().reservaId(reserva.getId())
				.puntuacion(5).build();

		assertThrows(IllegalArgumentException.class, () -> calificacionService.calificar(pasajeroId, request));
	}

	@Test
	void calificarDebeFallarSiLaReservaNoExiste() {
		when(usuarioRepository.findById(pasajeroId)).thenReturn(Optional.of(pasajero));
		when(reservaRepository.findById(reserva.getId())).thenReturn(Optional.empty());
		CrearCalificacionRequest request = CrearCalificacionRequest.builder().reservaId(reserva.getId())
				.puntuacion(5).build();

		assertThrows(IllegalArgumentException.class, () -> calificacionService.calificar(pasajeroId, request));
	}

	@Test
	void calificarDebeFallarSiLaReservaNoEstaCompletada() {
		reserva.setEstado(EstadoReserva.CONFIRMADA);
		when(usuarioRepository.findById(pasajeroId)).thenReturn(Optional.of(pasajero));
		when(reservaRepository.findById(reserva.getId())).thenReturn(Optional.of(reserva));
		CrearCalificacionRequest request = CrearCalificacionRequest.builder().reservaId(reserva.getId())
				.puntuacion(5).build();

		assertThrows(IllegalArgumentException.class, () -> calificacionService.calificar(pasajeroId, request));
	}

	@Test
	void calificarDebeFallarSiElCalificadorNoEsPasajeroNiConductorDeLaReserva() {
		UUID ajenoId = UUID.randomUUID();
		Usuario ajeno = Usuario.builder().id(ajenoId).build();
		when(usuarioRepository.findById(ajenoId)).thenReturn(Optional.of(ajeno));
		when(reservaRepository.findById(reserva.getId())).thenReturn(Optional.of(reserva));
		CrearCalificacionRequest request = CrearCalificacionRequest.builder().reservaId(reserva.getId())
				.puntuacion(5).build();

		assertThrows(IllegalArgumentException.class, () -> calificacionService.calificar(ajenoId, request));
	}

	@Test
	void calificarDebeFallarSiYaCalificoEstaReserva() {
		when(usuarioRepository.findById(pasajeroId)).thenReturn(Optional.of(pasajero));
		when(reservaRepository.findById(reserva.getId())).thenReturn(Optional.of(reserva));
		when(calificacionRepository.existsByReservaAndCalificador(reserva, pasajero)).thenReturn(true);
		CrearCalificacionRequest request = CrearCalificacionRequest.builder().reservaId(reserva.getId())
				.puntuacion(5).build();

		assertThrows(IllegalArgumentException.class, () -> calificacionService.calificar(pasajeroId, request));
	}

	@Test
	void calificarComoPasajeroDebeCalificarAlConductorYRecalcularPromedio() {
		when(usuarioRepository.findById(pasajeroId)).thenReturn(Optional.of(pasajero));
		when(reservaRepository.findById(reserva.getId())).thenReturn(Optional.of(reserva));
		when(calificacionRepository.existsByReservaAndCalificador(reserva, pasajero)).thenReturn(false);
		when(calificacionRepository.save(any(Calificacion.class))).thenAnswer(inv -> {
			Calificacion c = inv.getArgument(0);
			c.setId(UUID.randomUUID());
			c.setCreadoEn(LocalDateTime.now());
			return c;
		});
		Calificacion existente = Calificacion.builder().puntuacion(5).calificado(conductor).build();
		when(calificacionRepository.findByCalificado(conductor)).thenReturn(List.of(existente));

		CrearCalificacionRequest request = CrearCalificacionRequest.builder().reservaId(reserva.getId())
				.puntuacion(5).comentario("Excelente viaje").build();

		CalificacionResponse response = calificacionService.calificar(pasajeroId, request);

		assertEquals(conductorId.toString(), response.getCalificadoId());
		assertEquals(5.0, conductor.getCalificacionPromedio());
		assertEquals(1, conductor.getTotalCalificaciones());
		verify(usuarioRepository, times(1)).save(conductor);
	}

	@Test
	void calificarComoConductorDebeCalificarAlPasajero() {
		when(usuarioRepository.findById(conductorId)).thenReturn(Optional.of(conductor));
		when(reservaRepository.findById(reserva.getId())).thenReturn(Optional.of(reserva));
		when(calificacionRepository.existsByReservaAndCalificador(reserva, conductor)).thenReturn(false);
		when(calificacionRepository.save(any(Calificacion.class))).thenAnswer(inv -> {
			Calificacion c = inv.getArgument(0);
			c.setId(UUID.randomUUID());
			c.setCreadoEn(LocalDateTime.now());
			return c;
		});
		when(calificacionRepository.findByCalificado(pasajero)).thenReturn(List.of());

		CrearCalificacionRequest request = CrearCalificacionRequest.builder().reservaId(reserva.getId())
				.puntuacion(4).build();

		CalificacionResponse response = calificacionService.calificar(conductorId, request);

		assertEquals(pasajeroId.toString(), response.getCalificadoId());
	}

	// ---------- pendientes ----------

	@Test
	void pendientesDebeFallarSiElUsuarioNoExiste() {
		when(usuarioRepository.findById(pasajeroId)).thenReturn(Optional.empty());

		assertThrows(IllegalArgumentException.class, () -> calificacionService.pendientes(pasajeroId));
	}

	@Test
	void pendientesDebeIncluirReservaComoPasajeroSinCalificarAun() {
		when(usuarioRepository.findById(pasajeroId)).thenReturn(Optional.of(pasajero));
		when(reservaRepository.findByPasajeroIdAndEstado(pasajeroId, EstadoReserva.COMPLETADA))
				.thenReturn(List.of(reserva));
		when(reservaRepository.findByViajeConductorIdAndEstado(pasajeroId, EstadoReserva.COMPLETADA))
				.thenReturn(List.of());
		when(calificacionRepository.existsByReservaAndCalificador(reserva, pasajero)).thenReturn(false);

		List<CalificacionPendiente> pendientes = calificacionService.pendientes(pasajeroId);

		assertEquals(1, pendientes.size());
		assertEquals(conductorId.toString(), pendientes.get(0).getACalificarId());
	}

	@Test
	void pendientesNoDebeIncluirReservaYaCalificada() {
		when(usuarioRepository.findById(pasajeroId)).thenReturn(Optional.of(pasajero));
		when(reservaRepository.findByPasajeroIdAndEstado(pasajeroId, EstadoReserva.COMPLETADA))
				.thenReturn(List.of(reserva));
		when(reservaRepository.findByViajeConductorIdAndEstado(pasajeroId, EstadoReserva.COMPLETADA))
				.thenReturn(List.of());
		when(calificacionRepository.existsByReservaAndCalificador(reserva, pasajero)).thenReturn(true);

		List<CalificacionPendiente> pendientes = calificacionService.pendientes(pasajeroId);

		assertTrue(pendientes.isEmpty());
	}

	@Test
	void pendientesDebeIncluirReservaComoConductorSinCalificarAun() {
		when(usuarioRepository.findById(conductorId)).thenReturn(Optional.of(conductor));
		when(reservaRepository.findByPasajeroIdAndEstado(conductorId, EstadoReserva.COMPLETADA))
				.thenReturn(List.of());
		when(reservaRepository.findByViajeConductorIdAndEstado(conductorId, EstadoReserva.COMPLETADA))
				.thenReturn(List.of(reserva));
		when(calificacionRepository.existsByReservaAndCalificador(reserva, conductor)).thenReturn(false);

		List<CalificacionPendiente> pendientes = calificacionService.pendientes(conductorId);

		assertEquals(1, pendientes.size());
		assertEquals(pasajeroId.toString(), pendientes.get(0).getACalificarId());
	}
}

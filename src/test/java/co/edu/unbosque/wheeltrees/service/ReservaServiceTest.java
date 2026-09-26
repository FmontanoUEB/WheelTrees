package co.edu.unbosque.wheeltrees.service;

import co.edu.unbosque.wheeltrees.DTO.ReservaDTO.MarcarAbordoRequest;
import co.edu.unbosque.wheeltrees.DTO.ReservaDTO.ResponderReservaRequest;
import co.edu.unbosque.wheeltrees.DTO.ReservaDTO.ReservaResponse;
import co.edu.unbosque.wheeltrees.DTO.ReservaDTO.SolicitarReservaRequest;
import co.edu.unbosque.wheeltrees.model.*;
import co.edu.unbosque.wheeltrees.repository.ReservaRepository;
import co.edu.unbosque.wheeltrees.repository.UsuarioRepository;
import co.edu.unbosque.wheeltrees.repository.ViajeRepository;

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
class ReservaServiceTest {

	@Mock
	private ReservaRepository reservaRepository;
	@Mock
	private ViajeRepository viajeRepository;
	@Mock
	private UsuarioRepository usuarioRepository;

	@InjectMocks
	private ReservaService reservaService;

	private UUID pasajeroId;
	private UUID conductorId;
	private Usuario pasajero;
	private Usuario conductor;
	private Vehiculo vehiculo;
	private Viaje viaje;

	@BeforeEach
	void setUp() {
		pasajeroId = UUID.randomUUID();
		conductorId = UUID.randomUUID();

		pasajero = Usuario.builder().id(pasajeroId).nombre("Ana").apellido("Ríos")
				.email("ana@unbosque.edu.co").rol(RolUsuario.PASAJERO).build();
		conductor = Usuario.builder().id(conductorId).nombre("Carlos").apellido("Pérez")
				.email("carlos@unbosque.edu.co").rol(RolUsuario.CONDUCTOR).build();

		vehiculo = Vehiculo.builder().id(UUID.randomUUID()).conductor(conductor).placa("ABC123")
				.marca("Mazda").modelo("3").color("Rojo").build();

		viaje = Viaje.builder().id(UUID.randomUUID()).conductor(conductor).vehiculo(vehiculo)
				.origenDescripcion("Suba").destinoDescripcion("Universidad")
				.fechaHoraSalida(LocalDateTime.now().plusHours(1)).cuposTotales(4).cuposDisponibles(2)
				.estado(EstadoViaje.PROGRAMADO).build();
	}

	// ---------- solicitar ----------

	@Test
	void solicitarDebeFallarSiElUsuarioNoExiste() {
		when(usuarioRepository.findById(pasajeroId)).thenReturn(Optional.empty());
		SolicitarReservaRequest request = SolicitarReservaRequest.builder().viajeId(viaje.getId()).build();

		assertThrows(IllegalArgumentException.class, () -> reservaService.solicitar(pasajeroId, request));
	}

	@Test
	void solicitarDebeFallarSiElUsuarioNoEsPasajeroNiAmbos() {
		pasajero.setRol(RolUsuario.CONDUCTOR);
		when(usuarioRepository.findById(pasajeroId)).thenReturn(Optional.of(pasajero));
		SolicitarReservaRequest request = SolicitarReservaRequest.builder().viajeId(viaje.getId()).build();

		assertThrows(IllegalArgumentException.class, () -> reservaService.solicitar(pasajeroId, request));
	}

	@Test
	void solicitarDebeFallarSiElViajeNoExiste() {
		when(usuarioRepository.findById(pasajeroId)).thenReturn(Optional.of(pasajero));
		when(viajeRepository.findById(viaje.getId())).thenReturn(Optional.empty());
		SolicitarReservaRequest request = SolicitarReservaRequest.builder().viajeId(viaje.getId()).build();

		assertThrows(IllegalArgumentException.class, () -> reservaService.solicitar(pasajeroId, request));
	}

	@Test
	void solicitarDebeFallarSiElViajeNoEstaProgramado() {
		viaje.setEstado(EstadoViaje.CANCELADO);
		when(usuarioRepository.findById(pasajeroId)).thenReturn(Optional.of(pasajero));
		when(viajeRepository.findById(viaje.getId())).thenReturn(Optional.of(viaje));
		SolicitarReservaRequest request = SolicitarReservaRequest.builder().viajeId(viaje.getId()).build();

		assertThrows(IllegalArgumentException.class, () -> reservaService.solicitar(pasajeroId, request));
	}

	@Test
	void solicitarDebeFallarSiNoHayCuposDisponibles() {
		viaje.setCuposDisponibles(0);
		when(usuarioRepository.findById(pasajeroId)).thenReturn(Optional.of(pasajero));
		when(viajeRepository.findById(viaje.getId())).thenReturn(Optional.of(viaje));
		SolicitarReservaRequest request = SolicitarReservaRequest.builder().viajeId(viaje.getId()).build();

		assertThrows(IllegalArgumentException.class, () -> reservaService.solicitar(pasajeroId, request));
	}

	@Test
	void solicitarDebeFallarSiElPasajeroEsElConductorDelViaje() {
		// Rol AMBOS a propósito: así pasa el chequeo de rol y la ejecución
		// llega hasta la validación de "no puedes reservar en tu propio
		// viaje", que es lo que este test verifica.
		conductor.setRol(RolUsuario.AMBOS);
		when(usuarioRepository.findById(conductorId)).thenReturn(Optional.of(conductor));
		when(viajeRepository.findById(viaje.getId())).thenReturn(Optional.of(viaje));
		SolicitarReservaRequest request = SolicitarReservaRequest.builder().viajeId(viaje.getId()).build();

		assertThrows(IllegalArgumentException.class, () -> reservaService.solicitar(conductorId, request));
	}

	@Test
	void solicitarDebeFallarSiYaTieneUnaReservaEnElViaje() {
		when(usuarioRepository.findById(pasajeroId)).thenReturn(Optional.of(pasajero));
		when(viajeRepository.findById(viaje.getId())).thenReturn(Optional.of(viaje));
		when(reservaRepository.existsByViajeAndPasajero(viaje, pasajero)).thenReturn(true);
		SolicitarReservaRequest request = SolicitarReservaRequest.builder().viajeId(viaje.getId()).build();

		assertThrows(IllegalArgumentException.class, () -> reservaService.solicitar(pasajeroId, request));
	}

	@Test
	void solicitarDebeDescontarCupoYCrearReservaPendienteCuandoTodoEsValido() {
		when(usuarioRepository.findById(pasajeroId)).thenReturn(Optional.of(pasajero));
		when(viajeRepository.findById(viaje.getId())).thenReturn(Optional.of(viaje));
		when(reservaRepository.existsByViajeAndPasajero(viaje, pasajero)).thenReturn(false);
		when(reservaRepository.save(any(Reserva.class))).thenAnswer(inv -> {
			Reserva r = inv.getArgument(0);
			r.setId(UUID.randomUUID());
			return r;
		});
		SolicitarReservaRequest request = SolicitarReservaRequest.builder().viajeId(viaje.getId())
				.notasPasajero("Voy con maleta").build();

		ReservaResponse response = reservaService.solicitar(pasajeroId, request);

		assertEquals(1, viaje.getCuposDisponibles());
		assertEquals("PENDIENTE", response.getEstado());
		verify(viajeRepository, times(1)).save(viaje);
	}

	// ---------- responder ----------

	@Test
	void responderDebeFallarSiLaReservaNoExiste() {
		UUID reservaId = UUID.randomUUID();
		when(reservaRepository.findById(reservaId)).thenReturn(Optional.empty());
		ResponderReservaRequest request = ResponderReservaRequest.builder().aceptar(true).build();

		assertThrows(IllegalArgumentException.class, () -> reservaService.responder(conductorId, reservaId, request));
	}

	@Test
	void responderDebeFallarSiElConductorNoEsDueñoDelViaje() {
		Reserva reserva = construirReserva(EstadoReserva.PENDIENTE);
		UUID otroConductorId = UUID.randomUUID();
		when(reservaRepository.findById(reserva.getId())).thenReturn(Optional.of(reserva));
		ResponderReservaRequest request = ResponderReservaRequest.builder().aceptar(true).build();

		assertThrows(IllegalArgumentException.class,
				() -> reservaService.responder(otroConductorId, reserva.getId(), request));
	}

	@Test
	void responderDebeFallarSiLaReservaYaFueRespondida() {
		Reserva reserva = construirReserva(EstadoReserva.CONFIRMADA);
		when(reservaRepository.findById(reserva.getId())).thenReturn(Optional.of(reserva));
		ResponderReservaRequest request = ResponderReservaRequest.builder().aceptar(true).build();

		assertThrows(IllegalArgumentException.class,
				() -> reservaService.responder(conductorId, reserva.getId(), request));
	}

	@Test
	void responderDebeConfirmarLaReservaCuandoElConductorAcepta() {
		Reserva reserva = construirReserva(EstadoReserva.PENDIENTE);
		when(reservaRepository.findById(reserva.getId())).thenReturn(Optional.of(reserva));
		when(reservaRepository.save(any(Reserva.class))).thenAnswer(inv -> inv.getArgument(0));
		ResponderReservaRequest request = ResponderReservaRequest.builder().aceptar(true).build();

		ReservaResponse response = reservaService.responder(conductorId, reserva.getId(), request);

		assertEquals("CONFIRMADA", response.getEstado());
		verify(viajeRepository, never()).save(any());
	}

	@Test
	void responderDebeRechazarYDevolverElCupoCuandoElConductorRechaza() {
		Reserva reserva = construirReserva(EstadoReserva.PENDIENTE);
		when(reservaRepository.findById(reserva.getId())).thenReturn(Optional.of(reserva));
		when(reservaRepository.save(any(Reserva.class))).thenAnswer(inv -> inv.getArgument(0));
		int cuposAntes = viaje.getCuposDisponibles();
		ResponderReservaRequest request = ResponderReservaRequest.builder().aceptar(false).build();

		ReservaResponse response = reservaService.responder(conductorId, reserva.getId(), request);

		assertEquals("RECHAZADA", response.getEstado());
		assertEquals(cuposAntes + 1, viaje.getCuposDisponibles());
		verify(viajeRepository, times(1)).save(viaje);
	}

	// ---------- cancelar ----------

	@Test
	void cancelarDebeFallarSiLaReservaNoExiste() {
		UUID reservaId = UUID.randomUUID();
		when(reservaRepository.findById(reservaId)).thenReturn(Optional.empty());

		assertThrows(IllegalArgumentException.class, () -> reservaService.cancelar(pasajeroId, reservaId));
	}

	@Test
	void cancelarDebeFallarSiElPasajeroNoEsDueñoDeLaReserva() {
		Reserva reserva = construirReserva(EstadoReserva.CONFIRMADA);
		UUID otroPasajeroId = UUID.randomUUID();
		when(reservaRepository.findById(reserva.getId())).thenReturn(Optional.of(reserva));

		assertThrows(IllegalArgumentException.class,
				() -> reservaService.cancelar(otroPasajeroId, reserva.getId()));
	}

	@Test
	void cancelarDebeFallarSiLaReservaYaEstaCanceladaOCompletada() {
		Reserva reserva = construirReserva(EstadoReserva.CANCELADA);
		when(reservaRepository.findById(reserva.getId())).thenReturn(Optional.of(reserva));

		assertThrows(IllegalArgumentException.class, () -> reservaService.cancelar(pasajeroId, reserva.getId()));
	}

	@Test
	void cancelarDebeDevolverElCupoSiElViajeSigueProgramado() {
		Reserva reserva = construirReserva(EstadoReserva.CONFIRMADA);
		when(reservaRepository.findById(reserva.getId())).thenReturn(Optional.of(reserva));
		when(reservaRepository.save(any(Reserva.class))).thenAnswer(inv -> inv.getArgument(0));
		int cuposAntes = viaje.getCuposDisponibles();

		ReservaResponse response = reservaService.cancelar(pasajeroId, reserva.getId());

		assertEquals("CANCELADA", response.getEstado());
		assertEquals(cuposAntes + 1, viaje.getCuposDisponibles());
	}

	@Test
	void cancelarNoDebeDevolverCupoSiElViajeYaNoEstaProgramado() {
		viaje.setEstado(EstadoViaje.EN_CURSO);
		Reserva reserva = construirReserva(EstadoReserva.CONFIRMADA);
		when(reservaRepository.findById(reserva.getId())).thenReturn(Optional.of(reserva));
		when(reservaRepository.save(any(Reserva.class))).thenAnswer(inv -> inv.getArgument(0));
		int cuposAntes = viaje.getCuposDisponibles();

		reservaService.cancelar(pasajeroId, reserva.getId());

		assertEquals(cuposAntes, viaje.getCuposDisponibles());
		verify(viajeRepository, never()).save(any());
	}

	// ---------- marcarAbordo ----------

	@Test
	void marcarAbordoDebeFallarSiLaReservaNoExiste() {
		UUID reservaId = UUID.randomUUID();
		when(reservaRepository.findById(reservaId)).thenReturn(Optional.empty());
		MarcarAbordoRequest request = MarcarAbordoRequest.builder().abordo(true).build();

		assertThrows(IllegalArgumentException.class,
				() -> reservaService.marcarAbordo(conductorId, reservaId, request));
	}

	@Test
	void marcarAbordoDebeFallarSiElViajeNoEstaEnCurso() {
		Reserva reserva = construirReserva(EstadoReserva.CONFIRMADA);
		when(reservaRepository.findById(reserva.getId())).thenReturn(Optional.of(reserva));
		MarcarAbordoRequest request = MarcarAbordoRequest.builder().abordo(true).build();

		assertThrows(IllegalArgumentException.class,
				() -> reservaService.marcarAbordo(conductorId, reserva.getId(), request));
	}

	@Test
	void marcarAbordoDebeFallarSiLaReservaNoEstaConfirmada() {
		viaje.setEstado(EstadoViaje.EN_CURSO);
		Reserva reserva = construirReserva(EstadoReserva.PENDIENTE);
		when(reservaRepository.findById(reserva.getId())).thenReturn(Optional.of(reserva));
		MarcarAbordoRequest request = MarcarAbordoRequest.builder().abordo(true).build();

		assertThrows(IllegalArgumentException.class,
				() -> reservaService.marcarAbordo(conductorId, reserva.getId(), request));
	}

	@Test
	void marcarAbordoDebeActualizarElEstadoDeAbordajeCuandoTodoEsValido() {
		viaje.setEstado(EstadoViaje.EN_CURSO);
		Reserva reserva = construirReserva(EstadoReserva.CONFIRMADA);
		when(reservaRepository.findById(reserva.getId())).thenReturn(Optional.of(reserva));
		when(reservaRepository.save(any(Reserva.class))).thenAnswer(inv -> inv.getArgument(0));
		MarcarAbordoRequest request = MarcarAbordoRequest.builder().abordo(true).build();

		ReservaResponse response = reservaService.marcarAbordo(conductorId, reserva.getId(), request);

		assertEquals(Boolean.TRUE, response.getAbordo());
	}

	// ---------- consultas ----------

	@Test
	void misReservasDebeFallarSiElUsuarioNoExiste() {
		when(usuarioRepository.findById(pasajeroId)).thenReturn(Optional.empty());

		assertThrows(IllegalArgumentException.class, () -> reservaService.misReservas(pasajeroId));
	}

	@Test
	void misReservasDebeRetornarLasReservasDelPasajero() {
		Reserva reserva = construirReserva(EstadoReserva.CONFIRMADA);
		when(usuarioRepository.findById(pasajeroId)).thenReturn(Optional.of(pasajero));
		when(reservaRepository.findByPasajero(pasajero)).thenReturn(List.of(reserva));

		List<ReservaResponse> resultado = reservaService.misReservas(pasajeroId);

		assertEquals(1, resultado.size());
	}

	@Test
	void reservasDeViajeDebeFallarSiElConductorNoEsDueñoDelViaje() {
		UUID otroConductorId = UUID.randomUUID();
		when(viajeRepository.findById(viaje.getId())).thenReturn(Optional.of(viaje));

		assertThrows(IllegalArgumentException.class,
				() -> reservaService.reservasDeViaje(otroConductorId, viaje.getId()));
	}

	@Test
	void reservasDeViajeDebeRetornarLasReservasDelViaje() {
		Reserva reserva = construirReserva(EstadoReserva.CONFIRMADA);
		when(viajeRepository.findById(viaje.getId())).thenReturn(Optional.of(viaje));
		when(reservaRepository.findByViaje(viaje)).thenReturn(List.of(reserva));

		List<ReservaResponse> resultado = reservaService.reservasDeViaje(conductorId, viaje.getId());

		assertEquals(1, resultado.size());
	}

	private Reserva construirReserva(EstadoReserva estado) {
		return Reserva.builder().id(UUID.randomUUID()).viaje(viaje).pasajero(pasajero).estado(estado)
				.creadoEn(LocalDateTime.now()).build();
	}
}
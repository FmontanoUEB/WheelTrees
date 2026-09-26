package co.edu.unbosque.wheeltrees.service;

import co.edu.unbosque.wheeltrees.DTO.ViajeDTO.PublicarViajeRequest;
import co.edu.unbosque.wheeltrees.DTO.ViajeDTO.UbicacionEvento;
import co.edu.unbosque.wheeltrees.DTO.ViajeDTO.UbicacionRequest;
import co.edu.unbosque.wheeltrees.DTO.ViajeDTO.ViajeResponse;
import co.edu.unbosque.wheeltrees.model.*;
import co.edu.unbosque.wheeltrees.repository.ReservaRepository;
import co.edu.unbosque.wheeltrees.repository.UsuarioRepository;
import co.edu.unbosque.wheeltrees.repository.VehiculoRepository;
import co.edu.unbosque.wheeltrees.repository.ViajeRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ViajeServiceTest {

	@Mock
	private ViajeRepository viajeRepository;
	@Mock
	private VehiculoRepository vehiculoRepository;
	@Mock
	private UsuarioRepository usuarioRepository;
	@Mock
	private ReservaRepository reservaRepository;
	@Mock
	private SimpMessagingTemplate messagingTemplate;

	@InjectMocks
	private ViajeService viajeService;

	private UUID conductorId;
	private Usuario conductor;
	private Vehiculo vehiculo;
	private PublicarViajeRequest requestValido;

	@BeforeEach
	void setUp() {
		conductorId = UUID.randomUUID();
		conductor = Usuario.builder().id(conductorId).nombre("Carlos").apellido("Pérez")
				.email("carlos@unbosque.edu.co").rol(RolUsuario.CONDUCTOR).build();

		vehiculo = Vehiculo.builder().id(UUID.randomUUID()).conductor(conductor).placa("ABC123")
				.marca("Mazda").modelo("3").color("Rojo").capacidadPasajeros(4).activo(true).build();

		requestValido = PublicarViajeRequest.builder().vehiculoId(vehiculo.getId())
				.origenDescripcion("Suba").destinoDescripcion("Universidad")
				.fechaHoraSalida(LocalDateTime.now().plusHours(2)).cuposTotales(3)
				.aportePorPasajero(BigDecimal.TEN).build();
	}

	// ---------- publicar ----------

	@Test
	void publicarDebeFallarSiElUsuarioNoExiste() {
		when(usuarioRepository.findById(conductorId)).thenReturn(Optional.empty());

		assertThrows(IllegalArgumentException.class, () -> viajeService.publicar(conductorId, requestValido));
	}

	@Test
	void publicarDebeFallarSiElUsuarioNoEsConductorNiAmbos() {
		conductor.setRol(RolUsuario.PASAJERO);
		when(usuarioRepository.findById(conductorId)).thenReturn(Optional.of(conductor));

		assertThrows(IllegalArgumentException.class, () -> viajeService.publicar(conductorId, requestValido));
	}

	@Test
	void publicarDebeFallarSiElVehiculoNoExiste() {
		when(usuarioRepository.findById(conductorId)).thenReturn(Optional.of(conductor));
		when(vehiculoRepository.findById(vehiculo.getId())).thenReturn(Optional.empty());

		assertThrows(IllegalArgumentException.class, () -> viajeService.publicar(conductorId, requestValido));
	}

	@Test
	void publicarDebeFallarSiElVehiculoNoLePerteneceAlConductor() {
		Usuario otroConductor = Usuario.builder().id(UUID.randomUUID()).rol(RolUsuario.CONDUCTOR).build();
		vehiculo.setConductor(otroConductor);
		when(usuarioRepository.findById(conductorId)).thenReturn(Optional.of(conductor));
		when(vehiculoRepository.findById(vehiculo.getId())).thenReturn(Optional.of(vehiculo));

		assertThrows(IllegalArgumentException.class, () -> viajeService.publicar(conductorId, requestValido));
	}

	@Test
	void publicarDebeFallarSiElVehiculoEstaDesactivado() {
		vehiculo.setActivo(false);
		when(usuarioRepository.findById(conductorId)).thenReturn(Optional.of(conductor));
		when(vehiculoRepository.findById(vehiculo.getId())).thenReturn(Optional.of(vehiculo));

		assertThrows(IllegalArgumentException.class, () -> viajeService.publicar(conductorId, requestValido));
	}

	@Test
	void publicarDebeFallarSiLosCuposSuperanLaCapacidadDelVehiculo() {
		requestValido.setCuposTotales(10);
		when(usuarioRepository.findById(conductorId)).thenReturn(Optional.of(conductor));
		when(vehiculoRepository.findById(vehiculo.getId())).thenReturn(Optional.of(vehiculo));

		assertThrows(IllegalArgumentException.class, () -> viajeService.publicar(conductorId, requestValido));
	}

	@Test
	void publicarDebeFallarSiLaSalidaEsMuyPronto() {
		requestValido.setFechaHoraSalida(LocalDateTime.now().plusMinutes(5));
		when(usuarioRepository.findById(conductorId)).thenReturn(Optional.of(conductor));
		when(vehiculoRepository.findById(vehiculo.getId())).thenReturn(Optional.of(vehiculo));

		assertThrows(IllegalArgumentException.class, () -> viajeService.publicar(conductorId, requestValido));
	}

	@Test
	void publicarDebeCrearElViajeConCuposDisponiblesIgualesATotalesCuandoTodoEsValido() {
		when(usuarioRepository.findById(conductorId)).thenReturn(Optional.of(conductor));
		when(vehiculoRepository.findById(vehiculo.getId())).thenReturn(Optional.of(vehiculo));
		when(viajeRepository.save(any(Viaje.class))).thenAnswer(inv -> {
			Viaje v = inv.getArgument(0);
			v.setId(UUID.randomUUID());
			return v;
		});

		ViajeResponse response = viajeService.publicar(conductorId, requestValido);

		assertEquals(3, response.getCuposDisponibles());
		assertEquals("PROGRAMADO", response.getEstado());
	}

	// ---------- buscarDisponibles ----------

	@Test
	void buscarDisponiblesSinFiltrosDebeUsarLaConsultaGeneral() {
		Viaje viaje = construirViaje(EstadoViaje.PROGRAMADO);
		when(viajeRepository.findByEstadoAndCuposDisponiblesGreaterThanAndFechaHoraSalidaAfter(
				eq(EstadoViaje.PROGRAMADO), eq(0), any())).thenReturn(List.of(viaje));

		List<ViajeResponse> resultado = viajeService.buscarDisponibles(null, null);

		assertEquals(1, resultado.size());
	}

	@Test
	void buscarDisponiblesConOrigenDebeFiltrarPorTexto() {
		Viaje viaje = construirViaje(EstadoViaje.PROGRAMADO);
		when(viajeRepository.buscarPorOrigen(eq("Suba"), any())).thenReturn(List.of(viaje));

		List<ViajeResponse> resultado = viajeService.buscarDisponibles("Suba", null);

		assertEquals(1, resultado.size());
	}

	@Test
	void buscarDisponiblesConTipoDebeFiltrarPorTipoDeVehiculo() {
		Viaje viaje = construirViaje(EstadoViaje.PROGRAMADO);
		when(viajeRepository.buscarPorTipoVehiculo(eq(TipoVehiculo.CARRO), any())).thenReturn(List.of(viaje));

		List<ViajeResponse> resultado = viajeService.buscarDisponibles(null, TipoVehiculo.CARRO);

		assertEquals(1, resultado.size());
	}

	@Test
	void buscarDisponiblesConOrigenYTipoDebeCombinarAmbosFiltros() {
		Viaje viaje = construirViaje(EstadoViaje.PROGRAMADO);
		vehiculo.setTipo(TipoVehiculo.CARRO);
		when(viajeRepository.buscarPorOrigen(eq("Suba"), any())).thenReturn(List.of(viaje));

		List<ViajeResponse> resultado = viajeService.buscarDisponibles("Suba", TipoVehiculo.CARRO);

		assertEquals(1, resultado.size());
	}

	@Test
	void buscarDisponiblesConOrigenYTipoQueNoCoincideDebeFiltrarloFuera() {
		Viaje viaje = construirViaje(EstadoViaje.PROGRAMADO);
		vehiculo.setTipo(TipoVehiculo.MOTO);
		when(viajeRepository.buscarPorOrigen(eq("Suba"), any())).thenReturn(List.of(viaje));

		List<ViajeResponse> resultado = viajeService.buscarDisponibles("Suba", TipoVehiculo.CARRO);

		assertEquals(0, resultado.size());
	}

	// ---------- misViajes / detalle ----------

	@Test
	void misViajesDebeRetornarLosViajesDelConductor() {
		Viaje viaje = construirViaje(EstadoViaje.PROGRAMADO);
		when(viajeRepository.findByConductorIdOrderByFechaHoraSalidaAsc(conductorId)).thenReturn(List.of(viaje));

		List<ViajeResponse> resultado = viajeService.misViajes(conductorId);

		assertEquals(1, resultado.size());
	}

	@Test
	void detalleDebeFallarSiElViajeNoExiste() {
		UUID viajeId = UUID.randomUUID();
		when(viajeRepository.findById(viajeId)).thenReturn(Optional.empty());

		assertThrows(IllegalArgumentException.class, () -> viajeService.detalle(viajeId));
	}

	@Test
	void detalleDebeRetornarElViajeCuandoExiste() {
		Viaje viaje = construirViaje(EstadoViaje.PROGRAMADO);
		when(viajeRepository.findById(viaje.getId())).thenReturn(Optional.of(viaje));

		ViajeResponse response = viajeService.detalle(viaje.getId());

		assertEquals(viaje.getId().toString(), response.getId());
	}

	// ---------- cancelar / iniciar / completar ----------

	@Test
	void cancelarDebeFallarSiElConductorNoEsDueño() {
		Viaje viaje = construirViaje(EstadoViaje.PROGRAMADO);
		UUID otroConductorId = UUID.randomUUID();
		when(viajeRepository.findById(viaje.getId())).thenReturn(Optional.of(viaje));

		assertThrows(IllegalArgumentException.class, () -> viajeService.cancelar(otroConductorId, viaje.getId()));
	}

	@Test
	void cancelarDebeFallarSiElViajeYaEstaCompletado() {
		Viaje viaje = construirViaje(EstadoViaje.COMPLETADO);
		when(viajeRepository.findById(viaje.getId())).thenReturn(Optional.of(viaje));

		assertThrows(IllegalArgumentException.class, () -> viajeService.cancelar(conductorId, viaje.getId()));
	}

	@Test
	void cancelarDebeCambiarElEstadoYPublicarElEvento() {
		Viaje viaje = construirViaje(EstadoViaje.PROGRAMADO);
		when(viajeRepository.findById(viaje.getId())).thenReturn(Optional.of(viaje));
		when(viajeRepository.save(any(Viaje.class))).thenAnswer(inv -> inv.getArgument(0));

		viajeService.cancelar(conductorId, viaje.getId());

		assertEquals(EstadoViaje.CANCELADO, viaje.getEstado());
		verify(messagingTemplate, times(1)).convertAndSend(anyString(), any(UbicacionEvento.class));
	}

	@Test
	void iniciarDebeFallarSiElViajeNoEstaProgramado() {
		Viaje viaje = construirViaje(EstadoViaje.EN_CURSO);
		when(viajeRepository.findById(viaje.getId())).thenReturn(Optional.of(viaje));

		assertThrows(IllegalArgumentException.class, () -> viajeService.iniciar(conductorId, viaje.getId()));
	}

	@Test
	void iniciarDebeCambiarElEstadoAEnCurso() {
		Viaje viaje = construirViaje(EstadoViaje.PROGRAMADO);
		when(viajeRepository.findById(viaje.getId())).thenReturn(Optional.of(viaje));
		when(viajeRepository.save(any(Viaje.class))).thenAnswer(inv -> inv.getArgument(0));

		ViajeResponse response = viajeService.iniciar(conductorId, viaje.getId());

		assertEquals("EN_CURSO", response.getEstado());
	}

	@Test
	void completarDebeFallarSiElViajeNoEstaEnCurso() {
		Viaje viaje = construirViaje(EstadoViaje.PROGRAMADO);
		when(viajeRepository.findById(viaje.getId())).thenReturn(Optional.of(viaje));

		assertThrows(IllegalArgumentException.class, () -> viajeService.completar(conductorId, viaje.getId()));
	}

	@Test
	void completarDebeCerrarReservasConfirmadasSinAbordajeComoNoPresentado() {
		Viaje viaje = construirViaje(EstadoViaje.EN_CURSO);
		Usuario pasajero = Usuario.builder().id(UUID.randomUUID()).rol(RolUsuario.PASAJERO).build();
		Reserva reserva = Reserva.builder().id(UUID.randomUUID()).viaje(viaje).pasajero(pasajero)
				.estado(EstadoReserva.CONFIRMADA).abordo(null).build();

		when(viajeRepository.findById(viaje.getId())).thenReturn(Optional.of(viaje));
		when(reservaRepository.findByViaje(viaje)).thenReturn(List.of(reserva));
		when(viajeRepository.save(any(Viaje.class))).thenAnswer(inv -> inv.getArgument(0));

		ViajeResponse response = viajeService.completar(conductorId, viaje.getId());

		assertEquals("COMPLETADO", response.getEstado());
		assertEquals(EstadoReserva.COMPLETADA, reserva.getEstado());
		assertEquals(Boolean.FALSE, reserva.getAbordo());
		verify(reservaRepository, times(1)).save(reserva);
	}

	@Test
	void completarNoDebeTocarReservasQueNoEstanConfirmadas() {
		Viaje viaje = construirViaje(EstadoViaje.EN_CURSO);
		Usuario pasajero = Usuario.builder().id(UUID.randomUUID()).rol(RolUsuario.PASAJERO).build();
		Reserva reserva = Reserva.builder().id(UUID.randomUUID()).viaje(viaje).pasajero(pasajero)
				.estado(EstadoReserva.CANCELADA).build();

		when(viajeRepository.findById(viaje.getId())).thenReturn(Optional.of(viaje));
		when(reservaRepository.findByViaje(viaje)).thenReturn(List.of(reserva));
		when(viajeRepository.save(any(Viaje.class))).thenAnswer(inv -> inv.getArgument(0));

		viajeService.completar(conductorId, viaje.getId());

		assertEquals(EstadoReserva.CANCELADA, reserva.getEstado());
		verify(reservaRepository, never()).save(any());
	}

	// ---------- ubicación ----------

	@Test
	void actualizarUbicacionDebeFallarSiElConductorNoEsDueño() {
		Viaje viaje = construirViaje(EstadoViaje.EN_CURSO);
		UUID otroConductorId = UUID.randomUUID();
		when(viajeRepository.findById(viaje.getId())).thenReturn(Optional.of(viaje));
		UbicacionRequest request = UbicacionRequest.builder().viajeId(viaje.getId()).lat(4.6).lng(-74.1).build();

		assertThrows(IllegalArgumentException.class,
				() -> viajeService.actualizarUbicacion(otroConductorId, request));
	}

	@Test
	void actualizarUbicacionDebeFallarSiElViajeNoEstaEnCurso() {
		Viaje viaje = construirViaje(EstadoViaje.PROGRAMADO);
		when(viajeRepository.findById(viaje.getId())).thenReturn(Optional.of(viaje));
		UbicacionRequest request = UbicacionRequest.builder().viajeId(viaje.getId()).lat(4.6).lng(-74.1).build();

		assertThrows(IllegalArgumentException.class,
				() -> viajeService.actualizarUbicacion(conductorId, request));
	}

	@Test
	void actualizarUbicacionDebeGuardarYPublicarLaNuevaPosicion() {
		Viaje viaje = construirViaje(EstadoViaje.EN_CURSO);
		when(viajeRepository.findById(viaje.getId())).thenReturn(Optional.of(viaje));
		when(viajeRepository.save(any(Viaje.class))).thenAnswer(inv -> inv.getArgument(0));
		UbicacionRequest request = UbicacionRequest.builder().viajeId(viaje.getId()).lat(4.6).lng(-74.1).build();

		UbicacionEvento evento = viajeService.actualizarUbicacion(conductorId, request);

		assertEquals(4.6, evento.getLat());
		assertEquals(-74.1, evento.getLng());
		verify(messagingTemplate, times(1)).convertAndSend(anyString(), any(UbicacionEvento.class));
	}

	@Test
	void obtenerUbicacionDebeFallarSiElViajeNoExiste() {
		UUID viajeId = UUID.randomUUID();
		when(viajeRepository.findById(viajeId)).thenReturn(Optional.empty());

		assertThrows(IllegalArgumentException.class, () -> viajeService.obtenerUbicacion(viajeId));
	}

	@Test
	void obtenerUbicacionDebeRetornarLaUltimaPosicionConocida() {
		Viaje viaje = construirViaje(EstadoViaje.EN_CURSO);
		viaje.setUbicacionLat(4.6);
		viaje.setUbicacionLng(-74.1);
		when(viajeRepository.findById(viaje.getId())).thenReturn(Optional.of(viaje));

		UbicacionEvento evento = viajeService.obtenerUbicacion(viaje.getId());

		assertEquals(4.6, evento.getLat());
		assertEquals("EN_CURSO", evento.getEstado());
	}

	private Viaje construirViaje(EstadoViaje estado) {
		return Viaje.builder().id(UUID.randomUUID()).conductor(conductor).vehiculo(vehiculo)
				.origenDescripcion("Suba").destinoDescripcion("Universidad")
				.fechaHoraSalida(LocalDateTime.now().plusHours(1)).cuposTotales(3).cuposDisponibles(2)
				.aportePorPasajero(BigDecimal.TEN).estado(estado).build();
	}
}

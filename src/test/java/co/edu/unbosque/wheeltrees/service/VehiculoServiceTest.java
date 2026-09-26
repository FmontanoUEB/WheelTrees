package co.edu.unbosque.wheeltrees.service;

import co.edu.unbosque.wheeltrees.DTO.VehiculoDTO.VehiculoRequest;
import co.edu.unbosque.wheeltrees.DTO.VehiculoDTO.VehiculoResponse;
import co.edu.unbosque.wheeltrees.model.RolUsuario;
import co.edu.unbosque.wheeltrees.model.TipoVehiculo;
import co.edu.unbosque.wheeltrees.model.Usuario;
import co.edu.unbosque.wheeltrees.model.Vehiculo;
import co.edu.unbosque.wheeltrees.repository.UsuarioRepository;
import co.edu.unbosque.wheeltrees.repository.VehiculoRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehiculoServiceTest {

	@Mock
	private VehiculoRepository vehiculoRepository;
	@Mock
	private UsuarioRepository usuarioRepository;

	@InjectMocks
	private VehiculoService vehiculoService;

	private UUID conductorId;
	private Usuario conductor;
	private VehiculoRequest requestValido;

	@BeforeEach
	void setUp() {
		conductorId = UUID.randomUUID();
		conductor = Usuario.builder().id(conductorId).nombre("Carlos").apellido("Pérez")
				.email("carlos.perez@unbosque.edu.co").rol(RolUsuario.CONDUCTOR).build();

		requestValido = VehiculoRequest.builder().placa("abc123").marca("Mazda").modelo("3").anio(2020)
				.color("Rojo").tipo(TipoVehiculo.CARRO).cedulaPropietario("123456789").capacidadPasajeros(4)
				.terminosAceptados(true).build();
	}

	@Test
	void registrarDebeFallarSiElUsuarioNoExiste() {
		when(usuarioRepository.findById(conductorId)).thenReturn(Optional.empty());

		assertThrows(IllegalArgumentException.class, () -> vehiculoService.registrar(conductorId, requestValido));
		verify(vehiculoRepository, never()).save(any());
	}

	@Test
	void registrarDebeFallarSiElUsuarioNoEsConductorNiAmbos() {
		conductor.setRol(RolUsuario.PASAJERO);
		when(usuarioRepository.findById(conductorId)).thenReturn(Optional.of(conductor));

		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> vehiculoService.registrar(conductorId, requestValido));
		assertTrue(ex.getMessage().contains("conductores"));
		verify(vehiculoRepository, never()).save(any());
	}

	@Test
	void registrarDebePermitirRolAmbos() {
		conductor.setRol(RolUsuario.AMBOS);
		when(usuarioRepository.findById(conductorId)).thenReturn(Optional.of(conductor));
		when(vehiculoRepository.existsByPlaca("ABC123")).thenReturn(false);
		when(vehiculoRepository.save(any(Vehiculo.class))).thenAnswer(inv -> {
			Vehiculo v = inv.getArgument(0);
			v.setId(UUID.randomUUID());
			return v;
		});

		VehiculoResponse response = vehiculoService.registrar(conductorId, requestValido);

		assertNotNull(response);
		assertEquals("ABC123", response.getPlaca());
	}

	@Test
	void registrarDebeFallarSiLaPlacaYaExiste() {
		when(usuarioRepository.findById(conductorId)).thenReturn(Optional.of(conductor));
		when(vehiculoRepository.existsByPlaca("ABC123")).thenReturn(true);

		assertThrows(IllegalArgumentException.class, () -> vehiculoService.registrar(conductorId, requestValido));
		verify(vehiculoRepository, never()).save(any());
	}

	@Test
	void registrarDebeFallarSiNoAceptaTerminos() {
		requestValido.setTerminosAceptados(false);
		when(usuarioRepository.findById(conductorId)).thenReturn(Optional.of(conductor));
		when(vehiculoRepository.existsByPlaca("ABC123")).thenReturn(false);

		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> vehiculoService.registrar(conductorId, requestValido));
		assertTrue(ex.getMessage().contains("términos"));
	}

	@Test
	void registrarDebeGuardarVehiculoConPlacaEnMayusculasCuandoTodoEsValido() {
		when(usuarioRepository.findById(conductorId)).thenReturn(Optional.of(conductor));
		when(vehiculoRepository.existsByPlaca("ABC123")).thenReturn(false);
		when(vehiculoRepository.save(any(Vehiculo.class))).thenAnswer(inv -> {
			Vehiculo v = inv.getArgument(0);
			v.setId(UUID.randomUUID());
			return v;
		});

		VehiculoResponse response = vehiculoService.registrar(conductorId, requestValido);

		assertEquals("ABC123", response.getPlaca());
		assertTrue(response.isActivo());
		assertTrue(response.isTerminosAceptados());
		verify(vehiculoRepository, times(1)).save(any(Vehiculo.class));
	}

	@Test
	void listarMisVehiculosDebeFallarSiElUsuarioNoExiste() {
		when(usuarioRepository.findById(conductorId)).thenReturn(Optional.empty());

		assertThrows(IllegalArgumentException.class, () -> vehiculoService.listarMisVehiculos(conductorId));
	}

	@Test
	void listarMisVehiculosDebeRetornarSoloLosActivos() {
		Vehiculo v = construirVehiculo(conductor);
		when(usuarioRepository.findById(conductorId)).thenReturn(Optional.of(conductor));
		when(vehiculoRepository.findByConductorAndActivoTrue(conductor)).thenReturn(List.of(v));

		List<VehiculoResponse> resultado = vehiculoService.listarMisVehiculos(conductorId);

		assertEquals(1, resultado.size());
		assertEquals("ABC123", resultado.get(0).getPlaca());
	}

	@Test
	void actualizarDebeFallarSiElVehiculoNoExiste() {
		UUID vehiculoId = UUID.randomUUID();
		when(vehiculoRepository.findById(vehiculoId)).thenReturn(Optional.empty());

		assertThrows(IllegalArgumentException.class,
				() -> vehiculoService.actualizar(conductorId, vehiculoId, requestValido));
	}

	@Test
	void actualizarDebeFallarSiElVehiculoNoLePerteneceAlConductor() {
		Vehiculo v = construirVehiculo(conductor);
		UUID otroConductorId = UUID.randomUUID();
		when(vehiculoRepository.findById(v.getId())).thenReturn(Optional.of(v));

		assertThrows(IllegalArgumentException.class,
				() -> vehiculoService.actualizar(otroConductorId, v.getId(), requestValido));
		verify(vehiculoRepository, never()).save(any());
	}

	@Test
	void actualizarDebeFallarSiNoAceptaTerminos() {
		Vehiculo v = construirVehiculo(conductor);
		requestValido.setTerminosAceptados(false);
		when(vehiculoRepository.findById(v.getId())).thenReturn(Optional.of(v));

		assertThrows(IllegalArgumentException.class,
				() -> vehiculoService.actualizar(conductorId, v.getId(), requestValido));
	}

	@Test
	void actualizarDebeGuardarLosCambiosCuandoTodoEsValido() {
		Vehiculo v = construirVehiculo(conductor);
		requestValido.setColor("Azul");
		when(vehiculoRepository.findById(v.getId())).thenReturn(Optional.of(v));
		when(vehiculoRepository.save(any(Vehiculo.class))).thenAnswer(inv -> inv.getArgument(0));

		VehiculoResponse response = vehiculoService.actualizar(conductorId, v.getId(), requestValido);

		assertEquals("Azul", response.getColor());
		verify(vehiculoRepository, times(1)).save(v);
	}

	@Test
	void desactivarDebeFallarSiElVehiculoNoExiste() {
		UUID vehiculoId = UUID.randomUUID();
		when(vehiculoRepository.findById(vehiculoId)).thenReturn(Optional.empty());

		assertThrows(IllegalArgumentException.class, () -> vehiculoService.desactivar(conductorId, vehiculoId));
	}

	@Test
	void desactivarDebeFallarSiElVehiculoNoLePerteneceAlConductor() {
		Vehiculo v = construirVehiculo(conductor);
		UUID otroConductorId = UUID.randomUUID();
		when(vehiculoRepository.findById(v.getId())).thenReturn(Optional.of(v));

		assertThrows(IllegalArgumentException.class, () -> vehiculoService.desactivar(otroConductorId, v.getId()));
		verify(vehiculoRepository, never()).save(any());
	}

	@Test
	void desactivarDebeMarcarElVehiculoComoInactivo() {
		Vehiculo v = construirVehiculo(conductor);
		when(vehiculoRepository.findById(v.getId())).thenReturn(Optional.of(v));
		when(vehiculoRepository.save(any(Vehiculo.class))).thenAnswer(inv -> inv.getArgument(0));

		vehiculoService.desactivar(conductorId, v.getId());

		assertFalse(v.isActivo());
		verify(vehiculoRepository, times(1)).save(v);
	}

	private Vehiculo construirVehiculo(Usuario dueño) {
		return Vehiculo.builder().id(UUID.randomUUID()).conductor(dueño).placa("ABC123").marca("Mazda")
				.modelo("3").anio(2020).color("Rojo").tipo(TipoVehiculo.CARRO).cedulaPropietario("123456789")
				.capacidadPasajeros(4).activo(true).terminosAceptados(true).build();
	}
}

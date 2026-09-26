package co.edu.unbosque.wheeltrees.service;

import co.edu.unbosque.wheeltrees.DTO.PerfilDTO.ActualizarPerfilRequest;
import co.edu.unbosque.wheeltrees.DTO.PerfilDTO.PerfilResponse;
import co.edu.unbosque.wheeltrees.model.RolUsuario;
import co.edu.unbosque.wheeltrees.model.Usuario;
import co.edu.unbosque.wheeltrees.repository.UsuarioRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

	@Mock
	private UsuarioRepository usuarioRepository;

	@InjectMocks
	private UsuarioService usuarioService;

	private UUID usuarioId;
	private Usuario usuario;

	@BeforeEach
	void setUp() {
		usuarioId = UUID.randomUUID();
		usuario = Usuario.builder().id(usuarioId).nombre("Ana").apellido("Ríos").email("ana@unbosque.edu.co")
				.rol(RolUsuario.PASAJERO).build();
	}

	@Test
	void miPerfilDebeFallarSiElUsuarioNoExiste() {
		when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.empty());

		assertThrows(IllegalArgumentException.class, () -> usuarioService.miPerfil(usuarioId));
	}

	@Test
	void miPerfilDebeRetornarLosDatosDelUsuario() {
		when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));

		PerfilResponse response = usuarioService.miPerfil(usuarioId);

		assertEquals("Ana", response.getNombre());
		assertEquals("PASAJERO", response.getRol());
	}

	@Test
	void actualizarPerfilDebeFallarSiElUsuarioNoExiste() {
		when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.empty());
		ActualizarPerfilRequest request = ActualizarPerfilRequest.builder().nombre("Ana").apellido("Ríos").build();

		assertThrows(IllegalArgumentException.class, () -> usuarioService.actualizarPerfil(usuarioId, request));
	}

	@Test
	void actualizarPerfilDebeGuardarLosNuevosDatos() {
		when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
		when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));
		ActualizarPerfilRequest request = ActualizarPerfilRequest.builder().nombre("Ana María").apellido("Ríos")
				.direccionCasa("Calle 145").casaLat(4.7).casaLng(-74.05).build();

		PerfilResponse response = usuarioService.actualizarPerfil(usuarioId, request);

		assertEquals("Ana María", response.getNombre());
		assertEquals("Calle 145", response.getDireccionCasa());
		verify(usuarioRepository, times(1)).save(usuario);
	}

	@Test
	void actualizarRolDebeFallarSiElUsuarioNoExiste() {
		when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.empty());

		assertThrows(IllegalArgumentException.class,
				() -> usuarioService.actualizarRol(usuarioId, "CONDUCTOR"));
	}

	@Test
	void actualizarRolDebeFallarSiElRolNoEsUnValorValido() {
		when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));

		assertThrows(IllegalArgumentException.class,
				() -> usuarioService.actualizarRol(usuarioId, "NO_EXISTE"));
	}

	@Test
	void actualizarRolDebeFallarSiElRolEsAmbos() {
		when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));

		assertThrows(IllegalArgumentException.class, () -> usuarioService.actualizarRol(usuarioId, "AMBOS"));
	}

	@Test
	void actualizarRolDebeCambiarElRolCuandoEsValido() {
		when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
		when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

		PerfilResponse response = usuarioService.actualizarRol(usuarioId, "conductor");

		assertEquals("CONDUCTOR", response.getRol());
	}

	@Test
	void actualizarFcmTokenDebeFallarSiElUsuarioNoExiste() {
		when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.empty());

		assertThrows(IllegalArgumentException.class,
				() -> usuarioService.actualizarFcmToken(usuarioId, "token-abc"));
	}

	@Test
	void actualizarFcmTokenDebeGuardarElNuevoToken() {
		when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
		when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

		usuarioService.actualizarFcmToken(usuarioId, "token-abc");

		assertEquals("token-abc", usuario.getFcmToken());
		verify(usuarioRepository, times(1)).save(usuario);
	}
}

package co.edu.unbosque.wheeltrees.service;

import co.edu.unbosque.wheeltrees.DTO.MensajeDTO.ChatResumen;
import co.edu.unbosque.wheeltrees.DTO.MensajeDTO.EnviarMensajeRequest;
import co.edu.unbosque.wheeltrees.DTO.MensajeDTO.MensajeResponse;
import co.edu.unbosque.wheeltrees.model.Mensaje;
import co.edu.unbosque.wheeltrees.model.RolUsuario;
import co.edu.unbosque.wheeltrees.model.Usuario;
import co.edu.unbosque.wheeltrees.repository.MensajeRepository;
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
class MensajeServiceTest {

	@Mock
	private MensajeRepository mensajeRepository;
	@Mock
	private ReservaRepository reservaRepository;
	@Mock
	private UsuarioRepository usuarioRepository;

	@InjectMocks
	private MensajeService mensajeService;

	private UUID remitenteId;
	private UUID destinatarioId;
	private Usuario remitente;
	private Usuario destinatario;

	@BeforeEach
	void setUp() {
		remitenteId = UUID.randomUUID();
		destinatarioId = UUID.randomUUID();
		remitente = Usuario.builder().id(remitenteId).nombre("Ana").apellido("Ríos").rol(RolUsuario.PASAJERO).build();
		destinatario = Usuario.builder().id(destinatarioId).nombre("Carlos").apellido("Pérez")
				.rol(RolUsuario.CONDUCTOR).build();
	}

	// ---------- enviar ----------

	@Test
	void enviarDebeFallarSiFaltaElDestinatario() {
		EnviarMensajeRequest request = EnviarMensajeRequest.builder().contenido("Hola").build();

		assertThrows(IllegalArgumentException.class, () -> mensajeService.enviar(remitenteId, request));
	}

	@Test
	void enviarDebeFallarSiEsConsigoMismo() {
		EnviarMensajeRequest request = EnviarMensajeRequest.builder().destinatarioId(remitenteId).contenido("Hola")
				.build();

		assertThrows(IllegalArgumentException.class, () -> mensajeService.enviar(remitenteId, request));
	}

	@Test
	void enviarDebeFallarSiNoHayRelacionConfirmada() {
		when(reservaRepository.existeRelacionConfirmada(remitenteId, destinatarioId)).thenReturn(false);
		EnviarMensajeRequest request = EnviarMensajeRequest.builder().destinatarioId(destinatarioId)
				.contenido("Hola").build();

		assertThrows(IllegalArgumentException.class, () -> mensajeService.enviar(remitenteId, request));
	}

	@Test
	void enviarDebeFallarSiElRemitenteNoExiste() {
		when(reservaRepository.existeRelacionConfirmada(remitenteId, destinatarioId)).thenReturn(true);
		when(usuarioRepository.findById(remitenteId)).thenReturn(Optional.empty());
		EnviarMensajeRequest request = EnviarMensajeRequest.builder().destinatarioId(destinatarioId)
				.contenido("Hola").build();

		assertThrows(IllegalArgumentException.class, () -> mensajeService.enviar(remitenteId, request));
	}

	@Test
	void enviarDebeGuardarElMensajeCuandoTodoEsValido() {
		when(reservaRepository.existeRelacionConfirmada(remitenteId, destinatarioId)).thenReturn(true);
		when(usuarioRepository.findById(remitenteId)).thenReturn(Optional.of(remitente));
		when(usuarioRepository.findById(destinatarioId)).thenReturn(Optional.of(destinatario));
		when(mensajeRepository.save(any(Mensaje.class))).thenAnswer(inv -> {
			Mensaje m = inv.getArgument(0);
			m.setId(UUID.randomUUID());
			m.setEnviadoEn(LocalDateTime.now());
			return m;
		});
		EnviarMensajeRequest request = EnviarMensajeRequest.builder().destinatarioId(destinatarioId)
				.contenido("  Hola, ¿ya llegaste?  ").build();

		MensajeResponse response = mensajeService.enviar(remitenteId, request);

		assertEquals("Hola, ¿ya llegaste?", response.getContenido());
		assertEquals(remitenteId.toString(), response.getRemitenteId());
	}

	// ---------- historial ----------

	@Test
	void historialDebeFallarSiNoHayRelacionConfirmada() {
		when(reservaRepository.existeRelacionConfirmada(remitenteId, destinatarioId)).thenReturn(false);

		assertThrows(IllegalArgumentException.class,
				() -> mensajeService.historial(remitenteId, destinatarioId));
	}

	@Test
	void historialDebeRetornarLosMensajesEntreLosDosUsuarios() {
		Mensaje mensaje = Mensaje.builder().id(UUID.randomUUID()).remitente(remitente).destinatario(destinatario)
				.contenido("Hola").enviadoEn(LocalDateTime.now()).build();
		when(reservaRepository.existeRelacionConfirmada(remitenteId, destinatarioId)).thenReturn(true);
		when(mensajeRepository.historialEntre(remitenteId, destinatarioId)).thenReturn(List.of(mensaje));

		List<MensajeResponse> historial = mensajeService.historial(remitenteId, destinatarioId);

		assertEquals(1, historial.size());
	}

	// ---------- marcarLeidos ----------

	@Test
	void marcarLeidosDebeFallarSiNoHayRelacionConfirmada() {
		when(reservaRepository.existeRelacionConfirmada(remitenteId, destinatarioId)).thenReturn(false);

		assertThrows(IllegalArgumentException.class,
				() -> mensajeService.marcarLeidos(remitenteId, destinatarioId));
	}

	@Test
	void marcarLeidosDebeInvocarElUpdateCuandoLaRelacionEsValida() {
		when(reservaRepository.existeRelacionConfirmada(remitenteId, destinatarioId)).thenReturn(true);

		mensajeService.marcarLeidos(remitenteId, destinatarioId);

		verify(mensajeRepository, times(1)).marcarLeidos(remitenteId, destinatarioId);
	}

	// ---------- misChats ----------

	@Test
	void misChatsDebeRetornarUnResumenPorCadaContraparte() {
		when(reservaRepository.findContrapartesConfirmadas(remitenteId)).thenReturn(List.of(destinatarioId));
		when(usuarioRepository.findById(destinatarioId)).thenReturn(Optional.of(destinatario));
		Mensaje ultimo = Mensaje.builder().contenido("Nos vemos mañana").enviadoEn(LocalDateTime.now()).build();
		when(mensajeRepository.ultimoMensajeEntre(remitenteId, destinatarioId)).thenReturn(Optional.of(ultimo));
		when(mensajeRepository.countByRemitenteIdAndDestinatarioIdAndLeidoFalse(destinatarioId, remitenteId))
				.thenReturn(2L);

		List<ChatResumen> chats = mensajeService.misChats(remitenteId);

		assertEquals(1, chats.size());
		assertEquals("Nos vemos mañana", chats.get(0).getUltimoMensaje());
		assertEquals(2L, chats.get(0).getNoLeidos());
	}

	@Test
	void misChatsDebeDeduplicarContrapartesRepetidas() {
		when(reservaRepository.findContrapartesConfirmadas(remitenteId))
				.thenReturn(List.of(destinatarioId, destinatarioId));
		when(usuarioRepository.findById(destinatarioId)).thenReturn(Optional.of(destinatario));
		when(mensajeRepository.ultimoMensajeEntre(remitenteId, destinatarioId)).thenReturn(Optional.empty());
		when(mensajeRepository.countByRemitenteIdAndDestinatarioIdAndLeidoFalse(destinatarioId, remitenteId))
				.thenReturn(0L);

		List<ChatResumen> chats = mensajeService.misChats(remitenteId);

		assertEquals(1, chats.size());
		verify(usuarioRepository, times(1)).findById(destinatarioId);
	}

	@Test
	void misChatsSinContrapartesDebeRetornarListaVacia() {
		when(reservaRepository.findContrapartesConfirmadas(remitenteId)).thenReturn(List.of());

		List<ChatResumen> chats = mensajeService.misChats(remitenteId);

		assertTrue(chats.isEmpty());
	}
}

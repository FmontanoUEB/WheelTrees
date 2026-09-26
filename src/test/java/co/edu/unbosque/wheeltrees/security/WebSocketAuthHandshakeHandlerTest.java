package co.edu.unbosque.wheeltrees.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class WebSocketAuthHandshakeHandlerTest {

	@Mock
	private ServerHttpRequest request;
	@Mock
	private WebSocketHandler wsHandler;

	private final WebSocketAuthHandshakeHandler handler = new WebSocketAuthHandshakeHandler();

	@Test
	void determineUserDebeRetornarNullSiNoHayUsuarioIdEnLosAtributos() {
		Map<String, Object> atributos = new HashMap<>();

		Principal principal = handler.determineUser(request, wsHandler, atributos);

		assertNull(principal);
	}

	@Test
	void determineUserDebeConstruirUnUsuarioPrincipalConElIdDeLosAtributos() {
		UUID id = UUID.randomUUID();
		Map<String, Object> atributos = new HashMap<>();
		atributos.put("usuarioId", id.toString());

		Principal principal = handler.determineUser(request, wsHandler, atributos);

		assertInstanceOf(UsuarioPrincipal.class, principal);
		assertEquals(id, ((UsuarioPrincipal) principal).getUsuarioId());
	}
}

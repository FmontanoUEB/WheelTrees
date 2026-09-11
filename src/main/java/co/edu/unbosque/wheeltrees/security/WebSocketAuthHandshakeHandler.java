package co.edu.unbosque.wheeltrees.security;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;
import java.util.UUID;

/**
 * Convierte el "usuarioId" dejado por JwtHandshakeInterceptor en un
 * Principal real, disponible luego en @MessageMapping como parámetro
 * Principal y usado por Spring para el enrutamiento de mensajes.
 */
@Component
public class WebSocketAuthHandshakeHandler extends DefaultHandshakeHandler {

	@Override
	protected Principal determineUser(org.springframework.http.server.ServerHttpRequest request,
			WebSocketHandler wsHandler, Map<String, Object> attributes) {

		Object usuarioId = attributes.get("usuarioId");
		if (usuarioId == null) {
			return null; // no debería pasar: el interceptor ya rechazó la conexión sin token válido
		}
		return new UsuarioPrincipal(UUID.fromString(usuarioId.toString()));
	}
}

package co.edu.unbosque.wheeltrees.security;

import lombok.RequiredArgsConstructor;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

/**
 * Se ejecuta en el handshake HTTP inicial (antes de subir a WebSocket).
 * El cliente no puede mandar headers custom en la conexión WS de RN, así
 * que el JWT llega como query param: ws://host/ws?token=xxxxx
 *
 * Si el token es inválido o falta, rechaza la conexión con 401 y nunca
 * llega a abrirse el socket. Si es válido, guarda el id del usuario en
 * los atributos de la sesión para que WebSocketAuthHandshakeHandler arme
 * el Principal.
 */
@Component
@RequiredArgsConstructor
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

	private final JwtUtil jwtUtil;

	@Override
	public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
			WebSocketHandler wsHandler, Map<String, Object> attributes) {

		System.err.println("🔌 WS handshake intent desde " + request.getRemoteAddress()
				+ " → " + request.getURI());

		String token = extraerToken(request);

		if (token == null) {
			System.err.println("🔌 WS RECHAZADO: no llegó el query param 'token' en la URL");
			response.setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
			return false;
		}

		if (!jwtUtil.validarToken(token)) {
			System.err.println("🔌 WS RECHAZADO: token presente pero inválido/expirado");
			response.setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
			return false; // corta la conexión antes de abrir el socket
		}

		String usuarioId = jwtUtil.extraerUsuarioId(token).toString();
		System.err.println("🔌 WS ACEPTADO para usuario " + usuarioId);
		attributes.put("usuarioId", usuarioId);
		return true;
	}

	@Override
	public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
			WebSocketHandler wsHandler, Exception exception) {
		// no-op
	}

	private String extraerToken(ServerHttpRequest request) {
		if (request instanceof ServletServerHttpRequest servletRequest) {
			String query = servletRequest.getServletRequest().getQueryString();
			if (query == null) return null;
			return UriComponentsBuilder.newInstance()
					.query(query)
					.build()
					.getQueryParams()
					.getFirst("token");
		}
		return null;
	}
}

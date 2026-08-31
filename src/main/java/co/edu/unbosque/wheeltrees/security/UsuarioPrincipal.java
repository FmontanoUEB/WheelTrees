package co.edu.unbosque.wheeltrees.security;

import java.security.Principal;
import java.util.UUID;

/**
 * Principal mínimo usado en las sesiones STOMP/WebSocket: solo guarda el
 * id del usuario autenticado (extraído del JWT durante el handshake).
 */
public class UsuarioPrincipal implements Principal {

	private final UUID usuarioId;

	public UsuarioPrincipal(UUID usuarioId) {
		this.usuarioId = usuarioId;
	}

	public UUID getUsuarioId() {
		return usuarioId;
	}

	@Override
	public String getName() {
		return usuarioId.toString();
	}
}

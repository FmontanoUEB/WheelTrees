package co.edu.unbosque.wheeltrees.security;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UsuarioPrincipalTest {

	@Test
	void getUsuarioIdDebeRetornarElIdConElQueFueCreado() {
		UUID id = UUID.randomUUID();
		UsuarioPrincipal principal = new UsuarioPrincipal(id);

		assertEquals(id, principal.getUsuarioId());
	}

	@Test
	void getNameDebeRetornarElIdComoTexto() {
		UUID id = UUID.randomUUID();
		UsuarioPrincipal principal = new UsuarioPrincipal(id);

		assertEquals(id.toString(), principal.getName());
	}
}

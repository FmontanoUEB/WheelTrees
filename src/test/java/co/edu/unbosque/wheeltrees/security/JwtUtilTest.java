package co.edu.unbosque.wheeltrees.security;

import co.edu.unbosque.wheeltrees.config.JwtProperties;
import co.edu.unbosque.wheeltrees.model.RolUsuario;
import co.edu.unbosque.wheeltrees.model.Usuario;
import co.edu.unbosque.wheeltrees.security.JwtUtil;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

	private JwtUtil jwtUtil;
	private Usuario usuario;

	@BeforeEach
	void setUp() {
		JwtProperties props = new JwtProperties();
		props.setSecret("5XUNJK+GUcXAwF4B73I9BnVDx+ucTa1By7aF5Z42hSA=");
		props.setExpirationMs(86400000L);

		jwtUtil = new JwtUtil(props);

		usuario = Usuario.builder().id(UUID.randomUUID()).nombre("Ana").apellido("Ríos")
				.email("ana.rios@unbosque.edu.co").rol(RolUsuario.PASAJERO).build();
	}

	@Test
	void debeGenerarYValidarUnTokenCorrecto() {
		String token = jwtUtil.generarToken(usuario);

		assertNotNull(token);
		assertTrue(jwtUtil.validarToken(token));
		assertEquals(usuario.getId(), jwtUtil.extraerUsuarioId(token));
		assertEquals(usuario.getEmail(), jwtUtil.extraerEmail(token));
	}

	@Test
	void debeRechazarUnTokenManipulado() {
		String token = jwtUtil.generarToken(usuario);
		String tokenAlterado = token.substring(0, token.length() - 5) + "abcde";

		assertFalse(jwtUtil.validarToken(tokenAlterado));
	}
}
package co.edu.unbosque.wheeltrees.security;

import co.edu.unbosque.wheeltrees.model.RolUsuario;
import co.edu.unbosque.wheeltrees.model.Usuario;
import co.edu.unbosque.wheeltrees.repository.UsuarioRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtFilterTest {

	@Mock
	private JwtUtil jwtUtil;
	@Mock
	private UsuarioRepository usuarioRepository;
	@Mock
	private HttpServletRequest request;
	@Mock
	private HttpServletResponse response;
	@Mock
	private FilterChain filterChain;

	private JwtFilter jwtFilter;

	@BeforeEach
	void setUp() {
		jwtFilter = new JwtFilter(jwtUtil, usuarioRepository);
	}

	@AfterEach
	void limpiarContexto() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void debePermitirElPasoSinValidarTokenEnRutasPublicas() throws Exception {
		when(request.getServletPath()).thenReturn("/api/auth/login");

		jwtFilter.doFilterInternal(request, response, filterChain);

		verify(filterChain, times(1)).doFilter(request, response);
		verifyNoInteractions(jwtUtil);
	}

	@Test
	void debeContinuarSinAutenticarSiNoHayHeaderAuthorization() throws Exception {
		when(request.getServletPath()).thenReturn("/api/viajes/disponibles");
		when(request.getHeader("Authorization")).thenReturn(null);

		jwtFilter.doFilterInternal(request, response, filterChain);

		verify(filterChain, times(1)).doFilter(request, response);
		assertNull(SecurityContextHolder.getContext().getAuthentication());
	}

	@Test
	void debeContinuarSinAutenticarSiElHeaderNoEsBearer() throws Exception {
		when(request.getServletPath()).thenReturn("/api/viajes/disponibles");
		when(request.getHeader("Authorization")).thenReturn("Basic algo");

		jwtFilter.doFilterInternal(request, response, filterChain);

		verify(filterChain, times(1)).doFilter(request, response);
		assertNull(SecurityContextHolder.getContext().getAuthentication());
	}

	@Test
	void debeResponder401YCortarLaCadenaSiElTokenNoEsValido() throws Exception {
		when(request.getServletPath()).thenReturn("/api/viajes/disponibles");
		when(request.getHeader("Authorization")).thenReturn("Bearer token-invalido");
		when(jwtUtil.validarToken("token-invalido")).thenReturn(false);

		jwtFilter.doFilterInternal(request, response, filterChain);

		verify(response, times(1)).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		verify(filterChain, never()).doFilter(any(), any());
	}

	@Test
	void debeResponder401SiJwtUtilLanzaUnaExcepcion() throws Exception {
		when(request.getServletPath()).thenReturn("/api/viajes/disponibles");
		when(request.getHeader("Authorization")).thenReturn("Bearer token-expirado");
		when(jwtUtil.validarToken("token-expirado")).thenThrow(new RuntimeException("expirado"));

		jwtFilter.doFilterInternal(request, response, filterChain);

		verify(response, times(1)).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		verify(filterChain, never()).doFilter(any(), any());
	}

	@Test
	void debeAutenticarAlUsuarioCuandoElTokenEsValidoYElUsuarioExiste() throws Exception {
		Usuario usuario = Usuario.builder().id(UUID.randomUUID()).email("ana@unbosque.edu.co")
				.rol(RolUsuario.PASAJERO).build();

		when(request.getServletPath()).thenReturn("/api/viajes/disponibles");
		when(request.getHeader("Authorization")).thenReturn("Bearer token-valido");
		when(jwtUtil.validarToken("token-valido")).thenReturn(true);
		when(jwtUtil.extraerEmail("token-valido")).thenReturn("ana@unbosque.edu.co");
		when(usuarioRepository.findByEmail("ana@unbosque.edu.co")).thenReturn(Optional.of(usuario));

		jwtFilter.doFilterInternal(request, response, filterChain);

		assertNotNull(SecurityContextHolder.getContext().getAuthentication());
		assertEquals(usuario, SecurityContextHolder.getContext().getAuthentication().getPrincipal());
		verify(filterChain, times(1)).doFilter(request, response);
	}

	@Test
	void noDebeAutenticarSiElUsuarioDelTokenNoExisteEnLaBaseDeDatos() throws Exception {
		when(request.getServletPath()).thenReturn("/api/viajes/disponibles");
		when(request.getHeader("Authorization")).thenReturn("Bearer token-valido");
		when(jwtUtil.validarToken("token-valido")).thenReturn(true);
		when(jwtUtil.extraerEmail("token-valido")).thenReturn("no-existe@unbosque.edu.co");
		when(usuarioRepository.findByEmail("no-existe@unbosque.edu.co")).thenReturn(Optional.empty());

		jwtFilter.doFilterInternal(request, response, filterChain);

		assertNull(SecurityContextHolder.getContext().getAuthentication());
		verify(filterChain, times(1)).doFilter(request, response);
	}
}

package co.edu.unbosque.wheeltrees.service;

import co.edu.unbosque.wheeltrees.DTO.AuthDTO.RegistroRequest;
import co.edu.unbosque.wheeltrees.model.RolUsuario;
import co.edu.unbosque.wheeltrees.model.Usuario;
import co.edu.unbosque.wheeltrees.repository.RecuperarContrasenaRepository;
import co.edu.unbosque.wheeltrees.repository.UsuarioRepository;
import co.edu.unbosque.wheeltrees.repository.VerificacionEmailRepository;
import co.edu.unbosque.wheeltrees.security.JwtUtil;
import co.edu.unbosque.wheeltrees.service.AuthService;
import co.edu.unbosque.wheeltrees.service.EmailService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

	@Mock
	private UsuarioRepository usuarioRepository;
	@Mock
	private VerificacionEmailRepository verificacionEmailRepository;
	@Mock
	private RecuperarContrasenaRepository recuperacionPasswordRepository;
	@Mock
	private PasswordEncoder passwordEncoder;
	@Mock
	private JwtUtil jwtUtil;
	@Mock
	private AuthenticationManager authenticationManager;
	@Mock
	private EmailService emailService;

	@InjectMocks
	private AuthService authService;

	private RegistroRequest registroValido;

	@BeforeEach
	void setUp() {
		registroValido = RegistroRequest.builder().nombre("Ana").apellido("Ríos").email("ana.rios@unbosque.edu.co")
				.password("password123").rol(RolUsuario.PASAJERO).build();
	}

	@Test
	void registrarDebeFallarConCorreoNoInstitucional() {
		registroValido.setEmail("ana@gmail.com");

		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> authService.registrar(registroValido));

		assertTrue(ex.getMessage().contains("institucionales"));
		verify(usuarioRepository, never()).save(any());
	}

	@Test
	void registrarDebeFallarSiElCorreoYaExiste() {
		when(usuarioRepository.existsByEmail(registroValido.getEmail())).thenReturn(true);

		assertThrows(IllegalArgumentException.class, () -> authService.registrar(registroValido));
		verify(usuarioRepository, never()).save(any());
	}

	@Test
	void registrarDebeGuardarUsuarioYEnviarOtpCuandoTodoEsValido() {
		when(usuarioRepository.existsByEmail(registroValido.getEmail())).thenReturn(false);
		when(passwordEncoder.encode(registroValido.getPassword())).thenReturn("hashSimulado");

		authService.registrar(registroValido);

		verify(usuarioRepository, times(1)).save(any(Usuario.class));
		verify(emailService, times(1)).enviarOtpVerificacion(eq(registroValido.getEmail()), eq("Ana"), any());
	}

	@Test
	void solicitarRecuperacionDebeFallarSiElUsuarioNoExiste() {
		when(usuarioRepository.findByEmail("no-existe@unbosque.edu.co")).thenReturn(Optional.empty());

		assertThrows(IllegalArgumentException.class,
				() -> authService.solicitarRecuperacionPassword("no-existe@unbosque.edu.co"));
	}
}
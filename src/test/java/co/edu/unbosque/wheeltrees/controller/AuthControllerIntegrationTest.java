package co.edu.unbosque.wheeltrees.controller;

import co.edu.unbosque.wheeltrees.model.Usuario;
import co.edu.unbosque.wheeltrees.model.VerificacionEmail;
import co.edu.unbosque.wheeltrees.repository.UsuarioRepository;
import co.edu.unbosque.wheeltrees.repository.VerificacionEmailRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerIntegrationTest {

	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private UsuarioRepository usuarioRepository;
	@Autowired
	private VerificacionEmailRepository verificacionEmailRepository;

	@Test
	void flujoCompletoRegistroVerificacionYLogin() throws Exception {
		String email = "test.integracion@unbosque.edu.co";

		Map<String, Object> registro = Map.of("nombre", "Test", "apellido", "Integración", "email", email, "password",
				"password123", "rol", "PASAJERO");

		// 1) Registro
		mockMvc.perform(post("/api/auth/registrar").contentType("application/json")
				.content(objectMapper.writeValueAsString(registro))).andExpect(status().isCreated())
				.andExpect(jsonPath("$.exito").value(true));

		// 2) El OTP no llega por correo real en pruebas -> lo leemos de la BD H2
		Usuario usuario = usuarioRepository.findByEmail(email).orElseThrow();
		VerificacionEmail verificacion = verificacionEmailRepository
				.findTopByUsuarioAndUsadoFalseOrderByCreadoEnDesc(usuario).orElseThrow();

		// 3) Verificar email con el OTP real
		mockMvc.perform(post("/api/auth/verificar-email").contentType("application/json").content(
				objectMapper.writeValueAsString(Map.of("email", email, "codigoOtp", verificacion.getCodigoOtp()))))
				.andExpect(status().isOk()).andExpect(jsonPath("$.exito").value(true));

		// 4) Login ya debe funcionar
		mockMvc.perform(post("/api/auth/login").contentType("application/json")
				.content(objectMapper.writeValueAsString(Map.of("email", email, "password", "password123"))))
				.andExpect(status().isOk()).andExpect(jsonPath("$.accessToken").exists())
				.andExpect(jsonPath("$.usuario.nombre").value("Test")); // <- si esto pasa, el cifrado/descifrado
																		// funciona de punta a punta
	}

	@Test
	void loginDebeFallarConCredencialesIncorrectas() throws Exception {
		mockMvc.perform(post("/api/auth/login").contentType("application/json").content(
				objectMapper.writeValueAsString(Map.of("email", "no-existe@unbosque.edu.co", "password", "loquesea"))))
				.andExpect(status().isUnauthorized());
	}
}
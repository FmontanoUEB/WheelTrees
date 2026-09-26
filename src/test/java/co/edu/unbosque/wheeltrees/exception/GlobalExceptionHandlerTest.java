package co.edu.unbosque.wheeltrees.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

	private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

	@Test
	void handleBadRequestDebeRetornar400ConElMensajeDeLaExcepcion() {
		IllegalArgumentException ex = new IllegalArgumentException("Correos institucionales solamente");

		ResponseEntity<Map<String, Object>> response = handler.handleBadRequest(ex);

		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertEquals("Correos institucionales solamente", response.getBody().get("error"));
		assertEquals(400, response.getBody().get("status"));
	}

	@Test
	void handleValidationErrorsDebeRetornar400ConLosErroresPorCampo() {
		MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
		BindingResult bindingResult = mock(BindingResult.class);
		FieldError fieldError = new FieldError("registroRequest", "password", "debe tener al menos 8 caracteres");

		when(ex.getBindingResult()).thenReturn(bindingResult);
		when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

		ResponseEntity<Map<String, Object>> response = handler.handleValidationErrors(ex);

		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		@SuppressWarnings("unchecked")
		Map<String, String> errores = (Map<String, String>) response.getBody().get("errores");
		assertEquals("debe tener al menos 8 caracteres", errores.get("password"));
	}

	@Test
	void handleUnauthorizedDebeRetornar401ConMensajeGenerico() {
		BadCredentialsException ex = new BadCredentialsException("bad creds");

		ResponseEntity<Map<String, Object>> response = handler.handleUnauthorized(ex);

		assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
		assertEquals("Credenciales incorrectas", response.getBody().get("error"));
	}

	@Test
	void handleGeneralDebeRetornar500ConElDetalleDelError() {
		Exception ex = new RuntimeException("fallo inesperado");

		ResponseEntity<Map<String, Object>> response = handler.handleGeneral(ex);

		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
		assertEquals("Error interno del servidor", response.getBody().get("error"));
		assertEquals("fallo inesperado", response.getBody().get("detalle"));
	}
}

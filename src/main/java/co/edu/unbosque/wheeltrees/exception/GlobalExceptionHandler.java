package co.edu.unbosque.wheeltrees.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.authentication.BadCredentialsException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

	// ─────────────────────────────────────────────
	// 🔴 400 - Errores de lógica (tus throw new IllegalArgumentException)
	// ─────────────────────────────────────────────
	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<Map<String, Object>> handleBadRequest(IllegalArgumentException ex) {

		Map<String, Object> response = new HashMap<>();
		response.put("error", ex.getMessage());
		response.put("status", 400);
		response.put("timestamp", LocalDateTime.now());

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	}

	// ─────────────────────────────────────────────
	// 🔴 400 - Validaciones @Valid (ej: password < 8, email inválido)
	// ─────────────────────────────────────────────
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException ex) {

		Map<String, String> errores = new HashMap<>();

		ex.getBindingResult().getFieldErrors()
				.forEach(error -> errores.put(error.getField(), error.getDefaultMessage()));

		Map<String, Object> response = new HashMap<>();
		response.put("errores", errores);
		response.put("status", 400);
		response.put("timestamp", LocalDateTime.now());

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	}

	// ─────────────────────────────────────────────
	// 🔴 401 - Login incorrecto
	// ─────────────────────────────────────────────
	@ExceptionHandler(BadCredentialsException.class)
	public ResponseEntity<Map<String, Object>> handleUnauthorized(BadCredentialsException ex) {

		Map<String, Object> response = new HashMap<>();
		response.put("error", "Credenciales incorrectas");
		response.put("status", 401);
		response.put("timestamp", LocalDateTime.now());

		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
	}

	// ─────────────────────────────────────────────
	// 🔴 500 - Error general (fallback)
	// ─────────────────────────────────────────────
	@ExceptionHandler(Exception.class)
	public ResponseEntity<Map<String, Object>> handleGeneral(Exception ex) {

		Map<String, Object> response = new HashMap<>();
		response.put("error", "Error interno del servidor");
		response.put("detalle", ex.getMessage());
		response.put("status", 500);
		response.put("timestamp", LocalDateTime.now());

		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	}
}
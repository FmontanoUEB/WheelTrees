package co.edu.unbosque.wheeltrees.controller;

import co.edu.unbosque.wheeltrees.DTO.AuthDTO.AuthResponse;
import co.edu.unbosque.wheeltrees.DTO.AuthDTO.LoginRequest;
import co.edu.unbosque.wheeltrees.DTO.AuthDTO.MensajeResponse;
import co.edu.unbosque.wheeltrees.DTO.AuthDTO.RegistroRequest;
import co.edu.unbosque.wheeltrees.DTO.AuthDTO.VerificarEmailRequest;
import co.edu.unbosque.wheeltrees.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Registro, verificación y login")
public class AuthController {

	private final AuthService authService;

	@Operation(summary = "Registrar usuario")
	@PostMapping("/registrar")
	public ResponseEntity<MensajeResponse> registrar(@Valid @RequestBody RegistroRequest request) {

		return ResponseEntity.status(HttpStatus.CREATED).body(authService.registrar(request));
	}

	@Operation(summary = "Verificar correo con OTP")
	@PostMapping("/verificar-email")
	public ResponseEntity<MensajeResponse> verificarEmail(@Valid @RequestBody VerificarEmailRequest request) {

		return ResponseEntity.ok(authService.verificarEmail(request));
	}

	@Operation(summary = "Iniciar sesión")
	@PostMapping("/login")
	public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {

		return ResponseEntity.ok(authService.login(request));
	}
	@Operation(summary = "Reenviar OTP de verificación")
	@PostMapping("/reenviar-otp")
	public ResponseEntity<MensajeResponse> reenviarOtp(@RequestParam @Email String email) {
	    return ResponseEntity.ok(authService.reenviarOtp(email));
	}
}
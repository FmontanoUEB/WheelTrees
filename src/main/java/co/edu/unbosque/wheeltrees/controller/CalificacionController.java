package co.edu.unbosque.wheeltrees.controller;

import co.edu.unbosque.wheeltrees.DTO.CalificacionDTO.*;
import co.edu.unbosque.wheeltrees.security.JwtUtil;
import co.edu.unbosque.wheeltrees.service.CalificacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/calificaciones")
@RequiredArgsConstructor
@Tag(name = "Calificaciones", description = "Calificación recíproca pasajero/conductor al finalizar un viaje")
@SecurityRequirement(name = "bearerAuth")
public class CalificacionController {

	private final CalificacionService calificacionService;
	private final JwtUtil jwtUtil;

	@Operation(summary = "Calificar a la contraparte de una reserva ya completada")
	@PostMapping
	public ResponseEntity<CalificacionResponse> calificar(@RequestHeader("Authorization") String authHeader,
			@Valid @RequestBody CrearCalificacionRequest request) {

		UUID calificadorId = extraerId(authHeader);
		return ResponseEntity.status(HttpStatus.CREATED).body(calificacionService.calificar(calificadorId, request));
	}

	@Operation(summary = "Viajes completados donde aún falta calificar a la contraparte")
	@GetMapping("/pendientes")
	public ResponseEntity<List<CalificacionPendiente>> pendientes(@RequestHeader("Authorization") String authHeader) {

		UUID usuarioId = extraerId(authHeader);
		return ResponseEntity.ok(calificacionService.pendientes(usuarioId));
	}

	private UUID extraerId(String authHeader) {
		String token = authHeader.replace("Bearer ", "");
		return jwtUtil.extraerUsuarioId(token);
	}
}

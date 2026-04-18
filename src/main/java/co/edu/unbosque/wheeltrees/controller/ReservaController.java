package co.edu.unbosque.wheeltrees.controller;

import co.edu.unbosque.wheeltrees.DTO.ReservaDTO.*;
import co.edu.unbosque.wheeltrees.security.JwtUtil;
import co.edu.unbosque.wheeltrees.service.ReservaService;
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
@RequestMapping("/api/reservas")
@RequiredArgsConstructor
@Tag(name = "Reservas", description = "Gestión de reservas de viaje")
@SecurityRequirement(name = "bearerAuth")
public class ReservaController {

	private final ReservaService reservaService;
	private final JwtUtil jwtUtil;

	@Operation(summary = "Solicitar cupo en un viaje (pasajero)")
	@PostMapping
	public ResponseEntity<ReservaResponse> solicitar(@RequestHeader("Authorization") String authHeader,
			@Valid @RequestBody SolicitarReservaRequest request) {

		UUID pasajeroId = extraerId(authHeader);
		return ResponseEntity.status(HttpStatus.CREATED).body(reservaService.solicitar(pasajeroId, request));
	}

	@Operation(summary = "Aceptar o rechazar una reserva (conductor)")
	@PatchMapping("/{reservaId}/responder")
	public ResponseEntity<ReservaResponse> responder(@RequestHeader("Authorization") String authHeader,
			@PathVariable UUID reservaId, @Valid @RequestBody ResponderReservaRequest request) {

		UUID conductorId = extraerId(authHeader);
		return ResponseEntity.ok(reservaService.responder(conductorId, reservaId, request));
	}

	@Operation(summary = "Cancelar mi reserva (pasajero)")
	@PatchMapping("/{reservaId}/cancelar")
	public ResponseEntity<ReservaResponse> cancelar(@RequestHeader("Authorization") String authHeader,
			@PathVariable UUID reservaId) {

		UUID pasajeroId = extraerId(authHeader);
		return ResponseEntity.ok(reservaService.cancelar(pasajeroId, reservaId));
	}

	@Operation(summary = "Ver mis reservas (pasajero)")
	@GetMapping("/mis-reservas")
	public ResponseEntity<List<ReservaResponse>> misReservas(@RequestHeader("Authorization") String authHeader) {

		UUID pasajeroId = extraerId(authHeader);
		return ResponseEntity.ok(reservaService.misReservas(pasajeroId));
	}

	@Operation(summary = "Ver reservas de un viaje (conductor)")
	@GetMapping("/viaje/{viajeId}")
	public ResponseEntity<List<ReservaResponse>> reservasDeViaje(@RequestHeader("Authorization") String authHeader,
			@PathVariable UUID viajeId) {

		UUID conductorId = extraerId(authHeader);
		return ResponseEntity.ok(reservaService.reservasDeViaje(conductorId, viajeId));
	}

	private UUID extraerId(String authHeader) {
		String token = authHeader.replace("Bearer ", "");
		return jwtUtil.extraerUsuarioId(token);
	}
}
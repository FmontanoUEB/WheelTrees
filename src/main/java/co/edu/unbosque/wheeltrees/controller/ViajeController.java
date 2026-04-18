package co.edu.unbosque.wheeltrees.controller;

import co.edu.unbosque.wheeltrees.DTO.ViajeDTO.*;
import co.edu.unbosque.wheeltrees.security.JwtUtil;
import co.edu.unbosque.wheeltrees.service.ViajeService;
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
@RequestMapping("/api/viajes")
@RequiredArgsConstructor
@Tag(name = "Viajes", description = "Publicación y búsqueda de viajes")
@SecurityRequirement(name = "bearerAuth")
public class ViajeController {

    private final ViajeService viajeService;
    private final JwtUtil jwtUtil;

    @Operation(summary = "Publicar un viaje (conductor)")
    @PostMapping
    public ResponseEntity<ViajeResponse> publicar(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody PublicarViajeRequest request) {

        UUID conductorId = extraerId(authHeader);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(viajeService.publicar(conductorId, request));
    }

    @Operation(summary = "Buscar viajes disponibles (pasajero)")
    @GetMapping
    public ResponseEntity<List<ViajeResponse>> buscarDisponibles(
            @RequestParam(required = false) String origen) {

        return ResponseEntity.ok(viajeService.buscarDisponibles(origen));
    }

    @Operation(summary = "Ver detalle de un viaje")
    @GetMapping("/{viajeId}")
    public ResponseEntity<ViajeResponse> detalle(@PathVariable UUID viajeId) {
        return ResponseEntity.ok(viajeService.detalle(viajeId));
    }

    @Operation(summary = "Mis viajes publicados (conductor)")
    @GetMapping("/mis-viajes")
    public ResponseEntity<List<ViajeResponse>> misViajes(
            @RequestHeader("Authorization") String authHeader) {

        UUID conductorId = extraerId(authHeader);
        return ResponseEntity.ok(viajeService.misViajes(conductorId));
    }

    @Operation(summary = "Cancelar un viaje (conductor)")
    @DeleteMapping("/{viajeId}")
    public ResponseEntity<Void> cancelar(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable UUID viajeId) {

        UUID conductorId = extraerId(authHeader);
        viajeService.cancelar(conductorId, viajeId);
        return ResponseEntity.noContent().build();
    }

    private UUID extraerId(String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        return jwtUtil.extraerUsuarioId(token);
    }
}
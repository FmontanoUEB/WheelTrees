package co.edu.unbosque.wheeltrees.controller;

import co.edu.unbosque.wheeltrees.DTO.VehiculoDTO.*;
import co.edu.unbosque.wheeltrees.security.JwtUtil;
import co.edu.unbosque.wheeltrees.service.VehiculoService;
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
@RequestMapping("/api/vehiculos")
@RequiredArgsConstructor
@Tag(name = "Vehículos", description = "Gestión de vehículos del conductor")
@SecurityRequirement(name = "bearerAuth")
public class VehiculoController {

    private final VehiculoService vehiculoService;
    private final JwtUtil jwtUtil;

    @Operation(summary = "Registrar un vehículo")
    @PostMapping
    public ResponseEntity<VehiculoResponse> registrar(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody VehiculoRequest request) {

        UUID conductorId = extraerId(authHeader);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(vehiculoService.registrar(conductorId, request));
    }

    @Operation(summary = "Listar mis vehículos")
    @GetMapping("/mis-vehiculos")
    public ResponseEntity<List<VehiculoResponse>> listarMisVehiculos(
            @RequestHeader("Authorization") String authHeader) {

        UUID conductorId = extraerId(authHeader);
        return ResponseEntity.ok(vehiculoService.listarMisVehiculos(conductorId));
    }

    @Operation(summary = "Actualizar un vehículo")
    @PutMapping("/{vehiculoId}")
    public ResponseEntity<VehiculoResponse> actualizar(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable UUID vehiculoId,
            @Valid @RequestBody VehiculoRequest request) {

        UUID conductorId = extraerId(authHeader);
        return ResponseEntity.ok(vehiculoService.actualizar(conductorId, vehiculoId, request));
    }

    @Operation(summary = "Desactivar un vehículo")
    @DeleteMapping("/{vehiculoId}")
    public ResponseEntity<Void> desactivar(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable UUID vehiculoId) {

        UUID conductorId = extraerId(authHeader);
        vehiculoService.desactivar(conductorId, vehiculoId);
        return ResponseEntity.noContent().build();
    }

    private UUID extraerId(String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        return jwtUtil.extraerUsuarioId(token);
    }
}
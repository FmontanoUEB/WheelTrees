package co.edu.unbosque.wheeltrees.controller;

import co.edu.unbosque.wheeltrees.DTO.PerfilDTO.*;
import co.edu.unbosque.wheeltrees.security.JwtUtil;
import co.edu.unbosque.wheeltrees.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@Tag(name = "Usuarios", description = "Perfil del usuario autenticado")
@SecurityRequirement(name = "bearerAuth")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final JwtUtil jwtUtil;

    @Operation(summary = "Ver mi perfil")
    @GetMapping("/me")
    public ResponseEntity<PerfilResponse> miPerfil(
            @RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(usuarioService.miPerfil(extraerId(authHeader)));
    }

    @Operation(summary = "Actualizar mi perfil")
    @PutMapping("/me")
    public ResponseEntity<PerfilResponse> actualizar(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody ActualizarPerfilRequest request) {
        return ResponseEntity.ok(usuarioService.actualizarPerfil(extraerId(authHeader), request));
    }

    @Operation(summary = "Actualizar FCM token (notificaciones push)")
    @PatchMapping("/me/fcm-token")
    public ResponseEntity<Void> actualizarFcmToken(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody FcmTokenRequest request) {
        usuarioService.actualizarFcmToken(extraerId(authHeader), request.getFcmToken());
        return ResponseEntity.noContent().build();
    }

    private UUID extraerId(String authHeader) {
        return jwtUtil.extraerUsuarioId(authHeader.replace("Bearer ", ""));
    }
}
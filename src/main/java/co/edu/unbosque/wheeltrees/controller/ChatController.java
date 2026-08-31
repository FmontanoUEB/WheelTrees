package co.edu.unbosque.wheeltrees.controller;

import co.edu.unbosque.wheeltrees.DTO.MensajeDTO.*;
import co.edu.unbosque.wheeltrees.security.JwtUtil;
import co.edu.unbosque.wheeltrees.service.MensajeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Chat directo entre dos personas. No está atado a un viaje ni a una
 * reserva puntual: basta con haber compartido alguna vez un viaje con
 * reserva CONFIRMADA para tener una conversación continua.
 */
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Tag(name = "Chat", description = "Mensajería directa entre personas que ya compartieron un viaje confirmado")
@SecurityRequirement(name = "bearerAuth")
public class ChatController {

	private final MensajeService mensajeService;
	private final JwtUtil jwtUtil;
	private final SimpMessagingTemplate messagingTemplate;

	@Operation(summary = "Historial de mensajes con otra persona")
	@GetMapping("/{otroUsuarioId}/mensajes")
	public ResponseEntity<List<MensajeResponse>> historial(@RequestHeader("Authorization") String authHeader,
			@PathVariable UUID otroUsuarioId) {

		UUID usuarioId = extraerId(authHeader);
		return ResponseEntity.ok(mensajeService.historial(usuarioId, otroUsuarioId));
	}

	@Operation(summary = "Enviar un mensaje (alternativa REST al WebSocket)")
	@PostMapping("/{otroUsuarioId}/mensajes")
	public ResponseEntity<MensajeResponse> enviar(@RequestHeader("Authorization") String authHeader,
			@PathVariable UUID otroUsuarioId, @Valid @RequestBody EnviarMensajeRequest request) {

		UUID usuarioId = extraerId(authHeader);
		request.setDestinatarioId(otroUsuarioId);

		MensajeResponse mensaje = mensajeService.enviar(usuarioId, request);

		// También se retransmite por WebSocket para que el otro participante
		// lo reciba en vivo aunque este mensaje haya llegado por REST.
		String topic = "/topic/chat." + ChatWebSocketController.topicPar(usuarioId, otroUsuarioId);
		messagingTemplate.convertAndSend(topic, mensaje);

		return ResponseEntity.status(HttpStatus.CREATED).body(mensaje);
	}

	@Operation(summary = "Marcar como leídos los mensajes recibidos de otra persona")
	@PatchMapping("/{otroUsuarioId}/leidos")
	public ResponseEntity<Void> marcarLeidos(@RequestHeader("Authorization") String authHeader,
			@PathVariable UUID otroUsuarioId) {

		UUID usuarioId = extraerId(authHeader);
		mensajeService.marcarLeidos(usuarioId, otroUsuarioId);
		return ResponseEntity.noContent().build();
	}

	@Operation(summary = "Listar mis chats: una fila por cada persona con quien compartí un viaje confirmado")
	@GetMapping("/mis-chats")
	public ResponseEntity<List<ChatResumen>> misChats(@RequestHeader("Authorization") String authHeader) {
		UUID usuarioId = extraerId(authHeader);
		return ResponseEntity.ok(mensajeService.misChats(usuarioId));
	}

	private UUID extraerId(String authHeader) {
		String token = authHeader.replace("Bearer ", "");
		return jwtUtil.extraerUsuarioId(token);
	}
}

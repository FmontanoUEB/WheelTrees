package co.edu.unbosque.wheeltrees.controller;

import co.edu.unbosque.wheeltrees.DTO.MensajeDTO.*;
import co.edu.unbosque.wheeltrees.service.MensajeService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.UUID;

/**
 * Puente en tiempo real del chat. El historial y la lista de conversaciones
 * se sirven por REST (ChatController); este controlador solo reenvía
 * mensajes nuevos a quien esté conectado y suscrito al topic de esa pareja
 * de personas — independiente de cualquier viaje o reserva puntual.
 */
@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

	private final MensajeService mensajeService;
	private final SimpMessagingTemplate messagingTemplate;

	@MessageMapping("/chat.enviar")
	public void enviar(@Payload EnviarMensajeRequest request, Principal principal) {
		UUID remitenteId = UUID.fromString(principal.getName());

		try {
			MensajeResponse mensaje = mensajeService.enviar(remitenteId, request);

			// Ambos participantes, sin importar quién escribió, están
			// suscritos al mismo topic (par de ids ordenado alfabéticamente
			// para que da igual quién inicie la conversación).
			String topic = "/topic/chat." + topicPar(remitenteId, request.getDestinatarioId());
			messagingTemplate.convertAndSend(topic, mensaje);
		} catch (IllegalArgumentException e) {
			// Error de validación (sin relación confirmada, etc.): se le
			// devuelve solo al remitente, sin tumbar la conexión.
			messagingTemplate.convertAndSendToUser(
					principal.getName(),
					"/queue/errores",
					ChatError.builder().mensaje(e.getMessage()).build()
			);
		}
	}

	/** Par de ids ordenado de forma determinística (da igual quién lo llame). */
	static String topicPar(UUID a, UUID b) {
		return a.toString().compareTo(b.toString()) < 0
				? a + "_" + b
				: b + "_" + a;
	}
}

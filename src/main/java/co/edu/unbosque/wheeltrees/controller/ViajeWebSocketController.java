package co.edu.unbosque.wheeltrees.controller;

import co.edu.unbosque.wheeltrees.DTO.ViajeDTO.*;
import co.edu.unbosque.wheeltrees.service.ViajeService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.UUID;

/**
 * Puente en tiempo real del seguimiento de viaje. El conductor (app en
 * primer plano mientras el viaje está EN_CURSO) publica su posición GPS
 * aquí cada pocos segundos; este controlador la guarda (ViajeService) y la
 * retransmite a quien esté suscrito al topic de ese viaje puntual —
 * normalmente los pasajeros con reserva CONFIRMADA que abrieron la
 * pantalla de seguimiento.
 *
 * Los cambios de estado del viaje (iniciar/completar/cancelar) NO pasan
 * por aquí: llegan por REST (ViajeController) y ViajeService se encarga de
 * difundirlos al mismo topic, para que el pasajero sepa cuándo empieza y
 * cuándo termina el seguimiento sin tener que hacer polling.
 */
@Controller
@RequiredArgsConstructor
public class ViajeWebSocketController {

	private final ViajeService viajeService;
	private final SimpMessagingTemplate messagingTemplate;

	@MessageMapping("/viaje.ubicacion")
	public void actualizarUbicacion(@Payload UbicacionRequest request, Principal principal) {
		UUID conductorId = UUID.fromString(principal.getName());

		try {
			viajeService.actualizarUbicacion(conductorId, request);
			// La difusión a /topic/viaje.{id}.ubicacion ya la hace
			// ViajeService, así que aquí no hace falta reenviar nada más.
		} catch (IllegalArgumentException e) {
			// Ej: el viaje no es EN_CURSO todavía, o no le pertenece.
			// Se le avisa solo al remitente, sin tumbar la conexión.
			messagingTemplate.convertAndSendToUser(
					principal.getName(),
					"/queue/errores",
					ViajeError.builder().mensaje(e.getMessage()).build()
			);
		}
	}
}

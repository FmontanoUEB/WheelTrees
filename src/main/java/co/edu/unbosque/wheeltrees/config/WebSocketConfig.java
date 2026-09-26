package co.edu.unbosque.wheeltrees.config;

import co.edu.unbosque.wheeltrees.security.JwtHandshakeInterceptor;
import co.edu.unbosque.wheeltrees.security.WebSocketAuthHandshakeHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

	private final JwtHandshakeInterceptor jwtHandshakeInterceptor;
	private final WebSocketAuthHandshakeHandler webSocketAuthHandshakeHandler;

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		// El cliente (RN) se conecta directamente por WebSocket nativo a esta
		// URL: ws://<host>:8080/ws?token=<jwt>  (sin necesidad de SockJS).
		registry.addEndpoint("/ws")
				.setAllowedOriginPatterns("*")
				.addInterceptors(jwtHandshakeInterceptor)
				.setHandshakeHandler(webSocketAuthHandshakeHandler);
	}

	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry) {
		// Broker en memoria: cada pareja de personas tiene su propio canal
		// /topic/chat.{idMenor}_{idMayor} — independiente de viajes/reservas.
		registry.enableSimpleBroker("/topic");
		// Los clientes publican mensajes hacia /app/chat.enviar
		registry.setApplicationDestinationPrefixes("/app");
	}
}

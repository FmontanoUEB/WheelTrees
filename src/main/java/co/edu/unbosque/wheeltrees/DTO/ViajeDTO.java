package co.edu.unbosque.wheeltrees.DTO;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
public class ViajeDTO {
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class PublicarViajeRequest {
        @NotNull private UUID vehiculoId;
        @NotBlank private String origenDescripcion;
        private Double origenLat;
        private Double origenLng;
        @NotBlank private String destinoDescripcion;
        private Double destinoLat;
        private Double destinoLng;
        @NotNull @Future private LocalDateTime fechaHoraSalida;
        @NotNull @Min(1) @Max(8) private Integer cuposTotales;
        @DecimalMin("0.0") private BigDecimal aportePorPasajero = BigDecimal.ZERO;
        @Size(max = 500) private String notas;
    }
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ViajeResponse {
        private String id;
        private String conductorId;
        private String conductorNombre;
        private String vehiculoPlaca;
        private String vehiculoDescripcion;
        private String origenDescripcion;
        private Double origenLat;
        private Double origenLng;
        private String destinoDescripcion;
        private Double destinoLat;
        private Double destinoLng;
        private LocalDateTime fechaHoraSalida;
        private Integer cuposDisponibles;
        private Integer cuposTotales;
        private BigDecimal aportePorPasajero;
        private String estado;
        private String notas;
        private Double ubicacionLat;
        private Double ubicacionLng;
        private LocalDateTime ubicacionActualizadaEn;
    }

    /** Payload que el conductor publica por WebSocket (/app/viaje.ubicacion). */
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class UbicacionRequest {
        @NotNull private UUID viajeId;
        @NotNull private Double lat;
        @NotNull private Double lng;
    }

    /**
     * Evento que se retransmite por /topic/viaje.{id}.ubicacion y que
     * también devuelve el GET de fallback. Sirve tanto para posiciones
     * (lat/lng no nulos) como para avisos de cambio de estado del viaje
     * (iniciar/completar/cancelar), para que la pantalla del pasajero sepa
     * cuándo empezar y cuándo dejar de escuchar.
     */
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class UbicacionEvento {
        private String viajeId;
        private String estado;
        private Double lat;
        private Double lng;
        private LocalDateTime actualizadaEn;
    }

    /** Error de negocio enviado por la cola privada del WebSocket de viaje. */
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ViajeError {
        private String mensaje;
    }
}
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
        private String conductorNombre;
        private String vehiculoPlaca;
        private String vehiculoDescripcion;
        private String origenDescripcion;
        private String destinoDescripcion;
        private LocalDateTime fechaHoraSalida;
        private Integer cuposDisponibles;
        private Integer cuposTotales;
        private BigDecimal aportePorPasajero;
        private String estado;
        private String notas;
    }
}
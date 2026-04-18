package co.edu.unbosque.wheeltrees.DTO;

import jakarta.validation.constraints.*;
import lombok.*;

public class VehiculoDTO {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class VehiculoRequest {
        @NotBlank @Pattern(regexp = "^[A-Z0-9]{5,6}$", message = "Placa inválida")
        private String placa;
        @NotBlank private String marca;
        @NotBlank private String modelo;
        @NotNull @Min(2000) @Max(2027) private Integer anio;
        @NotBlank private String color;
        @NotNull @Min(1) @Max(8) private Integer capacidadPasajeros;
        private String fotoVehiculo;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class VehiculoResponse {
        private String id;
        private String placa;
        private String marca;
        private String modelo;
        private Integer anio;
        private String color;
        private Integer capacidadPasajeros;
        private String fotoVehiculo;
        private boolean activo;
    }
}
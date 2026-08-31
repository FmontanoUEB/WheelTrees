package co.edu.unbosque.wheeltrees.DTO;

import jakarta.validation.constraints.*;
import lombok.*;

public class PerfilDTO {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class PerfilResponse {
        private String id;
        private String nombre;
        private String apellido;
        private String email;
        private String rol;
        private String fotoPerfil;
        private boolean emailVerificado;
        private String direccionCasa;
        private Double casaLat;
        private Double casaLng;
        private String direccionTrabajo;
        private Double trabajoLat;
        private Double trabajoLng;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ActualizarPerfilRequest {
        @NotBlank @Size(min = 2, max = 100)
        private String nombre;
        @NotBlank @Size(min = 2, max = 100)
        private String apellido;
        private String fotoPerfil;

        @Size(max = 255)
        private String direccionCasa;
        private Double casaLat;
        private Double casaLng;

        @Size(max = 255)
        private String direccionTrabajo;
        private Double trabajoLat;
        private Double trabajoLng;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class FcmTokenRequest {
        @NotBlank
        private String fcmToken;
    }
}
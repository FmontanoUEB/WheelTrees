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
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ActualizarPerfilRequest {
        @NotBlank @Size(min = 2, max = 100)
        private String nombre;
        @NotBlank @Size(min = 2, max = 100)
        private String apellido;
        private String fotoPerfil;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class FcmTokenRequest {
        @NotBlank
        private String fcmToken;
    }
}
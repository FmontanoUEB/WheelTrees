package co.edu.unbosque.wheeltrees.DTO;

import co.edu.unbosque.wheeltrees.model.RolUsuario;
import jakarta.validation.constraints.*;
import lombok.*;

public class AuthDTO {

    // ── Registro ─────────────────────────────────────────────────

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class RegistroRequest {

        @NotBlank(message = "El nombre es obligatorio")
        @Size(min = 2, max = 100)
        private String nombre;

        @NotBlank(message = "El apellido es obligatorio")
        @Size(min = 2, max = 100)
        private String apellido;

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "Formato de email inválido")
        @Pattern(
            regexp = "^[a-zA-Z0-9._%+\\-]+@unbosque\\.edu\\.co$",
            message = "Solo se permiten correos @unbosque.edu.co"
        )
        private String email;

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 8, message = "La contraseña debe tener mínimo 8 caracteres")
        private String password;

        @NotNull(message = "El rol es obligatorio")
        private RolUsuario rol;
    }

    // ── Login ─────────────────────────────────────────────────────

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class LoginRequest {

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "Formato de email inválido")
        private String email;

        @NotBlank(message = "La contraseña es obligatoria")
        private String password;
    }

    // ── Verificación OTP ──────────────────────────────────────────

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class VerificarEmailRequest {

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "Formato de email inválido")
        private String email;

        @NotBlank(message = "El código OTP es obligatorio")
        @Size(min = 6, max = 6, message = "El OTP debe tener 6 dígitos")
        private String codigoOtp;
    }

    // ── Response autenticación ────────────────────────────────────

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class AuthResponse {
        private String accessToken;
        private String tipo;
        private UsuarioResponse usuario;
    }

    // ── Datos del usuario en el response ─────────────────────────

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class UsuarioResponse {
        private String id;
        private String nombre;
        private String apellido;
        private String email;
        private String rol;
        private String fotoPerfil;
    }

    // ── Response genérico ─────────────────────────────────────────

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class MensajeResponse {
        private String mensaje;
        private boolean exito;
    }
}
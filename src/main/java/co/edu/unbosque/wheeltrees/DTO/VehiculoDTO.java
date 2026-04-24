package co.edu.unbosque.wheeltrees.DTO;

import co.edu.unbosque.wheeltrees.model.TipoVehiculo;
import jakarta.validation.constraints.*;
import lombok.*;

public class VehiculoDTO {

	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class VehiculoRequest {

		@NotBlank
		@Pattern(regexp = "^[A-Z0-9]{5,6}$", message = "Placa inválida")
		private String placa;

		@NotBlank
		private String marca;

		@NotBlank
		private String modelo;

		@NotNull
		@Min(2000)
		@Max(2027)
		private Integer anio;

		@NotBlank
		private String color;

		@NotNull
		private TipoVehiculo tipo; // CARRO o MOTO

		@NotBlank
		@Pattern(regexp = "^[0-9]{6,12}$", message = "Cédula inválida")
		private String cedulaPropietario;

		@NotNull
		@Min(1)
		@Max(8)
		private Integer capacidadPasajeros;

		private String fotoVehiculo;
	}

	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class VehiculoResponse {
		private String id;
		private String placa;
		private String marca;
		private String modelo;
		private Integer anio;
		private String color;
		private String tipo;
		private String cedulaPropietario;
		private Integer capacidadPasajeros;
		private String fotoVehiculo;
		private boolean activo;
	}
}
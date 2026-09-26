package co.edu.unbosque.wheeltrees.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import co.edu.unbosque.wheeltrees.security.CifradoAttributeConverter;

import static org.junit.jupiter.api.Assertions.*;

class CifradoAttributeConverterTest {

	private CifradoAttributeConverter converter;

	@BeforeEach
	void setUp() {
		converter = new CifradoAttributeConverter();
		ReflectionTestUtils.setField(converter, "claveBase64", "90lhDwDHb6NOIQjDmgYFE+qoQmQaPQl2uJOApsR7D6g=");
	}

	@Test
	void debeCifrarYDescifrarCorrectamente() {
		String original = "Juan Pérez";

		String cifrado = converter.convertToDatabaseColumn(original);
		String descifrado = converter.convertToEntityAttribute(cifrado);

		assertNotEquals(original, cifrado, "El texto cifrado no debe ser igual al original");
		assertEquals(original, descifrado, "Al descifrar debe recuperarse el texto original");
	}

	@Test
	void debeManejarValoresNulos() {
		assertNull(converter.convertToDatabaseColumn(null));
		assertNull(converter.convertToEntityAttribute(null));
	}

	@Test
	void mismoTextoDebeProducirCifradosDistintos() {
		
		String original = "Bogotá, Calle 145";

		String cifrado1 = converter.convertToDatabaseColumn(original);
		String cifrado2 = converter.convertToDatabaseColumn(original);

		assertNotEquals(cifrado1, cifrado2);
		assertEquals(original, converter.convertToEntityAttribute(cifrado1));
		assertEquals(original, converter.convertToEntityAttribute(cifrado2));
	}

	@Test
	void debeLanzarExcepcionSiElValorNoEsValido() {
		assertThrows(IllegalStateException.class, () -> converter.convertToEntityAttribute("esto-no-es-base64-valido-de-aes-gcm"));
	}
}
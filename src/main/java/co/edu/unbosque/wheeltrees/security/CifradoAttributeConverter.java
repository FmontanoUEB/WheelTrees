package co.edu.unbosque.wheeltrees.security;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Convierte de forma transparente los campos de texto anotados con
 * @Convert(converter = CifradoAttributeConverter.class):
 *  - Al guardar (convertToDatabaseColumn): cifra con AES-256-GCM.
 *  - Al leer (convertToEntityAttribute): descifra automáticamente.
 *
 * El resto del código (Service, Controller, DTO) nunca ve el texto
 * cifrado: siempre trabaja con el String plano.
 *
 * Formato guardado en la columna: Base64( IV(12 bytes) + textoCifrado + tag(16 bytes) )
 */
@Converter
@Component
public class CifradoAttributeConverter implements AttributeConverter<String, String> {

	private static final String ALGORITMO = "AES/GCM/NoPadding";
	private static final int TAMANIO_TAG_BITS = 128;
	private static final int TAMANIO_IV_BYTES = 12;

	@Value("${app.encryption.key}")
	private String claveBase64;

	@Override
	public String convertToDatabaseColumn(String valorPlano) {
		if (valorPlano == null) {
			return null;
		}
		try {
			byte[] iv = new byte[TAMANIO_IV_BYTES];
			new SecureRandom().nextBytes(iv);

			Cipher cipher = Cipher.getInstance(ALGORITMO);
			cipher.init(Cipher.ENCRYPT_MODE, obtenerClave(), new GCMParameterSpec(TAMANIO_TAG_BITS, iv));

			byte[] cifrado = cipher.doFinal(valorPlano.getBytes(StandardCharsets.UTF_8));

			ByteBuffer buffer = ByteBuffer.allocate(iv.length + cifrado.length);
			buffer.put(iv);
			buffer.put(cifrado);

			return Base64.getEncoder().encodeToString(buffer.array());

		} catch (Exception e) {
			throw new IllegalStateException("Error cifrando el dato", e);
		}
	}

	@Override
	public String convertToEntityAttribute(String valorCifrado) {
		if (valorCifrado == null) {
			return null;
		}
		try {
			byte[] datos = Base64.getDecoder().decode(valorCifrado);
			ByteBuffer buffer = ByteBuffer.wrap(datos);

			byte[] iv = new byte[TAMANIO_IV_BYTES];
			buffer.get(iv);
			byte[] cifrado = new byte[buffer.remaining()];
			buffer.get(cifrado);

			Cipher cipher = Cipher.getInstance(ALGORITMO);
			cipher.init(Cipher.DECRYPT_MODE, obtenerClave(), new GCMParameterSpec(TAMANIO_TAG_BITS, iv));

			byte[] plano = cipher.doFinal(cifrado);
			return new String(plano, StandardCharsets.UTF_8);

		} catch (Exception e) {
			throw new IllegalStateException("Error descifrando el dato. ¿El valor en la BD ya estaba cifrado?", e);
		}
	}

	private SecretKeySpec obtenerClave() {
		byte[] keyBytes = Base64.getDecoder().decode(claveBase64);
		return new SecretKeySpec(keyBytes, "AES");
	}
}
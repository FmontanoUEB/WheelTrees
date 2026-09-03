package co.edu.unbosque.wheeltrees.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.wheeltrees.DTO.AuthDTO.AuthResponse;
import co.edu.unbosque.wheeltrees.DTO.AuthDTO.LoginRequest;
import co.edu.unbosque.wheeltrees.DTO.AuthDTO.MensajeResponse;
import co.edu.unbosque.wheeltrees.DTO.AuthDTO.RegistroRequest;
import co.edu.unbosque.wheeltrees.DTO.AuthDTO.UsuarioResponse;
import co.edu.unbosque.wheeltrees.DTO.AuthDTO.VerificarEmailRequest;
import co.edu.unbosque.wheeltrees.model.Usuario;
import co.edu.unbosque.wheeltrees.model.VerificacionEmail;
import co.edu.unbosque.wheeltrees.repository.UsuarioRepository;
import co.edu.unbosque.wheeltrees.repository.VerificacionEmailRepository;
import co.edu.unbosque.wheeltrees.security.JwtUtil;

import java.security.SecureRandom;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final UsuarioRepository usuarioRepository;
	private final VerificacionEmailRepository verificacionEmailRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtUtil jwtUtil;
	private final AuthenticationManager authenticationManager;
	private final EmailService emailService;

	private static final String DOMINIO_INSTITUCIONAL = "@unbosque.edu.co";

	@Transactional
	public MensajeResponse eliminarUsuarioPorEmail(String email) {

		Usuario usuario = usuarioRepository.findByEmail(email)
				.orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

		// 🔥 ELIMINA TODAS las verificaciones (no solo algunas)
		verificacionEmailRepository.deleteByUsuario(usuario);

		// 🔥 AHORA sí elimina el usuario
		usuarioRepository.delete(usuario);

		return MensajeResponse.builder().mensaje("Usuario eliminado correctamente").exito(true).build();
	}

	@Transactional
	public MensajeResponse registrar(RegistroRequest request) {

		if (!request.getEmail().endsWith(DOMINIO_INSTITUCIONAL)) {
			throw new IllegalArgumentException("Solo se permiten correos institucionales @unbosque.edu.co");
		}

		if (usuarioRepository.existsByEmail(request.getEmail())) {
			throw new IllegalArgumentException("Ya existe una cuenta con este correo institucional");
		}

		Usuario usuario = Usuario.builder().nombre(request.getNombre()).apellido(request.getApellido())
				.email(request.getEmail()).passwordHash(passwordEncoder.encode(request.getPassword()))
				.rol(request.getRol()).emailVerificado(false).cuentaActiva(true).build();

		usuarioRepository.save(usuario);

		String otp = generarOtp();

		VerificacionEmail verificacion = VerificacionEmail.builder().usuario(usuario).codigoOtp(otp).usado(false)
				.build();

		verificacionEmailRepository.save(verificacion);

		// 🔥 ENVÍO REAL DEL CORREO
		emailService.enviarOtpVerificacion(usuario.getEmail(), usuario.getNombre(), otp);

		System.out.println("OTP enviado (debug): " + otp);

		return MensajeResponse.builder()
				.mensaje("Registro exitoso. Revisa tu correo institucional para verificar tu cuenta.").exito(true)
				.build();
	}

	@Transactional
	public MensajeResponse verificarEmail(VerificarEmailRequest request) {

		Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
				.orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

		VerificacionEmail verificacion = verificacionEmailRepository
				.findTopByUsuarioAndCodigoOtpAndUsadoFalseOrderByCreadoEnDesc(usuario, request.getCodigoOtp())
				.orElseThrow(() -> new IllegalArgumentException("Código OTP inválido"));

		if (!verificacion.estaVigente()) {
			throw new IllegalArgumentException("El OTP ha expirado. Solicita uno nuevo.");
		}

		verificacion.setUsado(true);
		verificacionEmailRepository.save(verificacion);
		usuario.setEmailVerificado(true);
		usuarioRepository.save(usuario);

		return MensajeResponse.builder().mensaje("Email verificado correctamente. Ya puedes iniciar sesión.")
				.exito(true).build();
	}

	public AuthResponse login(LoginRequest request) {

		authenticationManager
				.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

		Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
				.orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

		if (!usuario.isEmailVerificado()) {
			throw new IllegalArgumentException("Debes verificar tu correo antes de iniciar sesión");
		}

		String token = jwtUtil.generarToken(usuario);

		UsuarioResponse usuarioResponse = UsuarioResponse.builder().id(usuario.getId().toString())
				.nombre(usuario.getNombre()).apellido(usuario.getApellido()).email(usuario.getEmail())
				.rol(usuario.getRol().name()).fotoPerfil(usuario.getFotoPerfil()).build();

		return AuthResponse.builder().accessToken(token).tipo("Bearer").usuario(usuarioResponse).build();
	}

	private String generarOtp() {
		SecureRandom random = new SecureRandom();
		return String.valueOf(100000 + random.nextInt(900000));
	}

	@Transactional
	public MensajeResponse reenviarOtp(String email) {

		Usuario usuario = usuarioRepository.findByEmail(email)
				.orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

		if (usuario.isEmailVerificado()) {
			throw new IllegalArgumentException("Este correo ya fue verificado");
		}

		// Invalida OTPs anteriores
		List<VerificacionEmail> anteriores = verificacionEmailRepository.findByUsuarioAndUsadoFalse(usuario);
		anteriores.forEach(v -> v.setUsado(true));
		verificacionEmailRepository.saveAll(anteriores);

		String otp = generarOtp();

		VerificacionEmail verificacion = VerificacionEmail.builder().usuario(usuario).codigoOtp(otp).usado(false)
				.build();

		verificacionEmailRepository.save(verificacion);
		emailService.enviarOtpVerificacion(usuario.getEmail(), usuario.getNombre(), otp);

		return MensajeResponse.builder().mensaje("Se envió un nuevo código OTP a tu correo.").exito(true).build();
	}
}
package co.edu.unbosque.wheeltrees.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import co.edu.unbosque.wheeltrees.repository.UsuarioRepository;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

	private final JwtUtil jwtUtil;
	private final UsuarioRepository usuarioRepository;

	// 🔥 Debe coincidir con las rutas públicas de SecurityConfig.RUTAS_PUBLICAS
	private static final List<String> RUTAS_SIN_JWT = List.of(
			"/api/auth/",
			"/h2-console/",
			"/swagger",
			"/v3/api-docs",
			"/swagger-resources",
			"/webjars/",
			"/test/"
	);

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		String path = request.getServletPath();

		boolean esRutaPublica = RUTAS_SIN_JWT.stream().anyMatch(path::startsWith);

		if (esRutaPublica) {
			filterChain.doFilter(request, response);
			return;
		}

		final String authHeader = request.getHeader("Authorization");

		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			filterChain.doFilter(request, response);
			return;
		}

		final String token = authHeader.substring(7);

		try {
			// ✅ VALIDAR primero
			if (!jwtUtil.validarToken(token)) {
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				return;
			}

			// ✅ Luego extraer datos
			final String email = jwtUtil.extraerEmail(token);

			if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

				var usuario = usuarioRepository.findByEmail(email).orElse(null);

				if (usuario != null) {
					UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(usuario,
							null, usuario.getAuthorities());

					authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
					SecurityContextHolder.getContext().setAuthentication(authToken);
				}
			}

		} catch (Exception e) {
			// 🔥 Manejo de token expirado o inválido
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			return;
		}

		filterChain.doFilter(request, response);
	}
}
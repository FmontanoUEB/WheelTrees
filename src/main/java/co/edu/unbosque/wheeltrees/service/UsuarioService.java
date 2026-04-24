package co.edu.unbosque.wheeltrees.service;

import co.edu.unbosque.wheeltrees.DTO.PerfilDTO.*;
import co.edu.unbosque.wheeltrees.model.Usuario;
import co.edu.unbosque.wheeltrees.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public PerfilResponse miPerfil(UUID usuarioId) {
        Usuario u = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        return toResponse(u);
    }

    @Transactional
    public PerfilResponse actualizarPerfil(UUID usuarioId, ActualizarPerfilRequest request) {
        Usuario u = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        u.setNombre(request.getNombre());
        u.setApellido(request.getApellido());
        u.setFotoPerfil(request.getFotoPerfil());
        return toResponse(usuarioRepository.save(u));
    }

    @Transactional
    public void actualizarFcmToken(UUID usuarioId, String fcmToken) {
        Usuario u = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        u.setFcmToken(fcmToken);
        usuarioRepository.save(u);
    }

    private PerfilResponse toResponse(Usuario u) {
        return PerfilResponse.builder()
                .id(u.getId().toString())
                .nombre(u.getNombre())
                .apellido(u.getApellido())
                .email(u.getEmail())
                .rol(u.getRol().name())
                .fotoPerfil(u.getFotoPerfil())
                .emailVerificado(u.isEmailVerificado())
                .build();
    }
}
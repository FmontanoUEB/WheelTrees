package co.edu.unbosque.wheeltrees.service;

import co.edu.unbosque.wheeltrees.DTO.PerfilDTO.*;
import co.edu.unbosque.wheeltrees.model.Usuario;
import co.edu.unbosque.wheeltrees.model.RolUsuario;
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
        u.setDireccionCasa(request.getDireccionCasa());
        u.setCasaLat(request.getCasaLat());
        u.setCasaLng(request.getCasaLng());
        u.setDireccionTrabajo(request.getDireccionTrabajo());
        u.setTrabajoLat(request.getTrabajoLat());
        u.setTrabajoLng(request.getTrabajoLng());
        return toResponse(usuarioRepository.save(u));
    }

    @Transactional
    public PerfilResponse actualizarRol(UUID usuarioId, String rol) {
        Usuario u = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        final RolUsuario nuevoRol;
        try {
            nuevoRol = RolUsuario.valueOf(rol.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Rol no válido. Usa CONDUCTOR o PASAJERO");
        }

        if (nuevoRol != RolUsuario.CONDUCTOR && nuevoRol != RolUsuario.PASAJERO) {
            throw new IllegalArgumentException("El rol debe ser CONDUCTOR o PASAJERO");
        }

        u.setRol(nuevoRol);
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
                .direccionCasa(u.getDireccionCasa())
                .casaLat(u.getCasaLat())
                .casaLng(u.getCasaLng())
                .direccionTrabajo(u.getDireccionTrabajo())
                .trabajoLat(u.getTrabajoLat())
                .trabajoLng(u.getTrabajoLng())
                .build();
    }
}
package co.edu.unbosque.wheeltrees.service;

import co.edu.unbosque.wheeltrees.DTO.VehiculoDTO.*;
import co.edu.unbosque.wheeltrees.model.RolUsuario;
import co.edu.unbosque.wheeltrees.model.Usuario;
import co.edu.unbosque.wheeltrees.model.Vehiculo;
import co.edu.unbosque.wheeltrees.repository.UsuarioRepository;
import co.edu.unbosque.wheeltrees.repository.VehiculoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VehiculoService {

    private final VehiculoRepository vehiculoRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public VehiculoResponse registrar(UUID conductorId, VehiculoRequest request) {

        Usuario conductor = usuarioRepository.findById(conductorId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        if (conductor.getRol() != RolUsuario.CONDUCTOR) {
            throw new IllegalArgumentException("Solo los conductores pueden registrar vehículos");
        }

        if (vehiculoRepository.existsByPlaca(request.getPlaca().toUpperCase())) {
            throw new IllegalArgumentException("Ya existe un vehículo con esta placa");
        }

        Vehiculo vehiculo = Vehiculo.builder()
                .conductor(conductor)
                .placa(request.getPlaca().toUpperCase())
                .marca(request.getMarca())
                .modelo(request.getModelo())
                .anio(request.getAnio())
                .color(request.getColor())
                .capacidadPasajeros(request.getCapacidadPasajeros())
                .fotoVehiculo(request.getFotoVehiculo())
                .activo(true)
                .build();

        return toResponse(vehiculoRepository.save(vehiculo));
    }

    public List<VehiculoResponse> listarMisVehiculos(UUID conductorId) {
        Usuario conductor = usuarioRepository.findById(conductorId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        return vehiculoRepository.findByConductorAndActivoTrue(conductor)
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public VehiculoResponse actualizar(UUID conductorId, UUID vehiculoId, VehiculoRequest request) {
        Vehiculo vehiculo = vehiculoRepository.findById(vehiculoId)
                .orElseThrow(() -> new IllegalArgumentException("Vehículo no encontrado"));

        if (!vehiculo.getConductor().getId().equals(conductorId)) {
            throw new IllegalArgumentException("No tienes permiso para editar este vehículo");
        }

        vehiculo.setMarca(request.getMarca());
        vehiculo.setModelo(request.getModelo());
        vehiculo.setAnio(request.getAnio());
        vehiculo.setColor(request.getColor());
        vehiculo.setCapacidadPasajeros(request.getCapacidadPasajeros());
        vehiculo.setFotoVehiculo(request.getFotoVehiculo());

        return toResponse(vehiculoRepository.save(vehiculo));
    }

    @Transactional
    public void desactivar(UUID conductorId, UUID vehiculoId) {
        Vehiculo vehiculo = vehiculoRepository.findById(vehiculoId)
                .orElseThrow(() -> new IllegalArgumentException("Vehículo no encontrado"));

        if (!vehiculo.getConductor().getId().equals(conductorId)) {
            throw new IllegalArgumentException("No tienes permiso para eliminar este vehículo");
        }

        vehiculo.setActivo(false);
        vehiculoRepository.save(vehiculo);
    }

    private VehiculoResponse toResponse(Vehiculo v) {
        return VehiculoResponse.builder()
                .id(v.getId().toString())
                .placa(v.getPlaca())
                .marca(v.getMarca())
                .modelo(v.getModelo())
                .anio(v.getAnio())
                .color(v.getColor())
                .capacidadPasajeros(v.getCapacidadPasajeros())
                .fotoVehiculo(v.getFotoVehiculo())
                .activo(v.isActivo())
                .build();
    }
}
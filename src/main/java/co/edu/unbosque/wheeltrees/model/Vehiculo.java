package co.edu.unbosque.wheeltrees.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "vehiculos")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Vehiculo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conductor_id", nullable = false)
    private Usuario conductor;

    @Column(name = "placa", nullable = false, unique = true, length = 10)
    private String placa;

    @Column(name = "marca", nullable = false, length = 50)
    private String marca;

    @Column(name = "modelo", nullable = false, length = 50)
    private String modelo;

    @Column(name = "anio", nullable = false)
    private Integer anio;

    @Column(name = "color", nullable = false, length = 30)
    private String color;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false)
    private TipoVehiculo tipo; // CARRO o MOTO

    @Column(name = "cedula_propietario", nullable = false, length = 20)
    private String cedulaPropietario;

    @Column(name = "capacidad_pasajeros", nullable = false)
    private Integer capacidadPasajeros;

    @Column(name = "foto_vehiculo", length = 500)
    private String fotoVehiculo;

    @Column(name = "activo", nullable = false)
    private boolean activo = true;

    /**
     * El conductor declara, al registrar o actualizar el vehículo, que tiene
     * todos los documentos legales al día (SOAT, tecnomecánica cuando aplique,
     * licencia de conducción vigente, tarjeta de propiedad, etc.).
     *
     * OJO: se guarda como Boolean (objeto) y la columna se deja SIN
     * "nullable = false" a propósito. Una columna nueva que admite NULL
     * siempre se puede agregar con ALTER TABLE a una tabla que ya tiene
     * filas, sin necesitar DEFAULT ni que la tabla esté vacía. Las filas
     * existentes quedarán con NULL, y eso se interpreta como "false" en
     * isTerminosAceptados() de abajo — no rompe nada y no requiere tocar
     * la base de datos a mano.
     */
    @Column(name = "terminos_aceptados")
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private Boolean terminosAceptados = Boolean.FALSE;

    public boolean isTerminosAceptados() {
        return Boolean.TRUE.equals(terminosAceptados);
    }

    public void setTerminosAceptados(boolean terminosAceptados) {
        this.terminosAceptados = terminosAceptados;
    }

    @Column(name = "terminos_aceptados_en")
    private LocalDateTime terminosAceptadosEn;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn;

    @PrePersist
    protected void onCreate() {
        this.creadoEn = LocalDateTime.now();
    }
}
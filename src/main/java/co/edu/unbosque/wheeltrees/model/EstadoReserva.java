package co.edu.unbosque.wheeltrees.model;

public enum EstadoReserva {
    PENDIENTE,   // pasajero solicitó, espera confirmación del conductor
    CONFIRMADA,  // conductor aceptó
    RECHAZADA,   // conductor rechazó
    CANCELADA,   // pasajero canceló
    COMPLETADA   // viaje terminado
}
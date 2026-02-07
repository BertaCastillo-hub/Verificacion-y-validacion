package es.unizar.eina.M132_quads.database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Clase anotada como entidad que representa una reserva y que consta
 * de identificador de reserva, nombre de cliente, número móvil, fecha
 * de recogida, fecha de devolución y precio total.
 */
@Entity(tableName = "Reserva")
public class Reserva {

    /** Identificador de la reserva. */
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "idReserva")
    private int idReserva;

    /** Nombre del cliente de la reserva. */
    @NonNull
    @ColumnInfo(name = "nombreCliente")
    private String nombreCliente;

    /** Número móvil del cliente de la reserva. */
    @NonNull
    @ColumnInfo(name = "numeroMovil")
    private int numeroMovil;

    /** Fecha de recogida de los quads de la reserva (Unix timestamp). */
    @NonNull
    @ColumnInfo(name = "fechaRecogida")
    private long fechaRecogida;

    /** Fecha de devolución de los quads de la reserva (Unix timestamp). */
    @NonNull
    @ColumnInfo(name = "fechaDevolucion")
    private long fechaDevolucion;

    /** Precio total de la reserva. */
    @NonNull
    @ColumnInfo(name = "precioTotal")
    private int precioTotal;

    public Reserva(@NonNull int idReserva, @NonNull String nombreCliente, @NonNull int numeroMovil,
            @NonNull long fechaRecogida, @NonNull long fechaDevolucion, @NonNull int precioTotal) {
        this.idReserva = idReserva;
        this.nombreCliente = nombreCliente;
        this.numeroMovil = numeroMovil;
        this.fechaRecogida = fechaRecogida;
        this.fechaDevolucion = fechaDevolucion;
        this.precioTotal = precioTotal;
    }

    /** Devuelve el identificador de la reserva */
    public int getIdReserva() {
        return this.idReserva;
    }

    /** Permite actualizar el identificador de la reserva */
    public void setIdReserva(int id) {
        this.idReserva = id;
    }

    /** Devuelve el nombre del cliente de la reserva */
    public String getNombreCliente() {
        return this.nombreCliente;
    }

    /** Permite actualizar el nombre del cliente de la reserva */
    public void setNombreCliente(String nombre) {
        this.nombreCliente = nombre;
    }

    /** Devuelve el número móvil del cliente de la reserva */
    public int getNumeroMovil() {
        return this.numeroMovil;
    }

    /** Permite actualizar el número móvil del cliente de la reserva */
    public void setNumeroMovil(int numero) {
        this.numeroMovil = numero;
    }

    /** Devuelve la fecha de recogida de la reserva */
    public long getFechaRecogida() {
        return this.fechaRecogida;
    }

    /** Permite actualizar la fecha de recogida de la reserva */
    public void setFechaRecogida(long fecha) {
        this.fechaRecogida = fecha;
    }

    /** Devuelve la fecha de devolución de la reserva */
    public long getFechaDevolucion() {
        return this.fechaDevolucion;
    }

    /** Permite actualizar la fecha de devolución de la reserva */
    public void setFechaDevolucion(long fecha) {
        this.fechaDevolucion = fecha;
    }

    /** Devuelve el precio total de la reserva */
    public int getPrecioTotal() {
        return this.precioTotal;
    }

    /** Permite actualizar el precio total de la reserva */
    public void setPrecioTotal(int precio) {
        this.precioTotal = precio;
    }

    public static String validateNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            return "El nombre no puede estar vacío";
        }
        return null;
    }

    // Ya no es necesario validar formato de string, pero podemos validar que sea
    // positiva
    public static String validateFecha(long fecha) {
        if (fecha <= 0) {
            return "Fecha inválida";
        }
        return null;
    }

    public static String validateFechas(long fechaRecogida, long fechaDevolucion) {
        if (fechaDevolucion < fechaRecogida) {
            return "La fecha de devolución no puede ser anterior a la de recogida";
        }
        return null; // Válidas
    }

    public static String validatePrecio(int precio) {
        if (precio < 0) {
            return "El precio no puede ser negativo";
        }
        return null;
    }
}

package es.unizar.eina.M132_quads.database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Clase anotada como entidad que representa los cascos asociados a una reserva
 * y un quad. Consta de un número de cascos, matricula del quad e identificador
 * de reserva.
 */
@Entity(tableName = "casco", primaryKeys = { "matriculaQuad", "idReserva" }, // Clave primaria compuesta
        foreignKeys = {
                @ForeignKey(entity = Quad.class, parentColumns = "matricula", // Campo en la tabla 'quad'
                        childColumns = "matriculaQuad", // Campo en esta tabla ('casco')
                        onDelete = ForeignKey.CASCADE), // Si se borra un quad, se borra esta entrada
                @ForeignKey(entity = Reserva.class, parentColumns = "idReserva", // Campo en la tabla 'reserva'
                        childColumns = "idReserva", // Campo en esta tabla ('casco')
                        onDelete = ForeignKey.CASCADE),
        }) // Si se borra una reserva, se borra esta entrada
public class Casco {

    /** Número de cascos. */
    @NonNull
    @ColumnInfo(name = "numCascos")
    private int numCascos;

    /** Matrícula del quad asociado. */
    @NonNull
    @ColumnInfo(name = "matriculaQuad")
    private String matriculaQuad;

    /** Identificador de la reserva asociada. */
    @NonNull
    @ColumnInfo(name = "idReserva")
    private int idReserva;

    public Casco(@NonNull int numCascos, String matriculaQuad, int idReserva) {
        this.numCascos = numCascos;
        this.matriculaQuad = matriculaQuad;
        this.idReserva = idReserva;
    }

    /** Devuelve el número de cascos. */
    public int getNumCascos() {
        return numCascos;
    }

    /** Permite actualizar el número de cascos. */
    public void setNumCascos(int numCascos) {
        this.numCascos = numCascos;
    }

    /** Devuelve la matrícula del quad. */
    @NonNull
    public String getMatriculaQuad() {
        return matriculaQuad;
    }

    /** Permite actualizar la matrícula del quad. */
    public void setMatriculaQuad(@NonNull String quadMatricula) {
        this.matriculaQuad = quadMatricula;
    }

    /** Devuelve el ID de la reserva. */
    public int getIdReserva() {
        return idReserva;
    }

    /** Permite actualizar el ID de la reserva. */
    public void setIdReserva(int reservaIdReserva) {
        this.idReserva = reservaIdReserva;
    }
}

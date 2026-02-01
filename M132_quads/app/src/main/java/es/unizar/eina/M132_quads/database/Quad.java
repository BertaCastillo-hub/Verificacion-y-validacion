package es.unizar.eina.M132_quads.database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Clase anotada como entidad que representa una quad y que consta de matrícula,
 * tipo, precio y descripción.
 */
@Entity(tableName = "quad")
public class Quad {

    /** Tipo de dato enumerado para el tipo de quad: Monoplaza o Biplaza. */
    public enum TipoQuad {
        Monoplaza,
        Biplaza
    }

    /** Matrícula del quad. */
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "matricula")
    private String matricula;

    /** Tipo del quad. */
    @NonNull
    @ColumnInfo(name = "tipo")
    // El tipo es 'Monoplaza' o 'Biplaza'.
    private TipoQuad tipo;

    /** Precio del quad. */
    @NonNull
    @ColumnInfo(name = "precio")
    // Es int porque está expresado en céntimos para que sea preciso.
    private int precio;

    /** Descripción del quad. */
    @ColumnInfo(name = "descripcion")
    private String descripcion;

    public Quad(String matricula, @NonNull TipoQuad tipo, @NonNull int precio,
            String descripcion) {
        this.matricula = matricula;
        this.tipo = tipo;
        this.precio = precio;
        this.descripcion = descripcion;
    }

    /** Devuelve la matricula del quad */
    public String getMatricula() {
        return this.matricula;
    }

    /** Permite actualizar la matricula de un quad */
    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    /** Devuelve el tipo del quad */
    public TipoQuad getTipo() {
        return this.tipo;
    }

    /** Permite actualizar el tipo de un quad */
    public void setTipo(TipoQuad tipo) {
        this.tipo = tipo;
    }

    /** Devuelve el precio del quad */
    public int getPrecio() {
        return this.precio;
    }

    /** Permite actualizar el precio de un quad */
    public void setPrecio(int precio) {
        this.precio = precio;
    }

    /** Devuelve la descripción del quad */
    public String getDescripcion() {
        return this.descripcion;
    }

    /** Permite actualizar la descripción de un quad */
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    /**
     * Valida la matrícula de un quad.
     * 
     * @param matricula Matrícula a validar.
     * @return Mensaje de error si es inválida, null si es válida.
     */
    public static String validateMatricula(String matricula) {
        if (matricula == null) {
            return "La matrícula no puede ser nula";
        }
        if (matricula.length() != 7) {
            return "La matrícula debe tener 7 caracteres";
        }
        if (!matricula.matches("^[0-9]{4}[A-Za-z]{3}$")) {
            return "La matrícula debe tener 4 números y 3 letras";
        }
        return null; // Válida
    }

    /**
     * Valida el precio de un quad.
     * 
     * @param precio Precio a validar.
     * @return Mensaje de error si es inválido, null si es válido.
     */
    public static String validatePrecio(int precio) {
        if (precio <= 0) {
            return "El precio debe ser mayor que 0";
        }
        return null; // Válido
    }

    /**
     * Valida el tipo de un quad.
     * 
     * @param tipo Tipo a validar.
     * @return Mensaje de error si es inválido, null si es válido.
     */
    public static String validateTipo(TipoQuad tipo) {
        if (tipo == null) {
            return "El tipo no puede ser nulo";
        }
        return null; // Válido
    }
}

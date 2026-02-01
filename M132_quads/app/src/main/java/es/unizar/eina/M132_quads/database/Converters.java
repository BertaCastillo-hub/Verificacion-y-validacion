package es.unizar.eina.M132_quads.database;

import androidx.room.TypeConverter;
import es.unizar.eina.M132_quads.database.Quad.TipoQuad;

/**
 * Clase para poder convertir el tipo enumerado a un tipo reconocido por la base
 * de datos.
 */
public class Converters {

    @TypeConverter
    public static TipoQuad fromString(String value) {
        // Si el valor es nulo, devuelve null. Si no, convierte el String al enum.
        return value == null ? null : TipoQuad.valueOf(value);
    }

    @TypeConverter
    public static String tipoQuadToString(TipoQuad tipo) {
        // Si el enum es nulo, devuelve null. Si no, devuelve su nombre como String.
        return tipo == null ? null : tipo.name();
    }
}

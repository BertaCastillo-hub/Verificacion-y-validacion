package es.unizar.eina.M132_quads.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

/** Definición de un Data Access Object para los quads */
@Dao
public interface QuadDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insert(Quad quad);

    @Update
    int update(Quad quad);

    @Delete
    int delete(Quad quad);

    @Query("DELETE FROM Quad")
    void deleteAll();

    /**
     * Obtiene la lista de todos los quads de la base de datos
     * ordenados en función de uno de sus campos (matricula, tipo o precio).
     *
     * @param orderBy Campo en base al cual se ordena la lista.
     * @return Un LiveData<List<Quad>> con la lista de quads.
     */
    @Query("SELECT * FROM Quad ORDER BY " +
            "CASE WHEN :orderBy = 'matricula' THEN matricula END ASC, " +
            "CASE WHEN :orderBy = 'tipo' THEN tipo END ASC, " +
            "CASE WHEN :orderBy = 'precio' THEN precio END ASC")
    LiveData<List<Quad>> getOrderedQuads(String orderBy);

    /**
     * Obtiene la información del quad almacenada en la base de datos
     * cuya matrícula es esa.
     *
     * @param matricula La matrícula del quad a buscar.
     * @return Un LiveData<Quad> con el quad correspondiente.
     */
    @Query("SELECT * FROM Quad WHERE matricula = :matricula")
    LiveData<Quad> getQuadByMatricula(String matricula);
}


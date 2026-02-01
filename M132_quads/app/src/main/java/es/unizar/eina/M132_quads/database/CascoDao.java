package es.unizar.eina.M132_quads.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import java.util.List;

/** Definición de un Data Access Object para los cascos */
@Dao
public interface CascoDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insert(Casco casco);

    @Update
    int update(Casco casco);

    @Delete
    int delete(Casco casco);

    @Query("DELETE FROM Casco")
    void deleteAll();

    @Query("SELECT * FROM Casco ORDER BY idReserva ASC")
    LiveData<List<Casco>> getOrderedCascos();

    /**
     * Selecciona todos los registros de 'Casco' que coincidan con un 'idReserva' específico.
     * La consulta SQL es directa: "SELECT * FROM casco WHERE id_reserva = :idReserva".
     *
     * @param idReserva El ID de la reserva por la que filtrar.
     * @return Un LiveData<List<Casco>>, que notificará a los observadores cuando los datos
     *         cambien. Room se encarga de que esta consulta se ejecute en un hilo secundario.
     */
    @Query("SELECT * FROM casco WHERE idReserva = :idReserva")
    LiveData<List<Casco>> getCascosForReserva(int idReserva);

    /**
     * Borra todos los cascos asociados a un idReserva específico.
     * Este será el primer paso en la transacción de actualización.
     */
    @Query("DELETE FROM casco WHERE idReserva = :idReserva")
    void deleteCascosByReservaId(int idReserva);

    /**
     * Método transaccional principal. Room ejecutará estas operaciones como una única
     * transacción atómica.
     * Primero borra todos los cascos existentes para la reserva. Después, inserta la
     * nueva lista de cascos.
     *
     * @param idReserva El ID de la reserva que se está actualizando.
     * @param nuevosCascos La nueva lista de objetos Casco a insertar.
     */
    @Transaction
    default void updateCascosForReserva(int idReserva, List<Casco> nuevosCascos) {
        // Borrar los cascos antiguos de esta reserva.
        deleteCascosByReservaId(idReserva);

        // Insertar todos los cascos de la nueva lista.
        for (Casco casco : nuevosCascos) {
            insert(casco);
        }
    }
}


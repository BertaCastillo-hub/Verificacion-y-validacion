package es.unizar.eina.M132_quads.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

/** Definición de un Data Access Object para las reservas */
@Dao
public interface ReservaDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insert(Reserva reserva);

    @Update
    int update(Reserva reserva);

    @Delete
    int delete(Reserva reserva);

    @Query("DELETE FROM Reserva")
    void deleteAll();

    /**
     * Obtiene la lista de todas las reservas de la base de datos
     * filtradas y ordenadas.
     *
     * @param orderBy          Campo en base al cual se ordena la lista.
     * @param filterType       Tipo de filtro ("Todas", "Previstas", "Vigentes",
     *                         "Caducadas").
     * @param currentTimestamp Fecha actual en milisegundos para comparar.
     * @return Un LiveData<List<Reserva>> con la lista de reservas.
     */
    @Query("SELECT * FROM Reserva WHERE " +
            "(:filterType = 'Todas') OR " +
            "(:filterType = 'Previstas' AND fechaRecogida > :currentTimestamp) OR " +
            "(:filterType = 'Vigentes' AND fechaRecogida <= :currentTimestamp AND fechaDevolucion >= :currentTimestamp) OR "
            +
            "(:filterType = 'Caducadas' AND fechaDevolucion < :currentTimestamp) " +
            "ORDER BY " +
            "CASE WHEN :orderBy = 'nombreCliente' THEN nombreCliente END ASC, " +
            "CASE WHEN :orderBy = 'numeroMovil' THEN numeroMovil END ASC, " +
            "CASE WHEN :orderBy = 'fechaRecogida' THEN fechaRecogida END ASC, " +
            "CASE WHEN :orderBy = 'fechaDevolucion' THEN fechaDevolucion END ASC")
    LiveData<List<Reserva>> getOrderedReservas(String orderBy, String filterType, long currentTimestamp);

    /**
     * Obtiene la información de la reserva almacenada en la base de datos
     * cuyo identificador es ese.
     *
     * @param matricula El identificador de la reserva a buscar.
     * @return Un LiveData<Reserva> con la reserva correspondiente.
     */
    @Query("SELECT * FROM Reserva WHERE idReserva = :idReserva")
    LiveData<Reserva> getReservaById(int idReserva);
}
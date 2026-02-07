package es.unizar.eina.M132_quads.database;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Clase que gestiona el acceso la fuente de datos.
 * Interacciona con la base de datos a través de las clases AppRoomDatabase y
 * ReservaDao.
 */
public class ReservaRepository {

    private final ReservaDao mReservaDao;
    private LiveData<List<Reserva>> mAllReservas;

    private final long TIMEOUT = 15000;

    /**
     * Constructor de ReservaRepository utilizando el contexto de la aplicación para
     * instanciar la base de datos. Alternativamente, se podría estudiar la
     * instanciación
     * del repositorio con una referencia a la base de datos siguiendo el ejemplo de
     * <a href=
     * "https://github.com/android/architecture-components-samples/blob/main/BasicSample/app/src/main/java/com/example/android/persistence/DataRepository.java">architecture-components-samples/.../persistence/DataRepository</a>
     */
    public ReservaRepository(Application application) {
        AppRoomDatabase db = AppRoomDatabase.getDatabase(application);
        mReservaDao = db.reservaDao();
        mAllReservas = mReservaDao.getOrderedReservas("nombreCliente", "Todas", System.currentTimeMillis());
    }

    /**
     * Devuelve un objeto de tipo LiveData con todas las reservas.
     * Room ejecuta todas las consultas en un hilo separado.
     * El objeto LiveData notifica a los observadores cuando los datos cambian.
     */
    public LiveData<List<Reserva>> getAllReservas() {
        return mAllReservas;
    }

    /**
     * Inserta una reserva nueva en la base de datos
     * 
     * @param reserva La reserva consta de: un nombreCliente
     *                (reserva.getNombreCliente()), un
     *                numeroMovil (reserva.getNumeroMovil()), una fechaRecogida
     *                (reserva.getFechaRecogida()), una fechaDevolucion
     *                (reserva.getFechaDevolucion())
     *                y un precioTotal (reserva.getPrecioTotal()), estos valores no
     *                pueden ser nulos.
     * @return Si la reserva se ha insertado correctamente, devuelve el
     *         identificador de la reserva
     *         que se ha creado. En caso contrario, devuelve -1 para indicar el
     *         fallo.
     */
    public long insert(Reserva reserva) {
        if (!validateReserva(reserva)) {
            return -1;
        }
        /*
         * Para que la App funcione correctamente y no lance una excepción, la
         * modificación de la
         * base de datos se debe lanzar en un hilo de ejecución separado
         * (databaseWriteExecutor.submit). Para poder sincronizar la recuperación del
         * resultado
         * devuelto por la base de datos, se puede utilizar un Future.
         */
        Future<Long> future = AppRoomDatabase.databaseWriteExecutor.submit(
                () -> mReservaDao.insert(reserva));
        try {
            return future.get(TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException ex) {
            Log.d("ReservaRepository", ex.getClass().getSimpleName() + ex.getMessage());
            return -1;
        }
    }

    /**
     * Actualiza una reserva en la base de datos
     * 
     * @param reserva La nota que se desea actualizar y que consta de: un
     *                identificador (reserva.getId()),
     *                un nombreCliente (reserva.getNombreCliente()), un numeroMovil
     *                (reserva.getNumeroMovil()),
     *                una fechaRecogida (reserva.getFechaRecogida()), una
     *                fechaDevolucion
     *                (reserva.getFechaDevolucion()) y un precioTotal
     *                (reserva.getPrecioTotal()),
     *                estos valores no pueden ser nulos.
     * @return Un valor entero con el número de filas modificadas: 1 si el
     *         identificador se corresponde con
     *         una reserva previamente insertada; 0 si no existe previamente una
     *         reserva con ese identificador,
     *         o hay algún problema con los atributos.
     */
    public int update(Reserva reserva) {
        if (!validateReserva(reserva)) {
            return 0;
        }
        Future<Integer> future = AppRoomDatabase.databaseWriteExecutor.submit(
                () -> mReservaDao.update(reserva));
        try {
            return future.get(TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException ex) {
            Log.d("ReservaRepository", ex.getClass().getSimpleName() + ex.getMessage());
            return -1;
        }
    }

    private boolean validateReserva(Reserva reserva) {
        if (reserva == null)
            return false;
        if (Reserva.validateNombre(reserva.getNombreCliente()) != null)
            return false;
        if (Reserva.validateFecha(reserva.getFechaRecogida()) != null)
            return false;
        if (Reserva.validateFecha(reserva.getFechaDevolucion()) != null)
            return false;
        if (Reserva.validateFechas(reserva.getFechaRecogida(), reserva.getFechaDevolucion()) != null)
            return false;
        if (Reserva.validatePrecio(reserva.getPrecioTotal()) != null)
            return false;
        return true;
    }

    /**
     * Elimina una reserva en la base de datos.
     * 
     * @param reserva Objeto reserva cuyo atributo identificador (reserva.getId())
     *                contiene la clave
     *                primaria de la reserva que se va a eliminar de la base de
     *                datos.
     * @return Un valor entero con el número de filas eliminadas: 1 si el
     *         identificador se corresponde
     *         con una reserva previamente insertada; 0 si no existe previamente una
     *         reserva con ese
     *         identificador o el identificador no es un valor aceptable.
     */
    public int delete(Reserva reserva) {
        Future<Integer> future = AppRoomDatabase.databaseWriteExecutor.submit(
                () -> mReservaDao.delete(reserva));
        try {
            return future.get(TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException ex) {
            Log.d("ReservaRepository", ex.getClass().getSimpleName() + ex.getMessage());
            return -1;
        }
    }

    /**
     * Este método delega la llamada al DAO para obtener las reservas ordenadas y
     * filtradas.
     *
     * @param orderBy    El criterio por el que se deben ordenar las reservas.
     * @param filterType El tipo de filtro a aplicar ("Todas", "Previstas",
     *                   "Vigentes", "Caducadas").
     * @return Un LiveData que contiene la lista de reservas ordenadas y filtradas.
     */
    public LiveData<List<Reserva>> getOrderedReservas(String orderBy, String filterType) {
        long currentTimestamp = System.currentTimeMillis();
        return mReservaDao.getOrderedReservas(orderBy, filterType, currentTimestamp);
    }

    /**
     * Obtiene un LiveData de la reserva cuyo identificador coincide con el
     * que se ha pasado como parámetro.
     * 
     * @param idReserva El identificador de la reserva a buscar.
     * @return LiveData que contiene la reserva.
     */
    public LiveData<Reserva> getReservaById(int idReserva) {
        return mReservaDao.getReservaById(idReserva);
    }
}

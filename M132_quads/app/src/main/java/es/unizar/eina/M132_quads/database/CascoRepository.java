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
 * Clase que gestiona el acceso a la fuente de datos.
 * Interacciona con la base de datos a través de las clases AppRoomDatabase y
 * CascoDao.
 */
public class CascoRepository {

    private final CascoDao mCascoDao;
    private final LiveData<List<Casco>> mAllCascos;

    private final long TIMEOUT = 15000;

    /**
     * Constructor de CascoRepository utilizando el contexto de la aplicación para
     * instanciar la base de datos.
     * Alternativamente, se podría estudiar la instanciación del repositorio con una
     * referencia a la base de datos
     * siguiendo el ejemplo de
     * <a href=
     * "https://github.com/android/architecture-components-samples/blob/main/BasicSample/app/src/main/java/com/example/android/persistence/DataRepository.java">architecture-components-samples/.../persistence/DataRepository</a>
     */
    public CascoRepository(Application application) {
        AppRoomDatabase db = AppRoomDatabase.getDatabase(application);
        mCascoDao = db.cascoDao();
        mAllCascos = mCascoDao.getOrderedCascos();
    }

    /**
     * Devuelve un objeto de tipo LiveData con todos los cascos.
     * Room ejecuta todas las consultas en un hilo separado.
     * El objeto LiveData notifica a los observadores cuando los datos cambian.
     */
    public LiveData<List<Casco>> getAllCascos() {
        return mAllCascos;
    }

    /**
     * Inserta un nuevo casco en la base de datos.
     * 
     * @param casco El objeto Casco a insertar. Debe tener valores no nulos para sus
     *              claves primarias
     *              (matriculaQuad y idReserva) y un número de cascos válido.
     * @return Si el casco se ha insertado correctamente, devuelve el identificador
     *         de la fila insertada (rowId).
     *         Devuelve -1 si la operación falla (por ejemplo, por un conflicto de
     *         clave primaria o un timeout).
     */
    public long insert(Casco casco) {
        /*
         * Para que la App funcione correctamente y no lance una excepción, la
         * modificación de la
         * base de datos se debe lanzar en un hilo de ejecución separado
         * (databaseWriteExecutor.submit). Para poder sincronizar la recuperación del
         * resultado
         * devuelto por la base de datos, se utiliza un Future.
         */
        Future<Long> future = AppRoomDatabase.databaseWriteExecutor.submit(
                () -> mCascoDao.insert(casco));
        try {
            return future.get(TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException ex) {
            Log.d("CascoRepository", ex.getClass().getSimpleName() + ex.getMessage());
            return -1;
        }
    }

    /**
     * Actualiza un registro de casco existente en la base de datos.
     * 
     * @param casco El objeto Casco a actualizar, identificado por su clave primaria
     *              compuesta
     *              (matriculaQuad y idReserva).
     * @return El número de filas modificadas. Debería ser 1 si la actualización fue
     *         exitosa,
     *         0 si no se encontró ningún registro con esa clave primaria, o -1 si
     *         ocurrió un error.
     */
    public int update(Casco casco) {
        Future<Integer> future = AppRoomDatabase.databaseWriteExecutor.submit(
                () -> mCascoDao.update(casco));
        try {
            return future.get(TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException ex) {
            Log.d("CascoRepository", ex.getClass().getSimpleName() + ex.getMessage());
            return -1;
        }
    }

    /**
     * Elimina un casco en la base de datos.
     * 
     * @param casco Objeto Casco que se va a eliminar. Room utilizará la clave
     *              primaria compuesta
     *              (matriculaQuad y idReserva) para encontrar el casco a borrar.
     * @return El número de filas eliminadas. Debería ser 1 si la eliminación fue
     *         exitosa,
     *         0 si no se encontró el registro, o -1 si ocurrió un error.
     */
    public int delete(Casco casco) {
        Future<Integer> future = AppRoomDatabase.databaseWriteExecutor.submit(
                () -> mCascoDao.delete(casco));
        try {
            return future.get(TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException ex) {
            Log.d("CascoRepository", ex.getClass().getSimpleName() + ex.getMessage());
            return -1;
        }
    }

    /**
     * Llama al método del DAO para obtener la lista observable de cascos.
     * Como el DAO ya devuelve LiveData, no es necesario usar el
     * 'databaseWriteExecutor',
     * ya que Room gestiona el hilo de forma automática para las consultas @Query
     * que
     * devuelven LiveData.
     *
     * @param idReserva El ID de la reserva.
     * @return El LiveData<List<Casco>> devuelto por el DAO.
     */
    public LiveData<List<Casco>> getCascosForReserva(int idReserva) {
        return mCascoDao.getCascosForReserva(idReserva);
    }

    /**
     * Guarda la selección de cascos para una reserva.
     * Ejecuta una transacción para primero borrar los cascos antiguos y luego
     * insertar los nuevos, asegurando la consistencia de los datos.
     *
     * @param reserva      La reserva que se está guardando/actualizando.
     * @param nuevosCascos La lista de nuevos objetos Casco a asociar con la
     *                     reserva.
     */
    public void saveReservaConCascos(Reserva reserva, List<Casco> nuevosCascos) {

        // Se ejecuta la operación en un hilo secundario.
        AppRoomDatabase.databaseWriteExecutor.execute(() -> {
            mCascoDao.updateCascosForReserva(reserva.getIdReserva(), nuevosCascos);
        });
    }

    /**
     * Comprueba si alguno de los quads seleccionados tiene solapes con otras
     * reservas en el periodo dado.
     * Se ejecuta de forma síncrona (bloqueante) porque se espera que se llame desde
     * un hilo secundario
     * (o mediante Future desde el ViewModel).
     *
     * @param quadsSeleccionados Lista de cascos con la matrícula de los quads
     *                           seleccionados.
     * @param fechaRecogida      Fecha de inicio de la nueva reserva.
     * @param fechaDevolucion    Fecha de fin de la nueva reserva.
     * @param currentReservaId   ID de la reserva actual (para excluirla de la
     *                           comprobación).
     * @return true si hay algún solape, false si no.
     */
    public boolean checkOverlaps(List<Casco> quadsSeleccionados, long fechaRecogida, long fechaDevolucion,
            int currentReservaId) {
        Future<Boolean> future = AppRoomDatabase.databaseWriteExecutor.submit(() -> {
            for (Casco casco : quadsSeleccionados) {
                int count = mCascoDao.countOverlappingReservas(casco.getMatriculaQuad(), currentReservaId,
                        fechaRecogida, fechaDevolucion);
                if (count > 0) {
                    return true; // Solape detectado
                }
            }
            return false; // No hay solapes
        });

        try {
            return future.get(TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException ex) {
            Log.d("CascoRepository", ex.getClass().getSimpleName() + ex.getMessage());
            return true; // Ante la duda (error), asumimos solape o bloqueamos para seguridad.
        }
    }
}

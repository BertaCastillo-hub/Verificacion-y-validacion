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
 * QuadDao.
 */
public class QuadRepository {

    private final QuadDao mQuadDao;
    private final LiveData<List<Quad>> mAllQuads;

    private final long TIMEOUT = 15000;

    /**
     * Constructor de QuadRepository utilizando el contexto de la aplicación para
     * instanciar la base de datos.
     * Alternativamente, se podría estudiar la instanciación del repositorio con una
     * referencia a la base de datos
     * siguiendo el ejemplo de
     * <a href=
     * "https://github.com/android/architecture-components-samples/blob/main/BasicSample/app/src/main/java/com/example/android/persistence/DataRepository.java">architecture-components-samples/.../persistence/DataRepository</a>
     */
    public QuadRepository(Application application) {
        AppRoomDatabase db = AppRoomDatabase.getDatabase(application);
        mQuadDao = db.quadDao();
        mAllQuads = mQuadDao.getOrderedQuads("matricula");
    }

    /**
     * Devuelve un objeto de tipo LiveData con todos los quads.
     * Room ejecuta todas las consultas en un hilo separado.
     * El objeto LiveData notifica a los observadores cuando los datos cambian.
     */
    public LiveData<List<Quad>> getAllQuads() {
        return mAllQuads;
    }

    /**
     * Inserta un nuevo quad en la base de datos.
     * 
     * @param quad El quad a insertar. La matrícula (quad.getMatricula())
     *             debe ser no nula, no vacía y única.
     * @return El identificador de la fila insertada (rowId). Si la inserción
     *         falla (por ejemplo, por una matrícula duplicada o un timeout),
     *         devuelve -1.
     */
    public long insert(Quad quad) {
        if (!validateQuad(quad)) {
            return -1;
        }
        /*
         * Para que la App funcione correctamente y no lance una excepción, la
         * modificación de la
         * base de datos se debe lanzar en un hilo de ejecución separado
         * (databaseWriteExecutor.submit). Para poder sincronizar la recuperación del
         * resultado devuelto por la base de datos, se utiliza un Future.
         */
        Future<Long> future = AppRoomDatabase.databaseWriteExecutor.submit(
                () -> mQuadDao.insert(quad));
        try {
            return future.get(TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException ex) {
            Log.d("QuadRepository", ex.getClass().getSimpleName() + ex.getMessage());
            return -1;
        }
    }

    /**
     * Actualiza un quad en la base de datos.
     * La operación se ejecuta en un hilo de trabajo separado.
     * 
     * @param quad El quad que se desea actualizar. Room utiliza la clave primaria
     *             (matrícula) para encontrar el registro. Todos sus campos deben
     *             ser válidos.
     * @return Un valor entero con el número de filas modificadas. Debería ser 1
     *         si la matrícula se corresponde con un quad existente. Devuelve 0
     *         si no se encontró ningún quad con esa matrícula, o -1 si ocurrió un
     *         error.
     */
    public int update(Quad quad) {
        if (!validateQuad(quad)) {
            return 0; // Validation failed, no rows updated
        }
        Future<Integer> future = AppRoomDatabase.databaseWriteExecutor.submit(
                () -> mQuadDao.update(quad));
        try {
            return future.get(TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException ex) {
            Log.d("QuadRepository", ex.getClass().getSimpleName() + ex.getMessage());
            return -1; // Specific error code
        }
    }

    /**
     * Elimina un quad en la base de datos.
     * 
     * @param quad Objeto Quad cuyo atributo matrícula (quad.getMatricula())
     *             contiene la clave primaria del quad que se va a eliminar.
     * @return Un valor entero con el número de filas eliminadas. Debería ser
     *         1 si la matrícula se corresponde con un quad existente.
     *         Devuelve 0 si no se encontró, o -1 si ocurrió un error.
     */
    public int delete(Quad quad) {
        // For delete, we only strictly need the ID, but basic null check is good
        if (quad == null || quad.getMatricula() == null) {
            return 0;
        }
        Future<Integer> future = AppRoomDatabase.databaseWriteExecutor.submit(
                () -> mQuadDao.delete(quad));
        try {
            return future.get(TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException ex) {
            Log.d("QuadRepository", ex.getClass().getSimpleName() + ex.getMessage());
            return -1;
        }
    }

    /**
     * Valida los datos de un quad según las reglas de negocio.
     * 
     * @param quad El quad a validar.
     * @return true si es válido, false en caso contrario.
     */
    private boolean validateQuad(Quad quad) {
        if (quad == null) {
            return false;
        }
        if (Quad.validateMatricula(quad.getMatricula()) != null) {
            return false;
        }
        if (Quad.validatePrecio(quad.getPrecio()) != null) {
            return false;
        }
        if (Quad.validateTipo(quad.getTipo()) != null) {
            return false;
        }
        return true;
    }

    /**
     * Este método no realiza ninguna lógica; simplemente delega la llamada
     * al DAO, que es el que realmente ejecuta la consulta SQL a través de Room.
     *
     * @param orderBy El criterio por el que se deben ordenar los quads.
     * @return Un LiveData que contiene la lista de quads ordenados.
     */
    public LiveData<List<Quad>> getOrderedQuads(String orderBy) {
        // La llamada se pasa directamente al método correspondiente en el DAO.
        return mQuadDao.getOrderedQuads(orderBy);
    }

    /**
     * Obtiene un LiveData del quad cuya matrícula coincide con la
     * que se ha pasado como parámetro.
     * 
     * @param matricula La matrícula a buscar.
     * @return LiveData que contiene el quad.
     */
    public LiveData<Quad> getQuadByMatricula(String matricula) {
        return mQuadDao.getQuadByMatricula(matricula);
    }
}

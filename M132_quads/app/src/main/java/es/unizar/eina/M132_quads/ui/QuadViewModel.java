package es.unizar.eina.M132_quads.ui;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import java.util.List;

import es.unizar.eina.M132_quads.database.Quad;
import es.unizar.eina.M132_quads.database.QuadRepository;

/**
 * ViewModel para la entidad Quad.
 * Proporciona datos a la UI y sobrevive a los cambios de configuración.
 * Actúa como un intermediario entre la UI y el Repositorio.
 */
public class QuadViewModel extends AndroidViewModel {

    private QuadRepository mRepository;

    // LiveData para el criterio de ordenación. Es 'Mutable' porque la UI (el Spinner) lo puede cambiar.
    private final MutableLiveData<String> mOrderBy = new MutableLiveData<>();

    // LiveData para la lista de quads. Es 'final' y no mutable directamente.
    // Su contenido cambiará en 'respuesta' a los cambios en mOrderBy.
    private final LiveData<List<Quad>> mAllQuads;

    public QuadViewModel(@NonNull Application application) {
        super(application);
        mRepository = new QuadRepository(application);

        // Establecer un valor inicial por defecto para la ordenación.
        // Si no se hiciera esto, la primera carga podría no estar ordenada.
        mOrderBy.setValue("matricula");

        //    Este método 'escucha' los cambios en mOrderBy.
        //    Cada vez que mOrderBy.setValue() es llamado, el código dentro del
        //    'switchMap' se ejecuta.
        //    Llama al método del repositorio con el nuevo criterio de ordenación y devuelve
        //    el LiveData<List<Quad>> correspondiente. La UI, que observa mAllQuads,
        //    se actualiza automáticamente.
        mAllQuads = Transformations.switchMap(mOrderBy,
                orderBy -> mRepository.getOrderedQuads(orderBy));
    }

    /**
     * Expone la lista de quads (ya ordenada) como LiveData para que la UI la observe.
     * La UI no necesita saber cómo está ordenada, solo que esta es la lista que debe mostrar.
     * @return un LiveData que contiene la lista de quads.
     */
    public LiveData<List<Quad>> getAllQuads() {
        return mAllQuads;
    }

    /**
     * Método que cambia el criterio de ordenación, establecido por el usuario.
     * @param orderBy El nuevo campo por el que ordenar.
     */
    public void setOrderBy(String orderBy) {
        // Al cambiar el valor de mOrderBy, se dispara automáticamente el switchMap,
        // que a su vez actualiza mAllQuads, y finalmente la UI se refresca.
        mOrderBy.setValue(orderBy);
    }


    /**
     * Delega la inserción de un nuevo quad al repositorio. Se ejecuta en un hilo secundario.
     * @param quad El quad a insertar.
     */
    public void insert(Quad quad) {
        mRepository.insert(quad);
    }

    /**
     * Delega la eliminación de un quad al repositorio.
     * @param quad El quad a eliminar.
     */
    public void delete(Quad quad) {
        mRepository.delete(quad);
    }

    /**
     * Delega la actualización de un quad al repositorio.
     * @param quad El quad a actualizar.
     */
    public void update(Quad quad) {
        mRepository.update(quad);
    }

    /**
     * Expone el LiveData para obtener un quad por su matrícula desde el repositorio.
     * La UI (QuadDetail) observará este LiveData.
     * @param matricula La matrícula del quad a obtener.
     * @return LiveData que contiene el quad.
     */
    public LiveData<Quad> getQuadByMatricula(String matricula) {
        return mRepository.getQuadByMatricula(matricula);
    }
}

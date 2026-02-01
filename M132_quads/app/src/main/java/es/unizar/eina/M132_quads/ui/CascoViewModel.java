package es.unizar.eina.M132_quads.ui;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import java.util.List;

import es.unizar.eina.M132_quads.database.Casco;
import es.unizar.eina.M132_quads.database.CascoRepository;

/**
 * ViewModel para gestionar los datos relacionados con la entidad Casco.
 * Su función principal es proporcionar a la UI una lista observable de cascos
 * para una reserva específica.
 */
public class CascoViewModel extends AndroidViewModel {

    private final CascoRepository mRepository;

    public CascoViewModel(@NonNull Application application) {
        super(application);
        mRepository = new CascoRepository(application);
    }

    /**
     * Obtiene una lista observable de Cascos para una reserva específica.
     * La UI (CascoList) observará este LiveData y se actualizará automáticamente
     * cuando los datos cambien en la base de datos.
     *
     * @param idReserva El ID de la reserva cuyos cascos se quieren obtener.
     * @return Un objeto LiveData que contiene la lista de Cascos.
     */
    public LiveData<List<Casco>> getCascosForReserva(int idReserva) {
        return mRepository.getCascosForReserva(idReserva);
    }

    /**
     * Delega la acción de insertar un nuevo objeto Casco al Repository.
     * @param casco El objeto Casco a insertar.
     */
    public void insert(Casco casco) {
        mRepository.insert(casco);
    }

    /**
     * Delega la acción de actualizar un nuevo objeto Casco al Repository.
     * @param casco El objeto Casco a actualizar.
     */
    public void update(Casco casco) {
        mRepository.update(casco);
    }
}

package es.unizar.eina.M132_quads.ui;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import java.util.List;

import es.unizar.eina.M132_quads.R;
import es.unizar.eina.M132_quads.database.CascoRepository;
import es.unizar.eina.M132_quads.database.QuadRepository;
import es.unizar.eina.M132_quads.database.ReservaRepository;
import es.unizar.eina.M132_quads.database.Quad;
import es.unizar.eina.M132_quads.database.Reserva;
import es.unizar.eina.M132_quads.database.Casco;

/** ViewModel para gestionar la selección de cascos. */
public class CascoSelectionViewModel extends AndroidViewModel {
    private final QuadRepository mQuadRepository;
    private final ReservaRepository mReservaRepository;
    private final CascoRepository mCascoRepository;
    private final LiveData<List<Quad>> mAllQuads;

    public CascoSelectionViewModel(@NonNull Application application) {
        super(application);
        mQuadRepository = new QuadRepository(application);
        mCascoRepository = new CascoRepository(application);
        mReservaRepository = new ReservaRepository(application);
        mAllQuads = mQuadRepository.getAllQuads();
    }

    /** Devuelve la lista de todos los quads. */
    public LiveData<List<Quad>> getAllQuads() {
        return mAllQuads;
    }

    /** Devuelve los cascos para UNA reserva específica. */
    public LiveData<List<Casco>> getCascosForReserva(int idReserva) {
        return mCascoRepository.getCascosForReserva(idReserva);
    }

    public void saveReservaConCascos(Reserva reserva, List<Casco> nuevosCascos) {
        // Guardar o actualizar el objeto Reserva principal.
        if (reserva.getIdReserva() == 0) { // Asumiendo que 0 es el ID para una nueva reserva
            mReservaRepository.insert(reserva);
        } else {
            mReservaRepository.update(reserva);
        }

        // Delegar la actualización transaccional de los cascos al CascoRepository.
        mCascoRepository.saveReservaConCascos(reserva, nuevosCascos);
    }

    /**
     * Obtiene un objeto LiveData que contiene una única Reserva por su ID.
     * La Activity observará esto para tener el objeto Reserva completo al guardar.
     */
    public LiveData<Reserva> getReservaById(int idReserva) {
        return mReservaRepository.getReservaById(idReserva);
    }
}

package es.unizar.eina.M132_quads.ui;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import java.util.List;

import es.unizar.eina.M132_quads.database.Quad;
import es.unizar.eina.M132_quads.database.Reserva;
import es.unizar.eina.M132_quads.database.ReservaRepository;

/** ViewModel para la entidad Reserva. */
public class ReservaViewModel extends AndroidViewModel {

    private ReservaRepository mRepository;

    private final MutableLiveData<String> mOrderBy = new MutableLiveData<>();

    private LiveData<List<Reserva>> mAllReservas;

    public ReservaViewModel(Application application) {
        super(application);
        mRepository = new ReservaRepository(application);
        mAllReservas = Transformations.switchMap(mOrderBy,
                orderBy -> mRepository.getOrderedReservas(orderBy));
    }

    LiveData<List<Reserva>> getAllReservas() {
        return mAllReservas;
    }

    public void setOrderBy(String orderBy) {
        // Al cambiar el valor de mOrderBy, se dispara automáticamente el switchMap,
        // que a su vez actualiza mAllReservas, y finalmente la UI se refresca.
        mOrderBy.setValue(orderBy);
    }

    public long insert(Reserva reserva) {
        return mRepository.insert(reserva);
    }

    public void update(Reserva reserva) {
        mRepository.update(reserva);
    }

    public void delete(Reserva reserva) {
        mRepository.delete(reserva);
    }

    public LiveData<Reserva> getReservaById(int id) {
        return mRepository.getReservaById(id);
    }
}

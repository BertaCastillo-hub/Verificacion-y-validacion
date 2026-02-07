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

    private final MutableLiveData<String> mOrderBy = new MutableLiveData<>("nombreCliente");
    private final MutableLiveData<String> mFilterType = new MutableLiveData<>("Todas"); // Por defecto "Todas"

    private LiveData<List<Reserva>> mAllReservas;
    // MediatorLiveData para combinar los cambios de orden y filtro
    private final androidx.lifecycle.MediatorLiveData<FilterParams> mFilterParams = new androidx.lifecycle.MediatorLiveData<>();

    public ReservaViewModel(Application application) {
        super(application);
        mRepository = new ReservaRepository(application);

        // Inicializar el valor combinado
        mFilterParams.setValue(new FilterParams("nombreCliente", "Todas"));

        // Observar cambios en mOrderBy
        mFilterParams.addSource(mOrderBy, orderBy -> {
            FilterParams current = mFilterParams.getValue();
            if (current != null) {
                mFilterParams.setValue(new FilterParams(orderBy, current.filterType));
            }
        });

        // Observar cambios en mFilterType
        mFilterParams.addSource(mFilterType, filterType -> {
            FilterParams current = mFilterParams.getValue();
            if (current != null) {
                mFilterParams.setValue(new FilterParams(current.orderBy, filterType));
            }
        });

        // switchMap observa los cambios en el objeto combinado (FilterParams) y
        // actualiza la lista
        mAllReservas = Transformations.switchMap(mFilterParams,
                params -> mRepository.getOrderedReservas(params.orderBy, params.filterType));
    }

    LiveData<List<Reserva>> getAllReservas() {
        return mAllReservas;
    }

    public void setOrderBy(String orderBy) {
        mOrderBy.setValue(orderBy);
    }

    public void setFilter(String filterType) {
        mFilterType.setValue(filterType);
    }

    // Clase auxiliar para mantener los dos estados
    private static class FilterParams {
        final String orderBy;
        final String filterType;

        FilterParams(String orderBy, String filterType) {
            this.orderBy = orderBy;
            this.filterType = filterType;
        }
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

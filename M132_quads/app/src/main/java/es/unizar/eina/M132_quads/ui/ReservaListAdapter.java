package es.unizar.eina.M132_quads.ui;

import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

import es.unizar.eina.M132_quads.database.Reserva;

/** Adapter para el RecyclerView que muestra la lista de Reservas. */
public class ReservaListAdapter extends ListAdapter<Reserva, ReservaViewHolder> {

    // OnClickListener para gestionar el clic en un elemento.
    private OnItemClickListener clickListener;
    // Listener para el clic en el botón de la papelera (para borrar)
    private OnDeleteClickListener deleteClickListener;

    public ReservaListAdapter(@NonNull DiffUtil.ItemCallback<Reserva> diffCallback) {
        super(diffCallback);
    }

    @Override
    public ReservaViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return ReservaViewHolder.create(parent);
    }

    @Override
    public void onBindViewHolder(ReservaViewHolder holder, int position) {

        Reserva current = getItem(position);
        // El método bind recibe los listeners para poder asignarlos
        // a las vistas correctas dentro del ViewHolder.
        holder.bind(current, clickListener, deleteClickListener);
    }

    // Interfaz para definir el listener de clics.
    /** Interfaz para gestionar el evento de click en una reserva. */
    public interface OnItemClickListener {
        void onItemClick(Reserva reserva);
    }

    // Interfaz para el clic en la papelera (borrar)
    /** Interfaz para gestionar el evento de eliminación de una reserva. */
    public interface OnDeleteClickListener {
        void onDeleteClick(Reserva reserva);
    }

    // Método para que la Activity pueda establecer el listener.
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.clickListener = listener;
    }

    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.deleteClickListener = listener;
    }

    /** Clase para calcular las diferencias entre dos listas de reservas. */
    static class ReservaDiff extends DiffUtil.ItemCallback<Reserva> {

        @Override
        public boolean areItemsTheSame(@NonNull Reserva oldItem, @NonNull Reserva newItem) {
            return oldItem.getIdReserva() == newItem.getIdReserva();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Reserva oldItem, @NonNull Reserva newItem) {
            // Se comparan los campos de las dos reservas pasadas como parámetro para
            // comprobar si son iguales.
            return oldItem.getNombreCliente().equals(newItem.getNombreCliente())
                    && oldItem.getFechaDevolucion() == newItem.getFechaDevolucion()
                    && oldItem.getFechaRecogida() == newItem.getFechaRecogida();
        }
    }
}

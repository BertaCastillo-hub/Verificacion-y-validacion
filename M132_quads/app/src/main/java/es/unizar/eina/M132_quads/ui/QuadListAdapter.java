package es.unizar.eina.M132_quads.ui;

import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

import es.unizar.eina.M132_quads.database.Quad;

/** Adapter para el RecyclerView que muestra la lista de Quads. */
public class QuadListAdapter extends ListAdapter<Quad, QuadViewHolder> {

    // OnClickListener para gestionar el clic en un elemento.
    private OnItemClickListener clickListener;
    // Listener para el clic en el botón de la papelera (para borrar)
    private OnDeleteClickListener deleteClickListener;

    public QuadListAdapter(@NonNull DiffUtil.ItemCallback<Quad> diffCallback) {
        super(diffCallback);
    }

    @Override
    public QuadViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Se crea el ViewHolder.
        return QuadViewHolder.create(parent);
    }

    @Override
    public void onBindViewHolder(QuadViewHolder holder, int position) {

        // Se obtiene el quad actual en esa posición de la lista.
        Quad current = getItem(position);
        // El método bind recibe los listeners para poder asignarlos
        // a las vistas correctas dentro del ViewHolder.
        holder.bind(current, clickListener, deleteClickListener);
    }

    // Interfaz para definir el listener de clics. La Activity implementará esto.
    /** Interfaz para gestionar el evento de click en un quad. */
    public interface OnItemClickListener {
        void onItemClick(Quad quad);
    }

    // Interfaz para el clic en la papelera (borrar)
    /** Interfaz para gestionar el evento de eliminación de un quad. */
    public interface OnDeleteClickListener {
        void onDeleteClick(Quad quad);
    }

    // Método para que la Activity pueda establecer el listener.
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.clickListener = listener;
    }

    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.deleteClickListener = listener;
    }

    /** Clase para calcular las diferencias entre dos listas de quads. */
    static class QuadDiff extends DiffUtil.ItemCallback<Quad> {

        @Override
        public boolean areItemsTheSame(@NonNull Quad oldItem, @NonNull Quad newItem) {
            // Los items son los mismos si su clave primaria es la misma.
            return oldItem.getMatricula().equals(newItem.getMatricula());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Quad oldItem, @NonNull Quad newItem) {
            // Se comparan todos los campos por completitud, pero solo se necesita la
            // matrícula,
            // que es lo que se muestra en la lista.
            return oldItem.getMatricula().equals(newItem.getMatricula())
                    && oldItem.getTipo().equals(newItem.getTipo())
                    && oldItem.getDescripcion().equals(newItem.getDescripcion());
        }
    }
}

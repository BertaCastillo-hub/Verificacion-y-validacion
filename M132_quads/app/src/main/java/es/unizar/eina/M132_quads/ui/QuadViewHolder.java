package es.unizar.eina.M132_quads.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import es.unizar.eina.M132_quads.R;
import es.unizar.eina.M132_quads.database.Quad;

/** ViewHolder para un ítem de la lista de quads. */
class QuadViewHolder extends RecyclerView.ViewHolder {

    // Elementos que aparecen en cada una de las posiciones de la lista.
    private final TextView matriculaTextView;
    private final ImageButton deleteButton;

    private QuadViewHolder(View itemView) {
        super(itemView);
        matriculaTextView = itemView.findViewById(R.id.matriculaTextView);
        deleteButton = itemView.findViewById(R.id.buttonDelete);
    }

    public void bind(Quad quad, final QuadListAdapter.OnItemClickListener clickListener,
            final QuadListAdapter.OnDeleteClickListener deleteClickListener) {

        // Asignar los datos del quad a las vistas.
        // Solo se muestra la matrícula.
        if (quad != null) {
            matriculaTextView.setText(quad.getMatricula());
        }

        // Asignar el listener para el clic en el item completo (para ver detalles)
        itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                // Notificamos al listener, pasándole el quad de esta fila
                clickListener.onItemClick(quad);
            }
        });

        // Asignar el listener para el clic en el botón de la papelera
        deleteButton.setOnClickListener(v -> {
            if (deleteClickListener != null) {
                // Se notifica al listener de borrado, pasándole el quad
                deleteClickListener.onDeleteClick(quad);
            }
        });
    }

    static QuadViewHolder create(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recyclerview_item, parent, false);
        return new QuadViewHolder(view);
    }

}

package es.unizar.eina.M132_quads.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import es.unizar.eina.M132_quads.R;
import es.unizar.eina.M132_quads.database.Reserva;

/** ViewHolder para un ítem de la lista de reservas. */
class ReservaViewHolder extends RecyclerView.ViewHolder {

    // Elementos que aparecen en cada una de las posiciones de la lista.
    private final TextView nombreTextView;
    private final TextView fechasTextView; // Un solo TextView para ambas fechas
    private final ImageButton deleteButton;

    private ReservaViewHolder(View itemView) {
        super(itemView);
        nombreTextView = itemView.findViewById(R.id.nombreTextView);
        fechasTextView = itemView.findViewById(R.id.fechasTextView);
        deleteButton = itemView.findViewById(R.id.buttonDelete);
    }

    public void bind(Reserva reserva, final ReservaListAdapter.OnItemClickListener clickListener,
            final ReservaListAdapter.OnDeleteClickListener deleteClickListener) {

        // Asignar los datos del quad a las vistas.
        // Solo se muestra la matrícula.
        if (reserva != null) {
            nombreTextView.setText(reserva.getNombreCliente());

            // Se concatenan las fechas en 1 solo string para mostrarlo en la lista.
            String fechaRecogida = reserva.getFechaRecogida();
            String fechaDevolucion = reserva.getFechaDevolucion();
            String fechasConcatenadas = fechaRecogida + " - " + fechaDevolucion;
            fechasTextView.setText(fechasConcatenadas);
        }

        // Asignar el listener para el clic en el item completo (para ver detalles)
        itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                // Notificamos al listener, pasándole el quad de esta fila
                clickListener.onItemClick(reserva);
            }
        });

        // Asignar el listener para el clic en el botón de la papelera.
        deleteButton.setOnClickListener(v -> {
            if (deleteClickListener != null) {
                // Notificar al listener de borrado, pasándole el quad
                deleteClickListener.onDeleteClick(reserva);
            }
        });
    }

    static ReservaViewHolder create(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recyclerview_reserva_item, parent, false);
        return new ReservaViewHolder(view);
    }

}

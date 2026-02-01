package es.unizar.eina.M132_quads.ui;

import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

// Se importa la clase Casco y el ID del recurso de layout
import es.unizar.eina.M132_quads.database.Casco;
import es.unizar.eina.M132_quads.R;

/**
 * ViewHolder independiente para un ítem de la lista de cascos.
 * Contiene las referencias a los TextViews del layout
 * 'recyclerview_quad_reserva_item.xml'
 * y la lógica para enlazar los datos de un objeto 'Casco' a esas vistas.
 */
public class CascoViewHolder extends RecyclerView.ViewHolder {

    // Referencias a las vistas del layout (matrícula y número de cascos).
    private final TextView mMatriculaTextView;
    private final TextView mCascosTextView;

    /**
     * Contructor que se ejecuta una vez por cada ViewHolder creado.
     * Busca las vistas y las guarda en los atributos.
     *
     * @param itemView La vista raíz del layout 'recyclerview_quad_reserva_item.xml'.
     */
    public CascoViewHolder(@NonNull View itemView) {
        super(itemView);

        // Se inicializan los atributos usando findViewById.
        mMatriculaTextView = itemView.findViewById(R.id.quadMatriculaTextView);
        mCascosTextView = itemView.findViewById(R.id.quadCascosTextView);
    }

    /**
     * Recibe un objeto de tipo Casco y rellena las vistas con sus datos.
     * Es llamado por onBindViewHolder en el Adapter.
     *
     * @param casco El objeto Casco que contiene la información a mostrar.
     */
    public void bind(Casco casco) {
        // Se comprueba que el objeto no sea nulo para evitar errores.
        if (casco != null) {
            // Se asignan los datos a los TextViews correspondientes.
            mMatriculaTextView.setText("Matrícula: " + casco.getMatriculaQuad());
            mCascosTextView.setText("Cascos: " + casco.getNumCascos());
        }
    }
}

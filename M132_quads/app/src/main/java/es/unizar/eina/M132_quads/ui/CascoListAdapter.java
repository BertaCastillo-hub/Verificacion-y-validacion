package es.unizar.eina.M132_quads.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

// Se importa la clase Casco y la clase independiente CascoViewHolder
import es.unizar.eina.M132_quads.database.Casco;
import es.unizar.eina.M132_quads.R; // O la ruta donde lo hayas creado

/**
 * Adapter para el RecyclerView que muestra la lista de Cascos de una reserva.
 * Utiliza una clase CascoViewHolder independiente.
 */
public class CascoListAdapter extends RecyclerView.Adapter<CascoViewHolder> {

    private final LayoutInflater mInflater;
    private List<Casco> mCascos = new ArrayList<>();

    public CascoListAdapter(Context context) {
        this.mInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public CascoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.recyclerview_quad_reserva_item, parent, false);
        return new CascoViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull CascoViewHolder holder, int position) {
        Casco currentCasco = mCascos.get(position);
        holder.bind(currentCasco); // La lógica de 'bind' ya está encapsulada en el ViewHolder
    }

    @Override
    public int getItemCount() {
        return mCascos.size();
    }

    public void setCascos(List<Casco> cascos) {
        this.mCascos = (cascos != null) ? cascos : new ArrayList<>();
        notifyDataSetChanged();
    }
}

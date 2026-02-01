package es.unizar.eina.M132_quads.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView; // Importación correcta
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.datepicker.OnSelectionChangedListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.unizar.eina.M132_quads.R;
import es.unizar.eina.M132_quads.database.Quad;
import es.unizar.eina.M132_quads.database.Casco;

/** Adapter para el RecyclerView que permite la selección de cascos. */
public class CascoSelectionListAdapter extends RecyclerView.Adapter<CascoSelectionViewHolder> {

    private final LayoutInflater mInflater;
    private List<Quad> mAllQuads = new ArrayList<>();
    private Map<String, Integer> mSelectionState = new HashMap<>();

    public CascoSelectionListAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
    }

    /** Interfaz para gestionar el cambio de selección de cascos. */
    public interface OnSelectionChangedListener {
        void onSelectionChanged();
    }

    private OnSelectionChangedListener mListener;

    @NonNull
    @Override
    public CascoSelectionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.recyclerview_edit_reserva_quad_item, parent, false);
        return new CascoSelectionViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull CascoSelectionViewHolder holder, int position) {
        Quad currentQuad = mAllQuads.get(position);

        // Llamada al método 'bind' pasando los datos y el estado
        holder.bind(currentQuad, mSelectionState, this);
    }

    @Override
    public int getItemCount() {
        return mAllQuads.size();
    }

    // --- Métodos de gestión de datos ---

    public void setAllQuads(List<Quad> quads) {
        mAllQuads = quads;
        // Inicializa el estado de selección usando la matrícula como clave
        // for (Quad quad : quads) {
        // mSelectionState.putIfAbsent(quad.getMatricula(), 0);
        // }
        notifyDataSetChanged();
    }

    // Este método clave para el modo Modificación.
    public void setInitialSelection(List<Casco> cascosDeLaReserva) {
        for (Casco casco : cascosDeLaReserva) {
            // Se usa la matrícula del quad asociada al casco
            mSelectionState.put(casco.getMatriculaQuad(), casco.getNumCascos());
        }
        notifyDataSetChanged();
    }

    // Método para que la Activity recupere el estado final
    public Map<String, Integer> getSelectionState() {
        return mSelectionState;
    }

    /**
     * Devuelve el estado actual de la selección de quads y cascos.
     * Este método es utilizado por la Activity (CascoEdit) para saber qué quads
     * están seleccionados y con cuántos cascos cada uno, para así poder
     * calcular el precio total y guardar los datos.
     *
     * @return Un mapa donde la clave es la matrícula del quad (String) y el valor
     *         es el número de cascos seleccionados (Integer). Un valor de 0 indica
     *         que el quad no está seleccionado.
     */
    public Map<String, Integer> getSelectedData() {
        return this.mSelectionState;
    }

    /**
     * Método que la Activity llamará para registrarse como oyente de los cambios.
     * 
     * @param listener La implementación del listener (normalmente, la propia
     *                 Activity).
     */
    public void setOnSelectionChangedListener(OnSelectionChangedListener listener) {
        this.mListener = listener;
    }

    /**
     * Método llamado desde el ViewHolder para notificar cambios en la selección.
     */
    public void notifyListener() {
        if (mListener != null) {
            mListener.onSelectionChanged();
        }
    }

    public void setSelectionState(Map<String, Integer> existingState) {
        mSelectionState.clear();
        if (existingState != null) {
            mSelectionState.putAll(existingState);
        }
        notifyDataSetChanged();
    }
}

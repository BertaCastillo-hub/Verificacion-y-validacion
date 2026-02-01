package es.unizar.eina.M132_quads.ui;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Map;

import es.unizar.eina.M132_quads.R;
import es.unizar.eina.M132_quads.database.Quad;

/** ViewHolder para un ítem de la lista de selección de cascos. */
public class CascoSelectionViewHolder extends RecyclerView.ViewHolder {

    final TextView quadMatriculaTextView;
    final CheckBox quadCheckBox;
    final Spinner cascosSpinner;

    public CascoSelectionViewHolder(@NonNull View itemView) {
        super(itemView);
        quadMatriculaTextView = itemView.findViewById(R.id.quad_matricula_text);
        quadCheckBox = itemView.findViewById(R.id.quad_selected_checkbox);
        cascosSpinner = itemView.findViewById(R.id.cascos_spinner);
    }

    public void bind(Quad quad, Map<String, Integer> selectionState, CascoSelectionListAdapter adapter) {
        String matricula = quad.getMatricula();

        // Configurar Textos
        // Mostramos el precio para que el usuario sepa qué se suma
        double precioEuros = quad.getPrecio() / 100.0;
        quadMatriculaTextView.setText(String.format("%s", matricula));

        // Configurar Spinner (Lógica Monoplaza/Biplaza)
        Integer[] opciones;
        if (quad.getTipo() == Quad.TipoQuad.Monoplaza) {
            opciones = new Integer[] { 0, 1 }; // 0 o 1 cascos
        } else {
            opciones = new Integer[] { 0, 1, 2 }; // 0, 1 o 2 cascos
        }

        ArrayAdapter<Integer> spinnerAdapter = new ArrayAdapter<>(
                itemView.getContext(),
                android.R.layout.simple_spinner_item,
                opciones);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cascosSpinner.setAdapter(spinnerAdapter);

        // Determinar estado actual.
        // Si la matrícula está en el mapa, el quad está seleccionado (aunque el valor
        // sea 0)
        boolean isSelected = selectionState.containsKey(matricula);

        // Se recupera el número de cascos guardados. Si no está seleccionado,
        // visualmente mostramos 0.
        int numCascosGuardado = isSelected ? selectionState.get(matricula) : 0;

        // Limpieza de listeners.
        quadCheckBox.setOnCheckedChangeListener(null);
        cascosSpinner.setOnItemSelectedListener(null);

        // Aplicar el estado visual.
        quadCheckBox.setChecked(isSelected);
        cascosSpinner.setEnabled(isSelected); // Solo habilitado si está seleccionado

        // Seleccionar posición segura en el spinner
        int maxIndex = opciones.length - 1;
        int spinnerPos = Math.max(0, Math.min(numCascosGuardado, maxIndex));
        cascosSpinner.setSelection(spinnerPos);

        // Asignar listeners

        // --- CHECKBOX ---
        quadCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Al marcar: se añade al mapa.
                // Se guarda la matrícula y el número de cascos actual del spinner (puede ser
                // 0).
                // Al existir la clave en el mapa, se sumará el precio del Quad.
                int valSpinner = (Integer) cascosSpinner.getSelectedItem();
                selectionState.put(matricula, valSpinner);

                cascosSpinner.setEnabled(true);
            } else {
                // Al desmarcar: se borra del mapa.
                // Al no existir la clave, no se cobrará el Quad.
                selectionState.remove(matricula);

                cascosSpinner.setEnabled(false);
                cascosSpinner.setSelection(0);
            }

            // Se notifican cambios
            adapter.notifyListener();
        });

        // --- SPINNER ---
        cascosSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Solo se guarda en el mapa si el CheckBox está marcado.
                // Esto evita que al iniciarse la pantalla (Crear Reserva), el evento automático
                // del spinner marque todos los quads como seleccionados.
                if (quadCheckBox.isChecked()) {
                    int numCascos = (Integer) parent.getItemAtPosition(position);

                    // Se actualiza el valor (puede ser 0, 1 o 2).
                    // La clave (matrícula) sigue existiendo, así que el precio del Quad se
                    // mantiene.
                    selectionState.put(matricula, numCascos);

                    // Se notifican cambios
                    adapter.notifyListener();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }
}
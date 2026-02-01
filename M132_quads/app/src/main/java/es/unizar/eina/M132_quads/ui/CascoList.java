package es.unizar.eina.M132_quads.ui;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import es.unizar.eina.M132_quads.R;

/**
 * Activity que muestra una lista de solo lectura de los quads y cascos
 * asociados a una reserva específica.
 */
public class CascoList extends AppCompatActivity {

    public static final String KEY_RESERVA_ID = "id_de_la_reserva";

    private CascoViewModel mCascoViewModel;
    private CascoListAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Lo asocia al layout creado para esta pantalla.
        setContentView(R.layout.activity_listado_cascos);

        // Configuración del RecyclerView
        RecyclerView recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        DividerItemDecoration divider = new DividerItemDecoration(recyclerView.getContext(),
                ((LinearLayoutManager) recyclerView.getLayoutManager()).getOrientation());
        recyclerView.addItemDecoration(divider);

        // Creación del Adapter
        mAdapter = new CascoListAdapter(this);
        recyclerView.setAdapter(mAdapter);

        // Obtención del ViewModel
        mCascoViewModel = new ViewModelProvider(this).get(CascoViewModel.class);

        // Observación de datos
        int idReserva = getIntent().getIntExtra(KEY_RESERVA_ID, -1);
        if (idReserva != -1) {
            mCascoViewModel.getCascosForReserva(idReserva).observe(this, cascos -> {
                // Cuando los datos llegan, se actualiza la lista en el adapter.
                mAdapter.setCascos(cascos);
            });
        }

        // Configurar la navegación inferior
        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        int[][] states = new int[][] { { android.R.attr.state_checked }, { -android.R.attr.state_checked } };
        int[] colors = new int[] { Color.WHITE, Color.WHITE };
        ColorStateList colorStateList = new ColorStateList(states, colors);
        bottomNav.setItemIconTintList(colorStateList);
        bottomNav.setItemTextColor(colorStateList);

        // Marcamos la pestaña de Reservas
        bottomNav.setSelectedItemId(R.id.navigation_reservas);

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_quads) {
                // Navegar a Quads
                Intent intent = new Intent(this, QuadList.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.navigation_reservas) {
                // Navegar a Reservas
                Intent intent = new Intent(this, ReservaList.class);
                startActivity(intent);
                return true;
            }
            return false;
        });
    }
}

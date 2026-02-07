package es.unizar.eina.M132_quads.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import es.unizar.eina.M132_quads.R;
import es.unizar.eina.M132_quads.database.Reserva;
import es.unizar.eina.M132_quads.database.QuadRepository;
import es.unizar.eina.M132_quads.database.ReservaRepository;
import android.view.Menu;
import android.view.MenuItem;
import androidx.annotation.NonNull;

/**
 * Pantalla utilizada para mostrar el listado de las reservas del sistema.
 * Implementa las interfaces del adaptador para gestionar clics y borrados.
 */
public class ReservaList extends AppCompatActivity
        implements ReservaListAdapter.OnItemClickListener, ReservaListAdapter.OnDeleteClickListener {

    private ReservaViewModel mReservaViewModel;
    private ReservaListAdapter mAdapter;

    // Lanzador para obtener resultados de la actividad de edición/creación
    private final ActivityResultLauncher<Intent> mReservaEditLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // La lista se actualiza automáticamente gracias al LiveData
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listado_reservas);

        // Configurar RecyclerView
        RecyclerView recyclerView = findViewById(R.id.recyclerview);
        mAdapter = new ReservaListAdapter(new ReservaListAdapter.ReservaDiff());
        recyclerView.setAdapter(mAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        DividerItemDecoration divider = new DividerItemDecoration(recyclerView.getContext(),
                ((LinearLayoutManager) recyclerView.getLayoutManager()).getOrientation());
        recyclerView.addItemDecoration(divider);

        // Se asigna esta actividad como listener de los eventos del adaptador
        mAdapter.setOnItemClickListener(this);
        mAdapter.setOnDeleteClickListener(this);

        // Configurar ViewModel
        mReservaViewModel = new ViewModelProvider(this).get(ReservaViewModel.class);
        mReservaViewModel.getAllReservas().observe(this, reservas -> {
            // Actualizar la lista de reservas en el adaptador.
            mAdapter.submitList(reservas);
        });

        // Configurar Spinner de Ordenación
        setupSpinner();

        // Configurar Botón Flotante (FAB) para Crear
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            Intent intent = new Intent(ReservaList.this, ReservaEdit.class);
            mReservaEditLauncher.launch(intent);
        });

        // Configurar Navegación Inferior
        setupBottomNavigation();
    }

    private void setupSpinner() {
        Spinner spinner = findViewById(R.id.spinner_filter);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.opciones_ordenar_reservas, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String orderBy = "Nombre Cliente";
                switch (position) {
                    case 0:
                        orderBy = "nombreCliente";
                        break;
                    case 1:
                        orderBy = "numeroMovil";
                        break;
                    case 2:
                        orderBy = "fechaRecogida";
                        break;
                    case 3:
                        orderBy = "fechaDevolucion";
                        break;
                }
                mReservaViewModel.setOrderBy(orderBy);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Configurar Spinner de Filtro
        setupFilterSpinner();
    }

    private void setupFilterSpinner() {
        Spinner spinner = findViewById(R.id.spinner_filter_type);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.opciones_filtro_reservas, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // El array de strings coincide con los valores que espera el ViewModel/DAO
                String selected = parent.getItemAtPosition(position).toString();
                mReservaViewModel.setFilter(selected);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    /**
     * Implementación de la interfaz OnItemClickListener.
     * Se ejecuta al pulsar sobre una fila de reserva (para editar).
     */
    @Override
    public void onItemClick(Reserva reserva) {
        Intent intent = new Intent(ReservaList.this, ReservaDetail.class);
        intent.putExtra(ReservaDetail.KEY_RESERVA_ID, reserva.getIdReserva());
        mReservaEditLauncher.launch(intent);
    }

    /**
     * Implementación de la interfaz OnDeleteClickListener.
     * Se ejecuta al pulsar el icono de la papelera.
     */
    @Override
    public void onDeleteClick(Reserva reserva) {
        showDeleteDialog(reserva);
    }

    /**
     * Muestra un cuadro de diálogo de confirmación antes de eliminar.
     */
    private void showDeleteDialog(Reserva reserva) {
        // Inflar el diseño personalizado del diálogo.
        // Se usa AlertDialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_confirm_delete, null);

        builder.setView(view);
        AlertDialog dialog = builder.create();

        // Configurar textos del layout inflado
        TextView message = view.findViewById(R.id.dialog_message);
        Button btnCancel = view.findViewById(R.id.button_cancel);
        Button btnConfirm = view.findViewById(R.id.button_delete);

        if (message != null)
            message.setText("¿Estás seguro de que quieres eliminar la reserva de " + reserva.getNombreCliente() + "?");

        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> dialog.dismiss());
        }

        if (btnConfirm != null) {
            btnConfirm.setOnClickListener(v -> {
                mReservaViewModel.delete(reserva);
                Toast.makeText(this, "Reserva eliminada", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            });
        }

        // Fondo transparente para bordes redondeados si el layout lo define
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        dialog.show();
    }

    // Configura la barra de navegación inferior.
    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        int[][] states = new int[][] { { android.R.attr.state_checked }, { -android.R.attr.state_checked } };
        int[] colors = new int[] { Color.WHITE, Color.WHITE };
        ColorStateList colorStateList = new ColorStateList(states, colors);
        bottomNav.setItemIconTintList(colorStateList);
        bottomNav.setItemTextColor(colorStateList);

        // Marcar la pestaña de Reservas
        bottomNav.setSelectedItemId(R.id.navigation_reservas);

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_quads) {
                // Navegar a Quads
                Intent intent = new Intent(this, QuadList.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.navigation_reservas) {
                // Ya está en Reservas
                return true;
            }
            return false;
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_inicio, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_tests_unitarios) {
            // Ejecutar tests unitarios
            Toast.makeText(this, "Ejecutando tests unitarios... Ver Logcat", Toast.LENGTH_SHORT).show();

            // Instanciar repositorio y clase de tests
            new Thread(() -> {
                QuadRepository repo = new QuadRepository(getApplication());
                UnitTests tests = new UnitTests();
                tests.testInsertParams(repo);
                tests.testUpdateParams(repo);
                tests.testDeleteQuadParams(repo);

                ReservaRepository reservaRepo = new ReservaRepository(getApplication());
                tests.testInsertReservaParams(reservaRepo);
                tests.testUpdateReservaParams(reservaRepo);
                tests.testDeleteReservaParams(reservaRepo);
            }).start();

            return true;
        }

        if (id == R.id.menu_tests_volumen) {
            Toast.makeText(this, "Ejecutando pruebas de volumen... Ver Logcat", Toast.LENGTH_SHORT).show();
            new Thread(() -> {
                QuadRepository repo = new QuadRepository(getApplication());
                ReservaRepository reservaRepo = new ReservaRepository(getApplication());
                UnitTests tests = new UnitTests();
                tests.testVolumen(repo, reservaRepo);
            }).start();
            return true;
        }

        if (id == R.id.menu_tests_sobrecarga) {
            Toast.makeText(this, "Ejecutando pruebas de sobrecarga... Ver Logcat", Toast.LENGTH_SHORT).show();
            new Thread(() -> {
                QuadRepository repo = new QuadRepository(getApplication());
                UnitTests tests = new UnitTests();
                tests.testSobrecarga(repo);
            }).start();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
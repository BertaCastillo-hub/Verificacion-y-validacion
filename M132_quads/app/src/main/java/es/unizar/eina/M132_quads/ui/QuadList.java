package es.unizar.eina.M132_quads.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import es.unizar.eina.M132_quads.R;
import es.unizar.eina.M132_quads.database.Quad;
import es.unizar.eina.M132_quads.database.QuadRepository;
import es.unizar.eina.M132_quads.database.ReservaRepository;
import android.view.Menu;
import android.view.MenuItem;

/**
 * Pantalla utilizada para mostrar el listado de los quads disponibles
 * en el sistema.
 */
public class QuadList extends AppCompatActivity
        implements QuadListAdapter.OnItemClickListener, QuadListAdapter.OnDeleteClickListener {

    private QuadViewModel mQuadViewModel;

    /**
     * Launcher para la actividad de creación/edición.
     * Se registra para recibir el resultado de una actividad.
     */
    private final ActivityResultLauncher<Intent> mStartForResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                // Este bloque (lambda) es el 'callback'. Se ejecuta cuando la actividad
                // llamada finaliza.
                if (result.getResultCode() == Activity.RESULT_OK) {
                    // El usuario guardó un quad (ya sea nuevo o editado).
                    // Mostrar un mensaje genérico de éxito.
                    // No es necesario saber si fue INSERT o EDIT, solo que la operación
                    // fue exitosa.
                    // LiveData se encargará de actualizar la lista automáticamente.
                    Toast.makeText(getApplicationContext(), "Quad guardado.", Toast.LENGTH_LONG).show();
                } else {
                    // El usuario canceló la operación (pulsó 'Atrás' o el botón de cancelar).
                    Toast.makeText(getApplicationContext(), "Operación cancelada.", Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listado_quads);

        RecyclerView recyclerView = findViewById(R.id.recyclerview);
        final QuadListAdapter adapter = new QuadListAdapter(new QuadListAdapter.QuadDiff());
        // Se configura el Adapter con todos los listeners que va a usar, antes de
        // asignarlo (de click normal y click de eliminación en el icono de la
        // papelera).
        adapter.setOnItemClickListener(this);
        adapter.setOnDeleteClickListener(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        DividerItemDecoration divider = new DividerItemDecoration(recyclerView.getContext(),
                ((LinearLayoutManager) recyclerView.getLayoutManager()).getOrientation());
        recyclerView.addItemDecoration(divider);

        // Se asigna el adapter ya configurado al RecyclerView.
        recyclerView.setAdapter(adapter);

        mQuadViewModel = new ViewModelProvider(this).get(QuadViewModel.class);
        mQuadViewModel.getAllQuads().observe(this, adapter::submitList);

        setupFab();
        setupSpinner();
        setupBottomNavigation();
    }

    private void setupFab() {
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            Intent intent = new Intent(QuadList.this, QuadEdit.class);
            // Usar el launcher para iniciar la actividad.
            mStartForResult.launch(intent);
        });
    }

    private void setupSpinner() {
        Spinner spinner = findViewById(R.id.spinner_filter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String orderBy = "";
                // Se define en base a qué hay que ordenar la lista de quads.
                if (position == 0) {
                    orderBy = "matricula";
                } else if (position == 1) {
                    orderBy = "tipo";
                } else if (position == 2) {
                    orderBy = "precio";
                }
                mQuadViewModel.setOrderBy(orderBy);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    // Método invocado cuando se hace clic en un quad en la lista. Se le pasa
    // como parámetro dicho quad para disponer de toda su información.
    @Override
    public void onItemClick(Quad quad) {
        // Pantalla a la que lleva tras pulsar un elemento de la lista de quads.
        Intent intent = new Intent(QuadList.this, QuadDetail.class);
        intent.putExtra(QuadDetail.MATRICULA_ID, quad.getMatricula());
        // Inicia la actividad de detalles de la forma estándar, sin esperar un
        // resultado.
        startActivity(intent);
    }

    // Para mostrar el mensaje de confirmación de eliminación no se crea una
    // nueva activity. Se usa 'AlertDialog'.
    @Override
    public void onDeleteClick(Quad quad) {
        // Inflar la vista personalizada que ahora incluye los botones
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_confirm_delete, null);

        // Crear el AlertDialog.Builder y asignarle la vista
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);

        // Crear el diálogo antes de configurar los listeners
        // Esto es importante para poder cerrar el diálogo desde dentro de los
        // listeners.
        final AlertDialog dialog = builder.create();

        // Obtener las referencias a los componentes dentro del layout personalizado
        TextView messageTextView = dialogView.findViewById(R.id.dialog_message);
        Button cancelButton = dialogView.findViewById(R.id.button_cancel);
        Button deleteButton = dialogView.findViewById(R.id.button_delete);

        // Establecer el mensaje dinámicamente
        String message = "¿Desea eliminar el quad\ncon matrícula " + quad.getMatricula() + "?";
        messageTextView.setText(message);

        // Configurar el listener para el botón de cancelar
        cancelButton.setOnClickListener(v -> {
            // Simplemente cierra el diálogo
            dialog.dismiss();
        });

        // Configurar el listener para el botón de eliminar
        deleteButton.setOnClickListener(v -> {
            // Ejecuta la acción de borrado
            mQuadViewModel.delete(quad);
            Toast.makeText(this, "Quad eliminado.", Toast.LENGTH_SHORT).show();
            // Cierra el diálogo
            dialog.dismiss();
        });

        // Hacer que el fondo de la ventana del diálogo sea transparente
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        // Mostrar el diálogo
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
        bottomNav.setSelectedItemId(R.id.navigation_quads);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_quads) {
                // Ya está en Quads
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

package es.unizar.eina.M132_quads.ui;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import es.unizar.eina.M132_quads.R;
import es.unizar.eina.M132_quads.database.Quad;

/**
 * Actividad para mostrar los detalles de un quad específico.
 * Recibe la matrícula del quad a través de un Intent, lo recupera de la base de
 * datos a través del ViewModel y muestra su información.
 * También permite iniciar la edición de dicho quad.
 */
public class QuadDetail extends AppCompatActivity {

    /** Clave para pasar la matrícula del quad en el Intent. */
    public static final String MATRICULA_ID = "es.unizar.eina.M132_quads.MATRICULA_ID";

    private QuadViewModel mQuadViewModel;
    private Quad quadActual; // Para almacenar el quad que se está mostrando

    private TextView mMatriculaDetail;
    private TextView mPrecioDetail;
    private TextView mTipoDetail;
    private TextView mDescripcionDetail;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Enlazar la vista (el fichero XML) con esta clase Java
        setContentView(R.layout.activity_detalles_quad);

        // Obtener las referencias a las vistas (TextViews) del layout
        mMatriculaDetail = findViewById(R.id.detail_matricula);
        mPrecioDetail = findViewById(R.id.detail_precio);
        mTipoDetail = findViewById(R.id.detail_tipo);
        mDescripcionDetail = findViewById(R.id.detail_descripcion);

        // Obtener el ViewModel
        mQuadViewModel = new ViewModelProvider(this).get(QuadViewModel.class);

        // Recuperar la matrícula del Intent que inició esta actividad
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(MATRICULA_ID)) {
            // Obtiene la matrícula recibida desde 'QuadList'.
            String matricula = intent.getStringExtra(MATRICULA_ID);

            // Observar los datos del quad específico desde el ViewModel
            // LiveData se encargará de actualizar la UI automáticamente si los datos
            // cambian
            mQuadViewModel.getQuadByMatricula(matricula).observe(this, quad -> {
                if (quad != null) {
                    this.quadActual = quad;
                    poblarVistas(quad);
                    // Configurar el botón flotante (FAB) de edición
                    setupFab();
                } else {
                    // Si no se encuentra el quad, se muestra un mensaje y se cierra la actividad
                    Toast.makeText(this, "Error: Quad no encontrado", Toast.LENGTH_LONG).show();
                    finish();
                }
            });
        } else {
            // Si no se pasó una matrícula, es un error. Se informa y se cierra
            // la actividad.
            Toast.makeText(this, "Error: No se ha proporcionado matrícula", Toast.LENGTH_LONG).show();
            finish();
        }

        // Configurar la barra de navegación inferior
        setupBottomNavigation();
    }

    /**
     * Rellena los TextViews con los datos del objeto Quad.
     * 
     * @param quad El quad cuyos datos se van a mostrar.
     */
    private void poblarVistas(Quad quad) {
        // Se usa String.format para construir los textos de forma segura y clara
        mMatriculaDetail.setText(String.format("Matrícula: %s", quad.getMatricula()));
        mPrecioDetail
                .setText(String.format(java.util.Locale.US, "Precio: %.2f €/día", (double) quad.getPrecio() / 100));
        mTipoDetail.setText(String.format("Tipo: %s", quad.getTipo().name()));
        mDescripcionDetail.setText(String.format("Descripción: %s", quad.getDescripcion())); // Asumiendo que 'marca'
                                                                                             // contiene la descripción
    }

    /**
     * Configura el listener del FloatingActionButton para iniciar la actividad de
     * edición.
     */
    private void setupFab() {
        FloatingActionButton fabEdit = findViewById(R.id.fab_edit);
        fabEdit.setOnClickListener(view -> {
            if (quadActual != null) {
                // Crear un Intent para ir a QuadEdit en modo Edición.
                Intent intent = new Intent(QuadDetail.this, QuadEdit.class);

                // Se adjuntan todos los datos del quad al Intent.
                // El ID es el más importante para que QuadEdit sepa que está en modo Edición.
                intent.putExtra(QuadEdit.EXTRA_MATRICULA, quadActual.getMatricula());
                intent.putExtra(QuadEdit.EXTRA_DESCRIPCION, quadActual.getDescripcion());
                intent.putExtra(QuadEdit.EXTRA_PRECIO, quadActual.getPrecio());
                intent.putExtra(QuadEdit.EXTRA_TIPO, quadActual.getTipo().name());

                // Lanzar la actividad de edición.
                startActivity(intent);
            } else {
                Toast.makeText(this, "No se pueden editar los detalles porque no se han cargado", Toast.LENGTH_SHORT)
                        .show();
            }
        });
    }

    /**
     * Configura la barra de navegación inferior.
     */
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
                Intent intent = new Intent(this, QuadList.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.navigation_reservas) {
                Intent intent = new Intent(this, ReservaList.class);
                startActivity(intent);
                return true;
            }
            return false;
        });
    }
}

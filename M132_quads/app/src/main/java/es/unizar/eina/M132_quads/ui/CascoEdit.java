package es.unizar.eina.M132_quads.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Date; // Import para la fecha

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import es.unizar.eina.M132_quads.R;
import es.unizar.eina.M132_quads.database.Casco;
import es.unizar.eina.M132_quads.database.Quad;
import es.unizar.eina.M132_quads.database.Reserva;

/** Pantalla utilizada para la creación o edición de un Casco. */
public class CascoEdit extends AppCompatActivity {

    // Claves para la comunicación a través de Intent
    public static final String KEY_ID_RESERVA = "id_reserva_para_cascos";
    public static final String KEY_MATRICULA_QUAD_A_EDITAR = "matricula_quad_a_editar";

    private static final int MODO_INVALIDO = -1;

    // Vistas de la UI
    private TextView mHeaderTitle;
    private TextView mPrecioTotalTextView;
    private RecyclerView mRecyclerView;
    private Button mCrearButton; // Botón para modo creación
    private Button mModificarButton; // Botón para modo modificación
    private Button mCancelarButton;

    // Componentes de Android y estado
    private CascoSelectionViewModel mViewModel;
    private CascoSelectionListAdapter mAdapter;
    private int mIdReserva;
    private String mMatriculaAEditar;
    private List<Quad> mListaCompletaQuads = new ArrayList<>();

    private Reserva mReservaActual; // Variable para guardar el objeto Reserva completo

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modificar_casco);

        // Inicializar el ViewModel
        mViewModel = new ViewModelProvider(this).get(CascoSelectionViewModel.class);

        // Obtener referencias a todas las vistas del layout
        initializeViews();

        // Analizar el Intent para determinar el modo
        if (!processIntent()) {
            Toast.makeText(this, "Error: Datos de la reserva no válidos", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Se observa la reserva para tenerla disponible al guardar.
        mViewModel.getReservaById(mIdReserva).observe(this, reserva -> {
            mReservaActual = reserva;
        });

        // Configurar el RecyclerView
        setupRecyclerView();

        // Configurar la UI y observar los datos según el modo
        if (mMatriculaAEditar != null) {
            // --- Modo modificación ---
            setupUiForModifyMode();
        } else {
            // --- Modo creación ---
            setupUiForCreateMode();
        }

        // Configurar los listeners de los botones
        setupButtonListeners();
    }

    /**
     * Obtiene las referencias a todas las vistas del layout XML.
     * Sigue el patrón de QuadEdit.java.
     */
    private void initializeViews() {
        mHeaderTitle = findViewById(R.id.header_title_cascos);
        mPrecioTotalTextView = findViewById(R.id.text_total_precio);
        mRecyclerView = findViewById(R.id.recyclerview_cascos);
        // Se obtienen las referencias a los dos botones por separado
        mCrearButton = findViewById(R.id.button_crear);
        mModificarButton = findViewById(R.id.button_modificar);
        mCancelarButton = findViewById(R.id.button_cancelar_cascos);
    }

    /**
     * Procesa los datos recibidos en el Intent y establece el estado inicial.
     */
    private boolean processIntent() {
        Bundle extras = getIntent().getExtras();
        if (extras == null)
            return false;
        mIdReserva = extras.getInt(KEY_ID_RESERVA, MODO_INVALIDO);
        mMatriculaAEditar = extras.getString(KEY_MATRICULA_QUAD_A_EDITAR);
        return mIdReserva != MODO_INVALIDO;
    }

    /**
     * Configura el RecyclerView, el Adapter y el listener para actualizar el
     * precio.
     */
    private void setupRecyclerView() {
        mAdapter = new CascoSelectionListAdapter(this);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        // El listener notifica a la Activity para que recalcule el precio.
        mAdapter.setOnSelectionChangedListener(this::updateTotalPrice);
    }

    /**
     * Configura la UI para el modo "Añadir Quads a Reserva".
     * Muestra el botón de crear y oculta el de modificar.
     */
    private void setupUiForCreateMode() {
        mHeaderTitle.setText("Añadir Quads a la Reserva");
        // Gestión de visibilidad de botones
        mCrearButton.setVisibility(View.VISIBLE);
        mModificarButton.setVisibility(View.GONE);

        // La lógica de observación de datos es la misma
        mViewModel.getAllQuads().observe(this, quads -> {
            mListaCompletaQuads = quads;
            mAdapter.setAllQuads(quads);
            mViewModel.getCascosForReserva(mIdReserva).observe(this, cascosExistentes -> {
                mAdapter.setInitialSelection(cascosExistentes);
                updateTotalPrice();
            });
        });
    }

    /**
     * Configura la UI para el modo "Modificar Cascos".
     * Muestra el botón de modificar y oculta el de crear.
     */
    private void setupUiForModifyMode() {
        mHeaderTitle.setText("Modificar Selección de Quads");
        // Gestión de visibilidad de botones
        mCrearButton.setVisibility(View.GONE);
        mModificarButton.setVisibility(View.VISIBLE);

        // La lógica para mostrar todos los quads y preseleccionar es la misma
        mViewModel.getAllQuads().observe(this, quads -> {
            mListaCompletaQuads = quads;
            mAdapter.setAllQuads(quads);
            mViewModel.getCascosForReserva(mIdReserva).observe(this, cascos -> {
                mAdapter.setInitialSelection(cascos);
                updateTotalPrice();
            });
        });
    }

    /**
     * Configura los OnClickListeners para los botones.
     * Ambos, crear y modificar, llaman al mismo método de guardado.
     */
    private void setupButtonListeners() {
        mCrearButton.setOnClickListener(view -> saveData());
        mModificarButton.setOnClickListener(view -> saveData());
        mCancelarButton.setOnClickListener(view -> {
            // Cierra la actividad sin guardar nada
            finish();
        });
    }

    private void saveData() {
        // Comprobación de seguridad: No guardar si la reserva no se ha cargado.
        if (mReservaActual == null) {
            Toast.makeText(this, "Error: No se pudo cargar la reserva. Intente de nuevo.", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Integer> selection = mAdapter.getSelectedData();
        List<Casco> cascosAGuardar = new ArrayList<>();
        int precioTotal = 0; // Variable para calcular el precio total

        for (Map.Entry<String, Integer> entry : selection.entrySet()) {
            cascosAGuardar.add(new Casco(entry.getValue(), entry.getKey(), mIdReserva));

            // Calcular el precio total sumando el precio de cada quad seleccionado
            for (Quad quad : mListaCompletaQuads) {
                if (quad.getMatricula().equals(entry.getKey())) {
                    precioTotal += quad.getPrecio();
                    break;
                }
            }
        }

        // Comprobar solapes antes de guardar
        if (mViewModel.checkOverlaps(cascosAGuardar, mReservaActual)) {
            Toast.makeText(this, "Error: Algunos quads seleccionados están ocupados en esas fechas.", Toast.LENGTH_LONG)
                    .show();
            return;
        }

        // Actualizar el precio total en la reserva antes de guardarla
        mReservaActual.setPrecioTotal(precioTotal);

        // Se pasa el objeto Reserva y la lista de Cascos.
        mViewModel.saveReservaConCascos(mReservaActual, cascosAGuardar);

        String mensaje = (mMatriculaAEditar != null) ? "Reserva actualizada correctamente"
                : "Reserva creada correctamente";
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, ReservaList.class);

        startActivity(intent);
    }

    /**
     * Calcula y actualiza el precio total mostrado en la UI.
     */

    private void updateTotalPrice() {
        Map<String, Integer> selection = mAdapter.getSelectedData();
        double precioTotal = 0.0;

        for (Quad quad : mListaCompletaQuads) {
            // Si está seleccionado (en el mapa), se suma el precio, aunque cascos sea 0.
            if (selection.containsKey(quad.getMatricula())) {
                precioTotal += quad.getPrecio();
            }
        }
        mPrecioTotalTextView.setText(String.format(Locale.getDefault(), "Precio Total: %.2f €", precioTotal / 100));
    }
}

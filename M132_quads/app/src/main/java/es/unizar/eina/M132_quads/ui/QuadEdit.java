package es.unizar.eina.M132_quads.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

import es.unizar.eina.M132_quads.R;
import es.unizar.eina.M132_quads.database.Quad;
import es.unizar.eina.M132_quads.database.Quad.TipoQuad;

/**
 * Pantalla utilizada para la creación o edición de un Quad.
 * Distingue entre modo Creación y modo Edición basándose en si se recibe un ID
 * en el Intent.
 */
public class QuadEdit extends AppCompatActivity {

    // Claves para pasar y recibir datos a través de Intents. Son públicas para ser
    // usadas por otras actividades.
    public static final String EXTRA_MATRICULA = "es.unizar.eina.M132_quads.EXTRA_MATRICULA";
    public static final String EXTRA_TIPO = "es.unizar.eina.M132_quads.EXTRA_TIPO";
    public static final String EXTRA_PRECIO = "es.unizar.eina.M132_quads.EXTRA_PRECIO";
    public static final String EXTRA_DESCRIPCION = "es.unizar.eina.M132_quads.EXTRA_DESCRIPCION";

    // Vistas de la UI
    private TextView mHeaderTitle;
    private TextInputEditText mMatriculaText;
    private TextInputEditText mPrecioText;
    private TextInputEditText mDescripcionText;
    private RadioGroup mTipoRadioGroup;
    private RadioButton mMonoplazaRadio;
    private RadioButton mBiplazaRadio;
    private Button mCrearButton;
    private Button mModificarButton;
    private Button mCancelarButton;

    // ViewModel para interactuar con la base de datos
    private QuadViewModel mQuadViewModel;

    // Variable para almacenar el ID del quad si estamos en modo edición.
    private String mQuadId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Enlazar este código con el layout XML correcto
        setContentView(R.layout.activity_modificar_quad);

        // Inicializar el ViewModel
        mQuadViewModel = new ViewModelProvider(this).get(QuadViewModel.class);

        // Obtener referencias a todas las vistas del layout
        initializeViews();

        // Analizar el Intent para determinar si es Creación o Edición
        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.containsKey(EXTRA_MATRICULA)) {
            // --- Si ha recibido la matrícula de un quad, se está en modo Edición ---
            mQuadId = extras.getString(EXTRA_MATRICULA);
            setupEditMode(extras);
        } else {
            // --- Si no ha recibido la información de ningún quad al ser
            // invocada, se está en modo Creación ---
            mQuadId = null; // Asegurar que el ID es nulo
            setupCreateMode();
        }

        // Configurar los listeners de los botones
        setupButtonListeners();
    }

    /**
     * Obtiene las referencias a todas las vistas del layout XML.
     */
    private void initializeViews() {
        mHeaderTitle = findViewById(R.id.header_title_edit);
        mMatriculaText = findViewById(R.id.edit_matricula);
        mPrecioText = findViewById(R.id.edit_precio);
        mDescripcionText = findViewById(R.id.edit_descripcion);
        mTipoRadioGroup = findViewById(R.id.radio_group_tipo);
        mMonoplazaRadio = findViewById(R.id.radio_monoplaza);
        mBiplazaRadio = findViewById(R.id.radio_biplaza);
        mCrearButton = findViewById(R.id.button_crear);
        mModificarButton = findViewById(R.id.button_modificar);
        mCancelarButton = findViewById(R.id.button_cancelar);
    }

    /**
     * Configura la UI para el modo de edición.
     * Rellena los campos con los datos existentes y ajusta la visibilidad de los
     * botones.
     */
    private void setupEditMode(Bundle extras) {
        // Cambiar el título de la cabecera
        mHeaderTitle.setText(R.string.modificar_quad);

        // Rellenar los campos con los datos recibidos
        mMatriculaText.setText(extras.getString(EXTRA_MATRICULA));
        // En modo edición, no se debe poder cambiar la matrícula (clave primaria)
        mMatriculaText.setEnabled(false);
        mMatriculaText.setFocusable(false);

        // Se recupera el valor como un 'int', que es como se guardó.
        int precio = extras.getInt(EXTRA_PRECIO, 0); // 0 es el valor por defecto

        // Convertir el 'int' a un 'String' para poder mostrarlo en el EditText.
        mPrecioText.setText(String.valueOf(precio / 100));

        mDescripcionText.setText(extras.getString(EXTRA_DESCRIPCION));

        // Seleccionar el RadioButton correcto, el que corresponde con el
        // tipo recibido como parámetro.
        String tipo = extras.getString(EXTRA_TIPO);
        if ("Monoplaza".equalsIgnoreCase(tipo)) {
            mMonoplazaRadio.setChecked(true);
        } else if ("Biplaza".equalsIgnoreCase(tipo)) {
            mBiplazaRadio.setChecked(true);
        }

        // Ajustar la visibilidad de los botones en función de si se está en
        // modo Edición o modo Creación.
        mCrearButton.setVisibility(View.GONE);
        mModificarButton.setVisibility(View.VISIBLE);
    }

    /**
     * Configura la UI para el modo de creación (estado por defecto del XML).
     */
    private void setupCreateMode() {
        // Aunque el título "Nuevo Quad" y los botones ya son correctos por defecto en
        // el XML.
        mHeaderTitle.setText(R.string.nuevo_quad);
        mCrearButton.setVisibility(View.VISIBLE);
        mModificarButton.setVisibility(View.GONE);
    }

    /**
     * Configura los OnClickListeners para los botones de la pantalla.
     */
    private void setupButtonListeners() {
        mCrearButton.setOnClickListener(view -> handleSave());
        mModificarButton.setOnClickListener(view -> handleSave());
        mCancelarButton.setOnClickListener(view -> {
            // Simplemente cierra la actividad sin guardar nada
            finish();
        });
    }

    /**
     * Gestiona la lógica de guardar, ya sea creando un nuevo quad o actualizando
     * uno existente.
     */
    private void handleSave() {
        // Validar que los campos no estén vacíos
        if (TextUtils.isEmpty(mMatriculaText.getText()) || TextUtils.isEmpty(mPrecioText.getText())) {
            Toast.makeText(getApplicationContext(), R.string.empty_not_saved, Toast.LENGTH_LONG).show();
            return;
        }

        // Recoger los datos de los campos
        String matricula = mMatriculaText.getText().toString();
        String descripcion = mDescripcionText.getText().toString();

        int precio = 0;
        try {
            precio = Integer.parseInt(mPrecioText.getText().toString());
        } catch (NumberFormatException e) {
            Toast.makeText(getApplicationContext(), "El precio debe ser un número válido", Toast.LENGTH_LONG).show();
            return;
        }

        int selectedRadioId = mTipoRadioGroup.getCheckedRadioButtonId();
        if (selectedRadioId == -1) {
            Toast.makeText(getApplicationContext(), R.string.empty_not_saved, Toast.LENGTH_LONG).show();
            return;
        }

        RadioButton selectedRadioButton = findViewById(selectedRadioId);
        // Se obtiene el tipo como uno de los valores del tipo enumerado TipoQuad
        // (Monoplaza, Biplaza).
        String tipoString = selectedRadioButton.getText().toString();

        // Se declara la variable para el tipo enumerado.
        TipoQuad tipo;

        // Se utiliza un bloque try-catch para manejar el caso en que el String no
        // corresponda a ningún valor del enum (por ejemplo, si estuviera mal escrito),
        // aunque en este caso no va a pasar por usar un Radiobutton con textos correctos.
        try {
            tipo = TipoQuad.valueOf(tipoString);
        } catch (IllegalArgumentException e) {
            // No debería pasar.
            Toast.makeText(this, "Tipo de quad interno no válido", Toast.LENGTH_SHORT).show();
            return;
        }

        // --- Validación ---
        String errorMatricula = Quad.validateMatricula(matricula);
        if (errorMatricula != null) {
            Toast.makeText(this, errorMatricula, Toast.LENGTH_LONG).show();
            return;
        }

        String errorPrecio = Quad.validatePrecio(precio * 100); // Se valida el precio en euros.

        String errorPrecioMsg = Quad.validatePrecio(precio * 100);
        if (errorPrecioMsg != null) {
            Toast.makeText(this, errorPrecioMsg, Toast.LENGTH_LONG).show();
            return;
        }

        // Crear el objeto Quad
        Quad quad = new Quad(matricula, tipo, precio * 100, descripcion);

        if (mQuadId != null) {
            // --- Lógica de modificación ---
            // Se asigna el ID al objeto para que Room sepa qué fila actualizar
            quad.setMatricula(mQuadId);
            mQuadViewModel.update(quad);
            Toast.makeText(this, "Quad modificado", Toast.LENGTH_SHORT).show();
        } else {
            // --- Lógica de creación ---
            mQuadViewModel.insert(quad);
            Toast.makeText(this, "Quad creado", Toast.LENGTH_SHORT).show();
        }

        // Finalizar la actividad y volver a la pantalla anterior
        finish();
    }
}

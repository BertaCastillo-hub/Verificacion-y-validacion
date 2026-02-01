package es.unizar.eina.M132_quads.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;
import java.util.Locale;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import es.unizar.eina.M132_quads.R;
import es.unizar.eina.M132_quads.database.Reserva;

/** Pantalla utilizada para la creación o edición de una Reserva. */
public class ReservaEdit extends AppCompatActivity {

    public static final String KEY_RESERVA_ID = "id_reserva";

    private TextInputEditText mNombreText;
    private TextInputEditText mMovilText;
    private TextInputEditText mFechaRecogidaText;
    private TextInputEditText mFechaDevolucionText;

    private TextView mHeaderTitle;

    // Botones de acción
    private Button mButtonNext;
    private Button mButtonCancel;

    private ReservaViewModel mReservaViewModel;

    private Integer mRowId;
    private int mCurrentPrecioTotal = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modificar_reserva);

        mReservaViewModel = new ViewModelProvider(this).get(ReservaViewModel.class);

        // Enlazar Vistas
        mNombreText = findViewById(R.id.edit_text_nombre);
        mMovilText = findViewById(R.id.edit_text_movil);
        mFechaRecogidaText = findViewById(R.id.edit_text_fecha_recogida);
        mFechaDevolucionText = findViewById(R.id.edit_text_fecha_devolucion);
        mHeaderTitle = findViewById(R.id.header_title_reserva);

        // Enlazar Botones (Ajusta los IDs según tu XML)
        mButtonNext = findViewById(R.id.button_siguiente); // Botón "Siguiente"
        mButtonCancel = findViewById(R.id.button_cancelar); // Botón "Cancelar"

        // Configurar selectores de fecha
        mFechaRecogidaText.setOnClickListener(v -> showDatePickerDialog(mFechaRecogidaText));
        mFechaDevolucionText.setOnClickListener(v -> showDatePickerDialog(mFechaDevolucionText));

        // Recuperar datos si es Edición
        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.containsKey(KEY_RESERVA_ID)) {
            mRowId = extras.getInt(KEY_RESERVA_ID);
            if (mHeaderTitle != null)
                mHeaderTitle.setText("Editar Reserva");

            // Cargar los datos de la reserva existente
            mReservaViewModel.getReservaById(mRowId).observe(this, reserva -> {
                if (reserva != null) {
                    mNombreText.setText(reserva.getNombreCliente());
                    mMovilText.setText(String.valueOf(reserva.getNumeroMovil()));
                    mFechaRecogidaText.setText(reserva.getFechaRecogida());
                    mFechaDevolucionText.setText(reserva.getFechaDevolucion());
                    mCurrentPrecioTotal = reserva.getPrecioTotal();
                }
            });
        } else {
            mRowId = null;
            if (mHeaderTitle != null)
                mHeaderTitle.setText("Nueva Reserva");
        }

        // Configurar Listeners de los Botones

        // Botón 'SIGUIENTE': Guarda y va a la selección de cascos
        mButtonNext.setOnClickListener(view -> saveReservaAndContinue());

        // Botón 'CANCELAR': Cierra la actividad sin hacer nada
        mButtonCancel.setOnClickListener(view -> finish());
    }

    private void saveReservaAndContinue() {
        // --- Validaciones ---
        if (TextUtils.isEmpty(mNombreText.getText()) || TextUtils.isEmpty(mMovilText.getText())) {
            Toast.makeText(this, "Nombre y Móvil son obligatorios.", Toast.LENGTH_LONG).show();
            return;
        }

        String nombre = mNombreText.getText().toString();
        String movilStr = mMovilText.getText().toString();

        // Validar formato de móvil español: 9 dígitos, empieza por 6 o 7
        if (!movilStr.matches("^[67][0-9]{8}$")) {
            mMovilText.setError("Debe ser un móvil válido (9 dígitos empezando por 6 o 7)");
            return;
        }

        int movil = Integer.parseInt(movilStr);

        String fechaRecogida = mFechaRecogidaText.getText().toString();
        String fechaDevolucion = mFechaDevolucionText.getText().toString();

        if (TextUtils.isEmpty(fechaRecogida) || TextUtils.isEmpty(fechaDevolucion)) {
            Toast.makeText(this, "Seleccione las fechas.", Toast.LENGTH_LONG).show();
            return;
        }

        // --- Preparar el objeto ---
        int id = (mRowId == null) ? 0 : mRowId;
        Reserva reserva = new Reserva(id, nombre, movil, fechaRecogida, fechaDevolucion, mCurrentPrecioTotal);

        long idResultado;

        // --- Guardar en la base de datos ---
        if (mRowId == null) {
            // Insertar (Reserva Nueva)
            idResultado = mReservaViewModel.insert(reserva);
        } else {
            // Actualizar (Reserva Existente)
            mReservaViewModel.update(reserva);
            idResultado = mRowId;
        }

        if (idResultado != -1) {
            Intent intent = new Intent(ReservaEdit.this, CascoEdit.class);
            // Le pasa a la siguiente pantalla el identificador de la reserva que se está
            // actualizando, para que sepa a qué reserva están asociados cada par de cascos
            // y quads.
            intent.putExtra(CascoEdit.KEY_ID_RESERVA, (int) idResultado);

            if (mRowId != null) {
                // Para avisar a la siguiente pantalla de que se está en modo editar.
                intent.putExtra(CascoEdit.KEY_MATRICULA_QUAD_A_EDITAR, "edicion");
            }

            // Si es edición, quizás quieras pasar más datos, pero con el ID basta
            startActivity(intent);

        } else {
            Toast.makeText(this, "Error al guardar la reserva", Toast.LENGTH_SHORT).show();
        }
    }

    private void showDatePickerDialog(final TextInputEditText editText) {
        Calendar calendar = Calendar.getInstance();

        // Configurar el Listener
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    // Formatear la fecha elegida
                    String selectedDate = String.format(Locale.getDefault(), "%02d/%02d/%04d", dayOfMonth, month + 1,
                            year);
                    editText.setText(selectedDate);

                    // Si se cambia la fecha de recogida, se borra la de DEVOLUCIÓN
                    if (editText == mFechaRecogidaText) {
                        mFechaDevolucionText.setText("");
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));

        // Lógica para restringir fechas (Devolución >= Recogida)
        if (editText == mFechaDevolucionText) {
            // Estamos editando la fecha de devolución.
            String fechaRecogidaStr = mFechaRecogidaText.getText().toString();

            if (!TextUtils.isEmpty(fechaRecogidaStr)) {
                try {
                    // Convertimos el texto de recogida a un objeto Date
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    Date fechaRecogida = sdf.parse(fechaRecogidaStr);

                    if (fechaRecogida != null) {
                        // Establecemos esa fecha como la MÍNIMA seleccionable
                        datePickerDialog.getDatePicker().setMinDate(fechaRecogida.getTime());
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            } else {
                // Si no hay fecha de recogida puesta, se avisa al usuario.
                Toast.makeText(this, "Seleccione primero la fecha de recogida", Toast.LENGTH_SHORT).show();
                // Bloqueamos fechas pasadas por defecto
                datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            }
        } else {
            // Editando la fecha de recogida. No se permite seleccionar una fecha
            // anterior a la actual.
            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        }

        // Se configura que la semana en el calendario mostrado empiece en Lunes.
        datePickerDialog.getDatePicker().setFirstDayOfWeek(Calendar.MONDAY);

        datePickerDialog.show();
    }
}
package es.unizar.eina.M132_quads.ui;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import es.unizar.eina.M132_quads.R;
import es.unizar.eina.M132_quads.database.Reserva;
import es.unizar.eina.send.SendAbstraction;
import es.unizar.eina.send.SendAbstractionImpl;

/**
 * Actividad para mostrar los detalles de una reserva específica.
 * Recibe el ID de la reserva a través de un Intent, recupera los datos de la BD
 * y permite navegar a la edición.
 */
public class ReservaDetail extends AppCompatActivity {

    public static final String KEY_RESERVA_ID = "id_reserva";

    private ReservaViewModel mReservaViewModel;
    private Reserva mReservaActual;

    private TextView mTvNombre;
    private TextView mTvMovil;
    private TextView mTvFechas;
    private TextView mTvPrecio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalles_reserva);

        mTvNombre = findViewById(R.id.tv_detail_cliente);
        mTvMovil = findViewById(R.id.tv_detail_movil);
        mTvFechas = findViewById(R.id.tv_detail_fechas);
        mTvPrecio = findViewById(R.id.tv_detail_precio);

        FloatingActionButton fabEdit = findViewById(R.id.fab_edit);

        mReservaViewModel = new ViewModelProvider(this).get(ReservaViewModel.class);

        int reservaId = getIntent().getIntExtra(KEY_RESERVA_ID, -1);

        if (reservaId == -1) {
            Toast.makeText(this, "Error: Reserva no encontrada", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        mReservaViewModel.getReservaById(reservaId).observe(this, reserva -> {
            if (reserva != null) {
                mReservaActual = reserva;
                updateUI(reserva);
            } else {
                Toast.makeText(this, "La reserva ya no existe", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        FloatingActionButton fabNav = findViewById(R.id.fab_navigation);
        fabNav.setOnClickListener(view -> {
            android.view.View dialogView = getLayoutInflater().inflate(R.layout.dialog_navigation, null);
            androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
            builder.setView(dialogView);
            final androidx.appcompat.app.AlertDialog dialog = builder.create();
            android.widget.TextView messageTextView = dialogView.findViewById(R.id.dialog_message);
            if (mReservaActual != null) {
                messageTextView.setText("¿Desea enviar los datos\nde la reserva al cliente\n"
                        + mReservaActual.getNombreCliente() + "?");
            }

            android.widget.Button cancelButton = dialogView.findViewById(R.id.button_cancel);
            android.widget.Button acceptButton = dialogView.findViewById(R.id.button_accept);
            cancelButton.setOnClickListener(v -> dialog.dismiss());
            acceptButton.setOnClickListener(v -> {
                dialog.dismiss();
                showSendMethodDialog();
            });
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            }
            dialog.show();
        });

        fabEdit.setOnClickListener(view -> {
            if (mReservaActual != null) {
                Intent intent = new Intent(ReservaDetail.this, ReservaEdit.class);
                intent.putExtra(ReservaEdit.KEY_RESERVA_ID, mReservaActual.getIdReserva());
                startActivity(intent);
            }
        });

        ImageButton btnInfo = findViewById(R.id.btn_info_precio);
        btnInfo.setOnClickListener(view -> {
            if (mReservaActual != null) {
                Intent intent = new Intent(ReservaDetail.this, CascoList.class);
                intent.putExtra(CascoList.KEY_RESERVA_ID, mReservaActual.getIdReserva());
                startActivity(intent);
            } else {
                Toast.makeText(this, "Espera a que carguen los datos", Toast.LENGTH_SHORT).show();
            }
        });

        setupBottomNavigation();
    }

    private void updateUI(Reserva reserva) {
        if (mTvNombre != null)
            mTvNombre.setText(reserva.getNombreCliente());
        if (mTvMovil != null)
            mTvMovil.setText(String.valueOf(reserva.getNumeroMovil()));

        if (mTvFechas != null) {
            String rangoFechas = reserva.getFechaRecogida() + " - " + reserva.getFechaDevolucion();
            mTvFechas.setText(rangoFechas);
        }

        if (mTvPrecio != null) {
            mTvPrecio.setText(reserva.getPrecioTotal() / 100.00 + " €");
        }
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        int[][] states = new int[][] { { android.R.attr.state_checked }, { -android.R.attr.state_checked } };
        int[] colors = new int[] { Color.WHITE, Color.WHITE };
        ColorStateList colorStateList = new ColorStateList(states, colors);
        bottomNav.setItemIconTintList(colorStateList);
        bottomNav.setItemTextColor(colorStateList);

        bottomNav.setSelectedItemId(R.id.navigation_reservas);

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

    private void showSendMethodDialog() {
        if (mReservaActual == null) {
            Toast.makeText(this, "No hay datos de reserva para enviar", Toast.LENGTH_SHORT).show();
            return;
        }
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Seleccionar método de envío");
        builder.setItems(new CharSequence[] { "WhatsApp", "SMS" }, (dialog, which) -> {
            String method = (which == 0) ? "WhatsApp" : "SMS";
            sendReservationData(method);
        });
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void sendReservationData(String method) {
        if (mReservaActual == null) {
            Toast.makeText(this, "No hay datos de reserva para enviar", Toast.LENGTH_SHORT).show();
            return;
        }
        String message = formatReservationData(mReservaActual);
        String phone = String.valueOf(mReservaActual.getNumeroMovil());
        SendAbstraction sendAbstraction = new SendAbstractionImpl(this, method);
        sendAbstraction.send(phone, message);
    }

    private String formatReservationData(Reserva reserva) {
        StringBuilder sb = new StringBuilder();
        sb.append("--- DATOS DE RESERVA ---\n\n");
        sb.append("Cliente: ").append(reserva.getNombreCliente()).append("\n");
        sb.append("Teléfono: ").append(reserva.getNumeroMovil()).append("\n");
        sb.append("Fecha recogida: ").append(reserva.getFechaRecogida()).append("\n");
        sb.append("Fecha devolución: ").append(reserva.getFechaDevolucion()).append("\n");
        sb.append("Precio total: ").append(reserva.getPrecioTotal()).append(" €\n");
        sb.append("\nGracias por su reserva!");
        return sb.toString();
    }
}
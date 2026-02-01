package es.unizar.eina.M132_quads.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import es.unizar.eina.M132_quads.R;
import es.unizar.eina.M132_quads.database.QuadRepository;
import es.unizar.eina.M132_quads.database.ReservaRepository;

/**
 * Actividad principal que se muestra al iniciar la aplicación.
 * Proporciona opciones para navegar a la sección de Quads o de Reservas.
 */
public class Inicio extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Enlazar la vista (el fichero XML) con esta clase Java
        setContentView(R.layout.activity_inicio);

        // Obtener las referencias a los botones definidos en el XML
        Button buttonQuads = findViewById(R.id.button_quads);
        Button buttonReservas = findViewById(R.id.button_reservas);

        // Configurar el listener para el botón "QUADS"
        buttonQuads.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Crear un Intent para iniciar la actividad QuadList
                Intent intent = new Intent(Inicio.this, QuadList.class);
                startActivity(intent);
            }
        });

        // Configurar el listener para el botón "RESERVAS"
        buttonReservas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /// Crear un Intent para iniciar la actividad ReservaList
                Intent intent = new Intent(Inicio.this, ReservaList.class);
                startActivity(intent);
            }
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
            // Se usa un hilo separado para no bloquear la UI aunque en
            // este caso el repositorio ya usa hilos internamente.
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

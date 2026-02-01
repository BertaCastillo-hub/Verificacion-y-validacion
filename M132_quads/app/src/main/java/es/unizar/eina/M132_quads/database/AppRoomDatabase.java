package es.unizar.eina.M132_quads.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.room.TypeConverters; // Para el tipo de dato enumerado.

@Database(entities = { Quad.class, Reserva.class, Casco.class }, version = 1, exportSchema = false)
@TypeConverters({ Converters.class })
/** Base de datos para la aplicación que gestiona Quads, Reservas y Cascos. */
public abstract class AppRoomDatabase extends RoomDatabase {

    public abstract QuadDao quadDao();

    public abstract ReservaDao reservaDao();

    public abstract CascoDao cascoDao();

    private static volatile AppRoomDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    static AppRoomDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppRoomDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppRoomDatabase.class, "app_database")
                            .addCallback(sRoomDatabaseCallback)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);

            // If you want to keep data through app restarts,
            // comment out the following block
            databaseWriteExecutor.execute(() -> {
                // Populate the database in the background.
                // If you want to start with more quads, just add them.

                // Obtener los DAOs para todas las tablas.
                QuadDao quadDao = INSTANCE.quadDao();
                CascoDao cascoDao = INSTANCE.cascoDao();
                ReservaDao reservaDao = INSTANCE.reservaDao();

                // Limpiar todas las tablas para asegurar un estado inicial limpio.
                quadDao.deleteAll();
                cascoDao.deleteAll();
                reservaDao.deleteAll();

                // Poblar la tabla 'Quad' con datos de ejemplo para Quads.
                Quad quad = new Quad("1111ABC", Quad.TipoQuad.Monoplaza, 5000, "Yamaha Raptor 700");
                quadDao.insert(quad);
                quad = new Quad("2222DEF", Quad.TipoQuad.Biplaza, 7550, "Honda TRX 450R");
                quadDao.insert(quad);
                quad = new Quad("3333GHI", Quad.TipoQuad.Monoplaza, 4500, "Suzuki LTZ 400");
                quadDao.insert(quad);

                // Poblar la tabla 'Reserva' con datos de ejemplo para Reservas.
                Reserva reserva1 = new Reserva(1, "Juan Pérez", 666111222, "20/11/2025", "22/11/2025", 5000);
                long idReserva1 = reservaDao.insert(reserva1); // Room genera el ID y lo devuelve

                Reserva reserva2 = new Reserva(2, "Ana García", 666333444, "01/12/2025", "03/12/2025", 7550);
                long idReserva2 = reservaDao.insert(reserva2); // Room genera otro ID y lo devuelve

                // Poblar la tabla 'Casco' con datos de ejemplo para Cascos.
                if (idReserva1 != -1) {
                    Casco casco1 = new Casco(2, "1111ABC", (int) idReserva1);
                    cascoDao.insert(casco1);
                }

                if (idReserva2 != -1) {
                    Casco casco2 = new Casco(1, "2222DEF", (int) idReserva2);
                    cascoDao.insert(casco2);
                }
            });
        }
    };

}

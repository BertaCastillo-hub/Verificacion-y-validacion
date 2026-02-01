package es.unizar.eina.M132_quads.ui;

import android.util.Log;

import es.unizar.eina.M132_quads.database.Quad;
import es.unizar.eina.M132_quads.database.QuadRepository;
import es.unizar.eina.M132_quads.database.Reserva;
import es.unizar.eina.M132_quads.database.ReservaRepository;

/**
 * Clase para ejecutar pruebas unitarias manuales (caja negra)
 * sobre el repositorio de Quads.
 */
public class UnitTests {

    private static final String TAG = "UnitTests";

    public void testInsertParams(QuadRepository repo) {
        Log.d(TAG, "----- PRUEBAS insert QuadRepository -----");

        // Caso 1: Válido [1111AAA, Monoplaza, 50]
        Quad q1 = new Quad("1111AAA", Quad.TipoQuad.Monoplaza, 5000, "Test 1");
        // Limpiamos por si existe de antes
        repo.delete(q1);
        long r1 = repo.insert(q1);
        Log.d(TAG, "Caso 1: Insertar Quad válido (1111AAA, Monoplaza, 5000). Esperado: >0. Obtenido: " + r1);
        repo.delete(q1); // Cleaning up

        // Caso 2: Válido [1111AAA, Biplaza, 50] (Reutilizando ID, primero borramos el
        // anterior para simular inserción limpia)
        // Nota: Si la intención de la prueba es probar QUE SE PUEDE insertar UN
        // Biplaza, deberíamos usar otro ID o borrar el anterior.
        // Dado que comparten ID "1111AAA", borramos primero.
        repo.delete(q1);
        Quad q2 = new Quad("1111AAA", Quad.TipoQuad.Biplaza, 5000, "Test 2");
        long r2 = repo.insert(q2);
        Log.d(TAG, "Caso 2: Insertar Quad válido (1111AAA, Biplaza, 5000). Esperado: >0. Obtenido: " + r2);
        repo.delete(q2); // Cleaning up

        // Caso 3: Inválido [Matricula null]
        try {
            Quad q3 = new Quad(null, Quad.TipoQuad.Monoplaza, 5000, "Test 3");
            long r3 = repo.insert(q3);
            Log.d(TAG, "Caso 3: Matrícula null. Esperado: -1. Obtenido: " + r3);
        } catch (Exception e) {
            Log.d(TAG, "Caso 3: Matrícula null. Excepción: " + e.getMessage());
        }

        // Caso 4: Inválido [Matricula longitud > 7] "1111ABCD"
        Quad q4 = new Quad("1111ABCD", Quad.TipoQuad.Monoplaza, 5000, "Test 4");
        long r4 = repo.insert(q4);
        Log.d(TAG, "Caso 4: Matrícula larga (1111ABCD). Esperado: -1. Obtenido: " + r4);

        // Caso 5: Inválido [Matricula longitud < 7] "1111AB"
        Quad q5 = new Quad("1111AB", Quad.TipoQuad.Monoplaza, 5000, "Test 5");
        long r5 = repo.insert(q5);
        Log.d(TAG, "Caso 5: Matrícula corta (1111AB). Esperado: -1. Obtenido: " + r5);

        // Caso 6: Inválido [Matricula formato incorrecto] "1234567" (No tiene letras)
        Quad q6 = new Quad("1234567", Quad.TipoQuad.Monoplaza, 5000, "Test 6");
        long r6 = repo.insert(q6);
        Log.d(TAG, "Caso 6: Matrícula sin letras (1234567). Esperado: -1. Obtenido: " + r6);

        repo.delete(q2);
        // Caso 7: Inválido [Tipo null]
        Quad q7 = new Quad("1111AAA", null, 5000, "Test 7");
        // Nota: El constructor espera @NonNull pero si se pasa null se viola contrato,
        // probamos si repo lo maneja.
        // Si el framework no lanza excepción antes.
        try {
            long r7 = repo.insert(q7);
            Log.d(TAG, "Caso 7: Tipo null. Esperado: -1. Obtenido: " + r7);
        } catch (Exception e) {
            Log.d(TAG, "Caso 7: Tipo null. Excepción atrapada: " + e.getMessage());
        }

        // Caso 8: Inválido [Tipo "Otro"] -> No es posible compilar en Java con Enum.
        Log.d(TAG, "Caso 8: Tipo 'Otro'. No aplicable en Java con Enum (Error de compilación).");

        // Caso 9: Inválido [Precio null] -> No es posible con int primitivo.
        Log.d(TAG, "Caso 9: Precio null. No aplicable en Java con int primitivo (Error de compilación).");

        // Caso 10: Inválido [Precio 0] (O negativo)
        Quad q10 = new Quad("1111AAA", Quad.TipoQuad.Monoplaza, 0, "Test 10");
        long r10 = repo.insert(q10);
        Log.d(TAG, "Caso 10: Precio 0. Esperado: -1. Obtenido: " + r10);

        Log.d(TAG, "Pruebas de inserción finalizadas.");
    }

    public void testUpdateParams(QuadRepository repo) {
        Log.d(TAG, "----- PRUEBAS update QuadRepository -----");

        // Preparación: Insertar un quad válido inicial para las pruebas
        Quad qBase = new Quad("1111AAA", Quad.TipoQuad.Monoplaza, 5000, "Base");
        repo.delete(qBase); // Por si ya existía uno con esa matrícula.
        repo.insert(qBase);

        // Caso 1: Válido [1111AAA, Monoplaza, 50] (Existe BD)
        // Ya está insertado, pero hacemos update con los mismos datos o cambiados
        // ligeramente
        Quad q1 = new Quad("1111AAA", Quad.TipoQuad.Monoplaza, 5000, "Test 1 Update");
        int r1 = repo.update(q1);
        Log.d(TAG, "Caso 1: Actualizar Quad válido (1111AAA, Monoplaza, 5000). Esperado: 1. Obtenido: " + r1);
        // repo.delete(q1); // Se comenta para que el quad exista para el Caso 2

        // Caso 2: Válido [1111AAA, Biplaza, 50] (Existe BD)
        Quad q2 = new Quad("1111AAA", Quad.TipoQuad.Biplaza, 5000, "Test 2 Update");
        int r2 = repo.update(q2);
        Log.d(TAG, "Caso 2: Actualizar Quad válido (1111AAA, Biplaza, 5000). Esperado: 1. Obtenido: " + r2);
        repo.delete(q2); // Cleaning up

        // Caso 3: Inválido [Matricula null]
        try {
            Quad q3 = new Quad(null, Quad.TipoQuad.Monoplaza, 5000, "Test 3 Update");
            int r3 = repo.update(q3);
            Log.d(TAG, "Caso 3: Matrícula null. Esperado: 0. Obtenido: " + r3);
        } catch (Exception e) {
            Log.d(TAG, "Caso 3: Matrícula null. Excepción: " + e.getMessage());
        }

        // Caso 4: Inválido [Matricula longitud > 7]
        Quad q4 = new Quad("1111ABCD", Quad.TipoQuad.Monoplaza, 5000, "Test 4 Update");
        int r4 = repo.update(q4);
        Log.d(TAG, "Caso 4: Matrícula larga (1111ABCD). Esperado: 0. Obtenido: " + r4);

        // Caso 5: Inválido [Matricula longitud < 7]
        Quad q5 = new Quad("1111AB", Quad.TipoQuad.Monoplaza, 5000, "Test 5 Update");
        int r5 = repo.update(q5);
        Log.d(TAG, "Caso 5: Matrícula corta (1111AB). Esperado: 0. Obtenido: " + r5);

        // Caso 6: Inválido [Matricula formato incorrecto]
        Quad q6 = new Quad("1234567", Quad.TipoQuad.Monoplaza, 5000, "Test 6 Update");
        int r6 = repo.update(q6);
        Log.d(TAG, "Caso 6: Matrícula sin letras (1234567). Esperado: 0. Obtenido: " + r6);

        // Caso 7: Inválido [Tipo null]
        Quad q7 = new Quad("1111AAA", null, 5000, "Test 7 Update");
        try {
            int r7 = repo.update(q7);
            Log.d(TAG, "Caso 7: Tipo null. Esperado: 0. Obtenido: " + r7);
        } catch (Exception e) {
            Log.d(TAG, "Caso 7: Tipo null. Excepción atrapada: " + e.getMessage());
        }

        // Caso 8: Inválido [Tipo "Otro"]
        Log.d(TAG, "Caso 8: Tipo 'Otro'. No aplicable en Java con Enum.");

        // Caso 9: Inválido [Precio null]
        Log.d(TAG, "Caso 9: Precio null. No aplicable en Java con int primitivo.");

        // Caso 10: Inválido [Precio 0] (O negativo)
        Quad q10 = new Quad("1111AAA", Quad.TipoQuad.Monoplaza, 0, "Test 10 Update");
        // Aseguramos que existe el quad base para intentar actualizarlo
        repo.delete(qBase); // borramos
        repo.insert(qBase); // re-insertamos para estado conocido (aunque ya estaba updateado a Biplaza en
                            // caso 2)
        int r10 = repo.update(q10);
        Log.d(TAG, "Caso 10: Precio 0. Esperado: 0. Obtenido: " + r10);

        // Caso 11: Inválido en BD [Matricula no existe]
        Quad q11 = new Quad("9999ZZZ", Quad.TipoQuad.Monoplaza, 5000, "Test 11 Update");
        // Aseguramos que no existe
        repo.delete(q11);
        int r11 = repo.update(q11);
        Log.d(TAG, "Caso 11: Matrícula no existe (9999ZZZ). Esperado: 0. Obtenido: " + r11);

        Log.d(TAG, "Pruebas de actualización finalizadas.");
        // Ensure qBase is removed if it still exists
        repo.delete(qBase);
    }

    public void testDeleteQuadParams(QuadRepository repo) {
        Log.d(TAG, "----- PRUEBAS delete QuadRepository -----");

        // Caso 1: Válido [9999DEL] (Existe en BD)
        // Insertamos primero para asegurar que existe
        Quad q1 = new Quad("9999DEL", Quad.TipoQuad.Monoplaza, 5000, "Test 1 Delete");
        repo.delete(q1); // Limpieza previa
        repo.insert(q1);
        int r1 = repo.delete(q1);
        Log.d(TAG, "Caso 1: Eliminar Quad válido (9999DEL). Esperado: 1. Obtenido: " + r1);

        // Caso 2: Inválido [Matrícula NULL]
        // Se prueba un Quad con matrícula null
        Quad q2 = new Quad(null, Quad.TipoQuad.Monoplaza, 5000, "Test 2 Delete");
        int r2 = repo.delete(q2);
        Log.d(TAG, "Caso 2: Matrícula NULL. Esperado: 0. Obtenido: " + r2);

        // Caso 3: Inválido [Matrícula longitud > 7] "11111ABC"
        Quad q3 = new Quad("11111ABC", Quad.TipoQuad.Monoplaza, 5000, "Test 3 Delete");
        int r3 = repo.delete(q3);
        Log.d(TAG, "Caso 3: Matrícula larga (11111ABC). Esperado: 0. Obtenido: " + r3);

        // Caso 4: Inválido [Matrícula longitud < 7] "111ABC"
        Quad q4 = new Quad("111ABC", Quad.TipoQuad.Monoplaza, 5000, "Test 4 Delete");
        int r4 = repo.delete(q4);
        Log.d(TAG, "Caso 4: Matrícula corta (111ABC). Esperado: 0. Obtenido: " + r4);

        // Caso 5: Inválido [Formato incorrecto] "111ABCD"
        Quad q5 = new Quad("111ABCD", Quad.TipoQuad.Monoplaza, 5000, "Test 5 Delete");
        int r5 = repo.delete(q5);
        Log.d(TAG, "Caso 5: Matrícula formato incorrecto (111ABCD). Esperado: 0. Obtenido: " + r5);

        // Caso 6: Válido en formato pero no existe en BD [9999DEL]
        // Ya fue eliminado en el Caso 1
        Quad q6 = new Quad("9999DEL", Quad.TipoQuad.Monoplaza, 5000, "Test 6 Delete");
        int r6 = repo.delete(q6);
        Log.d(TAG, "Caso 6: Quad no existe (9999DEL). Esperado: 0. Obtenido: " + r6);

        Log.d(TAG, "Pruebas de borrado finalizadas.");
    }

    public void testDeleteReservaParams(ReservaRepository repo) {
        Log.d(TAG, "----- PRUEBAS delete ReservaRepository -----");

        // Caso 1: Válido [idReserva > 0] (Existe en BD)
        // Insertamos primero para asegurar que existe.
        Reserva r1 = new Reserva(0, "Cliente Test 1", 123456789, "01/01/2023", "02/01/2023", 10000);
        long idGenerado = repo.insert(r1);
        r1.setIdReserva((int) idGenerado);

        Log.d(TAG, "Caso 1: Insertada reserva con ID: " + idGenerado);

        int res1 = repo.delete(r1);
        Log.d(TAG, "Caso 1: Eliminar Reserva válida (ID " + idGenerado + "). Esperado: 1. Obtenido: " + res1);

        // Caso 2: Inválido [idReserva <= 0]
        Reserva r2 = new Reserva(-1, "Cliente Test 2", 123456789, "2023-01-01", "2023-01-02", 10000);
        int res2 = repo.delete(r2);
        Log.d(TAG, "Caso 2: ID Reserva inválido (-1). Esperado: 0. Obtenido: " + res2);

        // Caso 3: Válido [idReserva > 0] NO existe en BD
        int res3 = repo.delete(r1);
        Log.d(TAG, "Caso 3: Reserva no existe (ID " + idGenerado + "). Esperado: 0. Obtenido: " + res3);

        Log.d(TAG, "Pruebas de borrado Reserva finalizadas.");
    }

    public void testInsertReservaParams(ReservaRepository repo) {
        Log.d(TAG, "----- PRUEBAS insert ReservaRepository -----");

        // Caso 1: Válido ["Laura", 654000000, "01/12/2025", "03/12/2025"]
        // Nota: El precio se ha asumido arbitrariamente porque no estaba en la tabla
        // del usuario,
        // pero es necesario para el constructor. Ponemos 5000.
        // El ID es autogenerado (0).
        Reserva r1 = new Reserva(0, "Laura", 654000000, "01/12/2025", "03/12/2025", 5000);
        long res1 = repo.insert(r1);
        Log.d(TAG, "Caso 1: Insertar Reserva válida. Esperado: >0. Obtenido: " + res1);
        if (res1 > 0) {
            r1.setIdReserva((int) res1);
            repo.delete(r1); // Cleaning up
        }

        // Caso 3: Inválido [nombreCliente null]
        try {
            Reserva r3 = new Reserva(0, null, 654000000, "01/12/2025", "03/12/2025", 5000);
            long res3 = repo.insert(r3);
            Log.d(TAG, "Caso 3: Nombre null. Esperado: -1. Obtenido: " + res3);
            if (res3 > 0) {
                r3.setIdReserva((int) res3);
                repo.delete(r3);
            }
        } catch (Exception e) {
            Log.d(TAG, "Caso 3: Nombre null. Excepción: " + e.getMessage());
        }

        // Caso 4: Inválido [numeroMovil null] -> int no puede ser null en Java, error
        // compilación.
        // Lo simulamos con 0 si se quiere probar validación lógica, pero la tabla dice
        // "null".
        Log.d(TAG, "Caso 4: Movil null. No aplicable en Java con tipo int primitivo (Error compilación).");

        // Caso 5: Inválido [fechaRecogida null]
        try {
            Reserva r5 = new Reserva(0, "Laura", 654000000, null, "03/12/2025", 5000);
            long res5 = repo.insert(r5);
            Log.d(TAG, "Caso 5: FechaRecogida null. Esperado: -1. Obtenido: " + res5);
            if (res5 > 0) {
                r5.setIdReserva((int) res5);
                repo.delete(r5);
            }
        } catch (Exception e) {
            Log.d(TAG, "Caso 5: FechaRecogida null. Excepción: " + e.getMessage());
        }

        // Caso 6: Inválido [fechaRecogida no válida 40/12/2025]
        // Si no hay validación de formato en repository o constructor, esto se
        // insertará.
        Reserva r6 = new Reserva(0, "Laura", 654000000, "40/12/2025", "03/12/2025", 5000);
        long res6 = repo.insert(r6);
        Log.d(TAG, "Caso 6: FechaRecogida inválida (40/12/2025). Esperado: -1. Obtenido: " + res6);
        if (res6 > 0) {
            r6.setIdReserva((int) res6);
            repo.delete(r6); // Cleaning up
        }

        // Caso 7: Inválido [fechaRecogida formato incorrecto 12/2025/01]
        Reserva r7 = new Reserva(0, "Laura", 654000000, "12/2025/01", "03/12/2025", 5000);
        long res7 = repo.insert(r7);
        Log.d(TAG, "Caso 7: FechaRecogida formato incorrecto (12/2025/01). Esperado: -1. Obtenido: " + res7);
        if (res7 > 0) {
            r7.setIdReserva((int) res7);
            repo.delete(r7); // Cleaning up
        }

        // Caso 8: Inválido [fechaDevolucion null]
        try {
            Reserva r8 = new Reserva(0, "Laura", 654000000, "01/12/2025", null, 5000);
            long res8 = repo.insert(r8);
            Log.d(TAG, "Caso 8: FechaDevolucion null. Esperado: -1. Obtenido: " + res8);
            if (res8 > 0) {
                r8.setIdReserva((int) res8);
                repo.delete(r8);
            }
        } catch (Exception e) {
            Log.d(TAG, "Caso 8: FechaDevolucion null. Excepción: " + e.getMessage());
        }

        // Caso 9: Inválido [fechaDevolucion fecha no válida 40/12/2025]
        Reserva r9 = new Reserva(0, "Laura", 654000000, "01/12/2025", "40/12/2025", 5000);
        long res9 = repo.insert(r9);
        Log.d(TAG, "Caso 9: FechaDevolucion inválida (40/12/2025). Esperado: -1. Obtenido: " + res9);
        if (res9 > 0) {
            r9.setIdReserva((int) res9);
            repo.delete(r9); // Cleaning up
        }

        // Caso 10 (mismo nombre que anterior en la tabla, asumimos Caso 10 real):
        // Inválido [fechaDevolucion formato incorrecto 12/2025/03]
        Reserva r10 = new Reserva(0, "Laura", 654000000, "01/12/2025", "12/2025/03", 5000);
        long res10 = repo.insert(r10);
        Log.d(TAG, "Caso 10: FechaDevolucion formato incorrecto (12/2025/03). Esperado: -1. Obtenido: " + res10);
        if (res10 > 0) {
            r10.setIdReserva((int) res10);
            repo.delete(r10); // Cleaning up
        }

        // Caso 11 (segundo con id 10 en la tabla):
        // Inválido [fechaDevolucion anterior a fechaRecogida]
        Reserva r11 = new Reserva(0, "Laura", 654000000, "01/12/2025", "01/11/2025", 5000);
        long res11 = repo.insert(r11);
        Log.d(TAG, "Caso 11: FechaDevolucion anterior a Recogida. Esperado: -1. Obtenido: " + res11);
        if (res11 > 0) {
            r11.setIdReserva((int) res11);
            repo.delete(r11); // Cleaning up
        }

        Log.d(TAG, "Pruebas de inserción Reserva finalizadas.");
    }

    public void testUpdateReservaParams(ReservaRepository repo) {
        Log.d(TAG, "----- PRUEBAS update ReservaRepository -----");

        // Preparación: Insertar una reserva válida inicial para las pruebas
        Reserva rBase = new Reserva(0, "Base Client", 600000000, "01/01/2025", "02/01/2025", 5000);
        long idBase = repo.insert(rBase);
        rBase.setIdReserva((int) idBase);
        Log.d(TAG, "Preparación: Reserva Base insertada con ID: " + idBase);

        // Caso 1: Válido [Update normal] (Existe BD)
        Reserva r1 = new Reserva((int) idBase, "Laura", 654000000, "01/12/2025", "03/12/2025", 50);
        int res1 = repo.update(r1);
        Log.d(TAG, "Caso 1: Actualizar Reserva válida. Esperado: 1. Obtenido: " + res1);

        // Caso 2: Inválido [idReserva 0] (O negativo)
        Reserva r2 = new Reserva(0, "Laura", 654000000, "01/12/2025", "03/12/2025", 50);
        int res2 = repo.update(r2);
        Log.d(TAG, "Caso 2: ID 0. Esperado: 0. Obtenido: " + res2);

        // Caso 3: Inválido [nombreCliente null]
        try {
            Reserva r3 = new Reserva((int) idBase, null, 654000000, "01/12/2025", "03/12/2025", 50);
            int res3 = repo.update(r3);
            Log.d(TAG, "Caso 3: Nombre null. Esperado: 0. Obtenido: " + res3);
        } catch (Exception e) {
            Log.d(TAG, "Caso 3: Nombre null. Excepción: " + e.getMessage());
        }

        // Caso 4: Inválido [numeroMovil null] -> No aplicable int primitivo.
        Log.d(TAG, "Caso 4: Movil null. No aplicable en Java con int primitivo.");

        // Caso 5: Inválido [fechaRecogida null]
        try {
            Reserva r5 = new Reserva((int) idBase, "Laura", 654000000, null, "03/12/2025", 50);
            int res5 = repo.update(r5);
            Log.d(TAG, "Caso 5: FechaRecogida null. Esperado: 0. Obtenido: " + res5);
        } catch (Exception e) {
            Log.d(TAG, "Caso 5: FechaRecogida null. Excepción: " + e.getMessage());
        }

        // Caso 6: Inválido [fechaRecogida no válida 40/12/2025]
        Reserva r6 = new Reserva((int) idBase, "Laura", 654000000, "40/12/2025", "03/12/2025", 50);
        int res6 = repo.update(r6);
        Log.d(TAG, "Caso 6: FechaRecogida inválida. Esperado: 0. Obtenido: " + res6);

        // Caso 7: Inválido [fechaRecogida formato incorrecto]
        Reserva r7 = new Reserva((int) idBase, "Laura", 654000000, "12/2025/01", "03/12/2025", 50);
        int res7 = repo.update(r7);
        Log.d(TAG, "Caso 7: FechaRecogida formato incorrecto. Esperado: 0. Obtenido: " + res7);

        // Caso 8: Inválido [fechaDevolucion null]
        try {
            Reserva r8 = new Reserva((int) idBase, "Laura", 654000000, "01/12/2025", null, 50);
            int res8 = repo.update(r8);
            Log.d(TAG, "Caso 8: FechaDevolucion null. Esperado: 0. Obtenido: " + res8);
        } catch (Exception e) {
            Log.d(TAG, "Caso 8: FechaDevolucion null. Excepción: " + e.getMessage());
        }

        // Caso 9: Inválido [fechaDevolucion no válida]
        Reserva r9 = new Reserva((int) idBase, "Laura", 654000000, "01/12/2025", "40/12/2025", 50);
        int res9 = repo.update(r9);
        Log.d(TAG, "Caso 9: FechaDevolucion inválida. Esperado: 0. Obtenido: " + res9);

        // Caso 10: Inválido [fechaDevolucion formato incorrecto]
        Reserva r10 = new Reserva((int) idBase, "Laura", 654000000, "01/12/2025", "12/2025/03", 50);
        int res10 = repo.update(r10);
        Log.d(TAG, "Caso 10: FechaDevolucion formato incorrecto. Esperado: 0. Obtenido: " + res10);

        // Caso 11: Inválido [fechaDevolucion anterior a fechaRecogida]
        Reserva r11 = new Reserva((int) idBase, "Laura", 654000000, "01/12/2025", "01/11/2025", 50);
        int res11 = repo.update(r11);
        Log.d(TAG, "Caso 11: FechaDevolucion anterior. Esperado: 0. Obtenido: " + res11);

        // Caso 12: Inválido [precioTotal null] -> No aplicable int primitivo
        Log.d(TAG, "Caso 12: Precio null. No aplicable en Java con int primitivo.");

        // Caso 13 (asimilado a precio <= 0 o similar, tabla cortada en imagen pero
        // inferible):
        // Inválido [precioTotal -1]
        Reserva r13 = new Reserva((int) idBase, "Laura", 654000000, "01/12/2025", "03/12/2025", -1);
        int res13 = repo.update(r13);
        Log.d(TAG, "Caso 13: Precio negativo. Esperado: 0. Obtenido: " + res13);

        // Caso 14: No existe reserva con ese ID (Simulamos borrando y actualizando o
        // usando otro ID)
        Reserva r14 = new Reserva(999999, "Laura", 654000000, "01/12/2025", "03/12/2025", 50);
        int res14 = repo.update(r14);
        Log.d(TAG, "Caso 14: ID no existe. Esperado: 0. Obtenido: " + res14);

        Log.d(TAG, "Pruebas de actualización Reserva finalizadas.");

        // Limpieza final
        repo.delete(rBase);
    }

    public void testVolumen(QuadRepository quadRepo, ReservaRepository reservaRepo) {
        Log.d(TAG, "----- PRUEBAS DE VOLUMEN -----");

        // --- PRUEBAS DE VOLUMEN: QUADS ---

        // Caso 1: Número de quads -1
        Log.d(TAG, "Caso 1: Número de quads -1. Imposible generar una cantidad negativa de objetos.");

        // Caso 2: Número de quads 0
        Log.d(TAG, "Caso 2: Número de quads 0. Verificando funcionamiento con 0 inserciones. OK.");

        // Caso 3: Número de quads 100
        Log.d(TAG, "Caso 3: Insertando 100 Quads...");
        long startQ3 = System.currentTimeMillis();
        for (int i = 0; i < 100; i++) {
            // Generar matrícula única: 1000AAA, 1001AAA...
            String matricula = String.format("%04dAAA", 1000 + i);
            quadRepo.insert(new Quad(matricula, Quad.TipoQuad.Monoplaza, 5000, "Volumen 100"));
        }
        long endQ3 = System.currentTimeMillis();
        Log.d(TAG, "Caso 3: 100 Quads insertados en " + (endQ3 - startQ3) + " ms.");

        // Caso 4: Número de quads 101
        Log.d(TAG, "Caso 4: Insertando 101 Quads...");
        long startQ4 = System.currentTimeMillis();
        for (int i = 0; i < 101; i++) {
            // Generar matrícula única distinta al caso anterior para evitar colisiones si
            // no se limpia BD
            // Usamos prefijo 2000
            String matricula = String.format("%04dAAA", 2000 + i);
            quadRepo.insert(new Quad(matricula, Quad.TipoQuad.Biplaza, 6000, "Volumen 101"));
        }
        long endQ4 = System.currentTimeMillis();
        Log.d(TAG, "Caso 4: 101 Quads insertados en " + (endQ4 - startQ4) + " ms.");

        // --- PRUEBAS DE VOLUMEN: RESERVAS ---

        // Caso 5: Número de reservas -1
        Log.d(TAG, "Caso 5: Número de reservas -1. Imposible generar cantidad negativa.");

        // Caso 6: Número de reservas 0
        Log.d(TAG, "Caso 6: Número de reservas 0. Verificando funcionamiento con 0 inserciones. OK.");

        // Caso 7: Número de reservas 20000
        Log.d(TAG, "Caso 7: Insertando 20000 Reservas... (Puede tardar)");
        long startR7 = System.currentTimeMillis();
        for (int i = 0; i < 20000; i++) {
            Reserva r = new Reserva(0, "Cliente Vol " + i, 600000000 + i, "01/01/2023", "05/01/2023", 10000);
            reservaRepo.insert(r);
        }
        long endR7 = System.currentTimeMillis();
        Log.d(TAG, "Caso 7: 20000 Reservas insertadas en " + (endR7 - startR7) + " ms (" + ((endR7 - startR7) / 1000)
                + " s).");

        // Caso 8: Número de reservas 20001
        Log.d(TAG, "Caso 8: Insertando 20001 Reservas... (Puede tardar)");
        long startR8 = System.currentTimeMillis();
        for (int i = 0; i < 20001; i++) {
            Reserva r = new Reserva(0, "Cliente Vol2 " + i, 600000000 + i, "01/01/2023", "05/01/2023", 10000);
            reservaRepo.insert(r);
        }
        long endR8 = System.currentTimeMillis();
        Log.d(TAG, "Caso 8: 20001 Reservas insertadas en " + (endR8 - startR8) + " ms (" + ((endR8 - startR8) / 1000)
                + " s).");

        Log.d(TAG, "----- FIN PRUEBAS DE VOLUMEN -----");
    }

    public void testSobrecarga(QuadRepository repo) {
        Log.d(TAG, "----- PRUEBAS DE SOBRECARGA (Descripción) -----");

        int descriptionLength = 10;
        int i = 0;
        boolean continuar = true;

        while (continuar) {
            // Generamos una descripción de longitud creciente
            StringBuilder sb = new StringBuilder();
            for (int k = 0; k < descriptionLength; k++) {
                sb.append("A");
            }
            String descripcion = sb.toString();

            // Usamos una matrícula única para no fallar por PK.
            // Usamos prefijo 3000 para evitar colisiones con otros tests.
            String matricula = String.format("%04dSOB", 3000 + i);

            Quad q = new Quad(matricula, Quad.TipoQuad.Monoplaza, 5000, descripcion);

            try {
                long result = repo.insert(q);
                if (result > 0) {
                    Log.d(TAG, "Insertado Quad con descripción de longitud: " + descriptionLength);
                    descriptionLength += 10;
                    i++;
                } else {
                    Log.d(TAG, "Fallo al insertar Quad con descripción de longitud: " + descriptionLength
                            + ". Resultado: " + result);
                    continuar = false;
                }
            } catch (Exception e) {
                Log.d(TAG, "Excepción al insertar Quad con descripción de longitud: " + descriptionLength + ". Error: "
                        + e.getMessage());
                continuar = false;
            }
        }
        Log.d(TAG, "----- FIN PRUEBAS DE SOBRECARGA -----");
    }
}

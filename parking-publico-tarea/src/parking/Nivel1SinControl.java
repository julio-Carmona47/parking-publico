package parking;

import java.util.ArrayList;
import java.util.List;

/**
 * NIVEL 1 · Un parking SIN sincronizar (condición de carrera).
 *
 * El parking tiene 3 plazas y llegan 8 coches a la vez. Cada coche:
 *   1. mira el panel: ¿quedan plazas libres?
 *   2. si quedan, entra y resta una plaza
 *   3. está aparcado un rato, paga y sale (suma una plaza)
 *
 * Los pasos 1 y 2 NO son atómicos: entre mirar y entrar pasa un instante
 * (el conductor duda). Si dos coches miran a la vez, los dos ven «1 libre»
 * y entran los dos. Resultado: más coches dentro que plazas, plazas negativas
 * y una caja que no cuadra.
 *
 * Ejecuta este nivel varias veces: cada vez sale distinto. ESO es una condición de carrera.
 */
public class Nivel1SinControl {

    /** El parking: estado compartido por todos los hilos, sin ninguna protección. */
    static class Parking {
        private final int capacidad;
        private int libres;
        private int dentro = 0;        // coches aparcados ahora mismo
        private int maxDentro = 0;     // máximo que ha llegado a haber a la vez
        private int recaudado = 0;     // euros en caja

        Parking(int capacidad) {
            this.capacidad = capacidad;
            this.libres = capacidad;
        }

        /** Intenta entrar. Devuelve false si el parking está lleno. */
        boolean entrar() {
            if (libres > 0) {                 // 1. comprobar...
                Util.dormir(5);               //    (el conductor duda: aquí se cuela otro coche)
                libres--;                     // 2. ...y actuar. Entre 1 y 2 otro hilo puede cambiar libres
                dentro++;
                if (dentro > maxDentro) maxDentro = dentro;
                return true;
            }
            return false;
        }

        void salir(int euros) {
            int caja = recaudado;             // leer
            Util.dormir(10);                  // (el cajero tarda en contar)
            recaudado = caja + euros;         // escribir: si otro hilo pagó entre medias, su pago se pierde
            dentro--;
            libres++;
        }
    }

    public static void main(String[] args) throws InterruptedException {
        final int PLAZAS = 3, COCHES = 8, TARIFA = 2;
        Parking parking = new Parking(PLAZAS);
        int[] aparcaron = new int[1];         // cuántos consiguieron entrar (solo lo escribe main, al final)
        List<Thread> coches = new ArrayList<>();
        boolean[] entro = new boolean[COCHES];

        for (int i = 0; i < COCHES; i++) {
            final int n = i;
            Thread coche = new Thread(() -> {
                if (parking.entrar()) {
                    entro[n] = true;
                    Util.log("entra. Libres según el panel: " + parking.libres);
                    Util.dormir(Util.azar(100, 300));          // aparcado
                    parking.salir(TARIFA);
                    Util.log("paga " + TARIFA + " € y sale");
                } else {
                    Util.log("parking lleno: se va");
                }
            }, "Coche-" + (i + 1));
            coches.add(coche);
        }
        coches.forEach(Thread::start);        // todos llegan a la vez
        for (Thread c : coches) c.join();      // main espera a que todos terminen

        for (boolean b : entro) if (b) aparcaron[0]++;
        System.out.println("\n===== CIERRE DEL NIVEL 1 =====");
        System.out.println("Coches que aparcaron: " + aparcaron[0] + " · máximo a la vez: " + parking.maxDentro + " (plazas: " + PLAZAS + ")");
        System.out.println("Recaudado: " + parking.recaudado + " € (debería ser " + aparcaron[0] * TARIFA + " €)");
        Util.comprobar("Nunca hubo más coches que plazas", parking.maxDentro <= PLAZAS);
        Util.comprobar("La caja cuadra", parking.recaudado == aparcaron[0] * TARIFA);
        Util.comprobar("Al cerrar quedan " + PLAZAS + " plazas libres (quedan " + parking.libres + ")", parking.libres == PLAZAS);
    }
}

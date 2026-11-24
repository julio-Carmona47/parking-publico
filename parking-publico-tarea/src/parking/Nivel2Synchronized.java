package parking;

import java.util.ArrayList;
import java.util.List;

/**
 * NIVEL 2 · El parking como MONITOR con synchronized (exclusión mutua).
 *
 * >>> TAREA · TODO 2 <<<
 * Este código es el del nivel 1 (sin protección). Ejecútalo: las comprobaciones salen en ERROR.
 * Conviértelo en un monitor para que salgan todas en OK. Sigue el apartado 4 de la guía.
 *
 * Mismo parking y mismos coches que el nivel 1, con un único cambio:
 * los métodos que tocan el estado compartido son synchronized.
 *
 * Cada objeto Java tiene un cerrojo (monitor). Para ejecutar un método synchronized
 * hay que tener el cerrojo del objeto: si lo tiene otro hilo, se espera en estado BLOCKED.
 * Así «comprobar y actuar» se hacen de una vez, sin que nadie se cuele entre medias.
 *
 * Limitación: si el parking está lleno, el coche se va. ¿Y si queremos que espere a
 * que salga alguien? Eso es el nivel 3 (semáforos) y el nivel 4 (wait/notifyAll).
 */
public class Nivel2Synchronized {

    static class Parking {
        private final int capacidad;
        private int libres;
        private int dentro = 0;
        private int maxDentro = 0;
        private int recaudado = 0;

        Parking(int capacidad) {
            this.capacidad = capacidad;
            this.libres = capacidad;
        }

        /**
         * Comprobar y actuar dentro de la MISMA sección crítica.
         * Devuelve las plazas que quedan tras entrar, o -1 si estaba lleno.
         * (Se devuelve aquí dentro porque, si lo leyéramos después con getLibres(),
         * otro coche podría haber cambiado el valor entre medias.)
         */
        // TODO 2a: comprobar y actuar deben ser una sola sección crítica
        int entrar() {
            if (libres > 0) {
                Util.dormir(5);        // el conductor duda (no lo borres: simula el tiempo entre mirar y entrar)
                libres--;
                dentro++;
                maxDentro = Math.max(maxDentro, dentro);
                return libres;
            }
            return -1;
        }

        // TODO 2b: pagar y devolver la plaza también tocan el estado compartido
        void salir(int euros) {
            int caja = recaudado;
            Util.dormir(10);               // (el cajero tarda en contar)
            recaudado = caja + euros;
            dentro--;
            libres++;
        }

        // TODO 2c: ¿y los getters? Piensa en la inconsistencia de memoria
        int getLibres() { return libres; }
        int getMaxDentro() { return maxDentro; }
        int getRecaudado() { return recaudado; }
    }

    public static void main(String[] args) throws InterruptedException {
        final int PLAZAS = 3, COCHES = 8, TARIFA = 2;
        Parking parking = new Parking(PLAZAS);
        boolean[] entro = new boolean[COCHES];
        List<Thread> coches = new ArrayList<>();

        for (int i = 0; i < COCHES; i++) {
            final int n = i;
            coches.add(new Thread(() -> {
                int quedan = parking.entrar();
                if (quedan >= 0) {
                    entro[n] = true;
                    Util.log("entra. Libres: " + quedan);
                    Util.dormir(Util.azar(100, 300));
                    parking.salir(TARIFA);
                    Util.log("paga " + TARIFA + " € y sale");
                } else {
                    Util.log("parking lleno: se va");
                }
            }, "Coche-" + (i + 1)));
        }
        coches.forEach(Thread::start);
        for (Thread c : coches) c.join();

        int aparcaron = 0;
        for (boolean b : entro) if (b) aparcaron++;
        System.out.println("\n===== CIERRE DEL NIVEL 2 =====");
        System.out.println("Coches que aparcaron: " + aparcaron + " · se fueron sin aparcar: " + (COCHES - aparcaron));
        Util.comprobar("Nunca hubo más coches que plazas (máximo " + parking.getMaxDentro() + "/" + PLAZAS + ")", parking.getMaxDentro() <= PLAZAS);
        Util.comprobar("La caja cuadra (" + parking.getRecaudado() + " €)", parking.getRecaudado() == aparcaron * TARIFA);
        Util.comprobar("Al cerrar quedan " + PLAZAS + " plazas libres", parking.getLibres() == PLAZAS);
        System.out.println("  PERO   " + (COCHES - aparcaron) + " coches se han ido sin aparcar: nadie espera su turno.");
    }
}

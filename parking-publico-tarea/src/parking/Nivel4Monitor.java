package parking;

import java.util.ArrayList;
import java.util.List;

/**
 * NIVEL 4 · El mismo parking con un MONITOR hecho a mano: synchronized + wait() + notifyAll().
 *
 * >>> TAREA · TODO 4 <<<
 * Completa los TODO 4a a 4c siguiendo el apartado 6 de la guía. NO uses semáforos en este nivel.
 * El panel luminoso ya está hecho: úsalo para ver los estados de los coches.
 *
 * Hace lo mismo que el semáforo del nivel 3 (los coches esperan a que haya plaza),
 * pero programado por nosotros. Así se ve qué hace un semáforo «por dentro».
 *
 * Reglas de oro del monitor:
 *   1. Todo el estado compartido es privado y se toca solo en métodos synchronized.
 *   2. Se espera una condición con  while (no se cumple) wait();   ¡while, nunca if!
 *      wait() SUELTA el cerrojo mientras duerme y lo recupera al despertar.
 *   3. Quien cambia el estado avisa con notifyAll(). No suelta el cerrojo hasta salir del método.
 *
 * Además hay un PANEL LUMINOSO: un hilo demonio que cada 150 ms muestra las plazas libres
 * y el estado (Thread.State) de cada coche.
 */
public class Nivel4Monitor {

    static class Parking {
        private final int capacidad;
        private int libres;
        private int maxDentro = 0;
        private int recaudado = 0;

        Parking(int capacidad) {
            this.capacidad = capacidad;
            this.libres = capacidad;
        }

        /** Espera hasta que haya una plaza libre y la ocupa. Devuelve las plazas que quedan. */
        int entrar() throws InterruptedException {
            // TODO 4a: mientras no haya plazas libres, el coche espera.
            //          Antes de cada espera escribe: Util.log("espera: parking lleno");
            libres--;
            maxDentro = Math.max(maxDentro, capacidad - libres);
            return libres;
        }

        /** Libera la plaza, cobra y avisa a los que esperan. Devuelve las plazas libres. */
        int salir(int euros) {
            recaudado += euros;
            libres++;
            // TODO 4b: avisa a los coches que esperan plaza
            return libres;
        }

        // TODO 4c: un monitor protege TODO su estado. Revisa todos los métodos de esta clase.
        int getLibres() { return libres; }
        int getMaxDentro() { return maxDentro; }
        int getRecaudado() { return recaudado; }
    }

    public static void main(String[] args) throws InterruptedException {
        final int PLAZAS = 3, COCHES = 8, TARIFA = 2;
        Parking parking = new Parking(PLAZAS);
        List<Thread> coches = new ArrayList<>();

        for (int i = 1; i <= COCHES; i++) {
            coches.add(new Thread(() -> {
                try {
                    int quedan = parking.entrar();
                    Util.log("ENTRA. Libres: " + quedan);
                    Util.dormir(Util.azar(200, 500));
                    quedan = parking.salir(TARIFA);
                    Util.log("paga y SALE. Libres: " + quedan);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }, "Coche-" + i));
        }

        // Panel luminoso: hilo DEMONIO (no impide que el programa termine)
        Thread panel = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                StringBuilder sb = new StringBuilder("LIBRES " + parking.getLibres() + " |");
                for (Thread c : coches) sb.append(' ').append(c.getName().replace("Coche-", "C")).append('=').append(abreviar(c.getState()));
                Util.log(sb.toString());
                Util.dormir(150);
            }
        }, "Panel");
        panel.setDaemon(true);
        panel.start();

        coches.forEach(Thread::start);
        for (Thread c : coches) c.join();
        panel.interrupt();                              // apagamos el panel de forma ordenada
        panel.join();

        System.out.println("\n===== CIERRE DEL NIVEL 4 =====");
        Util.comprobar("Han aparcado los " + COCHES + " coches (recaudado " + parking.getRecaudado() + " €)", parking.getRecaudado() == COCHES * TARIFA);
        Util.comprobar("Nunca hubo más coches que plazas (máximo " + parking.getMaxDentro() + "/" + PLAZAS + ")", parking.getMaxDentro() <= PLAZAS);
        Util.comprobar("Al cerrar quedan " + PLAZAS + " plazas libres", parking.getLibres() == PLAZAS);
    }

    static String abreviar(Thread.State s) {
        return switch (s) {
            case NEW -> "NEW";
            case RUNNABLE -> "RUN";
            case BLOCKED -> "BLK";
            case WAITING -> "WAIT";
            case TIMED_WAITING -> "TWAIT";
            case TERMINATED -> "FIN";
        };
    }
}

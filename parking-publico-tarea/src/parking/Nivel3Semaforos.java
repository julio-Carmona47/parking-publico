package parking;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * NIVEL 3 · Semáforos: los coches ESPERAN su turno.
 *
 * >>> TAREA · TODO 3 <<<
 * Completa los TODO 3a a 3g siguiendo el apartado 5 de la guía. Hasta que lo hagas,
 * entran todos los coches a la vez y las comprobaciones salen en ERROR.
 *
 * Dos semáforos, cada uno con un papel distinto:
 *
 *   plazas  = new Semaphore(PLAZAS, true)   semáforo CONTADOR: hay N plazas.
 *             acquire() resta una plaza; si no quedan, el coche espera (WAITING).
 *             release() devuelve la plaza y despierta al primero de la cola.
 *             true = semáforo JUSTO: atiende por orden de llegada (nadie se cuela).
 *
 *   cajero  = new Semaphore(1)              semáforo BINARIO: un único cajero automático.
 *             Solo un conductor paga a la vez: funciona como un cerrojo.
 *
 * Y la caja (el dinero) es un monitor con synchronized: semáforo y synchronized se combinan.
 *
 * Además, el último coche es IMPACIENTE: usa tryAcquire con tiempo máximo y,
 * si no consigue plaza en 400 ms, se va.
 */
public class Nivel3Semaforos {

    /** La caja registradora: monitor con synchronized. */
    static class Caja {
        private int recaudado = 0;
        private int pagos = 0;
        // TODO 3g: la caja la usan todos los hilos: protégela como monitor
        void cobrar(int euros) { recaudado += euros; pagos++; }
        int getRecaudado() { return recaudado; }
        int getPagos() { return pagos; }
    }

    static final int PLAZAS = 3, COCHES = 8, TARIFA = 2;
    // TODO 3a: crea el semáforo de las plazas: contador, con PLAZAS permisos y JUSTO
    static Semaphore plazas = null;
    // TODO 3b: crea el semáforo del cajero: binario (un solo cajero)
    static Semaphore cajero = null;

    /** Plazas libres ahora mismo (-1 si el semáforo aún no existe). */
    static int libres() { return plazas == null ? -1 : plazas.availablePermits(); }
    static final Caja caja = new Caja();

    // Solo para COMPROBAR que el semáforo funciona: cuántos hay dentro y el máximo.
    private static int dentro = 0, maxDentro = 0;
    private static synchronized void contarEntrada() { dentro++; maxDentro = Math.max(maxDentro, dentro); }
    private static synchronized void contarSalida() { dentro--; }
    private static synchronized int getMaxDentro() { return maxDentro; }

    /** Lo que hace cada coche. */
    static void aparcar(boolean impaciente) throws InterruptedException {
        if (impaciente) {
            Util.log("llega con prisa: espera como mucho 400 ms");
            // TODO 3c: intenta coger plaza esperando como MUCHO 400 ms.
            //          Si no lo consigue: Util.log("se cansa de esperar y se va"); y return;
        } else {
            Util.log("llega a la barrera. Plazas libres: " + libres());
            // TODO 3d: coge una plaza. Si no hay, el coche tiene que esperar aquí
        }
        // TODO 3e: desde aquí el coche tiene una plaza y hay que devolverla SIEMPRE,
        //          aunque salte una excepción. Reorganiza este bloque.
        contarEntrada();
        Util.log("ENTRA. Plazas libres: " + libres());
        Util.dormir(Util.azar(200, 500));                       // aparcado

        // TODO 3f: solo hay un cajero: un conductor paga cada vez, y el cajero se libera siempre
        Util.log("paga " + TARIFA + " € en el cajero");
        Util.dormir(50);
        caja.cobrar(TARIFA);

        contarSalida();
        // (TODO 3e) aquí se devuelve la plaza
        Util.log("SALE. Plazas libres: " + libres());
    }

    public static void main(String[] args) throws InterruptedException {
        List<Thread> coches = new ArrayList<>();
        for (int i = 1; i <= COCHES; i++) {
            final boolean impaciente = (i == COCHES);
            coches.add(new Thread(() -> {
                try {
                    aparcar(impaciente);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    Util.log("interrumpido");
                }
            }, "Coche-" + i));
        }
        for (Thread c : coches) {
            c.start();
            Util.dormir(20);              // llegan casi a la vez, pero en orden: así se ve que el semáforo justo respeta la cola
        }
        for (Thread c : coches) c.join();

        System.out.println("\n===== CIERRE DEL NIVEL 3 =====");
        System.out.println("Pagos: " + caja.getPagos() + " · recaudado: " + caja.getRecaudado() + " €");
        Util.comprobar("Nunca hubo más coches que plazas (máximo " + getMaxDentro() + "/" + PLAZAS + ")", getMaxDentro() <= PLAZAS);
        Util.comprobar("La caja cuadra", caja.getRecaudado() == caja.getPagos() * TARIFA);
        Util.comprobar("Al cerrar, el semáforo tiene sus " + PLAZAS + " permisos", libres() == PLAZAS);
        System.out.println("  Han aparcado " + caja.getPagos() + " de " + COCHES + " coches (el impaciente puede irse sin aparcar).");
    }
}

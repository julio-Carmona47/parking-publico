package parking;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ThreadLocalRandom;

/** Utilidades comunes a todos los niveles: dormir, azar y una consola que no mezcla líneas. */
public final class Util {

    private static final Instant INICIO = Instant.now();

    private Util() { }

    /** Duerme el hilo actual. Si lo interrumpen, restaura la marca de interrupción. */
    public static void dormir(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /** Número aleatorio entre min y max (ambos incluidos). Seguro para usar desde varios hilos. */
    public static int azar(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    /**
     * Escribe una línea con el instante y el nombre del hilo que la escribe.
     * Es synchronized para que las líneas de dos hilos no salgan mezcladas.
     */
    public static synchronized void log(String mensaje) {
        long ms = Duration.between(INICIO, Instant.now()).toMillis();
        System.out.printf("[%5.2fs] %-9s %s%n", ms / 1000.0, Thread.currentThread().getName(), mensaje);
    }

    /** Línea de resultado final: OK o ERROR según se cumpla la condición. */
    public static void comprobar(String que, boolean ok) {
        System.out.println((ok ? "  OK     " : "  ERROR  ") + que);
    }
}

package race.race_con_problema;

import java.util.Arrays;
import java.util.Random;

public class InventarioConRace {
    static int[] stock = new int[10];
    static final Random rand = new Random();

    public static void main(String[] args) throws InterruptedException {
        for (int run = 1; run <= 10; run++) {
            Arrays.fill(stock, 100);
            Thread[] threads = new Thread[20];

            // 2 threads por producto (¡esto es clave!)
            threads[0] = new Thread(() -> vender(0, 10));      // Thread para vender prod 0
            threads[1] = new Thread(() -> reabastecer(0, 30)); // Thread para reabastecer prod 0
            
            threads[2] = new Thread(() -> vender(1, 15));
            threads[3] = new Thread(() -> reabastecer(1, 20));
            
            threads[4] = new Thread(() -> vender(2, 20));
            threads[5] = new Thread(() -> reabastecer(2, 40));
            
            threads[6] = new Thread(() -> vender(3, 5));
            threads[7] = new Thread(() -> reabastecer(3, 10));
            
            threads[8] = new Thread(() -> vender(4, 25));
            threads[9] = new Thread(() -> reabastecer(4, 35));
            
            threads[10] = new Thread(() -> vender(5, 15));
            threads[11] = new Thread(() -> reabastecer(5, 25));
            
            threads[12] = new Thread(() -> vender(6, 20));
            threads[13] = new Thread(() -> reabastecer(6, 30));
            
            threads[14] = new Thread(() -> vender(7, 10));
            threads[15] = new Thread(() -> reabastecer(7, 15));
            
            threads[16] = new Thread(() -> vender(8, 25));
            threads[17] = new Thread(() -> reabastecer(8, 40));
            
            threads[18] = new Thread(() -> vender(9, 15));
            threads[19] = new Thread(() -> reabastecer(9, 20));

            // Iniciar todos los threads
            for (Thread t : threads) t.start();
            for (Thread t : threads) t.join();

            System.out.printf("CON RC Ejecución #%d | Stock: %s%n", 
                run, Arrays.toString(stock));
        }
    }

    // ¡¡¡VENTANA CRÍTICA AMPLIADA CON SLEEP!!!
    static void vender(int id, int cantidad) {
        try {
            // Tiempo aleatorio ANTES de la operación (simula procesamiento)
            Thread.sleep(rand.nextInt(5, 25)); // 5-24 ms
            
            // ¡¡ESTA ES LA ZONA VULNERABLE!!
            int temporal = stock[id];          // 1. Leer valor
            Thread.sleep(rand.nextInt(1, 5));  // 2. ¡¡Retraso CRÍTICO aquí!!
            temporal -= cantidad;              // 3. Modificar
            stock[id] = temporal;              // 4. Escribir
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    static void reabastecer(int id, int cantidad) {
        try {
            Thread.sleep(rand.nextInt(5, 25)); // Tiempo aleatorio inicial
            
            // ¡¡VENTANA CRÍTICA VULNERABLE!!
            int temporal = stock[id];          // 1. Leer valor
            Thread.sleep(rand.nextInt(1, 5));  // 2. ¡¡Retraso que garantiza race!!
            temporal += cantidad;              // 3. Modificar
            stock[id] = temporal;              // 4. Escribir
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
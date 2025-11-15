package race.race_con_solucion;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

public class InventarioSinRace {
    static int[] stock = new int[10];
    static final ReentrantLock[] locks = new ReentrantLock[10];
    static final Random rand = new Random();
    
    static final int[] TIEMPOS_RAPIDOS = {10, 15, 20};
    static final int[] TIEMPOS_LENTOS = {30, 40, 50};

    static {
        for (int i = 0; i < 10; i++) {
            locks[i] = new ReentrantLock();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        for (int run = 1; run <= 10; run++) {
            Arrays.fill(stock, 100);
            Thread[] threads = new Thread[20];

            // MISMA CONFIGURACIÓN: 2 threads por producto
            threads[0] = new Thread(() -> vender(0, 10, elegirTiempo()));
            threads[1] = new Thread(() -> reabastecer(0, 30, elegirTiempo()));
            threads[2] = new Thread(() -> vender(1, 15, elegirTiempo()));
            threads[3] = new Thread(() -> reabastecer(1, 20, elegirTiempo()));
            threads[4] = new Thread(() -> vender(2, 20, elegirTiempo()));
            threads[5] = new Thread(() -> reabastecer(2, 40, elegirTiempo()));
            threads[6] = new Thread(() -> vender(3, 5, elegirTiempo()));
            threads[7] = new Thread(() -> reabastecer(3, 10, elegirTiempo()));
            threads[8] = new Thread(() -> vender(4, 25, elegirTiempo()));
            threads[9] = new Thread(() -> reabastecer(4, 35, elegirTiempo()));
            threads[10] = new Thread(() -> vender(5, 15, elegirTiempo()));
            threads[11] = new Thread(() -> reabastecer(5, 25, elegirTiempo()));
            threads[12] = new Thread(() -> vender(6, 20, elegirTiempo()));
            threads[13] = new Thread(() -> reabastecer(6, 30, elegirTiempo()));
            threads[14] = new Thread(() -> vender(7, 10, elegirTiempo()));
            threads[15] = new Thread(() -> reabastecer(7, 15, elegirTiempo()));
            threads[16] = new Thread(() -> vender(8, 25, elegirTiempo()));
            threads[17] = new Thread(() -> reabastecer(8, 40, elegirTiempo()));
            threads[18] = new Thread(() -> vender(9, 15, elegirTiempo()));
            threads[19] = new Thread(() -> reabastecer(9, 20, elegirTiempo()));

            mezclarArreglo(threads);
            
            for (Thread t : threads) t.start();
            for (Thread t : threads) t.join();

            System.out.printf("SIN RC Ejecución #%d | Stock: %s%n", 
                run, Arrays.toString(stock));
        }
    }

    static int elegirTiempo() {
        return rand.nextBoolean() ? 
            TIEMPOS_RAPIDOS[rand.nextInt(TIEMPOS_RAPIDOS.length)] : 
            TIEMPOS_LENTOS[rand.nextInt(TIEMPOS_LENTOS.length)];
    }

    static void mezclarArreglo(Thread[] arr) {
        for (int i = arr.length - 1; i > 0; i--) {
            int j = rand.nextInt(i + 1);
            Thread temp = arr[i];
            arr[i] = arr[j];
            arr[j] = temp;
        }
    }

    // ¡¡OPERACIONES SEGURAS CON LOCKS!!
    static void vender(int id, int cantidad, int tiempo) {
        simulateWork(tiempo, "Vender-" + id);
        
        locks[id].lock();
        try {
            int valorAnterior = stock[id];
            simulateWork(5, "Vender-calc-" + id);
            stock[id] = valorAnterior - cantidad;
        } finally {
            locks[id].unlock();
        }
    }

    static void reabastecer(int id, int cantidad, int tiempo) {
        simulateWork(tiempo, "Reabastecer-" + id);
        
        locks[id].lock();
        try {
            int valorAnterior = stock[id];
            simulateWork(5, "Reabastecer-calc-" + id);
            stock[id] = valorAnterior + cantidad;
        } finally {
            locks[id].unlock();
        }
    }

    static void simulateWork(int millis, String operation) {
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < millis) {
            for (int i = 0; i < 1000; i++) {
                double x = Math.sin(i * 0.1) * Math.cos(i * 0.2);
            }
        }
    }
}
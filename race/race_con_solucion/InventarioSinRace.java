package race.race_con_solucion;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

public class InventarioSinRace {
    static int[] stock = new int[10];
    static final ReentrantLock[] locks = new ReentrantLock[10];
    static final Random rand = new Random();

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
            threads[0] = new Thread(() -> vender(0, 10));
            threads[1] = new Thread(() -> reabastecer(0, 30));
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

            for (Thread t : threads) t.start();
            for (Thread t : threads) t.join();

            System.out.printf("SIN RC Ejecución #%d | Stock: %s%n", 
                run, Arrays.toString(stock));
        }
    }

    // MISMO RETRASO PERO CON PROTECCIÓN
    static void vender(int id, int cantidad) {
        try {
            Thread.sleep(rand.nextInt(5, 25)); // Simular procesamiento
            
            locks[id].lock();
            try {
                int temporal = stock[id];
                Thread.sleep(rand.nextInt(1, 5)); // Mismo retraso, pero seguro
                stock[id] = temporal - cantidad;
            } finally {
                locks[id].unlock();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    static void reabastecer(int id, int cantidad) {
        try {
            Thread.sleep(rand.nextInt(5, 25)); // Simular procesamiento
            
            locks[id].lock();
            try {
                int temporal = stock[id];
                Thread.sleep(rand.nextInt(1, 5)); // Mismo retraso, pero seguro
                stock[id] = temporal + cantidad;
            } finally {
                locks[id].unlock();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
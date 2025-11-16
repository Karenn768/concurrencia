package race.race_con_solucion;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;

public class Race_condition_solucion {
    
    // Configuración común
    static final int EJECUCIONES = 10;
    static final Random rand = new Random();
    
    // =============================
    // VERSIÓN CON MUTEX
    // =============================
    static class VersionMutex {
        int[] stock = new int[10];
        ReentrantLock[] locks = new ReentrantLock[10];
        
        VersionMutex() {
            Arrays.fill(stock, 100);
            for (int i = 0; i < 10; i++) {
                locks[i] = new ReentrantLock();
            }
        }
        
        void vender(int id, int cantidad) {
            try {
                Thread.sleep(rand.nextInt(5, 25));
                locks[id].lock();
                try {
                    int temporal = stock[id];
                    Thread.sleep(rand.nextInt(1, 5));
                    stock[id] = temporal - cantidad;
                } finally {
                    locks[id].unlock();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        void reabastecer(int id, int cantidad) {
            try {
                Thread.sleep(rand.nextInt(5, 25));
                locks[id].lock();
                try {
                    int temporal = stock[id];
                    Thread.sleep(rand.nextInt(1, 5));
                    stock[id] = temporal + cantidad;
                } finally {
                    locks[id].unlock();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        int[] getStock() {
            return Arrays.copyOf(stock, stock.length);
        }
    }
    
    // =============================
    // VERSIÓN CON SEMÁFOROS
    // =============================
    static class VersionSemaphores {
        int[] stock = new int[10];
        Semaphore[] semaphores = new Semaphore[10];
        
        VersionSemaphores() {
            Arrays.fill(stock, 100);
            for (int i = 0; i < 10; i++) {
                semaphores[i] = new Semaphore(1); 
            }
        }
        
        void vender(int id, int cantidad) {
            try {
                Thread.sleep(rand.nextInt(5, 25));
                semaphores[id].acquire();
                try {
                    int temporal = stock[id];
                    Thread.sleep(rand.nextInt(1, 5));
                    stock[id] = temporal - cantidad;
                } finally {
                    semaphores[id].release();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        void reabastecer(int id, int cantidad) {
            try {
                Thread.sleep(rand.nextInt(5, 25));
                semaphores[id].acquire();
                try {
                    int temporal = stock[id];
                    Thread.sleep(rand.nextInt(1, 5));
                    stock[id] = temporal + cantidad;
                } finally {
                    semaphores[id].release();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        int[] getStock() {
            return Arrays.copyOf(stock, stock.length);
        }
    }
    
    // =============================
    // EJECUCIÓN Y MEDICIÓN DE TIEMPOS
    // =============================
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=".repeat(60));
        System.out.println("📊 COMPARACIÓN DE RENDIMIENTO: MUTEX vs SEMÁFOROS");
        System.out.println("=".repeat(60));
        
        // Medir versión con Mutex
        long tiempoTotalMutex = 0;
        System.out.println("\n⏱️  Ejecutando versión con MUTEX (ReentrantLock)...");
        
        for (int run = 1; run <= EJECUCIONES; run++) {
            VersionMutex inventario = new VersionMutex();
            Thread[] threads = crearThreads(inventario);
            
            long inicio = System.nanoTime();
            ejecutarThreads(threads);
            long duracion = System.nanoTime() - inicio;
            
            tiempoTotalMutex += duracion;
            System.out.printf("   Ejecución #%d: %.2f ms | Stock: %s%n", 
                run, duracion / 1_000_000.0, Arrays.toString(inventario.getStock()));
        }
        
        // Medir versión con Semáforos
        long tiempoTotalSemaphores = 0;
        System.out.println("\n⏱️  Ejecutando versión con SEMÁFOROS...");
        
        for (int run = 1; run <= EJECUCIONES; run++) {
            VersionSemaphores inventario = new VersionSemaphores();
            Thread[] threads = crearThreads(inventario);
            
            long inicio = System.nanoTime();
            ejecutarThreads(threads);
            long duracion = System.nanoTime() - inicio;
            
            tiempoTotalSemaphores += duracion;
            System.out.printf("   Ejecución #%d: %.2f ms | Stock: %s%n", 
                run, duracion / 1_000_000.0, Arrays.toString(inventario.getStock()));
        }
        
        // =============================
        // RESULTADOS COMPARATIVOS
        // =============================
        double promedioMutex = tiempoTotalMutex / (double) EJECUCIONES / 1_000_000.0;
        double promedioSemaphores = tiempoTotalSemaphores / (double) EJECUCIONES / 1_000_000.0;
        double overhead = ((promedioSemaphores / promedioMutex) - 1) * 100;
        
        System.out.println("\n" + "-".repeat(60));
        System.out.printf("📈 RESULTADOS PROMEDIO (%d ejecuciones):%n", EJECUCIONES);
        System.out.printf("   • MUTEX :   %.2f ms%n", promedioMutex);
        System.out.printf("   • SEMÁFOROS:               %.2f ms%n", promedioSemaphores);
        System.out.printf("   • Overhead de semáforos:   %.1f%%%n", overhead);
        System.out.println("-".repeat(60));
        
        System.out.println("\n✅ CONCLUSIÓN:");
        if (overhead > 0) {
            System.out.printf("Los semáforos son un %.1f%% más lentos que los mutex para este caso de uso.%n", overhead);
        } else {
            System.out.println("Los semáforos son ligeramente más rápidos en este entorno específico.");
        }
    }
    
    // =============================
    // MÉTODOS AUXILIARES
    // =============================
    static Thread[] crearThreads(Object inventario) {
        Thread[] threads = new Thread[20];
        
        if (inventario instanceof VersionMutex) {
            VersionMutex inv = (VersionMutex) inventario;
            threads[0] = new Thread(() -> inv.vender(0, 10));
            threads[1] = new Thread(() -> inv.reabastecer(0, 30));
            threads[2] = new Thread(() -> inv.vender(1, 15));
            threads[3] = new Thread(() -> inv.reabastecer(1, 20));
            threads[4] = new Thread(() -> inv.vender(2, 20));
            threads[5] = new Thread(() -> inv.reabastecer(2, 40));
            threads[6] = new Thread(() -> inv.vender(3, 5));
            threads[7] = new Thread(() -> inv.reabastecer(3, 10));
            threads[8] = new Thread(() -> inv.vender(4, 25));
            threads[9] = new Thread(() -> inv.reabastecer(4, 35));
            threads[10] = new Thread(() -> inv.vender(5, 15));
            threads[11] = new Thread(() -> inv.reabastecer(5, 25));
            threads[12] = new Thread(() -> inv.vender(6, 20));
            threads[13] = new Thread(() -> inv.reabastecer(6, 30));
            threads[14] = new Thread(() -> inv.vender(7, 10));
            threads[15] = new Thread(() -> inv.reabastecer(7, 15));
            threads[16] = new Thread(() -> inv.vender(8, 25));
            threads[17] = new Thread(() -> inv.reabastecer(8, 40));
            threads[18] = new Thread(() -> inv.vender(9, 15));
            threads[19] = new Thread(() -> inv.reabastecer(9, 20));
        } 
        else if (inventario instanceof VersionSemaphores) {
            VersionSemaphores inv = (VersionSemaphores) inventario;
            threads[0] = new Thread(() -> inv.vender(0, 10));
            threads[1] = new Thread(() -> inv.reabastecer(0, 30));
            threads[2] = new Thread(() -> inv.vender(1, 15));
            threads[3] = new Thread(() -> inv.reabastecer(1, 20));
            threads[4] = new Thread(() -> inv.vender(2, 20));
            threads[5] = new Thread(() -> inv.reabastecer(2, 40));
            threads[6] = new Thread(() -> inv.vender(3, 5));
            threads[7] = new Thread(() -> inv.reabastecer(3, 10));
            threads[8] = new Thread(() -> inv.vender(4, 25));
            threads[9] = new Thread(() -> inv.reabastecer(4, 35));
            threads[10] = new Thread(() -> inv.vender(5, 15));
            threads[11] = new Thread(() -> inv.reabastecer(5, 25));
            threads[12] = new Thread(() -> inv.vender(6, 20));
            threads[13] = new Thread(() -> inv.reabastecer(6, 30));
            threads[14] = new Thread(() -> inv.vender(7, 10));
            threads[15] = new Thread(() -> inv.reabastecer(7, 15));
            threads[16] = new Thread(() -> inv.vender(8, 25));
            threads[17] = new Thread(() -> inv.reabastecer(8, 40));
            threads[18] = new Thread(() -> inv.vender(9, 15));
            threads[19] = new Thread(() -> inv.reabastecer(9, 20));
        }
        
        return threads;
    }
    
    static void ejecutarThreads(Thread[] threads) throws InterruptedException {
        for (Thread t : threads) t.start();
        for (Thread t : threads) t.join();
    }
}
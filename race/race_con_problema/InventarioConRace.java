package race.race_con_problema;

import java.util.Arrays;
import java.util.Random;

public class InventarioConRace {
    static int[] stock = new int[10];
    static final Random rand = new Random();
    
    // DOS PATRONES DE TIEMPO: rápido (10-20ms) y lento (30-50ms)
    static final int[] TIEMPOS_RAPIDOS = {10, 15, 20};
    static final int[] TIEMPOS_LENTOS = {30, 40, 50};

    public static void main(String[] args) throws InterruptedException {
        for (int run = 1; run <= 10; run++) {
            Arrays.fill(stock, 100);
            Thread[] threads = new Thread[20];

            // Configuración crítica: 2 threads por producto
            threads[0] = new Thread(() -> vender(0, 10, elegirTiempo()));  // Thread 1
            threads[1] = new Thread(() -> reabastecer(0, 30, elegirTiempo())); // Thread 2
            
            threads[2] = new Thread(() -> vender(1, 15, elegirTiempo()));  // Thread 3
            threads[3] = new Thread(() -> reabastecer(1, 20, elegirTiempo())); // Thread 4
            
            threads[4] = new Thread(() -> vender(2, 20, elegirTiempo()));  // Thread 5
            threads[5] = new Thread(() -> reabastecer(2, 40, elegirTiempo())); // Thread 6
            
            threads[6] = new Thread(() -> vender(3, 5, elegirTiempo()));   // Thread 7
            threads[7] = new Thread(() -> reabastecer(3, 10, elegirTiempo())); // Thread 8
            
            threads[8] = new Thread(() -> vender(4, 25, elegirTiempo()));  // Thread 9
            threads[9] = new Thread(() -> reabastecer(4, 35, elegirTiempo())); // Thread 10
            
            threads[10] = new Thread(() -> vender(5, 15, elegirTiempo())); // Thread 11
            threads[11] = new Thread(() -> reabastecer(5, 25, elegirTiempo())); // Thread 12
            
            threads[12] = new Thread(() -> vender(6, 20, elegirTiempo())); // Thread 13
            threads[13] = new Thread(() -> reabastecer(6, 30, elegirTiempo())); // Thread 14
            
            threads[14] = new Thread(() -> vender(7, 10, elegirTiempo())); // Thread 15
            threads[15] = new Thread(() -> reabastecer(7, 15, elegirTiempo())); // Thread 16
            
            threads[16] = new Thread(() -> vender(8, 25, elegirTiempo())); // Thread 17
            threads[17] = new Thread(() -> reabastecer(8, 40, elegirTiempo())); // Thread 18
            
            threads[18] = new Thread(() -> vender(9, 15, elegirTiempo())); // Thread 19
            threads[19] = new Thread(() -> reabastecer(9, 20, elegirTiempo())); // Thread 20

            // Mezclar el orden de inicio para más caos
            mezclarArreglo(threads);
            
            for (Thread t : threads) t.start();
            for (Thread t : threads) t.join();

            System.out.printf("CON RC Ejecución #%d | Stock: %s%n", 
                run, Arrays.toString(stock));
        }
    }

    // ELEGIR ALEATORIAMENTE ENTRE PATRONES DE TIEMPO
    static int elegirTiempo() {
        return rand.nextBoolean() ? 
            TIEMPOS_RAPIDOS[rand.nextInt(TIEMPOS_RAPIDOS.length)] : 
            TIEMPOS_LENTOS[rand.nextInt(TIEMPOS_LENTOS.length)];
    }

    // MEZCLAR ARREGLO PARA ORDEN ALEATORIO DE INICIO
    static void mezclarArreglo(Thread[] arr) {
        for (int i = arr.length - 1; i > 0; i--) {
            int j = rand.nextInt(i + 1);
            Thread temp = arr[i];
            arr[i] = arr[j];
            arr[j] = temp;
        }
    }

    // ¡¡¡ZONA CRÍTICA VULNERABLE!!!
    static void vender(int id, int cantidad, int tiempo) {
        simulateWork(tiempo, "Vender-" + id);
        
        // ¡¡NO ATÓMICO!! - Leer-Modificar-Escribir
        int valorAnterior = stock[id];
        simulateWork(5, "Vender-calc-" + id); // Simular cálculo
        int valorNuevo = valorAnterior - cantidad;
        stock[id] = valorNuevo; // ¡Escritura no sincronizada!
    }

    static void reabastecer(int id, int cantidad, int tiempo) {
        simulateWork(tiempo, "Reabastecer-" + id);
        
        // ¡¡NO ATÓMICO!! - Leer-Modificar-Escribir
        int valorAnterior = stock[id];
        simulateWork(5, "Reabastecer-calc-" + id);
        int valorNuevo = valorAnterior + cantidad;
        stock[id] = valorNuevo; // ¡Escritura no sincronizada!
    }

    // SIMULAR TRABAJO REAL CON RETRASO SIGNIFICATIVO
    static void simulateWork(int millis, String operation) {
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < millis) {
            // Trabajo ficticio pero consume CPU
            for (int i = 0; i < 1000; i++) {
                double x = Math.sin(i * 0.1) * Math.cos(i * 0.2);
            }
        }
    }
}
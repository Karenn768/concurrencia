package starvation.starvation_con_problema;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Implementación de un sistema de procesamiento de tareas CON PROBLEMA DE STARVATION
 * 
 * Problema: Las tareas tipo B sufren inanición porque siempre se priorizan las tareas A y M.
 * Las tareas tipo B pueden quedar esperando indefinidamente sin ser procesadas.
 * 
 * Características:
 * - Cola compartida con capacidad máxima de 20 tareas
 * - 3 tipos de tareas: Alta (A), Media (M), Baja (B)
 * - 5 threads productores: 60% B, 30% M, 10% A
 * - 3 threads consumidores
 * - Tiempos de procesamiento: A=50ms, M=100ms, B=150ms
 */
public class StarvationConProblema {
    
    // Tipos de tareas
    enum TipoTarea {
        A(50, "Alta"),
        M(100, "Media"),
        B(150, "Baja");
        
        private final int tiempoProcesamiento;
        private final String nombre;
        
        TipoTarea(int tiempo, String nombre) {
            this.tiempoProcesamiento = tiempo;
            this.nombre = nombre;
        }
        
        public int getTiempoProcesamiento() { return tiempoProcesamiento; }
        public String getNombre() { return nombre; }
    }
    
    // Clase que representa una tarea
    static class Tarea {
        private static int contadorId = 0;
        private final int id;
        private final TipoTarea tipo;
        private final long tiempoCreacion;
        
        public Tarea(TipoTarea tipo) {
            this.id = ++contadorId;
            this.tipo = tipo;
            this.tiempoCreacion = System.currentTimeMillis();
        }
        
        public int getId() { return id; }
        public TipoTarea getTipo() { return tipo; }
        public long getTiempoEspera() { 
            return System.currentTimeMillis() - tiempoCreacion; 
        }
        
        @Override
        public String toString() {
            return String.format("Tarea#%d[%s]", id, tipo.getNombre());
        }
    }
    
    // Cola compartida de tareas
    static class ColaCompartida {
        private final Queue<Tarea> cola;
        private final int capacidadMaxima;
        private final Lock lock;
        
        public ColaCompartida(int capacidad) {
            this.cola = new LinkedList<>();
            this.capacidadMaxima = capacidad;
            this.lock = new ReentrantLock();
        }
        
        // Agregar tarea a la cola
        public synchronized boolean agregar(Tarea tarea) throws InterruptedException {
            while (cola.size() >= capacidadMaxima) {
                wait(); // Espera si la cola está llena
            }
            cola.offer(tarea);
            notifyAll();
            return true;
        }
        
        // Obtener tarea CON PRIORIDAD (causa starvation)
        // PROBLEMA: Siempre busca primero tareas A, luego M, ignorando B
        public synchronized Tarea obtener() throws InterruptedException {
            while (cola.isEmpty()) {
                wait();
            }
            
            // POLÍTICA QUE CAUSA STARVATION: Siempre priorizar A y M
            Tarea tareaSeleccionada = null;
            
            // Buscar primero tareas tipo A
            for (Tarea t : cola) {
                if (t.getTipo() == TipoTarea.A) {
                    tareaSeleccionada = t;
                    break;
                }
            }
            
            // Si no hay A, buscar tipo M
            if (tareaSeleccionada == null) {
                for (Tarea t : cola) {
                    if (t.getTipo() == TipoTarea.M) {
                        tareaSeleccionada = t;
                        break;
                    }
                }
            }
            
            // Solo si no hay A ni M, tomar tipo B
            if (tareaSeleccionada == null) {
                tareaSeleccionada = cola.peek();
            }
            
            cola.remove(tareaSeleccionada);
            notifyAll();
            return tareaSeleccionada;
        }
        
        public synchronized int getTamano() {
            return cola.size();
        }
        
        public synchronized int contarTareasTipoB() {
            int count = 0;
            for (Tarea t : cola) {
                if (t.getTipo() == TipoTarea.B) {
                    count++;
                }
            }
            return count;
        }
        
        public synchronized void mostrarEstado() {
            System.out.println("\n=== ESTADO DE LA COLA ===");
            System.out.println("Tamaño actual: " + cola.size() + "/" + capacidadMaxima);
            int countA = 0, countM = 0, countB = 0;
            for (Tarea t : cola) {
                switch (t.getTipo()) {
                    case A: countA++; break;
                    case M: countM++; break;
                    case B: countB++; break;
                }
            }
            System.out.println("Tareas Alta (A): " + countA);
            System.out.println("Tareas Media (M): " + countM);
            System.out.println("Tareas Baja (B): " + countB);
        }
    }
    
    // Thread Productor
    static class Productor extends Thread {
        private final ColaCompartida cola;
        private final int id;
        private final Random random;
        private final int totalTareas = 6; // 30 tareas / 5 productores
        
        public Productor(ColaCompartida cola, int id) {
            this.cola = cola;
            this.id = id;
            this.random = new Random();
        }
        
        @Override
        public void run() {
            try {
                for (int i = 0; i < totalTareas; i++) {
                    TipoTarea tipo = generarTipoTarea();
                    Tarea tarea = new Tarea(tipo);
                    
                    cola.agregar(tarea);
                    System.out.println("Productor-" + id + " creó " + tarea);
                    
                    // Pequeña pausa entre creaciones
                    Thread.sleep(random.nextInt(100) + 50);
                }
                System.out.println(">>> Productor-" + id + " terminó de producir tareas");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        // Genera tipos de tarea según distribución: 60% B, 30% M, 10% A
        private TipoTarea generarTipoTarea() {
            int rand = random.nextInt(100);
            if (rand < 10) {
                return TipoTarea.A; // 10%
            } else if (rand < 40) {
                return TipoTarea.M; // 30%
            } else {
                return TipoTarea.B; // 60%
            }
        }
    }
    
    // Thread Consumidor
    static class Consumidor extends Thread {
        private final ColaCompartida cola;
        private final int id;
        private volatile boolean ejecutando = true;
        
        public Consumidor(ColaCompartida cola, int id) {
            this.cola = cola;
            this.id = id;
        }
        
        @Override
        public void run() {
            try {
                while (ejecutando) {
                    Tarea tarea = cola.obtener();
                    
                    long tiempoEspera = tarea.getTiempoEspera();
                    System.out.println(String.format(
                        "Consumidor-%d procesando %s (esperó %d ms)",
                        id, tarea, tiempoEspera
                    ));
                    
                    // Simular procesamiento
                    Thread.sleep(tarea.getTipo().getTiempoProcesamiento());
                    
                    System.out.println(String.format(
                        "Consumidor-%d completó %s",
                        id, tarea
                    ));
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        public void detener() {
            ejecutando = false;
            interrupt();
        }
    }
    
    // Monitor de tareas tipo B
    static class MonitorTareasB extends Thread {
        private final ColaCompartida cola;
        private volatile boolean ejecutando = true;
        private int maxTareasB = 0;
        
        public MonitorTareasB(ColaCompartida cola) {
            this.cola = cola;
        }
        
        @Override
        public void run() {
            try {
                while (ejecutando) {
                    Thread.sleep(2000); // Cada 2 segundos
                    int tareasB = cola.contarTareasTipoB();
                    maxTareasB = Math.max(maxTareasB, tareasB);
                    
                    System.out.println("\n*** MONITOR: Tareas tipo B en espera: " + tareasB + " ***");
                    if (tareasB > 5) {
                        System.out.println("*** ¡ADVERTENCIA! POSIBLE STARVATION DETECTADA ***");
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        public void detener() {
            ejecutando = false;
            interrupt();
        }
        
        public int getMaxTareasB() {
            return maxTareasB;
        }
    }
    
    public static void main(String[] args) {
        System.out.println("==============================================");
        System.out.println("  SISTEMA CON PROBLEMA DE STARVATION");
        System.out.println("==============================================");
        System.out.println("Características:");
        System.out.println("- Cola compartida: capacidad 20 tareas");
        System.out.println("- Tipos de tareas: A (10%), M (30%), B (60%)");
        System.out.println("- 5 productores, 3 consumidores");
        System.out.println("- PROBLEMA: Siempre se priorizan tareas A y M");
        System.out.println("==============================================\n");
        
        ColaCompartida cola = new ColaCompartida(20);
        
        // Crear productores
        Productor[] productores = new Productor[5];
        for (int i = 0; i < 5; i++) {
            productores[i] = new Productor(cola, i + 1);
        }
        
        // Crear consumidores
        Consumidor[] consumidores = new Consumidor[3];
        for (int i = 0; i < 3; i++) {
            consumidores[i] = new Consumidor(cola, i + 1);
        }
        
        // Monitor de tareas B
        MonitorTareasB monitor = new MonitorTareasB(cola);
        
        // Iniciar todos los threads
        long tiempoInicio = System.currentTimeMillis();
        
        monitor.start();
        
        for (Productor p : productores) {
            p.start();
        }
        
        for (Consumidor c : consumidores) {
            c.start();
        }
        
        // Esperar a que terminen los productores
        try {
            for (Productor p : productores) {
                p.join();
            }
            
            System.out.println("\n>>> Todos los productores terminaron. Esperando 10 segundos...\n");
            Thread.sleep(10000); // Esperar 10 segundos después de producción
            
            // Detener consumidores y monitor
            for (Consumidor c : consumidores) {
                c.detener();
            }
            monitor.detener();
            
            for (Consumidor c : consumidores) {
                c.join();
            }
            monitor.join();
            
            long tiempoTotal = System.currentTimeMillis() - tiempoInicio;
            
            // Resumen final
            System.out.println("\n==============================================");
            System.out.println("  RESULTADOS FINALES");
            System.out.println("==============================================");
            cola.mostrarEstado();
            System.out.println("\nMáximo de tareas tipo B en espera: " + monitor.getMaxTareasB());
            System.out.println("Tiempo total de ejecución: " + (tiempoTotal / 1000.0) + " segundos");
            System.out.println("\n*** PROBLEMA OBSERVADO: Las tareas tipo B sufren STARVATION ***");
            System.out.println("*** Muchas tareas B quedaron sin procesar debido a la priorización ***");
            System.out.println("==============================================");
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

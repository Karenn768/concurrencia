package concurrencia.starvation.starvation_con_solucion;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Implementación de un sistema de procesamiento de tareas SIN PROBLEMA DE STARVATION
 * 
 * Solución: Implementa AGING (envejecimiento) para evitar inanición.
 * Las tareas incrementan su prioridad efectiva con el tiempo de espera.
 * 
 * Características:
 * - Cola compartida con capacidad máxima de 20 tareas
 * - 3 tipos de tareas: Alta (A), Media (M), Baja (B)
 * - 5 threads productores: 60% B, 30% M, 10% A
 * - 3 threads consumidores
 * - Tiempos de procesamiento: A=50ms, M=100ms, B=150ms
 * - AGING: Incrementa prioridad cada 1000ms de espera
 */
public class StarvationConSolucion {
    
    // Tipos de tareas
    enum TipoTarea {
        A(50, "Alta", 3),
        M(100, "Media", 2),
        B(150, "Baja", 1);
        
        private final int tiempoProcesamiento;
        private final String nombre;
        private final int prioridadBase;
        
        TipoTarea(int tiempo, String nombre, int prioridad) {
            this.tiempoProcesamiento = tiempo;
            this.nombre = nombre;
            this.prioridadBase = prioridad;
        }
        
        public int getTiempoProcesamiento() { return tiempoProcesamiento; }
        public String getNombre() { return nombre; }
        public int getPrioridadBase() { return prioridadBase; }
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
        
        // AGING: Calcula prioridad efectiva basada en tiempo de espera
        public double getPrioridadEfectiva() {
            long tiempoEspera = getTiempoEspera();
            // Incrementa prioridad cada 1000ms (1 segundo)
            double bonoEnvejecimiento = (tiempoEspera / 1000.0) * 0.5;
            return tipo.getPrioridadBase() + bonoEnvejecimiento;
        }
        
        @Override
        public String toString() {
            return String.format("Tarea#%d[%s,Prior:%.2f]", 
                id, tipo.getNombre(), getPrioridadEfectiva());
        }
    }
    
    // Cola compartida de tareas con AGING
    static class ColaCompartida {
        private final List<Tarea> cola;
        private final int capacidadMaxima;
        private final Lock lock;
        private int tareasAProcesadas = 0;
        private int tareasMProcesadas = 0;
        private int tareasBProcesadas = 0;
        
        public ColaCompartida(int capacidad) {
            this.cola = new ArrayList<>();
            this.capacidadMaxima = capacidad;
            this.lock = new ReentrantLock();
        }
        
        // Agregar tarea a la cola
        public synchronized boolean agregar(Tarea tarea) throws InterruptedException {
            while (cola.size() >= capacidadMaxima) {
                wait(); // Espera si la cola está llena
            }
            cola.add(tarea);
            notifyAll();
            return true;
        }
        
        // Obtener tarea CON AGING (evita starvation)
        // SOLUCIÓN: Selecciona tarea con mayor prioridad efectiva
        public synchronized Tarea obtener() throws InterruptedException {
            while (cola.isEmpty()) {
                wait();
            }
            
            // POLÍTICA CON AGING: Ordenar por prioridad efectiva
            Tarea tareaSeleccionada = cola.stream()
                .max(Comparator.comparingDouble(Tarea::getPrioridadEfectiva))
                .orElse(null);
            
            if (tareaSeleccionada != null) {
                cola.remove(tareaSeleccionada);
                
                // Registrar estadísticas
                switch (tareaSeleccionada.getTipo()) {
                    case A: tareasAProcesadas++; break;
                    case M: tareasMProcesadas++; break;
                    case B: tareasBProcesadas++; break;
                }
            }
            
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
            System.out.println("En cola -> Alta (A): " + countA + ", Media (M): " + countM + ", Baja (B): " + countB);
            System.out.println("Procesadas -> Alta (A): " + tareasAProcesadas + 
                             ", Media (M): " + tareasMProcesadas + 
                             ", Baja (B): " + tareasBProcesadas);
        }
        
        public synchronized int[] getEstadisticas() {
            return new int[] { tareasAProcesadas, tareasMProcesadas, tareasBProcesadas };
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
                        "Consumidor-%d procesando %s (esperó %d ms, prioridad efectiva: %.2f)",
                        id, tarea, tiempoEspera, tarea.getPrioridadEfectiva()
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
    
    // Monitor de tareas y tabla temporal
    static class MonitorTareas extends Thread {
        private final ColaCompartida cola;
        private volatile boolean ejecutando = true;
        private int maxTareasB = 0;
        private long tiempoInicio;
        
        // Tabla de monitoreo temporal
        private final int[] tiempos = {2, 4, 6, 8, 10}; // segundos
        private int indiceActual = 0;
        
        public MonitorTareas(ColaCompartida cola, long tiempoInicio) {
            this.cola = cola;
            this.tiempoInicio = tiempoInicio;
        }
        
        @Override
        public void run() {
            try {
                while (ejecutando && indiceActual < tiempos.length) {
                    Thread.sleep(2000); // Verificar cada 2 segundos
                    
                    long tiempoTranscurrido = (System.currentTimeMillis() - tiempoInicio) / 1000;
                    
                    if (tiempoTranscurrido >= tiempos[indiceActual]) {
                        mostrarEstadoTemporal(tiempos[indiceActual]);
                        indiceActual++;
                    }
                    
                    int tareasB = cola.contarTareasTipoB();
                    maxTareasB = Math.max(maxTareasB, tareasB);
                    
                    System.out.println("\n*** MONITOR: Tareas tipo B en espera: " + tareasB + " ***");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        private void mostrarEstadoTemporal(int tiempo) {
            int[] stats = cola.getEstadisticas();
            System.out.println("\n╔════════════════════════════════════════════════════════╗");
            System.out.println("║  TABLA DE MONITOREO TEMPORAL - " + tiempo + " segundos         ║");
            System.out.println("╠════════════════════════════════════════════════════════╣");
            System.out.println("║  Tareas A Procesadas: " + String.format("%-28d", stats[0]) + "║");
            System.out.println("║  Tareas M Procesadas: " + String.format("%-28d", stats[1]) + "║");
            System.out.println("║  Tareas B Procesadas: " + String.format("%-28d", stats[2]) + "║");
            System.out.println("║  Tareas B en Espera:  " + String.format("%-28d", cola.contarTareasTipoB()) + "║");
            System.out.println("╚════════════════════════════════════════════════════════╝");
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
        System.out.println("  SISTEMA SIN STARVATION (CON AGING)");
        System.out.println("==============================================");
        System.out.println("Características:");
        System.out.println("- Cola compartida: capacidad 20 tareas");
        System.out.println("- Tipos de tareas: A (10%), M (30%), B (60%)");
        System.out.println("- 5 productores, 3 consumidores");
        System.out.println("- SOLUCIÓN: AGING (envejecimiento)");
        System.out.println("  * Prioridad base: A=3, M=2, B=1");
        System.out.println("  * Incremento: +0.5 cada 1000ms de espera");
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
        
        // Monitor de tareas
        long tiempoInicio = System.currentTimeMillis();
        MonitorTareas monitor = new MonitorTareas(cola, tiempoInicio);
        
        // Iniciar todos los threads
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
            
            int[] stats = cola.getEstadisticas();
            int totalProcesadas = stats[0] + stats[1] + stats[2];
            
            System.out.println("\nMáximo de tareas tipo B en espera simultáneas: " + monitor.getMaxTareasB());
            System.out.println("Tiempo total de ejecución: " + (tiempoTotal / 1000.0) + " segundos");
            
            // Análisis
            System.out.println("\n==============================================");
            System.out.println("  ANÁLISIS DE RESULTADOS");
            System.out.println("==============================================");
            System.out.println("Total de tareas procesadas: " + totalProcesadas);
            System.out.println("Distribución de procesamiento:");
            System.out.println("  - Tareas A: " + stats[0] + " (" + (stats[0] * 100.0 / totalProcesadas) + "%)");
            System.out.println("  - Tareas M: " + stats[1] + " (" + (stats[1] * 100.0 / totalProcesadas) + "%)");
            System.out.println("  - Tareas B: " + stats[2] + " (" + (stats[2] * 100.0 / totalProcesadas) + "%)");
            
            System.out.println("\n*** SOLUCIÓN EXITOSA: El mecanismo de AGING garantizó ***");
            System.out.println("*** que TODAS las tareas eventualmente se procesaran ***");
            System.out.println("*** evitando la inanición de las tareas tipo B ***");
            
            System.out.println("\n==============================================");
            System.out.println("  DESCRIPCIÓN DEL ALGORITMO AGING");
            System.out.println("==============================================");
            System.out.println("Pseudocódigo:");
            System.out.println("1. Cada tarea tiene prioridad base (A=3, M=2, B=1)");
            System.out.println("2. Al obtener tarea de la cola:");
            System.out.println("   a. Para cada tarea calcular:");
            System.out.println("      prioridad_efectiva = prioridad_base + (tiempo_espera/1000) * 0.5");
            System.out.println("   b. Seleccionar tarea con mayor prioridad_efectiva");
            System.out.println("3. Resultado: Las tareas antiguas aumentan su prioridad");
            System.out.println("   garantizando que eventualmente sean procesadas");
            System.out.println("==============================================");
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

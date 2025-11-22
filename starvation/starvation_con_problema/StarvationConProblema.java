package starvation.starvation_con_problema;
import java.util.concurrent.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.*;

public class StarvationConProblema {

       // Clase anidada estática Task
       public static class Task {

       public enum Type {
           A(0),
           M(1),
           B(3);

           public final int priority;

           Type(int priority) {
               this.priority = priority;
           }
       }

       public final Type type;
       public final long creationTime;

       public Task(Type type) {
           this.type = type;
           this.creationTime = System.currentTimeMillis();
       }

       @Override
       public String toString() {
           return type.name();
       }
    }

    // Cola con capacidad acotada que respeta prioridades
    static class BoundedPriorityQueue {
        private final int capacity;
        private final PriorityQueue<Task> queue;
        private final ReentrantLock lock = new ReentrantLock();
        private final Condition notFull = lock.newCondition();
        private final Condition notEmpty = lock.newCondition();

        public BoundedPriorityQueue(int capacity, Comparator<Task> comparator) {
            this.capacity = capacity;
            this.queue = new PriorityQueue<>(capacity, comparator);
        }

        public void put(Task task) throws InterruptedException {
            lock.lock();
            try {
                // Bloquea si la cola está llena
                while (queue.size() >= capacity) {
                    notFull.await();
                }
                queue.add(task);
                notEmpty.signal(); // Despierta a consumidores
            } finally {
                lock.unlock();
            }
        }

        public Task poll(long timeout, TimeUnit unit) throws InterruptedException {
            lock.lock();
            try {
                long nanos = unit.toNanos(timeout);
                while (queue.isEmpty()) {
                    nanos = notEmpty.awaitNanos(nanos);
                    if (nanos <= 0) return null;
                }
                Task task = queue.poll();
                notFull.signal(); // Despierta a productores
                return task;
            } finally {
                lock.unlock();
            }
        }

        public int size() {
            lock.lock();
            try {
                return queue.size();
            } finally {
                lock.unlock();
            }
        }

        public List<Task> copy() {
            lock.lock();
            try {
                return new ArrayList<>(queue);
            } finally {
                lock.unlock();
            }
        }
    }

    private static final int CAPACITY = 20;
    private static final long SIMULATION_TIME_MS = 10_000;

    // Secuencia inicial fija (30 tareas)
    private static final Task.Type[] INITIAL_SEQUENCE = {
        // 1-10
        Task.Type.B, Task.Type.B, Task.Type.M, Task.Type.B, Task.Type.B, Task.Type.B,
        Task.Type.A, Task.Type.M, Task.Type.B, Task.Type.B,
        // 11-20
        Task.Type.M, Task.Type.B, Task.Type.B, Task.Type.B, Task.Type.A, Task.Type.B,
        Task.Type.M, Task.Type.B, Task.Type.B, Task.Type.B,
        // 21-30
        Task.Type.B, Task.Type.B, Task.Type.B, Task.Type.M, Task.Type.A, Task.Type.B,
        Task.Type.B, Task.Type.M, Task.Type.B, Task.Type.B
    };

    // Contadores atómicos para seguridad de hilos
    private static final AtomicInteger generatedA = new AtomicInteger();
    private static final AtomicInteger generatedM = new AtomicInteger();
    private static final AtomicInteger generatedB = new AtomicInteger();
    private static final AtomicInteger processedA = new AtomicInteger();
    private static final AtomicInteger processedM = new AtomicInteger();
    private static final AtomicInteger processedB = new AtomicInteger();
    
    // Medición de tiempos de espera
    private static final AtomicLong maxWaitB = new AtomicLong(0);
    private static final AtomicLong totalWaitB = new AtomicLong(0);
    private static final AtomicInteger countWaitB = new AtomicInteger(0);

    // Comparador estático: A=0, M=1, B=3
    private static final Comparator<Task> STATIC_PRIORITY_COMPARATOR =
        (t1, t2) -> Integer.compare(t1.type.priority, t2.type.priority);

    private static long startTime;

    public static void main(String[] args) throws InterruptedException {
        // Cola con capacidad acotada y prioridades
        BoundedPriorityQueue queue = new BoundedPriorityQueue(CAPACITY, STATIC_PRIORITY_COMPARATOR);

        ExecutorService producerPool = Executors.newFixedThreadPool(5);
        ExecutorService consumerPool = Executors.newFixedThreadPool(3);

        // Monitor de estado de cola con snapshots cada segundo
        ScheduledExecutorService monitor = Executors.newSingleThreadScheduledExecutor();

        monitor.scheduleAtFixedRate(() -> {
           long now = System.currentTimeMillis();
           List<Task> copy = queue.copy();

           long pendientesA = copy.stream().filter(t -> t.type == Task.Type.A).count();
           long pendientesM = copy.stream().filter(t -> t.type == Task.Type.M).count();
           long pendientesB = copy.stream().filter(t -> t.type == Task.Type.B).count();

           int procesadasA = processedA.get();
           int procesadasM = processedM.get();
           int procesadasB = processedB.get();

           String state = String.format(
               "t=%.1fs | cola size=%d/%d | Pendientes (A=%d, M=%d, B=%d) | Procesadas (A=%d, M=%d, B=%d)",
               (now - startTime) / 1000.0,
               copy.size(),
               CAPACITY,
               pendientesA, pendientesM, pendientesB,
               procesadasA, procesadasM, procesadasB);
       
           System.out.println("[MONITOR] " + state);
       
           // Advertencia starvation
           if (pendientesB > 5) {
               System.out.println("*** ¡ADVERTENCIA! STARVATION DETECTADA: " + pendientesB + " tareas B esperando ***");
           }
    }, 2, 1, TimeUnit.SECONDS);


        startTime = System.currentTimeMillis();

        // Iniciar productores
        for (int i = 0; i < 5; i++) {
            final int id = i + 1;
            producerPool.submit(() -> producer(id, queue));
        }

        // Iniciar consumidores
        for (int i = 0; i < 3; i++) {
            final int id = i + 1;
            consumerPool.submit(() -> consumer(id, queue));
        }

        // Duración simulación
        Thread.sleep(SIMULATION_TIME_MS);

        // Apagar servicios
        monitor.shutdownNow();
        producerPool.shutdownNow();
        consumerPool.shutdown();

        // Esperar que consumidores terminen y vacíen cola (máx 2s)
        try {
            if (!monitor.awaitTermination(1, TimeUnit.SECONDS)) {
                System.err.println("Monitor no terminó a tiempo");
            }
            if (!consumerPool.awaitTermination(2, TimeUnit.SECONDS)) {
                consumerPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            monitor.shutdownNow();
            consumerPool.shutdownNow();
            Thread.currentThread().interrupt();
        }

        // Resultados finales
        System.out.println("\n" + "=".repeat(60));
        System.out.println("SIMULACIÓN FINALIZADA (CON STARVATION)");
        System.out.println("=".repeat(60));
        
        // Generadas por tipo
        int genA = generatedA.get();
        int genM = generatedM.get();
        int genB = generatedB.get();
        int totalGenerated = genA + genM + genB;
        System.out.printf("Generadas: A=%d, M=%d, B=%d | Total=%d%n", genA, genM, genB, totalGenerated);
        
        // Procesadas por tipo
        int procA = processedA.get();
        int procM = processedM.get();
        int procB = processedB.get();
        int totalProcessed = procA + procM + procB;
        System.out.printf("Procesadas: A=%d, M=%d, B=%d | Total=%d%n", procA, procM, procB, totalProcessed);
        
        // Pendientes por tipo y total
        int pendingA = genA - procA;
        int pendingM = genM - procM;
        int pendingB = genB - procB;
        int totalPending = pendingA + pendingM + pendingB;
        System.out.printf("Pendientes: A=%d, M=%d, B=%d | Total=%d%n", pendingA, pendingM, pendingB, totalPending);
        
        // Mensajes específicos
        if (pendingB == 0) {
            System.out.println("Todas las tareas B fueron procesadas: aging funcionó correctamente.");
        } else {
            System.out.println("Quedaron " + pendingB + " tareas B (esperado por starvation).");
        }
        
        // NUEVA SALIDA: Tiempos de espera de tareas B
        System.out.println("\n" + "-".repeat(60));
        System.out.println("ANÁLISIS DE TIEMPOS DE ESPERA (TAREAS B)");
        System.out.println("-".repeat(60));
        
        long maxWait = maxWaitB.get();
        int countB = countWaitB.get();
        long totalWait = totalWaitB.get();
        
        System.out.printf("Tiempo máximo de espera de una tarea B: %d ms (%.2f s)%n", 
            maxWait, maxWait / 1000.0);
        
        if (countB > 0) {
            long avgWait = totalWait / countB;
            System.out.printf("Tiempo promedio de espera de tareas B: %d ms (%.2f s)%n", 
                avgWait, avgWait / 1000.0);
            System.out.printf("Total de tareas B procesadas con medición: %d%n", countB);
        }
        
        if (maxWait > 5000) {
            System.out.println("STARVATION SEVERA: Tareas B esperaron más de 5 segundos");
        } else if (maxWait > 2000) {
            System.out.println("STARVATION MODERADA: Tareas B esperaron más de 2 segundos");
        } else {
            System.out.println("Tiempos de espera aceptables para tareas B");
        }
    }

    private static void producer(int id, BoundedPriorityQueue queue) {
        Random rand = new Random(id * 12345);
        int nextIndex = 0;

        while (!Thread.currentThread().isInterrupted()) {
            Task.Type type;
            if (nextIndex < INITIAL_SEQUENCE.length) {
                type = INITIAL_SEQUENCE[nextIndex++];
            } else {
                double r = rand.nextDouble();
                if (r < 0.1) type = Task.Type.A;
                else if (r < 0.4) type = Task.Type.M;
                else type = Task.Type.B;
            }

            Task task = new Task(type);
            try {
                // put() bloqueará si la cola está llena (capacidad 20)
                queue.put(task);
                
                switch (type) {
                    case A -> generatedA.incrementAndGet();
                    case M -> generatedM.incrementAndGet();
                    case B -> generatedB.incrementAndGet();
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }

            try { Thread.sleep(10); } catch (InterruptedException e) { break; }
        }
    }

    private static void consumer(int id, BoundedPriorityQueue queue) {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Task task = queue.poll(100, TimeUnit.MILLISECONDS);
                if (task == null) continue;

                // Calcular tiempo de espera en cola
                long now = System.currentTimeMillis();
                long waitTime = now - task.creationTime;

                // Registrar tiempos para tareas B
                if (task.type == Task.Type.B) {
                    maxWaitB.updateAndGet(prev -> Math.max(prev, waitTime));
                    totalWaitB.addAndGet(waitTime);
                    countWaitB.incrementAndGet();
                }

                long procTimeMs = switch (task.type) {
                    case A -> 50;
                    case M -> 100;
                    case B -> 150;
                };

                Thread.sleep(procTimeMs);

                switch (task.type) {
                    case A -> processedA.incrementAndGet();
                    case M -> processedM.incrementAndGet();
                    case B -> processedB.incrementAndGet();
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}
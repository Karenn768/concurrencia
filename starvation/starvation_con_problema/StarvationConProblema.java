package starvation.starvation_con_problema;
import java.util.concurrent.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

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

    // Comparador estático: A=0, M=1, B=2
    private static final Comparator<Task> STATIC_PRIORITY_COMPARATOR =
        (t1, t2) -> Integer.compare(t1.type.priority, t2.type.priority);

    private static long startTime;

    public static void main(String[] args) throws InterruptedException {
        BlockingQueue<Task> queue = new PriorityBlockingQueue<>(CAPACITY, STATIC_PRIORITY_COMPARATOR);


        ExecutorService producerPool = Executors.newFixedThreadPool(5);
        ExecutorService consumerPool = Executors.newFixedThreadPool(3);

        // Monitor de estado de cola con snapshots cada segundo
        ScheduledExecutorService monitor = Executors.newSingleThreadScheduledExecutor();

        monitor.scheduleAtFixedRate(() -> {
           long now = System.currentTimeMillis();
           List<Task> copy = new ArrayList<>(queue);

           long pendientesA = copy.stream().filter(t -> t.type == Task.Type.A).count();
           long pendientesM = copy.stream().filter(t -> t.type == Task.Type.M).count();
           long pendientesB = copy.stream().filter(t -> t.type == Task.Type.B).count();

           int procesadasA = processedA.get();
           int procesadasM = processedM.get();
           int procesadasB = processedB.get();

           int generadasA = generatedA.get();
           int generadasM = generatedM.get();
           int generadasB = generatedB.get();

           int sinProcesarA = generadasA - procesadasA;
           int sinProcesarM = generadasM - procesadasM;
           int sinProcesarB = generadasB - procesadasB;

           String state = String.format(
               "t=%.1fs | cola size=%d | Pendientes (A=%d, M=%d, B=%d) | Procesadas (A=%d, M=%d, B=%d) | Sin procesar (A=%d, M=%d, B=%d)",
               (now - startTime) / 1000.0,
               CAPACITY,
               pendientesA, pendientesM, pendientesB,
               procesadasA, procesadasM, procesadasB,
               sinProcesarA, sinProcesarM, sinProcesarB);
       
           System.out.println("[MONITOR] " + state);
       
           // Advertencia starvation
           if (pendientesB > 5) {
               System.out.println("*** ¡ADVERTENCIA! STARVATION DETECTADA ***");
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
        consumerPool.awaitTermination(2, TimeUnit.SECONDS);

        // Resultados finales
    System.out.println("\n" + "=".repeat(60));
    System.out.println("✅ SIMULACIÓN FINALIZADA (CON STARVATION)");
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
        
    // Mensajes específicos sobre B
    if (pendingB == 0) {
        System.out.println("🎉 Todas las tareas B fueron procesadas: aging funcionó correctamente.");
    } else {
        System.out.println("ℹ️ Quedaron " + pendingB + " tareas B (esperado por límite de tiempo).");
    }

    }

    private static void producer(int id, BlockingQueue<Task> queue) {
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
                if (queue.offer(task, 100, TimeUnit.MILLISECONDS)) {
                    switch (type) {
                        case A -> generatedA.incrementAndGet();
                        case M -> generatedM.incrementAndGet();
                        case B -> generatedB.incrementAndGet();
                    }
                    
                }
            
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }

            try { Thread.sleep(100); } catch (InterruptedException e) { break; }
        }
    }

    private static void consumer(int id, BlockingQueue<Task> queue) {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Task task = queue.poll(200, TimeUnit.MILLISECONDS);
                if (task == null) continue;

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

package starvation.starvation_con_solucion;

import java.util.concurrent.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class StarvationConSolucion {

    public static class Task {
        public enum Type {
            A(0), M(1), B(3);
            public final int basePriority;
            Type(int priority) { this.basePriority = priority; }
        }

        public final Type type;
        public final long creationTime;

        public Task(Type type) {
            this.type = type;
            this.creationTime = System.currentTimeMillis();
        }

        public int getEffectivePriority(long now) {
            long age = now - creationTime;
            if (type == Type.B) {
                if (age > 6000) return 0;
                if (age > 3000) return 1;
            }
            return type.basePriority;
        }

        @Override
        public String toString() { return type.name(); }
    }

    private static final int CAPACITY = 20;
    private static final long SIMULATION_TIME_MS = 10_000;

    private static final Task.Type[] INITIAL_SEQUENCE = {
        Task.Type.B, Task.Type.B, Task.Type.M, Task.Type.B, Task.Type.B, Task.Type.B,
        Task.Type.A, Task.Type.M, Task.Type.B, Task.Type.B,
        Task.Type.M, Task.Type.B, Task.Type.B, Task.Type.B, Task.Type.A, Task.Type.B,
        Task.Type.M, Task.Type.B, Task.Type.B, Task.Type.B,
        Task.Type.B, Task.Type.B, Task.Type.B, Task.Type.M, Task.Type.A, Task.Type.B,
        Task.Type.B, Task.Type.M, Task.Type.B, Task.Type.B
    };

    private static final AtomicInteger generatedA = new AtomicInteger();
    private static final AtomicInteger generatedM = new AtomicInteger();
    private static final AtomicInteger generatedB = new AtomicInteger();
    private static final AtomicInteger processedA = new AtomicInteger();
    private static final AtomicInteger processedM = new AtomicInteger();
    private static final AtomicInteger processedB = new AtomicInteger();

    private static long startTime;

    public static void main(String[] args) throws InterruptedException {
        BlockingQueue<Task> queue = new LinkedBlockingQueue<>(CAPACITY);
        ExecutorService producerPool = Executors.newFixedThreadPool(5);
        ExecutorService consumerPool = Executors.newFixedThreadPool(3);

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


            String state = String.format(
                "t=%.1fs | cola size=%d | Pendientes (A=%d, M=%d, B=%d) | Procesadas (A=%d, M=%d, B=%d)",
                (now - startTime) / 1000.0,
                copy.size(),
                pendientesA, pendientesM, pendientesB,
                procesadasA, procesadasM, procesadasB
            );
            System.out.println("[MONITOR] " + state);

            if (pendientesB > 10 && (now - startTime) > 8000) {
                System.out.println("*** ¡ATENCIÓN! Muchas B pendientes ***");
            }
        }, 2, 1, TimeUnit.SECONDS);

        startTime = System.currentTimeMillis();

        for (int i = 0; i < 5; i++) {
            final int id = i + 1;
            producerPool.submit(() -> producer(id, queue));
        }

        for (int i = 0; i < 3; i++) {
            final int id = i + 1;
            consumerPool.submit(() -> smartConsumer(id, queue));
        }

        Thread.sleep(SIMULATION_TIME_MS);

        monitor.shutdownNow();
        producerPool.shutdownNow();
        consumerPool.shutdown();
        consumerPool.awaitTermination(2, TimeUnit.SECONDS);

        System.out.println("\n" + "=".repeat(60));
        System.out.println("✅ SIMULACIÓN FINALIZADA (CON AGING)");
        System.out.println("=".repeat(60));

        int genA = generatedA.get();
        int genM = generatedM.get();
        int genB = generatedB.get();
        int totalGenerated = genA + genM + genB;
        System.out.printf("Generadas: A=%d, M=%d, B=%d | Total=%d%n", genA, genM, genB, totalGenerated);

        int procA = processedA.get();
        int procM = processedM.get();
        int procB = processedB.get();
        int totalProcessed = procA + procM + procB;
        System.out.printf("Procesadas: A=%d, M=%d, B=%d | Total=%d%n", procA, procM, procB, totalProcessed);

        int pendingA = genA - procA;
        int pendingM = genM - procM;
        int pendingB = genB - procB;
        int totalPending = pendingA + pendingM + pendingB;
        System.out.printf("Pendientes: A=%d, M=%d, B=%d | Total=%d%n", pendingA, pendingM, pendingB, totalPending);

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

    // 🚀 Consumidor OPTIMIZADO
    private static void smartConsumer(int id, BlockingQueue<Task> queue) {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                long now = System.currentTimeMillis();
                
                // Inspeccionar sin modificar
                List<Task> snapshot = new LinkedList<>(queue);
                
                if (snapshot.isEmpty()) {
                    Thread.sleep(100);
                    continue;
                }
                
                // Encontrar mejor tarea
                Task best = snapshot.stream()
                    .min((t1, t2) -> {
                        int p1 = t1.getEffectivePriority(now);
                        int p2 = t2.getEffectivePriority(now);
                        if (p1 != p2) return Integer.compare(p1, p2);
                        return Long.compare(t1.creationTime, t2.creationTime);
                    })
                    .orElse(null);
                Thread.sleep(30); 
                if (best == null) continue;
                
                // Extraer SOLO esa tarea
                if (!queue.remove(best)) continue;
                
                // Procesar
                long procTimeMs = switch (best.type) {
                    case A -> 50;
                    case M -> 100;
                    case B -> 150;
                };
                Thread.sleep(procTimeMs);
                
                switch (best.type) {
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

package starvation.starvation_con_solucion;

import java.util.concurrent.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class StarvationConSolucion {

    // Clase anidada estática Task — igual, pero con método de prioridad dinámica
    public static class Task {

        public enum Type {
            A(0),
            M(1),
            B(3);  // mantienes B=3 para prioridad baja, pero aging lo corregirá

            public final int basePriority;

            Type(int priority) {
                this.basePriority = priority;
            }
        }

        public final Type type;
        public final long creationTime;

        public Task(Type type) {
            this.type = type;
            this.creationTime = System.currentTimeMillis();
        }

        // 🔁 Prioridad efectiva con aging
        public int getEffectivePriority(long now) {
            long age = now - creationTime; // en ms
            if (type == Type.B) {
                if (age > 6000) return 0; // >6s → promoción a A
                if (age > 3000) return 1; // >3s → promoción a M
            }
            return type.basePriority;
        }

        @Override
        public String toString() {
            return type.name();
        }
    }

    private static final int CAPACITY = 20;
    private static final long SIMULATION_TIME_MS = 10_000;

    // Secuencia inicial fija (30 tareas) — idéntica
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

    // Contadores atómicos — igual
    private static final AtomicInteger generatedA = new AtomicInteger();
    private static final AtomicInteger generatedM = new AtomicInteger();
    private static final AtomicInteger generatedB = new AtomicInteger();
    private static final AtomicInteger processedA = new AtomicInteger();
    private static final AtomicInteger processedM = new AtomicInteger();
    private static final AtomicInteger processedB = new AtomicInteger();

    private static long startTime;

    public static void main(String[] args) throws InterruptedException {
        // 🔁 Usamos una cola FIFO estándar — la prioridad se aplica al extraer, no al insertar
        BlockingQueue<Task> queue = new LinkedBlockingQueue<>(CAPACITY);

        ExecutorService producerPool = Executors.newFixedThreadPool(5);
        ExecutorService consumerPool = Executors.newFixedThreadPool(3);

        // Monitor — idéntico en formato
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
                copy.size(),
                pendientesA, pendientesM, pendientesB,
                procesadasA, procesadasM, procesadasB,
                sinProcesarA, sinProcesarM, sinProcesarB
            );
            System.out.println("[MONITOR] " + state);

            // ✅ Ya NO mostramos advertencia de starvation (o sólo si es extremo)
            if (pendientesB > 10 && (now - startTime) > 8000) {
                System.out.println("*** ¡ATENCIÓN! Muchas B pendientes — revisar aging ***");
            }
        }, 2, 1, TimeUnit.SECONDS);

        startTime = System.currentTimeMillis();

        // Productores — reutilizamos lógica idéntica
        for (int i = 0; i < 5; i++) {
            final int id = i + 1;
            producerPool.submit(() -> producer(id, queue));
        }

        // 🔁 Consumidores inteligentes con aging
        for (int i = 0; i < 3; i++) {
            final int id = i + 1;
            consumerPool.submit(() -> smartConsumer(id, queue));
        }

        Thread.sleep(SIMULATION_TIME_MS);

        monitor.shutdownNow();
        producerPool.shutdownNow();
        consumerPool.shutdown();
        consumerPool.awaitTermination(2, TimeUnit.SECONDS);

        // Resultados finales — igual estilo
        System.out.println("\n" + "=".repeat(60));
        System.out.println("✅ SIMULACIÓN FINALIZADA (SIN STARVATION – CON AGING)");
        System.out.println("=".repeat(60));
        System.out.printf("Generadas: A=%d, M=%d, B=%d%n",
            generatedA.get(), generatedM.get(), generatedB.get());
        System.out.printf("Procesadas: A=%d, M=%d, B=%d%n",
            processedA.get(), processedM.get(), processedB.get());
        int pendingB = generatedB.get() - processedB.get();
        System.out.printf("→ Tareas B PENDIENTES: %d%n", pendingB);

        if (pendingB == 0) {
            System.out.println("🎉 Todas las tareas B fueron procesadas: aging funcionó correctamente.");
        } else {
            System.out.println("ℹ️ Quedaron " + pendingB + " tareas B (esperado por límite de tiempo).");
        }
    }

    // 🔁 Productor — idéntico (reutilizable)
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

    // 🔁 Consumidor inteligente — el corazón del aging
    private static void smartConsumer(int id, BlockingQueue<Task> queue) {
        while (!Thread.currentThread().isInterrupted()) {
            List<Task> drained = new ArrayList<>();
            Task best = null;

            try {
                // Extraer hasta 10 tareas para evaluar (evita scan completo si cola grande)
                queue.drainTo(drained, 10);

                if (drained.isEmpty()) {
                    Task candidate = queue.poll(200, TimeUnit.MILLISECONDS);
                    if (candidate != null) drained.add(candidate);
                    if (drained.isEmpty()) continue;
                }

                long now = System.currentTimeMillis();

                // Selección: la tarea con menor prioridad efectiva (y más antigua en empate)
                best = drained.stream()
                    .min((t1, t2) -> {
                        int p1 = t1.getEffectivePriority(now);
                        int p2 = t2.getEffectivePriority(now);
                        if (p1 != p2) return Integer.compare(p1, p2);
                        return Long.compare(t1.creationTime, t2.creationTime); // FIFO entre misma prioridad
                    })
                    .orElse(null);

                if (best != null) {
                    drained.remove(best);
                    // Devolver el resto a la cola
                    queue.addAll(drained);
                } else {
                    queue.addAll(drained);
                    continue;
                }

                // Simular procesamiento
                long procTimeMs = switch (best.type) {
                    case A -> 50;
                    case M -> 100;
                    case B -> 150;
                };
                Thread.sleep(procTimeMs);

                // Contar procesadas
                switch (best.type) {
                    case A -> processedA.incrementAndGet();
                    case M -> processedM.incrementAndGet();
                    case B -> processedB.incrementAndGet();
                }

            } catch (InterruptedException e) {
                if (best != null) queue.offer(best);
                queue.addAll(drained);
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                if (best != null) queue.offer(best);
                queue.addAll(drained);
            }
        }
    }
}
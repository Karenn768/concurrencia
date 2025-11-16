package deadlock.deadlock_con_solucion;

import java.util.concurrent.atomic.AtomicInteger;

public class Deadlock_solucion {
    private final int numeroCuenta;
    private double saldo;
    public static AtomicInteger transferenciasExitosas = new AtomicInteger(0);
    private static long startTime; // ✅ Tiempo de inicio en nanosegundos
    
    public Deadlock_solucion(int numeroCuenta, double saldoInicial) {
        this.numeroCuenta = numeroCuenta;
        this.saldo = saldoInicial;
    }
    
    // ✅ Nuevo método: tiempo transcurrido desde el inicio en ms
    private static String getElapsedTime() {
        long elapsedNanos = System.nanoTime() - startTime;
        double elapsedMs = elapsedNanos / 1_000_000.0;
        return String.format("%.2fms", elapsedMs);
    }
    
    public void transferir(Deadlock_solucion destino, double monto) {
        final Deadlock_solucion primera;
        final Deadlock_solucion segunda;
        
        if (this.numeroCuenta < destino.numeroCuenta) {
            primera = this;
            segunda = destino;
        } else {
            primera = destino;
            segunda = this;
        }

        System.out.println("[" + getElapsedTime() + "] 🔄 " + Thread.currentThread().getName() + 
            " INTENTA bloquear cuentas " + primera.numeroCuenta + " y " + segunda.numeroCuenta + 
            " (orden: menor→mayor)");
        
        synchronized (primera) {
            System.out.println("[" + getElapsedTime() + "] 🔒 " + Thread.currentThread().getName() + 
                " BLOQUEÓ cuenta " + primera.numeroCuenta);
            
            synchronized (segunda) {
                System.out.println("[" + getElapsedTime() + "] 🔒 " + Thread.currentThread().getName() + 
                    " BLOQUEÓ cuenta " + segunda.numeroCuenta);
                
                if (this.saldo >= monto) {
                    this.saldo -= monto;
                    destino.saldo += monto;
                    
                    try { Thread.sleep(100); } catch (InterruptedException e) {}
                    
                    System.out.println("[" + getElapsedTime() + "] ✓ " + Thread.currentThread().getName() + 
                        ": Transferencia exitosa " + this.numeroCuenta + "→" + 
                        destino.numeroCuenta + ", $" + String.format("%.0f", monto));
                    
                    transferenciasExitosas.incrementAndGet();
                } else {
                    System.out.println("[" + getElapsedTime() + "] ✗ " + Thread.currentThread().getName() + 
                        ": Saldo insuficiente en cuenta " + this.numeroCuenta +
                        " (saldo: $" + String.format("%.0f", this.saldo) + ")");
                }
            }
            System.out.println("[" + getElapsedTime() + "] ✅ " + Thread.currentThread().getName() + 
                " LIBERÓ cuenta " + segunda.numeroCuenta);
        }
        System.out.println("[" + getElapsedTime() + "] ✅ " + Thread.currentThread().getName() + 
            " LIBERÓ cuenta " + primera.numeroCuenta);
    }
    
    public synchronized double getSaldo() {
        return saldo;
    }
    
    public int getNumeroCuenta() {
        return numeroCuenta;
    }
    
    public static void imprimirTablaTransferencias(int[][][] transferencias) {
        System.out.println("╔════════╦═══════════════════════════╦═══════════════════════════╦═══════════════════════════╗");
        System.out.println("║ Thread ║     Transferencia 1       ║     Transferencia 2       ║     Transferencia 3       ║");
        System.out.println("║        ║   (Origen→Destino, Monto) ║   (Origen→Destino, Monto) ║   (Origen→Destino, Monto) ║");
        System.out.println("╠════════╬═══════════════════════════╬═══════════════════════════╬═══════════════════════════╣");
        
        for (int i = 0; i < transferencias.length; i++) {
            System.out.printf("║   %-4d ║", (i + 1));
            for (int j = 0; j < 3; j++) {
                int origen = transferencias[i][j][0];
                int destino = transferencias[i][j][1];
                int monto = transferencias[i][j][2];
                System.out.printf("      %d→%d, $%-4d          ║", origen, destino, monto);
            }
            System.out.println();
        }
        System.out.println("╚════════╩═══════════════════════════╩═══════════════════════════╩═══════════════════════════╝");
    }
    
    public static void main(String[] args) {
        int[][][] transferencias = {
            {{0,1,200}, {1,2,300}, {2,0,150}},  // Thread 1
            {{1,0,250}, {0,2,100}, {2,1,200}},  // Thread 2
            {{2,3,300}, {3,4,400}, {4,2,250}},  // Thread 3
            {{3,2,350}, {2,4,200}, {4,3,300}},  // Thread 4
            {{4,0,400}, {0,3,250}, {3,4,150}},  // Thread 5
            {{0,4,300}, {4,1,350}, {1,0,200}},  // Thread 6
            {{1,3,250}, {3,0,300}, {0,1,150}},  // Thread 7
            {{2,1,200}, {1,4,250}, {4,2,300}},  // Thread 8
            {{3,1,300}, {1,2,200}, {2,3,250}},  // Thread 9
            {{4,3,350}, {3,2,250}, {2,4,200}}   // Thread 10
        };
        
        Deadlock_solucion[] cuentas = new Deadlock_solucion[5];
        for (int i = 0; i < 5; i++) {
            cuentas[i] = new Deadlock_solucion(i, 1000 * (i + 1));
        }
        
        System.out.println("TABLA DE TRANSFERENCIAS PLANIFICADAS:\n");
        imprimirTablaTransferencias(transferencias);
        
        System.out.println("\n\nSALDOS INICIALES:");
        System.out.println("─────────────────────────────────");
        double totalInicial = 0;
        for (Deadlock_solucion cuenta : cuentas) {
            double saldo = cuenta.getSaldo();
            System.out.printf("  Cuenta %d: $%-6.0f\n", cuenta.getNumeroCuenta(), saldo);
            totalInicial += saldo;
        }
        System.out.println("─────────────────────────────────");
        System.out.printf("  Total:    $%-6.0f\n", totalInicial);
        
        System.out.println("\n\n═══════════════════════════════════════════════════════════════════════════════════════════════════");
        System.out.println("                        EJECUTANDO TRANSFERENCIAS (CONCURRENTEMENTE)");
        
        // ✅ Inicializar el contador de tiempo aquí
        startTime = System.nanoTime();
        long tiempoInicio = System.currentTimeMillis();
        System.out.println("                              Inicio: " + tiempoInicio + " ms desde epoch");
        System.out.println("═══════════════════════════════════════════════════════════════════════════════════════════════════\n");
        
        Thread[] threads = new Thread[10];
        for (int i = 0; i < 10; i++) {
            final int threadNum = i;
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 3; j++) {
                    int origen = transferencias[threadNum][j][0];
                    int destino = transferencias[threadNum][j][1];
                    int monto = transferencias[threadNum][j][2];
                    
                    System.out.println("[" + getElapsedTime() + "] ▶️  " + Thread.currentThread().getName() + 
                        " inicia Transferencia " + (j+1) + ": " + origen + "→" + destino + ", $" + monto);
                    
                    cuentas[origen].transferir(cuentas[destino], monto);
                }
            }, "Thread-" + (i + 1));
        }
        
        System.out.println("🚀 Iniciando threads...\n");
        for (int i = 0; i < 10; i++) {
            threads[i].start();
            try { Thread.sleep(20); } catch (InterruptedException e) {} 
        }
        
        System.out.println("\n⏰ Esperando hasta 3 segundos para que completen...\n");
        
        boolean deadlockDetectado = false;
        for (Thread thread : threads) {
            try {
                thread.join(3000);
                if (thread.isAlive()) {
                    deadlockDetectado = true;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        System.out.println("\n═══════════════════════════════════════════════════════════════════════════════════════════════════");
        System.out.println("                                    RESUMEN FINAL");
        System.out.println("                              Tiempo transcurrido: " + getElapsedTime());
        System.out.println("═══════════════════════════════════════════════════════════════════════════════════════════════════\n");
        
        System.out.println("✅ TRANSFERENCIAS COMPLETADAS: " + transferenciasExitosas.get() + "/30");
        
        System.out.println("\nSALDOS FINALES:");
        System.out.println("─────────────────────────────────");
        double totalFinal = 0;
        for (Deadlock_solucion cuenta : cuentas) {
            double saldo = cuenta.getSaldo();
            System.out.printf("  Cuenta %d: $%-6.0f\n", cuenta.getNumeroCuenta(), saldo);
            totalFinal += saldo;
        }
        System.out.println("─────────────────────────────────");
        System.out.printf("  Total:    $%-6.0f\n", totalFinal);
        
        if (Math.abs(totalFinal - 15000) < 0.01) {
            System.out.println("✅ TOTAL DE SALDOS CORRECTO: $15,000 (conservación del dinero)");
        } else {
            System.out.println("❌ ERROR: Total de saldos incorrecto. Debería ser $15,000");
        }
        
        if (deadlockDetectado) {
            System.out.println("\n  ⚠️  DEADLOCK DETECTADO (esto NO debería ocurrir con ordenamiento):");
            System.out.println("   • Revisar implementación de ordenamiento");
        } else {
            System.out.println("\n✅ Todas las transferencias completadas exitosamente");
        }
    }
}
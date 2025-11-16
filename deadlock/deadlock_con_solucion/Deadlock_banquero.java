package deadlock.deadlock_con_solucion;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public class Deadlock_banquero {
    private final int numeroCuenta;
    private double saldo;
    public static AtomicInteger transferenciasExitosas = new AtomicInteger(0);
    private static long startTime;
    
    // Estructuras del algoritmo del banquero
    private static final int NUM_THREADS = 10;
    private static final int NUM_CUENTAS = 5;
    private static final Object LOCK = new Object();
    
    private static double[] disponible = new double[NUM_CUENTAS];
    private static double[][] maximo = new double[NUM_THREADS][NUM_CUENTAS];
    private static double[][] asignacion = new double[NUM_THREADS][NUM_CUENTAS];
    private static double[][] necesidad = new double[NUM_THREADS][NUM_CUENTAS];
    
    public Deadlock_banquero(int numeroCuenta, double saldoInicial) {
        this.numeroCuenta = numeroCuenta;
        this.saldo = saldoInicial;
    }
    
    private static String getElapsedTime() {
        long elapsedNanos = System.nanoTime() - startTime;
        double elapsedMs = elapsedNanos / 1_000_000.0;
        return String.format("%.2fms", elapsedMs);
    }
    
    public static void inicializarBanquero(int[][][] transferencias, double[] saldosIniciales) {
        synchronized (LOCK) {
            System.out.println("\n🏦 INICIALIZANDO ALGORITMO DEL BANQUERO...\n");
            
            // Copiar recursos disponibles
            System.arraycopy(saldosIniciales, 0, disponible, 0, NUM_CUENTAS);
            
            System.out.println("📊 Recursos disponibles iniciales:");
            for (int i = 0; i < NUM_CUENTAS; i++) {
                System.out.printf("   Cuenta %d: $%.0f\n", i, disponible[i]);
            }
            
            // Limpiar matrices
            for (int i = 0; i < NUM_THREADS; i++) {
                Arrays.fill(maximo[i], 0);
                Arrays.fill(asignacion[i], 0);
                Arrays.fill(necesidad[i], 0);
            }
            
            // Calcular necesidades máximas por thread
            // La necesidad máxima es la suma TOTAL de lo que cada thread extraerá de cada cuenta
            System.out.println("\n📋 Calculando necesidades máximas por thread...");
            for (int t = 0; t < NUM_THREADS; t++) {
                for (int i = 0; i < 3; i++) {
                    int origen = transferencias[t][i][0];
                    int monto = transferencias[t][i][2];
                    
                    // El thread necesita poder extraer este monto de la cuenta origen
                    maximo[t][origen] += monto;
                }
                
                System.out.printf("\n   Thread-%d necesita extraer:", (t + 1));
                for (int c = 0; c < NUM_CUENTAS; c++) {
                    if (maximo[t][c] > 0) {
                        necesidad[t][c] = maximo[t][c];
                        System.out.printf(" Cuenta%d=$%.0f", c, maximo[t][c]);
                    }
                }
            }
            System.out.println("\n\n✅ Algoritmo del banquero inicializado correctamente\n");
        }
    }
    

    private static boolean esEstadoSeguro() {
        double[] trabajo = Arrays.copyOf(disponible, NUM_CUENTAS);
        boolean[] terminado = new boolean[NUM_THREADS];
        int contador = 0;
        
        while (contador < NUM_THREADS) {
            boolean encontrado = false;
            
            for (int i = 0; i < NUM_THREADS; i++) {
                if (!terminado[i]) {
                    boolean puedeCompletar = true;
                    
                    for (int j = 0; j < NUM_CUENTAS; j++) {
                        if (necesidad[i][j] > trabajo[j]) {
                            puedeCompletar = false;
                            break;
                        }
                    }
                    
                    if (puedeCompletar) {
                        for (int j = 0; j < NUM_CUENTAS; j++) {
                            trabajo[j] += asignacion[i][j];
                        }
                        terminado[i] = true;
                        contador++;
                        encontrado = true;
                    }
                }
            }
            
            if (!encontrado) {
                return false;
            }
        }
        
        return true;
    }
    
    public void transferir(Deadlock_banquero destino, double monto, int threadId) {
        transferir(destino, monto, threadId, true);
    }
    
    public void transferir(Deadlock_banquero destino, double monto, int threadId, boolean verbose) {
        if (verbose) {
            System.out.println("[" + getElapsedTime() + "] 🔄 Thread-" + (threadId + 1) + 
                " INTENTA transferencia " + this.numeroCuenta + "→" + destino.numeroCuenta + 
                " por $" + String.format("%.0f", monto) + " (usando lock global del banquero)");
        }
        
        // TODO con el lock global del banquero
        synchronized (LOCK) {
            if (verbose) {
                System.out.println("[" + getElapsedTime() + "] 🏦 Thread-" + (threadId + 1) + 
                    " solicita $" + String.format("%.0f", monto) + " de cuenta " + this.numeroCuenta);
            }
            
            // Verificar si excede necesidad máxima
            if (monto > necesidad[threadId][this.numeroCuenta]) {
                if (verbose) {
                    System.out.println("[" + getElapsedTime() + "] ❌ Thread-" + (threadId + 1) + 
                        " excede necesidad máxima");
                }
                return;
            }
            
            // Verificar recursos disponibles
            if (monto > disponible[this.numeroCuenta]) {
                if (verbose) {
                    System.out.println("[" + getElapsedTime() + "] ⏳ Thread-" + (threadId + 1) + 
                        " espera recursos");
                }
                return;
            }
            
            // Simular asignación
            disponible[this.numeroCuenta] -= monto;
            asignacion[threadId][this.numeroCuenta] += monto;
            necesidad[threadId][this.numeroCuenta] -= monto;
            
            // Verificar estado seguro
            if (verbose) {
                System.out.println("[" + getElapsedTime() + "] 🔍 Thread-" + (threadId + 1) + 
                    " verificando estado seguro...");
            }
            
            if (!esEstadoSeguro()) {
                // Revertir
                disponible[this.numeroCuenta] += monto;
                asignacion[threadId][this.numeroCuenta] -= monto;
                necesidad[threadId][this.numeroCuenta] += monto;
                
                if (verbose) {
                    System.out.println("[" + getElapsedTime() + "] ⚠️  Thread-" + (threadId + 1) + 
                        " RECHAZADO - Estado inseguro");
                }
                return;
            }
            
            if (verbose) {
                System.out.println("[" + getElapsedTime() + "] ✅ Thread-" + (threadId + 1) + 
                    " APROBADO - Estado seguro mantiene");
            }
            
            // Realizar la transferencia real (SIN synchronized adicional)
            if (verbose) {
                System.out.println("[" + getElapsedTime() + "] 🔒 Thread-" + (threadId + 1) + 
                    " accede a cuentas " + this.numeroCuenta + " y " + destino.numeroCuenta + 
                    " (protegido por lock global)");
            }
            
            if (this.saldo >= monto) {
                this.saldo -= monto;
                destino.saldo += monto;
                
                try { Thread.sleep(100); } catch (InterruptedException e) {}
                
                if (verbose) {
                    System.out.println("[" + getElapsedTime() + "] ✓ Thread-" + (threadId + 1) + 
                        ": Transferencia exitosa " + this.numeroCuenta + "→" + 
                        destino.numeroCuenta + ", $" + String.format("%.0f", monto));
                }
                
                transferenciasExitosas.incrementAndGet();
            } else {
                if (verbose) {
                    System.out.println("[" + getElapsedTime() + "] ✗ Thread-" + (threadId + 1) + 
                        ": Saldo insuficiente en cuenta " + this.numeroCuenta +
                        " (saldo: $" + String.format("%.0f", this.saldo) + ")");
                }
            }
            
            // Liberar recurso
            disponible[this.numeroCuenta] += monto;
            asignacion[threadId][this.numeroCuenta] -= monto;
            necesidad[threadId][this.numeroCuenta] += monto;
            
            if (verbose) {
                System.out.println("[" + getElapsedTime() + "] 🔓 Thread-" + (threadId + 1) + 
                    " liberó recursos de cuenta " + this.numeroCuenta);
            }
        } // fin del synchronized(LOCK)
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
        
        double[] saldosIniciales = {1000, 2000, 3000, 4000, 5000};
        
        Deadlock_banquero[] cuentas = new Deadlock_banquero[5];
        for (int i = 0; i < 5; i++) {
            cuentas[i] = new Deadlock_banquero(i, saldosIniciales[i]);
        }
        
        System.out.println("TABLA DE TRANSFERENCIAS PLANIFICADAS:\n");
        imprimirTablaTransferencias(transferencias);
        
        System.out.println("\n\nSALDOS INICIALES:");
        System.out.println("─────────────────────────────────");
        double totalInicial = 0;
        for (Deadlock_banquero cuenta : cuentas) {
            double saldo = cuenta.getSaldo();
            System.out.printf("  Cuenta %d: $%-6.0f\n", cuenta.getNumeroCuenta(), saldo);
            totalInicial += saldo;
        }
        System.out.println("─────────────────────────────────");
        System.out.printf("  Total:    $%-6.0f\n", totalInicial);
        
        // Inicializar banquero
        inicializarBanquero(transferencias, saldosIniciales);
        
        System.out.println("\n═══════════════════════════════════════════════════════════════════════════════════════════════════");
        System.out.println("              EJECUTANDO TRANSFERENCIAS CON ALGORITMO DEL BANQUERO");
        
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
                    
                    System.out.println("[" + getElapsedTime() + "] ▶️  Thread-" + (threadNum + 1) + 
                        " inicia Transferencia " + (j+1) + ": " + origen + "→" + destino + ", $" + monto);
                    
                    cuentas[origen].transferir(cuentas[destino], monto, threadNum);
                }
            }, "Thread-" + (i + 1));
        }
        
        System.out.println("🚀 Iniciando threads...\n");
        for (int i = 0; i < 10; i++) {
            threads[i].start();
            try { Thread.sleep(20); } catch (InterruptedException e) {} 
        }
        
        System.out.println("\n⏰ Esperando hasta 30 segundos para que completen...\n");
        
        boolean timeout = false;
        for (Thread thread : threads) {
            try {
                thread.join(30000);
                if (thread.isAlive()) {
                    timeout = true;
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
        for (Deadlock_banquero cuenta : cuentas) {
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
        
        if (timeout) {
            System.out.println("\n⚠️  TIMEOUT: Algunos threads no completaron (posible livelock)");
        } else {
            System.out.println("\n✅ Todas las transferencias completadas - Sin deadlock garantizado");
        }
    }
}
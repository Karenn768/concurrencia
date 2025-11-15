import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

class CuentaBancaria {
    private final int numeroCuenta;
    private double saldo;
    private static final DateTimeFormatter formatter = 
        DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
    
    public CuentaBancaria(int numeroCuenta, double saldoInicial) {
        this.numeroCuenta = numeroCuenta;
        this.saldo = saldoInicial;
    }
    
    private static String getTimestamp() {
        return LocalDateTime.now().format(formatter);
    }
    
    public void transferir(CuentaBancaria destino, double monto) {
        // PASO 1: Intenta bloquear la cuenta ORIGEN
        System.out.println("[" + getTimestamp() + "] 🔄 " + Thread.currentThread().getName() + 
            " INTENTA bloquear cuenta " + this.numeroCuenta);
        
        synchronized (this) {
            System.out.println("[" + getTimestamp() + "] 🔒 " + Thread.currentThread().getName() + 
                " BLOQUEÓ cuenta " + this.numeroCuenta + 
                " | Ahora intenta bloquear cuenta " + destino.numeroCuenta);
            
            if (this.saldo >= monto) {
                this.saldo -= monto;
                
                // Simular tiempo de procesamiento
                try { Thread.sleep(100); } catch (InterruptedException e) {}
                
                // PASO 2: Intenta bloquear la cuenta DESTINO (AQUÍ PUEDE OCURRIR DEADLOCK)
                System.out.println("[" + getTimestamp() + "] ⏳ " + Thread.currentThread().getName() + 
                    " INTENTA bloquear cuenta " + destino.numeroCuenta);
                
                synchronized (destino) {
                    System.out.println("[" + getTimestamp() + "] 🔒 " + Thread.currentThread().getName() + 
                        " BLOQUEÓ cuenta " + destino.numeroCuenta);
                    
                    destino.depositar(monto);
                    System.out.println("[" + getTimestamp() + "] ✓ " + Thread.currentThread().getName() + 
                        ": Transferencia exitosa " + this.numeroCuenta + "→" + 
                        destino.numeroCuenta + ", $" + String.format("%.0f", monto));
                }
                
                System.out.println("[" + getTimestamp() + "] ✅ " + Thread.currentThread().getName() + 
                    " LIBERÓ cuenta " + destino.numeroCuenta);
            } else {
                System.out.println("[" + getTimestamp() + "] ✗ " + Thread.currentThread().getName() + 
                    ": Saldo insuficiente en cuenta " + this.numeroCuenta);
            }
        }
        
        System.out.println("[" + getTimestamp() + "] ✅ " + Thread.currentThread().getName() + 
            " LIBERÓ cuenta " + this.numeroCuenta);
    }
    
    private synchronized void depositar(double monto) {
        this.saldo += monto;
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
        
        CuentaBancaria[] cuentas = new CuentaBancaria[5];
        for (int i = 0; i < 5; i++) {
            cuentas[i] = new CuentaBancaria(i, 1000 * (i + 1));
        }
         
        System.out.println("TABLA DE TRANSFERENCIAS PLANIFICADAS:\n");
        imprimirTablaTransferencias(transferencias);
        
        System.out.println("\n\nSALDOS INICIALES:");
        System.out.println("─────────────────────────────────");
        double totalInicial = 0;
        for (CuentaBancaria cuenta : cuentas) {
            double saldo = cuenta.getSaldo();
            System.out.printf("  Cuenta %d: $%-6.0f\n", cuenta.getNumeroCuenta(), saldo);
            totalInicial += saldo;
        }
        System.out.println("─────────────────────────────────");
        System.out.printf("  Total:    $%-6.0f\n", totalInicial);
        
        System.out.println("\n\n═══════════════════════════════════════════════════════════════════════════════════════════════════");
        System.out.println("                        EJECUTANDO TRANSFERENCIAS (CONCURRENTEMENTE)");
        System.out.println("                              Inicio: " + LocalDateTime.now().format(
            DateTimeFormatter.ofPattern("HH:mm:ss.SSS")));
        System.out.println("═══════════════════════════════════════════════════════════════════════════════════════════════════\n");
        
        Thread[] threads = new Thread[10];
        for (int i = 0; i < 10; i++) {
            final int threadNum = i;
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 3; j++) {
                    int origen = transferencias[threadNum][j][0];
                    int destino = transferencias[threadNum][j][1];
                    int monto = transferencias[threadNum][j][2];
                    
                    System.out.println("[" + getTimestamp() + "] ▶️  " + Thread.currentThread().getName() + 
                        " inicia Transferencia " + (j+1) + ": " + origen + "→" + destino + ", $" + monto);
                    
                    cuentas[origen].transferir(cuentas[destino], monto);
                    
                    try { Thread.sleep(15); } catch (InterruptedException e) {}
                }
                System.out.println("[" + getTimestamp() + "] 🏁 " + Thread.currentThread().getName() + 
                    " COMPLETÓ todas sus transferencias");
            }, "Thread-" + (i + 1));
        }
        
        System.out.println("🚀 Iniciando threads en orden secuencial...\n");
        for (int i = 0; i < 10; i++) {
            System.out.println("[" + getTimestamp() + "] Iniciando " + threads[i].getName() + "...");
            threads[i].start();
            try { Thread.sleep(17); } catch (InterruptedException e) {}
        }
        
        System.out.println("\n⏰ Todos los threads iniciados. Esperando hasta 3 segundos para que completen...\n");
        
        boolean deadlockDetectado = false;
        for (Thread thread : threads) {
            try {
                thread.join(300);
                if (thread.isAlive()) {
                    deadlockDetectado = true;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        System.out.println("\n═══════════════════════════════════════════════════════════════════════════════════════════════════");
        System.out.println("                                    RESUMEN FINAL");
        System.out.println("                              Fin: " + LocalDateTime.now().format(
            DateTimeFormatter.ofPattern("HH:mm:ss.SSS")));
        System.out.println("═══════════════════════════════════════════════════════════════════════════════════════════════════\n");
        
        System.out.println("SALDOS FINALES:");
        System.out.println("─────────────────────────────────");
        double totalFinal = 0;
        for (CuentaBancaria cuenta : cuentas) {
            double saldo = cuenta.getSaldo();
            System.out.printf("  Cuenta %d: $%-6.0f\n", cuenta.getNumeroCuenta(), saldo);
            totalFinal += saldo;
        }
        System.out.println("─────────────────────────────────");
        System.out.printf("  Total:    $%-6.0f\n", totalFinal);
        
        if (deadlockDetectado) {
            System.out.println("\n  DEADLOCK DETECTADO:");
            System.out.println("   • El sistema no pudo completar todas las transferencias");
            System.out.println("   • Causa: Ciclos de espera circular entre threads");
            System.out.println("   • Solución: Implementar ordenamiento consistente de locks");
        } else {
            System.out.println("\n✓ Todas las transferencias completadas exitosamente");
        }
    }
}

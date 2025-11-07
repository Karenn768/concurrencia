public class App {
    public static void main(String[] args) {
        final Object recurso1 = new Object();
        final Object recurso2 = new Object();

        Thread hilo1 = new Thread(() -> {
            synchronized (recurso1) {
                System.out.println("Hilo 1: Bloqueó recurso 1");

                try { Thread.sleep(100); } catch (InterruptedException e) {}

                System.out.println("Hilo 1: Intentando bloquear recurso 2");
                synchronized (recurso2) {
                    System.out.println("Hilo 1: Bloqueó recurso 2");
                }
            }
        });

        Thread hilo2 = new Thread(() -> {
            synchronized (recurso2) {
                System.out.println("Hilo 2: Bloqueó recurso 2");

                try { Thread.sleep(100); } catch (InterruptedException e) {}

                System.out.println("Hilo 2: Intentando bloquear recurso 1");
                synchronized (recurso1) {
                    System.out.println("Hilo 2: Bloqueó recurso 1");
                }
            }
        });

        hilo1.start();
        hilo2.start();
    }
}
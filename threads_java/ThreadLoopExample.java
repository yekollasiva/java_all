public class ThreadLoopExample {
    
    private static int counter = 0;
    
    
    public static void main(String[] args) throws InterruptedException {
        
        System.out.println("=== Unsafe Counter ===");
        counter = 0;
        
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 1000; i++) {
                counter++;  // Not atomic! This is read-modify-write
            }
        });
        
        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 1000; i++) {
                counter++;
            }
        });
        
        t1.start();
        t2.start();
        
        t1.join();
        t2.join();
        
        counter = 0;
        
        Object lock = new Object();
        
        Thread t3 = new Thread(() -> {
            for (int i = 0; i < 1000; i++) {
                synchronized (lock) {
                    counter++;
                }
            }
        });
        
        Thread t4 = new Thread(() -> {
            for (int i = 0; i < 1000; i++) {
                synchronized (lock) {
                    counter++;
                }
            }
        });
        
        t3.start();
        t4.start();
        t3.join();
        t4.join();
        
        java.util.concurrent.atomic.AtomicInteger atomicCounter = 
            new java.util.concurrent.atomic.AtomicInteger(0);
        
        Thread t5 = new Thread(() -> {
            for (int i = 0; i < 1000; i++) {
                atomicCounter.incrementAndGet();  // Atomic operation
            }
        });
        
        Thread t6 = new Thread(() -> {
            for (int i = 0; i < 1000; i++) {
                atomicCounter.incrementAndGet();
            }
        });
        
        t5.start();
        t6.start();
        t5.join();
        t6.join();
        
        System.out.println("Final counter (Atomic): " + atomicCounter.get());
    }
}
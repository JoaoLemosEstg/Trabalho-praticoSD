package sd.traffic.node;

import sd.traffic.common.Vehicle;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class SemaphoreController extends Thread {

    private final String direction;
    private final BlockingQueue<Vehicle> fila = new LinkedBlockingQueue<>();

    private final long greenMs;
    private final long redMs;

    private volatile boolean green = false;

    private final VehicleDispatcher dispatcher;

    private int maxQueue = 0;
    private long totalProcessed = 0;

    public interface VehicleDispatcher {
        void onVehicleReadyToLeave(Vehicle v);
    }

    public SemaphoreController(String direction, long greenMs, long redMs,
                               VehicleDispatcher dispatcher) {
        this.direction = direction;
        this.greenMs = greenMs;
        this.redMs = redMs;
        this.dispatcher = dispatcher;
    }

    public void enqueue(Vehicle v) {
        fila.add(v);
        maxQueue = Math.max(maxQueue, fila.size());
    }

    public int getQueueSize() { return fila.size(); }
    public int getMaxQueue() { return maxQueue; }
    public long getTotalProcessed() { return totalProcessed; }

    @Override
    public void run() {
        while (true) {
            try {
                // Verde
                green = true;
                long start = System.currentTimeMillis();
                while (System.currentTimeMillis() - start < greenMs) {
                    Vehicle v = fila.poll();
                    if (v != null) {
                        // tempo de passagem no semáforo (tsem)
                        long tsemMs = 500; // TODO: mete em config
                        Thread.sleep(tsemMs);
                        dispatcher.onVehicleReadyToLeave(v);
                        totalProcessed++;
                    } else {
                        Thread.sleep(50);
                    }
                }

                // Vermelho
                green = false;
                Thread.sleep(redMs);

            } catch (InterruptedException e) {
                return;
            }
        }
    }

    public String getDirection() { return direction; }
}

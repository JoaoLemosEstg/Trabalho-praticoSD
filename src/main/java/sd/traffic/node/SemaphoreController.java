package sd.traffic.node;

import sd.traffic.common.Vehicle;
import sd.traffic.common.EventLog;


import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class SemaphoreController extends Thread {

    private final String direction;
    private final BlockingQueue<Vehicle> fila = new LinkedBlockingQueue<>();

    private final long greenMs;
    private final long redMs;
    private final String nodeId;

    private volatile boolean green = false;

    private final VehicleDispatcher dispatcher;

    private int maxQueue = 0;
    private long totalProcessed = 0;
    private long totalWaitTimeMs = 0;
    private long maxWaitTimeMs = 0;


    public interface VehicleDispatcher {
        void onVehicleReadyToLeave(Vehicle v);
    }

    public SemaphoreController(String nodeId,String direction, long greenMs, long redMs,
                               VehicleDispatcher dispatcher) {
        this.nodeId = nodeId;
        this.direction = direction;
        this.greenMs = greenMs;
        this.redMs = redMs;
        this.dispatcher = dispatcher;
    }

    public void enqueue(Vehicle v) {
        long now = System.currentTimeMillis();
        v.setEnterQueueTime(now);
        fila.add(v);
        maxQueue = Math.max(maxQueue, fila.size());

        EventLog.log(
                nodeId,
                v.getId(),
                "QUEUE_ENTER",
                "dir=" + direction + " queueSize=" + fila.size()
        );
    }



    public int getQueueSize() { return fila.size(); }
    public int getMaxQueue() { return maxQueue; }
    public long getTotalProcessed() { return totalProcessed; }
    public long getTotalWaitTimeMs() { return totalWaitTimeMs; }
    public long getMaxWaitTimeMs() { return maxWaitTimeMs; }


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
                        long now = System.currentTimeMillis();
                        long wait = now - v.getEnterQueueTime();
                        totalWaitTimeMs += wait;
                        if (wait > maxWaitTimeMs) {
                            maxWaitTimeMs = wait;
                        }

                        EventLog.log(
                                nodeId,
                                v.getId(),
                                "QUEUE_LEAVE",
                                "dir=" + direction + " waitMs=" + wait
                        );

                        v.setEnterRoadTime(now);

                        long tsemMs = 500; // tempo de passagem no semáforo
                        Thread.sleep(tsemMs);

                        EventLog.log(
                                nodeId,
                                v.getId(),
                                "SEM_PASS",
                                "dir=" + direction + " tsemMs=" + tsemMs
                        );

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

package sd.traffic.common;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

public class Vehicle implements Serializable {
    private final String id;
    private final VehicleType type;
    private final String entryPoint; // E1/E2/E3
    private long arrivalTimeSystem;
    private long exitTimeSystem;
    private long enterQueueTime;   // quando entra na fila de um semáforo
    private long enterRoadTime;    // quando sai do semáforo (entra na estrada)
    private final List<String> path; // sequência de nós: E1, Cr1, Cr2, ..., S
    private int currentIndex = 0;

    public Vehicle(VehicleType type, String entryPoint, List<String> path, long arrivalTime) {
        this.id = UUID.randomUUID().toString();
        this.type = type;
        this.entryPoint = entryPoint;
        this.path = path;
        this.arrivalTimeSystem = arrivalTime;
    }

    public String getId() { return id; }
    public VehicleType getType() { return type; }
    public String getEntryPoint() { return entryPoint; }

    public List<String> getPath() { return path; }

    public synchronized String getCurrentNode() {
        return path.get(currentIndex);
    }

    public synchronized String getNextNode() {
        if (currentIndex + 1 < path.size()) {
            return path.get(currentIndex + 1);
        }
        return null;
    }

    public synchronized void advance() {
        if (currentIndex + 1 < path.size()) {
            currentIndex++;
        }
    }

    public long getArrivalTimeSystem() { return arrivalTimeSystem; }
    public void setExitTimeSystem(long exitTimeSystem) { this.exitTimeSystem = exitTimeSystem; }
    public long getExitTimeSystem() { return exitTimeSystem; }

    public long getEnterQueueTime() {
        return enterQueueTime;
    }

    public void setEnterQueueTime(long enterQueueTime) {
        this.enterQueueTime = enterQueueTime;
    }

    public long getEnterRoadTime() {
        return enterRoadTime;
    }

    public void setEnterRoadTime(long enterRoadTime) {
        this.enterRoadTime = enterRoadTime;
    }

}


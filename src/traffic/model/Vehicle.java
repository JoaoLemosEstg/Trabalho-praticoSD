package traffic.model;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

public class Vehicle implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String id;
    private final VehicleType type;
    private final NodeId entryPoint;
    private final List<NodeId> path;

    private final double entryTime;
    private Double exitTime; // pode ser null até sair

    public Vehicle(VehicleType type, NodeId entryPoint, List<NodeId> path, double entryTime) {
        this.id = UUID.randomUUID().toString();
        this.type = type;
        this.entryPoint = entryPoint;
        this.path = path;
        this.entryTime = entryTime;
    }

    public String getId() {
        return id;
    }

    public VehicleType getType() {
        return type;
    }

    public NodeId getEntryPoint() {
        return entryPoint;
    }

    public List<NodeId> getPath() {
        return path;
    }

    public double getEntryTime() {
        return entryTime;
    }

    public Double getExitTime() {
        return exitTime;
    }

    public void setExitTime(Double exitTime) {
        this.exitTime = exitTime;
    }

    @Override
    public String toString() {
        return "Vehicle{" +
                "id='" + id + '\'' +
                ", type=" + type +
                ", entryPoint=" + entryPoint +
                ", path=" + path +
                ", entryTime=" + entryTime +
                ", exitTime=" + exitTime +
                '}';
    }
}

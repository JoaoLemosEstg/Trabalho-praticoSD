package traffic.net;

import traffic.model.NodeId;
import traffic.model.Vehicle;

import java.io.Serializable;

public class Message implements Serializable {

    private static final long serialVersionUID = 1L;

    private final MessageType type;
    private final NodeId from;
    private final NodeId to;
    private final Vehicle vehicle;

    public Message(MessageType type, NodeId from, NodeId to, Vehicle vehicle) {
        this.type = type;
        this.from = from;
        this.to = to;
        this.vehicle = vehicle;
    }

    public MessageType getType() {
        return type;
    }

    public NodeId getFrom() {
        return from;
    }

    public NodeId getTo() {
        return to;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    @Override
    public String toString() {
        return "Message{" +
                "type=" + type +
                ", from=" + from +
                ", to=" + to +
                ", vehicle=" + (vehicle != null ? vehicle.getId() : "null") +
                '}';
    }
}

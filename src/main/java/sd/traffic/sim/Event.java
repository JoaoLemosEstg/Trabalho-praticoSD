package sd.traffic.sim;

import sd.traffic.common.Vehicle;

public class Event implements Comparable<Event> {
    private final long time;  // em ms de relógio de simulação
    private final EventType type;
    private final Vehicle vehicle;
    private final String nodeId;

    public Event(long time, EventType type, Vehicle vehicle, String nodeId) {
        this.time = time;
        this.type = type;
        this.vehicle = vehicle;
        this.nodeId = nodeId;
    }

    public long getTime() { return time; }
    public EventType getType() { return type; }
    public Vehicle getVehicle() { return vehicle; }
    public String getNodeId() { return nodeId; }

    @Override
    public int compareTo(Event o) {
        return Long.compare(this.time, o.time);
    }
}

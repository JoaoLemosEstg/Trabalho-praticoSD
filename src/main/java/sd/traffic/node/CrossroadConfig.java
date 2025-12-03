package sd.traffic.node;

import java.util.Map;

public class CrossroadConfig {
    public String nodeId;
    public int listenPort;
    public Map<String, HostPort> nextHop;
    public int dashboardPort;
    public long statsIntervalMs = 2000;
    public long baseTravelTimeMs = 1000; // por exemplo
}

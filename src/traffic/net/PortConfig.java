package traffic.net;

import traffic.model.NodeId;

import java.util.Map;

public class PortConfig {


    private static final Map<NodeId, Integer> PORTS = Map.of(
            NodeId.CR1, 5001,
            NodeId.CR2, 5002,
            NodeId.CR3, 5003,
            NodeId.CR4, 5004,
            NodeId.CR5, 5005,
            NodeId.S,   5010
    );

    public static int getPort(NodeId nodeId) {
        Integer port = PORTS.get(nodeId);
        if (port == null) {
            throw new IllegalArgumentException("Sem porta definida para " + nodeId);
        }
        return port;
    }
}

package sd.traffic.common;

import java.io.Serializable;

public class StatsSnapshot implements Serializable {
    public String nodeId;
    public long timestamp;
    public int filaN, filaS, filaE, filaW;
    public int maxFilaN, maxFilaS, maxFilaE, maxFilaW;
    public long totalVehiclesProcessed;
}

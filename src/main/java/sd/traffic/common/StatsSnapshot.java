package sd.traffic.common;

import java.io.Serializable;

public class StatsSnapshot implements Serializable {
    public String nodeId;
    public long timestamp;

    public int currentQueue;
    public int maxQueue;
    public double avgQueue;
    public double avgWaitTimeMs;
    public long   maxWaitTimeMs;
    public double avgTravelTimeMs;


    public int filaN, filaS, filaE, filaW;
    public int maxFilaN, maxFilaS, maxFilaE, maxFilaW;


    public long totalVehiclesProcessed;


    public long processedMoto;
    public long processedCarro;
    public long processedCamiao;


    public long MinMoto, AvgMoto, MaxMoto;
    public long MinCarro, AvgCarro, MaxCarro;
    public long MinCamiao, AvgCamiao, MaxCamiao;
}
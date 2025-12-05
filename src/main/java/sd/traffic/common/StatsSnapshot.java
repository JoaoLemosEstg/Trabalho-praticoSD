package sd.traffic.common;

import java.io.Serializable;

public class StatsSnapshot implements Serializable {
    public String nodeId;
    public long timestamp;

    public int currentQueue;
    public int maxQueue;
    public double avgQueue;
    public double avgWaitTimeMs;     // tempo médio de espera na fila
    public long   maxWaitTimeMs;     // maior tempo de espera observado
    public double avgTravelTimeMs;   // tempo médio de deslocamento entre nós


    public int filaN, filaS, filaE, filaW;
    public int maxFilaN, maxFilaS, maxFilaE, maxFilaW;


    public long totalVehiclesProcessed;

    // ---------------------------
    // Contagem de veículos por tipo
    // ---------------------------
    // Usado tanto nos cruzamentos como no nó S
    public long processedMoto;
    public long processedCarro;
    public long processedCamiao;

    // ---------------------------
    // Estatísticas globais de dwelling time (só S)
    // ---------------------------
    // tempos em milissegundos
    public long MinMoto, AvgMoto, MaxMoto;
    public long MinCarro, AvgCarro, MaxCarro;
    public long MinCamiao, AvgCamiao, MaxCamiao;
}
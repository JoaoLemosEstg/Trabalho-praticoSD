package sd.traffic.node;

import sd.traffic.common.StatsSnapshot;
import sd.traffic.common.Vehicle;
import sd.traffic.common.VehicleType;
import sd.traffic.common.EventLog;


import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class CrossroadProcess {
    // estatísticas de tempos de deslocamento (ruas deste nó)
    private long totalTravelTimeMs = 0;
    private long totalTravelCount = 0;


    private final CrossroadConfig config;

    // chave = id do próximo nó (ex: "Cr2", "Cr4", "Cr5", "S")
    private final Map<String, SemaphoreController> semaphores = new HashMap<>();

    // contagem de veículos por tipo que passaram por este cruzamento
    private final Map<VehicleType, Long> processedByType = new EnumMap<>(VehicleType.class);

    private ServerSocket serverSocket;

    public CrossroadProcess(CrossroadConfig config) {
        this.config = config;
        for (VehicleType t : VehicleType.values()) {
            processedByType.put(t, 0L);
        }
    }

    public void start() throws IOException {
        // 1) Criar um semáforo por saída configurada em nextHop
        for (String nextNodeId : config.nextHop.keySet()) {
            SemaphoreController sc = new SemaphoreController(
                    config.nodeId,  // nodeId deste cruzamento
                    nextNodeId,     // direção
                    3000,
                    3000,
                    this::dispatchVehicle
            );

            semaphores.put(nextNodeId, sc);
            sc.start();
        }

        startDashboardReporter();
        startReceiver();
    }

    private void startReceiver() throws IOException {
        serverSocket = new ServerSocket(config.listenPort);

        Thread t = new Thread(() -> {
            while (true) {
                try (Socket s = serverSocket.accept()) {
                    ObjectInputStream in = new ObjectInputStream(s.getInputStream());
                    Vehicle v = (Vehicle) in.readObject();

                    // o veículo já vem com currentNode = este nó (porque no nó anterior fizemos v.advance())
                    String next = v.getNextNode();

                    if (next == null) {
                        // isto só faria sentido se este nó fosse o S;
                        System.err.println("Vehicle " + v.getId() +
                                " chegou a " + config.nodeId +
                                " mas não tem próximo nó no caminho.");
                        continue;
                    }

                    SemaphoreController sc = semaphores.get(next);
                    if (sc == null) {
                        System.err.println("Sem semáforo para saída '" + next +
                                "' em " + config.nodeId);
                    } else {
                        sc.enqueue(v);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        t.setDaemon(true);
        t.start();
    }

    private void dispatchVehicle(Vehicle v) {
        // registar que este veículo foi processado neste nó
        synchronized (processedByType) {
            VehicleType type = v.getType();
            processedByType.put(type, processedByType.get(type) + 1);
        }

        // 3) Simular tempo de deslocamento entre este nó e o próximo
        String next = v.getNextNode(); // próximo nó ANTES de avançar

        if (next == null) {
            // se isto acontecer aqui, significa que este nó devia ser o S
            v.setExitTimeSystem(System.currentTimeMillis());
            return;
        }

        HostPort hp = config.nextHop.get(next);
        InetSocketAddress addr =
                new InetSocketAddress(hp.host, hp.port);

        if (addr == null) {
            System.err.println("Sem nextHop configurado para " + next +
                    " em " + config.nodeId);
            return;
        }

        // tempo de deslocamento na rua entre nós (t * fator do tipo)
        // tempo de deslocamento na rua entre nós (t * fator do tipo)
        long travelMs;
        EventLog.log(
                config.nodeId,
                v.getId(),
                "MOVE_START",
                "to=" + next
        );

        try {
            double factor = v.getType().travelTimeFactor();
            long base = config.baseTravelTimeMs; // definir no CrossroadConfig
            travelMs = (long) (base * factor);

            // acumular estatísticas de deslocamento
            synchronized (this) {
                totalTravelTimeMs += travelMs;
                totalTravelCount++;
            }

            Thread.sleep(travelMs);
            EventLog.log(
                    config.nodeId,
                    v.getId(),
                    "MOVE_END",
                    "to=" + next + " travelMs=" + travelMs
            );

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }



        // agora avançamos no caminho e enviamos para o nó seguinte
        v.advance();

        try (Socket s = new Socket(addr.getHostName(), addr.getPort())) {
            ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
            out.writeObject(v);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startDashboardReporter() {
        Thread t = new Thread(() -> {
            while (true) {
                try {
                    StatsSnapshot snap = new StatsSnapshot();
                    snap.nodeId = config.nodeId;
                    snap.timestamp = System.currentTimeMillis();

                    int totalQueue = 0;
                    int maxQueue = 0;
                    long totalProcessed = 0;

                    long totalWaitTimeMs = 0;
                    long maxWaitTimeMs = 0;

                    for (SemaphoreController sc : semaphores.values()) {
                        int q = sc.getQueueSize();
                        totalQueue += q;
                        maxQueue = Math.max(maxQueue, sc.getMaxQueue());
                        totalProcessed += sc.getTotalProcessed();

                        totalWaitTimeMs += sc.getTotalWaitTimeMs();
                        maxWaitTimeMs = Math.max(maxWaitTimeMs, sc.getMaxWaitTimeMs());
                    }


                    // novos campos de filas no StatsSnapshot
                    // novos campos de filas no StatsSnapshot
                    snap.currentQueue = totalQueue;
                    snap.maxQueue = maxQueue;
                    snap.avgQueue = semaphores.isEmpty()
                            ? 0.0
                            : (double) totalQueue / semaphores.size();

                    // se ainda quiseres aproveitar os antigos
                    snap.filaN = totalQueue;
                    snap.maxFilaN = maxQueue;

                    snap.totalVehiclesProcessed = totalProcessed;

                    // tempo médio de espera na fila neste nó
                    if (totalProcessed > 0) {
                        snap.avgWaitTimeMs = (double) totalWaitTimeMs / totalProcessed;
                    } else {
                        snap.avgWaitTimeMs = 0.0;
                    }
                    snap.maxWaitTimeMs = maxWaitTimeMs;

                    // tempo médio de deslocamento neste nó (entre nós)
                    synchronized (this) {
                        if (totalTravelCount > 0) {
                            snap.avgTravelTimeMs = (double) totalTravelTimeMs / totalTravelCount;
                        } else {
                            snap.avgTravelTimeMs = 0.0;
                        }
                    }

                    // contagem de veículos por tipo neste cruzamento
                    synchronized (processedByType) {
                        snap.processedMoto   = processedByType.get(VehicleType.MOTO);
                        snap.processedCarro  = processedByType.get(VehicleType.CARRO);
                        snap.processedCamiao = processedByType.get(VehicleType.CAMIAO);
                    }


                    sendStatsToDashboard(snap);
                    Thread.sleep(config.statsIntervalMs);
                } catch (InterruptedException e) {
                    return;
                }
            }
        });
        t.setDaemon(true);
        t.start();
    }

    private void sendStatsToDashboard(StatsSnapshot snap) {
        try (Socket s = new Socket("localhost", config.dashboardPort)) {
            ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
            out.writeObject(snap);
            out.flush();
        } catch (IOException e) {
            // ignora ou loga
        }
    }
}

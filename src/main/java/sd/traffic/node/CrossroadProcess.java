package sd.traffic.node;

import sd.traffic.common.StatsSnapshot;
import sd.traffic.common.Vehicle;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;

public class CrossroadProcess {

    private final CrossroadConfig config;

    // chave = id do próximo nó (ex: "Cr2", "Cr4", "Cr5", "S")
    private final Map<String, SemaphoreController> semaphores = new HashMap<>();

    private ServerSocket serverSocket;

    public CrossroadProcess(CrossroadConfig config) {
        this.config = config;
    }

    public void start() throws IOException {
        // 🔹 1) Criar um semáforo por saída configurada em nextHop
        //    (em vez de N/S hardcoded)
        for (String nextNodeId : config.nextHop.keySet()) {
            SemaphoreController sc = new SemaphoreController(
                    nextNodeId,
                    3000, // greenMs  -> TODO: podes pôr no config
                    3000, // redMs    -> idem
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

                    // 🔹 2) Aqui o veículo já vem com currentNode = este nó
                    //      (porque no nó anterior fizemos v.advance())
                    //      O próximo nó a seguir a este é:
                    String next = v.getNextNode();

                    if (next == null) {
                        // isto só faria sentido se este nó fosse o S;
                        // para cruzamento normal, logar erro:
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
        // 🔹 3) Simular tempo de deslocamento entre este nó e o próximo
        String next = v.getNextNode(); // próximo nó ANTES de avançar

        if (next == null) {
            // Se isto acontecer aqui, significa que este nó devia ser o S.
            // Mas o S vai ser tratado num processo próprio (SinkProcess),
            // portanto em princípio isto não deve acontecer.
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
        try {
            double factor = v.getType().travelTimeFactor();
            long base = config.baseTravelTimeMs; // definir no CrossroadConfig
            long travelMs = (long) (base * factor);
            Thread.sleep(travelMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
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

                    // 🔹 4) Agregar info de TODOS os semáforos deste nó
                    int totalQueue = 0;
                    int maxQueue = 0;
                    long totalProcessed = 0;

                    for (SemaphoreController sc : semaphores.values()) {
                        int q = sc.getQueueSize();
                        totalQueue += q;
                        maxQueue = Math.max(maxQueue, sc.getMaxQueue());
                        totalProcessed += sc.getTotalProcessed();
                    }

                    // Aqui estou a reutilizar os campos que já tens;
                    // se quiseres podes estender StatsSnapshot com mapas por saída.
                    snap.filaN = totalQueue;   // por ex: fila total
                    snap.maxFilaN = maxQueue;  // maior fila entre saídas
                    snap.totalVehiclesProcessed = totalProcessed;

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

package traffic.intersection;

import traffic.model.NodeId;
import traffic.model.Vehicle;
import traffic.net.*;

import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class IntersectionProcess {

    private final NodeId nodeId;

    // Fila de veículos deste cruzamento
    private final Queue<Vehicle> queue = new ConcurrentLinkedQueue<>();

    // Estado do semáforo (partilhado entre threads)
    private volatile TrafficLightState lightState = TrafficLightState.RED;

    // Temporizações (ajusta como quiseres)
    private final long greenDurationMs = 4000;    // 4s verde
    private final long redDurationMs   = 4000;    // 4s vermelho
    private final long vehiclePassMs   = 500;     // 0.5s para cada veículo atravessar

    public IntersectionProcess(NodeId nodeId) {
        this.nodeId = nodeId;
    }

    public void start() throws Exception {
        int port = PortConfig.getPort(nodeId);

        // Thread do semáforo
        Thread trafficLightThread = new Thread(this::runTrafficLight, nodeId + "-TrafficLight");
        trafficLightThread.start();

        // Thread do servidor de sockets (como já tinhas)
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("[" + nodeId + "] Listening on port " + port);

            while (true) {
                Socket client = serverSocket.accept();
                new Thread(() -> handleClient(client)).start();
            }
        }
    }

    // ------------------- Receber mensagens da rede -------------------

    private void handleClient(Socket client) {
        try (ObjectInputStream in = new ObjectInputStream(client.getInputStream())) {
            Message msg = (Message) in.readObject();
            System.out.println("[" + nodeId + "] received: " + msg);
            handleMessage(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleMessage(Message msg) {
        switch (msg.getType()) {
            case VEHICLE_ARRIVAL:
            case VEHICLE_FORWARD:
                receiveVehicle(msg.getVehicle());
                break;
            default:
                System.out.println("[" + nodeId + "] Unsupported message type: " + msg.getType());
        }
    }

    private void receiveVehicle(Vehicle v) {
        queue.add(v);
        System.out.println("[" + nodeId + "] vehicle queued: " + v.getId()
                + " | queue size = " + queue.size());
        // ⚠️ Já NÃO encaminhamos aqui.
        // O semáforo (runTrafficLight) é que decide quando o veículo sai.
    }

    // ------------------- Lógica do semáforo (thread) -------------------

    private void runTrafficLight() {
        try {
            while (true) {
                // Fase VERDE
                lightState = TrafficLightState.GREEN;
                System.out.println("[" + nodeId + "] LIGHT = GREEN");
                long greenEnd = System.currentTimeMillis() + greenDurationMs;

                while (System.currentTimeMillis() < greenEnd) {
                    Vehicle v = queue.poll();
                    if (v != null) {
                        System.out.println("[" + nodeId + "] vehicle passing: " + v.getId());
                        forwardToNextNode(v);
                        Thread.sleep(vehiclePassMs);
                    } else {
                        // nada na fila: espera um pouco
                        Thread.sleep(100);
                    }
                }

                // Fase VERMELHA
                lightState = TrafficLightState.RED;
                System.out.println("[" + nodeId + "] LIGHT = RED");
                Thread.sleep(redDurationMs);
            }
        } catch (InterruptedException e) {
            System.out.println("[" + nodeId + "] Traffic light stopped.");
        }
    }

    // ------------------- Encaminhar para o próximo nó -------------------

    private void forwardToNextNode(Vehicle v) {
        List<NodeId> path = v.getPath();
        int currentIndex = path.indexOf(nodeId);

        if (currentIndex == -1) {
            System.out.println("[" + nodeId + "] path não contém este nó: " + v.getId());
            return;
        }

        if (currentIndex == path.size() - 1) {
            System.out.println("[" + nodeId + "] já é o último nó do path.");
            return;
        }

        NodeId next = path.get(currentIndex + 1);
        System.out.println("[" + nodeId + "] forwarding to " + next);

        MessageType type = (next == NodeId.S)
                ? MessageType.VEHICLE_EXIT
                : MessageType.VEHICLE_FORWARD;

        try {
            MessageSender.send(new Message(
                    type,
                    nodeId,
                    next,
                    v
            ));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


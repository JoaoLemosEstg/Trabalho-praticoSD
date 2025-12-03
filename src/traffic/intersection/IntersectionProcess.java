package traffic.intersection;

import traffic.model.NodeId;
import traffic.model.Vehicle;
import traffic.net.*;

import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class IntersectionProcess {

    private final NodeId nodeId;


    private final Map<Direction, Queue<Vehicle>> queues =
            new EnumMap<>(Direction.class);


    private volatile TrafficLightState lightState = TrafficLightState.RED;


    private final long greenDurationMs = 4000;   // 4s verde
    private final long redDurationMs   = 4000;   // 4s vermelho
    private final long vehiclePassMs   = 500;    // 0.5s por veículo

    public IntersectionProcess(NodeId nodeId) {
        this.nodeId = nodeId;
        for (Direction d : Direction.values()) {
            queues.put(d, new ConcurrentLinkedQueue<>());
        }
    }

    public void start() throws Exception {
        int port = PortConfig.getPort(nodeId);


        Thread trafficLightThread = new Thread(this::runTrafficLight, nodeId + "-TrafficLight");
        trafficLightThread.start();


        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("[" + nodeId + "] Listening on port " + port);

            while (true) {
                Socket client = serverSocket.accept();
                new Thread(() -> handleClient(client)).start();
            }
        }
    }



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
            case VEHICLE_ARRIVAL, VEHICLE_FORWARD -> receiveVehicle(msg.getVehicle());
            default -> System.out.println("[" + nodeId + "] Unsupported message type: " + msg.getType());
        }
    }

    private void receiveVehicle(Vehicle v) {
        Direction dir = getIncomingDirection(v);
        Queue<Vehicle> q = queues.get(dir);
        q.add(v);
        System.out.println("[" + nodeId + "] vehicle queued from " + dir +
                ": " + v.getId() + " | queues = " + queueSizes());

    }

    private String queueSizes() {
        StringBuilder sb = new StringBuilder();
        for (var e : queues.entrySet()) {
            sb.append(e.getKey()).append("=").append(e.getValue().size()).append(" ");
        }
        return sb.toString();
    }


    private void runTrafficLight() {
        try {
            while (true) {

                phaseGreen(Direction.NORTH, Direction.SOUTH);

                phaseGreen(Direction.EAST, Direction.WEST);
            }
        } catch (InterruptedException e) {
            System.out.println("[" + nodeId + "] Traffic light stopped.");
        }
    }

    private void phaseGreen(Direction... greenDirs) throws InterruptedException {
        lightState = TrafficLightState.GREEN;
        System.out.println("[" + nodeId + "] LIGHT = GREEN for " + Arrays.toString(greenDirs));
        long end = System.currentTimeMillis() + greenDurationMs;

        while (System.currentTimeMillis() < end) {
            boolean moved = false;


            for (Direction d : greenDirs) {
                Queue<Vehicle> q = queues.get(d);
                Vehicle v = q.poll();
                if (v != null) {
                    System.out.println("[" + nodeId + "] vehicle from " + d +
                            " passing: " + v.getId());
                    forwardToNextNode(v);
                    Thread.sleep(vehiclePassMs);
                    moved = true;
                    break;
                }
            }

            if (!moved) {

                Thread.sleep(100);
            }
        }

        lightState = TrafficLightState.RED;
        System.out.println("[" + nodeId + "] LIGHT = RED for " + Arrays.toString(greenDirs));
        Thread.sleep(redDurationMs);
    }



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



    private Direction getIncomingDirection(Vehicle v) {
        List<NodeId> path = v.getPath();
        int idx = path.indexOf(nodeId);
        if (idx <= 0) {
            return Direction.NORTH; // default
        }
        NodeId prev = path.get(idx - 1);

        // ajusta isto se a tua malha for diferente;
        // é só um exemplo consistente com um grafo típico

        return switch (nodeId) {
            case CR1 -> {
                if (prev == NodeId.E1)   yield Direction.SOUTH;
                if (prev == NodeId.CR2)  yield Direction.EAST;
                if (prev == NodeId.CR4)  yield Direction.NORTH;
                yield Direction.WEST;
            }
            case CR2 -> {
                if (prev == NodeId.CR1)  yield Direction.WEST;
                if (prev == NodeId.CR3)  yield Direction.EAST;
                if (prev == NodeId.CR5)  yield Direction.SOUTH;
                if (prev == NodeId.E2)   yield Direction.NORTH;
                yield Direction.NORTH;
            }
            case CR3 -> {
                if (prev == NodeId.CR2)  yield Direction.SOUTH;
                if (prev == NodeId.E3)   yield Direction.NORTH;
                yield Direction.WEST;
            }
            case CR4 -> {
                if (prev == NodeId.CR1)  yield Direction.WEST;
                yield Direction.NORTH;
            }
            case CR5 -> {
                if (prev == NodeId.CR2)  yield Direction.NORTH;
                if (prev == NodeId.CR4)  yield Direction.WEST;
                yield Direction.SOUTH;
            }
            default -> Direction.NORTH;
        };
    }
}

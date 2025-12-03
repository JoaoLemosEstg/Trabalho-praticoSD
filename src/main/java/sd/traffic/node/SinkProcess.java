package sd.traffic.node;

import sd.traffic.common.StatsSnapshot;
import sd.traffic.common.Vehicle;
import sd.traffic.common.VehicleType;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.EnumMap;
import java.util.Map;

public class SinkProcess {

    private final CrossroadConfig config;

    private final Map<VehicleType, Long> count = new EnumMap<>(VehicleType.class);
    private final Map<VehicleType, Long> totalTime = new EnumMap<>(VehicleType.class);
    private final Map<VehicleType, Long> minTime = new EnumMap<>(VehicleType.class);
    private final Map<VehicleType, Long> maxTime = new EnumMap<>(VehicleType.class);

    public SinkProcess(CrossroadConfig config) {
        this.config = config;
        for (VehicleType t : VehicleType.values()) {
            count.put(t, 0L);
            totalTime.put(t, 0L);
            minTime.put(t, Long.MAX_VALUE);
            maxTime.put(t, Long.MIN_VALUE);
        }
    }

    public void start() throws Exception {
        startStatsReporter();

        try (ServerSocket server = new ServerSocket(config.listenPort)) {
            System.out.println("Sink S a ouvir na porta " + config.listenPort);
            while (true) {
                Socket s = server.accept();
                new Thread(() -> handleVehicle(s)).start();
            }
        }
    }

    private void handleVehicle(Socket s) {
        try (ObjectInputStream in = new ObjectInputStream(s.getInputStream())) {
            Vehicle v = (Vehicle) in.readObject();
            long exitTime = System.currentTimeMillis();
            v.setExitTimeSystem(exitTime);

            long dwell = exitTime - v.getArrivalTimeSystem();
            VehicleType type = v.getType();

            synchronized (this) {
                long c = count.get(type) + 1;
                count.put(type, c);
                totalTime.put(type, totalTime.get(type) + dwell);
                minTime.put(type, Math.min(minTime.get(type), dwell));
                maxTime.put(type, Math.max(maxTime.get(type), dwell));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startStatsReporter() {
        Thread t = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(config.statsIntervalMs);
                    sendGlobalStats();
                } catch (InterruptedException e) {
                    return;
                }
            }
        });
        t.setDaemon(true);
        t.start();
    }

    private void sendGlobalStats() {
        StatsSnapshot snap = new StatsSnapshot();
        snap.nodeId = "S";
        snap.timestamp = System.currentTimeMillis();

        // Exemplo simples de reutilizar campos:
        // filaN = total de CARROS
        // filaS = total de MOTOS
        // filaE = total de CAMIÕES
        synchronized (this) {
            snap.filaN = count.get(VehicleType.CARRO).intValue();
            snap.filaS = count.get(VehicleType.MOTO).intValue();
            snap.filaE = count.get(VehicleType.CAMIAO).intValue();
        }

        try (Socket s = new Socket("localhost", config.dashboardPort)) {
            ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
            out.writeObject(snap);
            out.flush();
        } catch (Exception ignored) {}
    }
}

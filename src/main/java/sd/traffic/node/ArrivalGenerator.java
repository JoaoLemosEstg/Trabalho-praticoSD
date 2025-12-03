package sd.traffic.node;

import sd.traffic.common.PathUtils;
import sd.traffic.common.Vehicle;
import sd.traffic.common.VehicleType;

import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.Random;

public class ArrivalGenerator extends Thread {

    private final String entryId; // "E1", ...
    private final double lambda;  // veiculos / segundo
    private final String firstNodeId;
    private final String firstNodeHost;
    private final int firstNodePort;

    private final Random rng = new Random();

    public ArrivalGenerator(String entryId, double lambda,
                            String firstNodeId, String host, int port) {
        this.entryId = entryId;
        this.lambda = lambda;
        this.firstNodeId = firstNodeId;
        this.firstNodeHost = host;
        this.firstNodePort = port;
    }

    @Override
    public void run() {
        while (true) {
            try {
                double u = rng.nextDouble();
                double intervalSeconds = -Math.log(1 - u) / lambda;
                Thread.sleep((long) (intervalSeconds * 1000));

                VehicleType type = randomVehicleType();
                List<String> path = PathUtils.randomPathForEntry(entryId);
                Vehicle v = new Vehicle(type, entryId, path, System.currentTimeMillis());
                v.advance();
                sendToFirstNode(v);
            } catch (InterruptedException e) {
                return;
            }
        }
    }

    private VehicleType randomVehicleType() {
        double p = rng.nextDouble();
        if (p < 0.3) return VehicleType.MOTO;
        if (p < 0.8) return VehicleType.CARRO;
        return VehicleType.CAMIAO;
    }

    private void sendToFirstNode(Vehicle v) {
        try (Socket s = new Socket(firstNodeHost, firstNodePort)) {
            ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
            out.writeObject(v);
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

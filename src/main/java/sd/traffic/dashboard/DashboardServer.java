package sd.traffic.dashboard;

import sd.traffic.common.StatsSnapshot;

import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DashboardServer {

    private final int port;
    private final Map<String, StatsSnapshot> lastStats = new ConcurrentHashMap<>();

    public DashboardServer(int port) {
        this.port = port;
    }

    public void start() throws Exception {
        ServerSocket server = new ServerSocket(port);
        System.out.println("Dashboard server a ouvir na porta " + port);
        Thread printer = new Thread(this::printLoop);
        printer.setDaemon(true);
        printer.start();

        while (true) {
            Socket s = server.accept();
            new Thread(() -> handleConnection(s)).start();
        }
    }

    private void handleConnection(Socket s) {
        try (ObjectInputStream in = new ObjectInputStream(s.getInputStream())) {
            StatsSnapshot snap = (StatsSnapshot) in.readObject();
            lastStats.put(snap.nodeId, snap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void printLoop() {
        while (true) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ignored) {}

            System.out.println("===== DASHBOARD =====");
            for (StatsSnapshot ss : lastStats.values()) {
                System.out.printf("Nó %s | filas N:%d (max %d)  S:%d (max %d)  totalProc:%d%n",
                        ss.nodeId, ss.filaN, ss.maxFilaN, ss.filaS, ss.maxFilaS, ss.totalVehiclesProcessed);
            }
            System.out.println("=====================");
        }
    }

    public static void main(String[] args) throws Exception {
        int port = 6000;
        new DashboardServer(port).start();
    }
}
